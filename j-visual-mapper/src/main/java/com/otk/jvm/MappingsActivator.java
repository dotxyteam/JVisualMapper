package com.otk.jvm;

import com.otk.jesb.activation.ActivationHandler;
import com.otk.jesb.activation.Activator;
import com.otk.jesb.solution.Solution;

public class MappingsActivator extends Activator {

	protected Class<?> sourceClass;
	protected Class<?> targetClass;

	public MappingsActivator(Class<?> sourceClass, Class<?> targetClass) {
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
	}

	@Override
	public Class<?> getInputClass(Solution solutionInstance) {
		return sourceClass;
	}

	@Override
	public Class<?> getOutputClass(Solution solutionInstance) {
		return targetClass;
	}

	@Override
	public void initializeAutomaticTrigger(ActivationHandler activationHandler, Solution solutionInstance)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void finalizeAutomaticTrigger(Solution solutionInstance) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAutomaticTriggerReady() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAutomaticallyTriggerable() {
		return false;
	}

}