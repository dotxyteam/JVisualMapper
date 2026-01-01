package com.otk.jvm;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import javax.swing.SwingUtilities;

import com.otk.jvm.Mapper.UI;
import com.otk.jvm.annotation.MappingsResource;

public interface MappingsExample {

	public static void main(String[] args) throws Exception {

		// Create a Mapper object and open it in the graphical editor:
		Mapper mapper = new Mapper(Rectangle.class, Polygon.class);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UI.INSTANCE.openObjectFrame(mapper);
			}
		});

		// Execute the mappings
		Rectangle source = new Rectangle();
		System.out.println(source);
		Polygon target = (Polygon) mapper.map(source);

		// Save/Load the mappings to/from a file (or a stream):
		File mappingsFile = new File("tmp/mappings.xml");
		mapper.saveMappings(mappingsFile);
		mapper.loadMappings(mappingsFile);

		// Conveniently declare a constant Mapper associated with a resource (if the
		// resource is found then the graphical editor will be automatically open and
		// the mappings modifications will be testable in realtime and persistable):
		/* public static */ final Mapper MY_MAPPER = Mapper.get(Rectangle.class, Polygon.class, MappingsExample.class,
				"mappings.xml");
		target = (Polygon) MY_MAPPER.map(source);
		System.out.println(target);

		// Hide the implementation details of the mappings in an interface and (may be)
		// cohabit with other mappings frameworks (MapStruct, ...):
		/* public static */ MappingsExample INSTANCE = com.otk.jvm.util.Mappers.getMapper(MappingsExample.class,
				com.otk.jvm.util.Mappers.MAP_STRUCT_FALLBACK_HANDLER);
		target = INSTANCE.map(source);
		System.out.println(target);
	}

	@MappingsResource(location = "mappings.xml")
	Polygon map(Rectangle rectangle);
}
