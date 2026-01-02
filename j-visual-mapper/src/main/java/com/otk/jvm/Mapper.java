package com.otk.jvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import com.otk.jesb.Session;
import com.otk.jesb.ValidationError;
import com.otk.jesb.instantiation.RootInstanceBuilder;
import com.otk.jesb.solution.Plan;
import com.otk.jesb.solution.Solution;
import com.otk.jvm.ui.MapperGUI;

/**
 * Allows you to specify rules for transforming a 'source' object into a
 * 'target' object based on the respective classes of these objects. These
 * mapping rules can be persisted as text.
 * 
 * @author olitank
 *
 */
public class Mapper extends Plan {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			throw new IllegalArgumentException("Unexpected command line arguments " + Arrays.toString(args)
					+ ": Expected <sourceClassName> <targetClassName> [<mappingsFilePath>]");
		}
		Class<?> sourceClass = Class.forName(args[0]);
		Class<?> targetClass = Class.forName(args[1]);
		Mapper mapper = new Mapper(sourceClass, targetClass);
		if (args.length > 2) {
			File mappingsFile = new File(args[2]);
			mapper.loadMappings(mappingsFile);
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UI.INSTANCE.openObjectFrame(mapper);
			}
		});
	}

	/**
	 * Creates an instance that transforms an object of type sourceClass into an
	 * object of type targetClass.
	 * 
	 * @param sourceClass The class of the transformation input object.
	 * @param targetClass The class of the transformation output object.
	 */
	public Mapper(Class<?> sourceClass, Class<?> targetClass) {
		setInputVariableName("SOURCE");
		getOutputBuilder().setRootInstanceName("TARGET");
		setActivator(new MappingsActivator(sourceClass, targetClass));
		getOutputBuilder().getFacade(getSolutionInstance()).getChildren().get(0).setConcrete(true);
	}

	/**
	 * Creates a {@link Mapper} instance and loads the transformation specification
	 * from the specified classpath-based resource. If the resource is not found
	 * then the mapper will be opened in a graphical editor.
	 * 
	 * @param sourceClass      The class of the transformation input object.
	 * @param targetClass      The class of the transformation output object.
	 * @param resourceClass    The class from which the resource is loaded (using
	 *                         {@link Class#getResource(String)}).
	 * @param resourceLocation The path to the resource (passed to
	 *                         {@link Class#getResource(String)}).
	 * @return The resulting {@link Mapper} instance.
	 * @throws IOException           If an error occurs during the mappings resource
	 *                               loading processing.
	 * @throws IllegalStateException If an inconsistency is detected in the loaded
	 *                               mappings data.
	 */
	public static Mapper get(Class<?> sourceClass, Class<?> targetClass, Class<?> resourceClass,
			String resourceLocation) throws IOException, IllegalStateException {
		Mapper result = new Mapper(sourceClass, targetClass);
		URL resourceURL = resourceClass.getResource(resourceLocation);
		if (resourceURL != null) {
			try (InputStream input = resourceURL.openStream()) {
				result.loadMappings(input);
				result.validate();
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String resourceName = new File(resourceLocation).getName();
					UI.INSTANCE.openMappingsEditor(result, resourceName,
							resourceClass.getName() + "/" + resourceLocation, false);
				}
			});
		}
		return result;
	}

	/**
	 * @return The class of the transformation input object.
	 */
	public Class<?> getSourceClass() {
		return getActivator().getInputClass(getSolutionInstance());
	}

	/**
	 * @return The class of the transformation output object.
	 */
	public Class<?> getTargetClass() {
		return getActivator().getOutputClass(getSolutionInstance());
	}

	/**
	 * Saves the transformation specification in the specified file.
	 * 
	 * @param file The output file.
	 * @throws IOException If an error occurs during the processing.
	 */
	public void saveMappings(File file) throws IOException {
		try (FileOutputStream output = new FileOutputStream(file)) {
			saveMappings(output);
		}
	}

	/**
	 * Loads the transformation specification from the specified file.
	 * 
	 * @param file The input file.
	 * @throws IOException If an error occurs during the processing.
	 */
	public void loadMappings(File file) throws IOException {
		try (FileInputStream input = new FileInputStream(file)) {
			loadMappings(input);
		}
	}

	/**
	 * Checks the consistency of the current mapper data.
	 * 
	 * @throws IllegalStateException If an inconsistency is detected.
	 */
	public void validate() throws IllegalStateException {
		try {
			validate(true, getSolutionInstance());
		} catch (ValidationError e) {
			throw new IllegalStateException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Saves the transformation specification in the specified stream.
	 * 
	 * @param output The output stream.
	 * @throws IOException If an error occurs during the processing.
	 */
	public void saveMappings(FileOutputStream output) throws IOException {
		getSolutionInstance().getSerializer().write(getOutputBuilder(), output);
	}

	/**
	 * Loads the transformation specification from the specified stream.
	 * 
	 * @param input The input stream.
	 * @throws IOException If an error occurs during the processing.
	 */
	public void loadMappings(InputStream input) throws IOException {
		setOutputBuilder((RootInstanceBuilder) getSolutionInstance().getSerializer().read(input));
	}

	/**
	 * Performs the transformation.
	 * 
	 * @param source The transformation input object.
	 * @return The transformation output object.
	 * @throws ExecutionError If an error occurs during the processing.
	 */
	public Object map(Object source) throws ExecutionError {
		return execute(source, Plan.ExecutionInspector.DEFAULT,
				new Plan.ExecutionContext(Session.openDummySession(getSolutionInstance()), this));
	}

	private static Solution getSolutionInstance() {
		return UI.INSTANCE.getSolutionInstance();
	}

	@Override
	public MappingsActivator getActivator() {
		return (MappingsActivator) super.getActivator();
	}

	/**
	 * Singleton class derived from {@link MapperGUI}.
	 * 
	 * @author olitank
	 *
	 */
	public static class UI extends MapperGUI {

		public static UI INSTANCE = new UI();

		private UI() {
		}

	}

}
