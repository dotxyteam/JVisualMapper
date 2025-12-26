package com.otk.jvm.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.otk.jesb.solution.Plan.ExecutionError;
import com.otk.jesb.solution.Solution;
import com.otk.jesb.ui.GUI;
import com.otk.jvm.Mapper;

import xy.reflect.ui.control.RenderingContext;
import xy.reflect.ui.control.swing.builder.AbstractEditorBuilder.EditorFrame;
import xy.reflect.ui.control.swing.builder.StandardEditorBuilder;
import xy.reflect.ui.control.swing.menu.AbstractFileMenuItem;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.app.IApplicationInfo;
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

	public void openMappingsEditor(Mapper result, String filePath, String title) {
		title = "J-Visual Mapper" + ((title == null) ? "" : (" - " + title));
		StandardEditorBuilder editorBuilder = createEditorBuilder(null, result, title, null, true);
		EditorFrame frame = editorBuilder.createFrame();
		Form mapperForm = SwingRendererUtils.findFirstObjectDescendantForm(result, frame, this);
		AbstractFileMenuItem.getLastFileByForm().put(mapperForm, new File(filePath));
		SwingRendererUtils.updateAncestorWindowMenu(mapperForm, this);
		showFrame(frame);
	}

	@Override
	protected JESBSubCustomizedUI createSubCustomizedUI(String switchIdentifier) {
		return new JESBSubCustomizedUI(switchIdentifier) {

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
				return new JESBBeforeInfoCustomizationsFactory() {

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
								public Object invoke(Object object, InvocationData invocationData) {
									final Form mapperForm = SwingRendererUtils.findContextualFormOfType(
											getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Mapper.class, null)),
											getReflectionUI().getRenderingContextThreadLocal().get());
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											Mapper mapper = (Mapper) mapperForm.getObject();
											Object source = onTypeInstantiationRequest(mapperForm,
													getReflectionUI().getTypeInfo(new JavaTypeInfoSource(
															mapper.getActivator().getInputClass(getSolutionInstance()),
															null)));
											if (source == null) {
												return;
											}
											Object target;
											try {
												target = mapper.map(source);
											} catch (ExecutionError e) {
												handleException(mapperForm, e);
												return;
											}
											if (target == null) {
												handleException(mapperForm, new NullPointerException());
												return;
											}
											openObjectDialog(mapperForm, target);
										}
									});
									return null;
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

				};
			}

		};
	}

}
