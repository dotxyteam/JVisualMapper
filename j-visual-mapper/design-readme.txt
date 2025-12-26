MIGRATION FROM OTHER FRAMEWORKS
-------------------------------
To facilitate adoption, the decision was made to initially simplify 
the partial or complete transition from projects using MapStruct. 
Therefore, it is possible to selectively switch mapping methods simply 
by replacing the org.mapstruct.factory.Mappers class with 
com.otk.jvm.util.Mappers and specifying the locations of the mappings 
files using the com.otk.jvm.annotation.MappingsResource annotation.