# J-Visual Mapper

![alt JESB icon](https://github.com/dotxyteam/JVisualMapper/blob/main/j-visual-mapper/misc/screenshot.png?raw=true)

# Overview

The goal of this project is to provide a graphical alternative to tools (such as [MapStruct](http://mapstruct.org/), [Dozer](https://github.com/DozerMapper/dozer), [Orika](https://github.com/orika-mapper/orika), [ModelMapper](http://modelmapper.org/), [JMapper](https://github.com/jmapper-framework/jmapper-core), [Selma](http://www.selma-java.org/), etc.) that implement mappings between Java types.

# Use Cases

- Format conversion
- Mappings between domain objects (see DDD=Domain-driven design) and DTOs (data transfer objects)
- …

# Advantages

- Graphical and XML-based
- JIT (just in time) mapping compilation
- Java-based expression editor (no specific language to learn)
- Can cohabit with other mapping frameworks (MapStruct, ...)

# Compatibility

Tested on Windows & Linux with Java 8 & 21.

# Licensing

It is distributed under this
[license](https://github.com/dotxyteam/JVisualMapper/blob/master/j-visual-mapper/LICENSE).

# Download

*  [Get the source code and the binaries↓](https://github.com/dotxyteam/JVisualMapper/releases)

# Getting started

*   Add the dependency to your project (Maven dependency above):
```xml
    <dependency>
    <groupId>com.github.dotxyteam</groupId>
    <artifactId>j-visual-mapper</artifactId>
    <version>LATEST</version>
    </dependency>
```

*   Snippets:
```java
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
public static final Mapper MY_MAPPER = Mapper.get(Rectangle.class, Polygon.class, MappingsExample.class,
		"mappings.xml");
target = (Polygon) MY_MAPPER.map(source);
System.out.println(target);

// Hide the implementation details of the mappings in an interface and (may be)
// cohabit with other mappings frameworks (MapStruct, ...):
public static MappingsExample INSTANCE = com.otk.jvm.util.Mappers.getMapper(MappingsExample.class,
		com.otk.jvm.util.Mappers.MAP_STRUCT_FALLBACK_HANDLER);
target = INSTANCE.map(source);
System.out.println(target);
```

# Support

The support page is hosted [here on GitHub](https://github.com/dotxyteam/JVisualMapper/issues). You can also contact us by email: [dotxyteam@yahoo.fr](mailto:dotxyteam@yahoo.fr).
