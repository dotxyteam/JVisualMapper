/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.mapstruct.example.mapper;

import javax.swing.SwingUtilities;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.example.dto.FishTankDto;
import org.mapstruct.example.model.FishTank;

import com.otk.jvm.annotation.MappingsResource;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

@Mapper
public interface FishTankMapper {

	public static void main(String[] args) {
		FishTank source = new FishTank();
		FishTankDto target = INSTANCE.map(source);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingRenderer.getDefault().openObjectFrame(target);
			}
		});
	}

	FishTankMapper INSTANCE = com.otk.jvm.util.Mappers.getMapper(FishTankMapper.class,
			com.otk.jvm.util.Mappers.MAP_STRUCT_FALLBACK_HANDLER);

	@Mapping(target = "fish.kind", source = "fish.type")
	@Mapping(target = "fish.name", ignore = true)
	@Mapping(target = "ornament", source = "interior.ornament")
	@Mapping(target = "material.materialType", source = "material")
	@Mapping(target = "quality.report.organisation.name", source = "quality.report.organisationName")
	FishTankDto map(FishTank source);

	@Mapping(target = "fish.kind", source = "source.fish.type")
	@Mapping(target = "fish.name", ignore = true)
	@Mapping(target = "ornament", source = "source.interior.ornament")
	@Mapping(target = "material.materialType", source = "source.material")
	@Mapping(target = "quality.report.organisation.name", source = "source.quality.report.organisationName")
	@MappingsResource(location = "FishTankMapper-mapAsWell-FishTank2FishTankDto.mappings")
	FishTankDto mapAsWell(FishTank source);

	@InheritInverseConfiguration(name = "map")
	FishTank map(FishTankDto source);

}
