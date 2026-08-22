package com.otk.jvm.ui;

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.otk.jesb.solution.Plan.ExecutionError;
import com.otk.jesb.solution.Solution;
import com.otk.jesb.ui.GUI;
import com.otk.jvm.Mapper;

import xy.reflect.ui.control.RenderingContext;
import xy.reflect.ui.control.swing.builder.AbstractEditorBuilder.EditorFrame;
import xy.reflect.ui.control.swing.builder.DialogBuilder.RenderedDialog;
import xy.reflect.ui.control.swing.builder.StandardEditorBuilder;
import xy.reflect.ui.control.swing.menu.AbstractFileMenuItem;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Allows mainly the display of {@link Mapper} instances in Swing components.
 * 
 * @author olitank
 *
 */
public class MapperGUI extends GUI {

	public static final String MAPPER_GUI_CUSTOMIZATIONS_IDENTIFIER = Mapper.class.getPackage().getName() + ".*";
	public static final String MAPPER_GUI_CUSTOMIZATIONS_RESOURCE_NAME = "jvm.icu";
	public static final String MAPPER_GUI_CUSTOMIZATIONS_RESOURCE_DIRECTORY_PROPERTY_KEY = MapperGUI.class.getPackage()
			.getName() + ".alternateUICustomizationsFileDirectory";

	public MapperGUI() {
		getReflectionUI().setRenderingContextThreadLocal(ThreadLocal.withInitial(() -> new RenderingContext(null) {
			@Override
			protected Object findObjectLocally(ITypeInfo type) {
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

	public void openMappingsEditor(Mapper mapper, String filePath, String title, boolean modal) {
		title = "J-Visual Mapper" + ((title == null) ? "" : (" - " + title));
		StandardEditorBuilder editorBuilder = createEditorBuilder(null, mapper, title, null, true);
		Consumer<Window> filePathConfigurator = window -> {
			if (filePath != null) {
				Form mapperForm = SwingRendererUtils.findFirstObjectDescendantForm(mapper, window, this);
				AbstractFileMenuItem.getLastFileByForm().put(mapperForm, new File(filePath));
				SwingRendererUtils.updateAncestorWindowMenu(mapperForm, this);
			}
		};
		if (modal) {
			RenderedDialog dialog = editorBuilder.createDialog();
			filePathConfigurator.accept(dialog);
			showDialog(dialog, true);
		} else {
			EditorFrame frame = editorBuilder.createFrame();
			filePathConfigurator.accept(frame);
			showFrame(frame);
		}
	}

	@Override
	protected JESBSubCustomizedUI createSubCustomizedUI(String switchIdentifier) {
		return new MapperSubCustomizedUI(switchIdentifier);
	}

	public static class MappingsTester {

		private Mapper mapper;
		private Object source;

		public MappingsTester(Mapper mapper) {
			this.mapper = mapper;
		}

		public Mapper getMapper() {
			return mapper;
		}

		public Object getSource() {
			return source;
		}

		public void setSource(Object source) {
			this.source = source;
		}

		public Object getTarget() throws ExecutionError {
			return mapper.map(source);
		}

	}

	protected class MapperSubCustomizedUI extends JESBSubCustomizedUI {

		public MapperSubCustomizedUI(String switchIdentifier) {
			super(switchIdentifier);
		}

		@Override
		protected IInfoProxyFactory createAfterInfoCustomizationsFactory() {
			return new InfoProxyFactory() {
				@Override
				protected ResourcePath getIconImagePath(IApplicationInfo appInfo) {
					return null;
				}
			};
		}

		@Override
		protected JESBBeforeInfoCustomizationsFactory createBeforeInfoCustomizationsFactory() {
			return new MapperBeforeInfoCustomizationsFactory();
		}

		protected class MapperBeforeInfoCustomizationsFactory extends JESBBeforeInfoCustomizationsFactory {

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				if (type.getName().equals(Mapper.class.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

						@Override
						public String getSignature() {
							return ReflectionUIUtils.buildMethodSignature(this);
						}

						@Override
						public String getName() {
							return "test";
						}

						@Override
						public String getCaption() {
							return ReflectionUIUtils.formatMethodCaption(this, getName(), 0);
						}

						@Override
						public ITypeInfo getReturnValueType() {
							return getReflectionUI().getTypeInfo(new JavaTypeInfoSource(MappingsTester.class, null));
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							Mapper mapper = (Mapper) object;
							return new MappingsTester(mapper);
						}

						@Override
						public boolean isReadOnly() {
							return true;
						}
					});
					return result;
				} else {
					return super.getMethods(type);
				}
			}

			@Override
			protected List<IMethodInfo> getAlternativeConstructors(IFieldInfo field, Object object,
					ITypeInfo objectType) {
				if (objectType.getName().equals(MappingsTester.class.getName())) {
					Mapper mapper = ((MappingsTester) object).getMapper();
					if (field.getName().equals("source")) {
						return getReflectionUI().getTypeInfo(new JavaTypeInfoSource(mapper.getSourceClass(), null))
								.getConstructors();
					}
					if (field.getName().equals("target")) {
						return getReflectionUI().getTypeInfo(new JavaTypeInfoSource(mapper.getTargetClass(), null))
								.getConstructors();
					}
				}
				return super.getAlternativeConstructors(field, object, objectType);
			}

			@Override
			protected boolean isConcrete(ITypeInfo type) {
				if (type.getName().equals(Object.class.getName())) {
					return true;
				}
				return super.isConcrete(type);
			}

		}

	}

}
