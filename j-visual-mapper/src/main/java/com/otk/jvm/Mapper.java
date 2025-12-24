package com.otk.jvm;

import javax.swing.SwingUtilities;

import com.otk.jesb.Session;
import com.otk.jesb.instantiation.RootInstanceBuilderFacade;
import com.otk.jesb.solution.Plan;
import com.otk.jesb.solution.Solution;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

public class Mapper<S, T> extends Plan {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GUI_INSTANCE.openObjectFrame(new Mapper<Object, Object>(Object.class, Object.class));
			}
		});
	}

	public static MapperGUI GUI_INSTANCE = new MapperGUI();

	private Class<? extends S> sourceClass;
	private Class<? extends T> targetClass;

	public Mapper(Class<? extends S> sourceClass, Class<? extends T> targetClass) {
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
		setInputVariableName("INPUT");
		getOutputBuilder().setRootInstanceName("Mappings");
		configure(sourceClass, targetClass);
	}

	private void configure(Class<?> actualSsourceClass, Class<?> actualTargetClass) {
		setActivator(new MappingsActivator(actualSsourceClass.asSubclass(sourceClass),
				actualTargetClass.asSubclass(targetClass)));
		RootInstanceBuilderFacade outputBuilderFacade = getOutputBuilder().getFacade(getSolutionInstance());
		if (outputBuilderFacade.getChildren().size() > 0) {
			outputBuilderFacade.getChildren().get(0).setConcrete(false);
			outputBuilderFacade.getChildren().get(0).setConcrete(true);
		}
	}

	private void configure(String sourceClassName, String targetClassName) {
		configure(loadClass(sourceClassName), loadClass(targetClassName));
	}

	private Class<?> loadClass(String className) {
		return getSolutionInstance().getRuntime().getJESBClass(className);
	}

	private Solution getSolutionInstance() {
		return GUI_INSTANCE.getSolutionInstance();
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

	public void changeSourceClassName(String sourceClassName) {
		configure(sourceClassName, getTargetClassName());
	}

	public void changeTargetClassName(String targetClassName) {
		configure(getSourceClassName(), targetClassName);
	}

	public T map(S source) throws ExecutionError {
		return targetClass.cast(execute(source, Plan.ExecutionInspector.DEFAULT,
				new Plan.ExecutionContext(Session.openDummySession(getSolutionInstance()), this)));
	}

	public void test() {
		final Form mapperForm = SwingRendererUtils.findContextualFormOfType(
				GUI_INSTANCE.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Mapper.class, null)),
				GUI_INSTANCE.getReflectionUI().getRenderingContextThreadLocal().get());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Object source = GUI_INSTANCE.onTypeInstantiationRequest(mapperForm,
						GUI_INSTANCE.getReflectionUI().getTypeInfo(
								new JavaTypeInfoSource(getActivator().getInputClass(getSolutionInstance()), null)));
				if (source == null) {
					return;
				}
				T target;
				try {
					target = targetClass.cast(map(sourceClass.cast(source)));
				} catch (ExecutionError e) {
					GUI_INSTANCE.handleException(mapperForm, e);
					return;
				}
				if (target == null) {
					GUI_INSTANCE.handleException(mapperForm, new NullPointerException());
					return;
				}
				GUI_INSTANCE.openObjectDialog(mapperForm, target);
			}
		});
	}

}
