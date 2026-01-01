package com.otk.jvm;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import javax.swing.SwingUtilities;

import com.otk.jvm.Mapper.UI;
import com.otk.jvm.annotation.MappingsResource;

public interface Example {

	public static void main(String[] args) throws Exception {

		Mapper mapper = new Mapper(Rectangle.class, Polygon.class);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				UI.INSTANCE.openObjectDialog(null, mapper);
			}
		});
		mapper.validate();

		Rectangle source = new Rectangle();
		System.out.println(source);
		Polygon target = (Polygon) mapper.map(source);

		File mappingsFile = new File("tmp/mappings.xml");
		mapper.saveMappings(mappingsFile);
		mapper.loadMappings(mappingsFile);

		final Mapper MY_MAPPER = Mapper.get(Rectangle.class, Polygon.class, Example.class,
				"mappings.xml");
		target = (Polygon) MY_MAPPER.map(source);
		System.out.println(target);

		Example INSTANCE = com.otk.jvm.util.Mappers.getMapper(Example.class,
				com.otk.jvm.util.Mappers.MAP_STRUCT_FALLBACK_HANDLER);
		target = INSTANCE.map(source);
		System.out.println(target);
	}

	@MappingsResource(location = "mappings.xml")
	Polygon map(Rectangle rectangle);
}
