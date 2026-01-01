package com.otk.jvm;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.util.Locale;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.otk.jesb.JESB;
import com.otk.jesb.ui.GUI;
import com.otk.jvm.Mapper.UI;
import com.otk.jvm.annotation.MappingsResource;

import xy.reflect.ui.control.swing.builder.StandardEditorBuilder;
import xy.reflect.ui.util.MiscUtils;
import xy.ui.testing.Tester;

public class ApiTests {

	Tester tester = new Tester();

	protected static void checkSystemProperty(String key, String expectedValue) {
		String value = System.getProperty(key);
		if (!MiscUtils.equalsOrBothNull(expectedValue, value)) {
			String errorMsg = "System property invalid value:\n" + "-D" + key + "=" + value + "\nExpected:\n" + "-D"
					+ key + "=" + expectedValue;
			System.err.println(errorMsg);
			throw new AssertionError(errorMsg);

		}
	}

	public static void setupTestEnvironment() {
		checkSystemProperty(JESB.DEBUG_PROPERTY_KEY, null);
		checkSystemProperty(GUI.GUI_CUSTOMIZATIONS_RESOURCE_DIRECTORIES_PROPERTY_KEY, null);
		Locale.setDefault(Locale.US);
	}

	@BeforeClass
	public static void beforeAllTests() {
		setupTestEnvironment();
	}

	@Test
	public void testExample() throws Exception {

		Mapper mapper = new Mapper(Rectangle.class, Polygon.class);
		StandardEditorBuilder[] editorBuilder = new StandardEditorBuilder[1];
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				editorBuilder[0] = UI.INSTANCE.openObjectFrame(mapper);
			}
		});
		editorBuilder[0].getCreatedFrame().dispose();

		Rectangle source = new Rectangle();
		Polygon target = (Polygon) mapper.map(source);
		if (target == null) {
			Assert.fail();
		}

		File mappingsFile = new File("tmp/mappings.xml");
		mapper.saveMappings(mappingsFile);
		mapper.loadMappings(mappingsFile);
		if(!mappingsFile.delete()) {
			Assert.fail();
		}

		/* public static */ final Mapper MY_MAPPER = Mapper.get(Rectangle.class, Polygon.class, MappingsExample.class,
				"mappings.xml");
		Polygon target2 = (Polygon) MY_MAPPER.map(source);
		if (!target.getBounds().equals(target2.getBounds())) {
			Assert.fail();
		}

		/* public static */ MappingsExample INSTANCE = com.otk.jvm.util.Mappers.getMapper(MappingsExample.class,
				com.otk.jvm.util.Mappers.MAP_STRUCT_FALLBACK_HANDLER);
		Polygon target3 = INSTANCE.map(source);
		if (!target.getBounds().equals(target3.getBounds())) {
			Assert.fail();
		}
	}

	public interface MappingsExample {

		@MappingsResource(location = "mappings.xml")
		Polygon map(Rectangle rectangle);
	}

}
