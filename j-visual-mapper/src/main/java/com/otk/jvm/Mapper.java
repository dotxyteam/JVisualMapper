package com.otk.jvm;

import javax.swing.SwingUtilities;

import com.otk.jesb.Session;
import com.otk.jesb.solution.Plan;
import com.otk.jesb.solution.Solution;

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
		configure(sourceClass, targetClass);
		getOutputBuilder().setRootInstanceName("Mappings");
	}

	private void configure(Class<?> actualSsourceClass, Class<?> actualTargetClass) {
		setActivator(new MappingsActivator(actualSsourceClass.asSubclass(sourceClass),
				actualTargetClass.asSubclass(targetClass)));
		getOutputBuilder().getFacade(getSolutionInstance()).getChildren().get(0).setConcrete(false);
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Object source = GUI_INSTANCE.onTypeInstantiationRequest(null,
						GUI_INSTANCE.getReflectionUI().getTypeInfo(
								new JavaTypeInfoSource(getActivator().getInputClass(getSolutionInstance()), null)));
				if (source == null) {
					return;
				}
				T target;
				try {
					target = targetClass.cast(map(sourceClass.cast(source)));
				} catch (ExecutionError e) {
					GUI_INSTANCE.handleException(null, e);
					return;
				}
				if (target == null) {
					GUI_INSTANCE.handleException(null, new NullPointerException());
					return;
				}
				GUI_INSTANCE.openObjectDialog(null, target);
			}
		});
	}

}
