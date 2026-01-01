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
- JIT (Just in time) mapping compilation
- Java-based expression editor (no specific language to learn)
- Can coexist with other mapping frameworks (MapStruct, ...)

# Compatibility

Tested on Windows & Linux.

# Licensing

It is distributed under this
[license](https://github.com/dotxyteam/JVisualMapper/blob/master/j-visual-mapper/LICENSE).

# Download

*  [Get the source code and the binaries↓](https://github.com/dotxyteam/JVisualMapper/releases)

# Getting started

*   Add the dependency to your project (Maven dependency above):

    <dependency>
    <groupId>com.github.dotxyteam</groupId>
    <artifactId>j-visual-mapper</artifactId>
    <version>LATEST</version>
    </dependency>

*   Create a Mapper object and open it in the graphical editor:

    Mapper mapper = new Mapper(sourceClass, targetClass);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UI.INSTANCE.openObjectFrame(mapper);
			}
		});



# Documentation

Explore the [Wiki](https://github.com/dotxyteam/JEnterpriseServiceBus/wiki) to learn more.

# Support

The support page is hosted [here on GitHub](https://github.com/dotxyteam/JEnterpriseServiceBus/issues). You can also contact us by email: [dotxyteam@yahoo.fr](mailto:dotxyteam@yahoo.fr).
