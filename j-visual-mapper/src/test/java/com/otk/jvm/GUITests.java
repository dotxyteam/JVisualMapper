package com.otk.jvm;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mapstruct.example.dto.FishTankDto;
import org.mapstruct.example.mapper.FishTankMapper;
import org.mapstruct.example.model.Fish;
import org.mapstruct.example.model.FishTank;
import org.mapstruct.example.model.Interior;
import org.mapstruct.example.model.MaterialType;
import org.mapstruct.example.model.Ornament;

import com.otk.jesb.JESB;
import com.otk.jesb.ui.GUI;
import com.otk.jvm.annotation.MappingsResource;

import xy.reflect.ui.control.swing.customizer.MultiSwingCustomizer.SubSwingCustomizer;
import xy.reflect.ui.util.MiscUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public class GUITests {

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
	public void testFirstUse() throws Exception {

		FishTank source = new FishTank();
		source.setFish(new Fish());
		String FISH_TYPE = "FT";
		source.getFish().setType(FISH_TYPE);
		source.setInterior(new Interior());
		source.getInterior().setOrnament(new Ornament());
		String ORNAMENT_TYPE = "OT";
		source.getInterior().getOrnament().setType(ORNAMENT_TYPE);
		source.setMaterial(new MaterialType());
		String MATERIAL_TYPE = "MT";
		source.getMaterial().setType(MATERIAL_TYPE);

		FishTankDto target;
		target = FishTankMapper.INSTANCE.map(source);
		if (Mapper.UI.INSTANCE.getSubCustomizerByIdentifier().values().stream()
				.map(SubSwingCustomizer::getAllDisplayedForms).map(List::size).reduce(Integer::sum).orElse(0) > 0) {
			Assert.fail();
		}
		if (!FISH_TYPE.equals(target.getFish().getKind())) {
			Assert.fail();
		}
		if (!ORNAMENT_TYPE.equals(target.getOrnament().getType())) {
			Assert.fail();
		}
		if (!MATERIAL_TYPE.equals(target.getMaterial().getMaterialType().getType())) {
			Assert.fail();
		}

		target = FishTankMapper.INSTANCE.mapAsWell(source);
		if (target.getFish() != null) {
			Assert.fail();
		}

		try {
			TestingUtils.assertSuccessfulReplay(tester, new File("test-specifications/testFirstUse.stt"));
		} finally {
			String mappingsFileName = new File(FishTankMapper.class.getMethod("mapAsWell", FishTank.class)
					.getAnnotation(MappingsResource.class).location()).getName();
			File mappingsFile = new File(mappingsFileName);
			if (mappingsFile.exists()) {
				if (!mappingsFile.delete()) {
					Assert.fail();
				}
			} else {
				Assert.fail();
			}
		}
		if (Mapper.UI.INSTANCE.getSubCustomizerByIdentifier().values().stream()
				.map(SubSwingCustomizer::getAllDisplayedForms).map(List::size).reduce(Integer::sum).orElse(0) > 0) {
			Assert.fail();
		}

		target = FishTankMapper.INSTANCE.mapAsWell(source);
		if (target.getFish().getKind() == null) {
			Assert.fail();
		}

		if (Mapper.UI.INSTANCE.getSubCustomizerByIdentifier().values().stream()
				.map(SubSwingCustomizer::getAllDisplayedForms).map(List::size).reduce(Integer::sum).orElse(0) > 0) {
			Assert.fail();
		}

	}

}
