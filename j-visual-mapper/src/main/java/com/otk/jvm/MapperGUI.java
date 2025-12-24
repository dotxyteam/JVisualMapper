package com.otk.jvm;

import com.otk.jesb.solution.Solution;
import com.otk.jesb.ui.GUI;

import xy.reflect.ui.control.RenderingContext;
import xy.reflect.ui.info.type.ITypeInfo;

public class MapperGUI extends GUI {

	public static final String MAPPER_GUI_CUSTOMIZATIONS_IDENTIFIER = Mapper.class.getPackage().getName() + ".*";
	public static final String MAPPER_GUI_CUSTOMIZATIONS_RESOURCE_NAME = "jvm.icu";
	public static final String MAPPER_GUI_CUSTOMIZATIONS_RESOURCE_DIRECTORY_PROPERTY_KEY = MapperGUI.class.getPackage()
			.getName() + ".alternateUICustomizationsFileDirectory";

	public MapperGUI() {
		getReflectionUI().setRenderingContextThreadLocal(ThreadLocal.withInitial(() -> new RenderingContext(null) {
			@Override
			protected Object findCurrentObjectLocally(ITypeInfo type) {
				if (type.getName().equals(Solution.class.getName())) {
					return MapperGUI.this.getSolutionInstance();
				}
				return null;
			}
		}));

	}

	@Override
	protected void preLoadBuiltInCustomizations() {
	}

	@Override
	protected String selectSubCustomizationsSwitch(Class<?> objectClass) {
		if (objectClass.getName().startsWith(Mapper.class.getPackage().getName())) {
			return MAPPER_GUI_CUSTOMIZATIONS_IDENTIFIER;
		}
		return super.selectSubCustomizationsSwitch(objectClass);
	}

	@Override
	public String getInfoCustomizationsOutputFilePath(String customizationsIdentifier) {
		if (MAPPER_GUI_CUSTOMIZATIONS_IDENTIFIER.equals(customizationsIdentifier)) {
			String customizationsDirectoryPath = System
					.getProperty(MAPPER_GUI_CUSTOMIZATIONS_RESOURCE_DIRECTORY_PROPERTY_KEY);
			if (customizationsDirectoryPath != null) {
				return customizationsDirectoryPath + "/" + getInfoCustomizationsResourceName(customizationsIdentifier);
			} else {
				return null;
			}
		}
		return super.getInfoCustomizationsOutputFilePath(customizationsIdentifier);
	}

	protected String getInfoCustomizationsResourceName(String customizationsIdentifier) {
		if (MAPPER_GUI_CUSTOMIZATIONS_IDENTIFIER.equals(customizationsIdentifier)) {
			return MAPPER_GUI_CUSTOMIZATIONS_RESOURCE_NAME;
		}
		return super.getInfoCustomizationsResourceName(customizationsIdentifier);
	}

	@Override
	protected Class<?> getMainCustomizedClass(String customizationsIdentifier) {
		if (MAPPER_GUI_CUSTOMIZATIONS_IDENTIFIER.equals(customizationsIdentifier)) {
			return null;
		}
		return super.getMainCustomizedClass(customizationsIdentifier);
	}

}
