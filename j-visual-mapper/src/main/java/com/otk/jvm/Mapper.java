package com.otk.jvm;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.SwingUtilities;

import com.otk.jesb.Session;
import com.otk.jesb.instantiation.RootInstanceBuilder;
import com.otk.jesb.solution.Plan;
import com.otk.jesb.solution.Solution;
import com.otk.jesb.util.MiscUtils;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

public class Mapper extends Plan {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UI.INSTANCE.openObjectFrame(new Mapper(Rectangle.class, Polygon.class));
			}
		});
	}

	public Mapper(Class<?> sourceClass, Class<?> targetClass) {
		setInputVariableName("SOURCE");
		getOutputBuilder().setRootInstanceName("TARGET");
		setActivator(new MappingsActivator(sourceClass, targetClass));
		getOutputBuilder().getFacade(getSolutionInstance()).getChildren().get(0).setConcrete(true);
	}

	public static Mapper get(Class<?> sourceClass, Class<?> targetClass, Class<?> resourceClass,
			String resourceLocation) throws IOException {
		URL resourceURL = resourceClass.getResource(resourceLocation);
		Mapper result = new Mapper(sourceClass, targetClass);
		if (resourceURL != null) {
			try (InputStream input = resourceURL.openStream()) {
				result.setOutputBuilder((RootInstanceBuilder) MiscUtils.deserialize(input,
						getSolutionInstance().getRuntime().getXstream()));
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String resourceName = new File(resourceLocation).getName();
					UI.INSTANCE.openMappingsEditor(result, resourceName,
							"J-Visual Mapper - " + resourceClass.getName() + "/" + resourceLocation);
				}
			});
		}
		return result;
	}

	private static Solution getSolutionInstance() {
		return UI.INSTANCE.getSolutionInstance();
	}

	@Override
	public MappingsActivator getActivator() {
		return (MappingsActivator) super.getActivator();
	}

	public String getSourceClassName() {
		return getActivator().getInputClass(getSolutionInstance()).getName();
	}

	public String getTargetClassName() {
		return getActivator().getOutputClass(getSolutionInstance()).getName();
	}

	public void saveMappings(File file) throws IOException {
		try (FileOutputStream output = new FileOutputStream(file)) {
			MiscUtils.serialize(getOutputBuilder(), output, getSolutionInstance().getRuntime().getXstream());
		}
	}

	public void loadMappings(File file) throws IOException {
		try (FileInputStream input = new FileInputStream(file)) {
			setOutputBuilder((RootInstanceBuilder) MiscUtils.deserialize(input,
					getSolutionInstance().getRuntime().getXstream()));
		}
	}

	public Object map(Object source) throws ExecutionError {
		return execute(source, Plan.ExecutionInspector.DEFAULT,
				new Plan.ExecutionContext(Session.openDummySession(getSolutionInstance()), this));
	}

	public void test() {
		final Form mapperForm = SwingRendererUtils.findContextualFormOfType(
				UI.INSTANCE.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Mapper.class, null)),
				UI.INSTANCE.getReflectionUI().getRenderingContextThreadLocal().get());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Object source = UI.INSTANCE.onTypeInstantiationRequest(mapperForm,
						UI.INSTANCE.getReflectionUI().getTypeInfo(
								new JavaTypeInfoSource(getActivator().getInputClass(getSolutionInstance()), null)));
				if (source == null) {
					return;
				}
				Object target;
				try {
					target = map(source);
				} catch (ExecutionError e) {
					UI.INSTANCE.handleException(mapperForm, e);
					return;
				}
				if (target == null) {
					UI.INSTANCE.handleException(mapperForm, new NullPointerException());
					return;
				}
				UI.INSTANCE.openObjectDialog(mapperForm, target);
			}
		});
	}

	public static class UI extends MapperGUI {

		public static UI INSTANCE = new UI();

		private UI() {
		}

	}

}
