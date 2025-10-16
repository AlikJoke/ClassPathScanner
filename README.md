# ClassPathScanner

A library that enables scanning classpath resources. 

Clients of the library construct scan criteria using the scan API. 
The library filters resources based on these criteria. 

Resources for filtering are determined at build time 
(compilation) using the API declared in ```javax.annotation.processing```.

## Supported resource types
Types of resources the library works with:
1. JPMS modules
2. Packages
3. Classes (standard classes, interfaces, records, enums)
4. Class fields (only for top-level and nested classes; fields are not indexed for local and anonymous classes)
5. Class methods (only for top-level and nested classes; methods are not indexed for local and anonymous classes)
6. Class constructors (only for top-level and nested classes; constructors are not indexed for local and anonymous classes)

## Indexed data
The data indexed and available for runtime scanning includes:
1. The JPMS module to which the resource belongs (if it's a named module);
2. The package containing the resource (if the package is not empty);
3. Resource type;
4. Resource modifiers (e.g., final, abstract, sealed, transient);
5. Resource aliases, specified either via ```ru.joke.classpath.ClassPathIndexed``` or through the configuration file;
6. Annotations applied to the resource (considering annotation inheritance hierarchy; i.e., annotations are effectively inherited);
7. Resource name;
8. For classes:
   1. List of superclasses up to ```java.lang.Object```;
   2. List of interfaces (from all levels of inheritance);
   3. Class type (enum, record, interface, class).


## Resource Indexing Configuration
To specify which resources should be indexed at build time, the library user must either annotate relevant 
elements with the ```ru.joke.classpath.ClassPathIndexed``` annotation, or create a configuration file named 
```scanning-resources.conf``` in the ```META-INF/classpath-indexing``` directory within the client module. 

### Configuration file format
This file must have a format similar to the following:
```
#annotations
ru.joke.test.TestAnnotation1
ru.joke.test.TestAnnotation2

#classes
ru.joke.test.TestAbstractClass
#interfaces
ru.joke.test.TestInterface

#aliases
ru.joke.test/ru.joke.test.TestInterface#field:fieldA;fieldB
```

Four labels are reserved:

- ```#annotations```: defines a list of annotations;
- ```#classes```: defines a list of classes;
- ```#interfaces```: defines a list of interfaces;
- ```#aliases```: defines a set of names for various resources, allowing aliases to be assigned to them.


#### Classes
If a regular Java class ***A*** is annotated with ```ru.joke.classpath.ClassPathIndexed``` or is present 
in the ```#classes``` list in the configuration file, then the following data will be indexed:
1. Class ***A*** itself.
2. All its descendants at any level of the inheritance hierarchy.

For example, if classes ***B*** and ***C*** inherit from ***A***, and class ***D*** inherits from ***B***, 
then ***A***, ***B***, ***C***, and ***D*** will all be included in the index.


#### Interfaces
If an interface ***A*** is annotated with ```ru.joke.classpath.ClassPathIndexed``` or is present in the 
```#interfaces``` list in the configuration file, then the following data will be indexed:
1. Interface ***A*** itself.
2. All interfaces extending it and all implementing classes at any level of the inheritance hierarchy.

For example, if class ***B*** implements interface ***A***, and class ***C*** inherits from ***B***, then
***A***, ***B***, and ***C*** will all be included in the index.

If ***D*** is an interface extending ***A***, and ***E*** is a class implementing ***D***, then ***A***,
***D***, and ***E*** will also be included in the index.


#### Annotations
If an annotation ***A*** is annotated with ```ru.joke.classpath.ClassPathIndexed``` or is present in the 
```#annotations``` list in the configuration file, then the following data will be indexed:
1. Annotation ***A*** itself.
2. All resources that are annotated with annotation ***A***.
3. If any resources annotated with ***A*** are themselves annotations, then resources annotated with 
these discovered annotations will also be indexed.

For example, if:
1. class ***B*** is annotated with annotation ***A***
2. annotation ***C*** is annotated with ***A***
3. class ***D*** is annotated with ***C***
4. class ***E*** inherits from ***D***
5. class ***F*** inherits from ***B***

then ***A***, ***B***, ***C***, ***D***, ***E***, and ***F*** will all be included in the index.


#### Aliases
Alias processing rules do not involve recursive indexing. This means that only the explicitly aliased 
resources themselves are included in the index.


#### Resource Indexing Configuration in a Multi-Module Project
In a multi-module client project, the configuration file can be defined in multiple modules. 
Resources should be specified in configuration files within the modules where they are declared, 
or alternatively, in the root project (the compilation order is crucial).

To enable indexing, the root module of the project must include a dependency on the 
```ru.joke.classpath:classpath-indexer``` module and specify the name of the annotation processor class. 

For example, in a Maven project, this would appear as follows:
```
   <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
         <annotationProcessorPaths>
            <annotationProcessorPath>
               <groupId>ru.joke.utils</groupId>
               <artifactId>classpath-indexer</artifactId>
               <version>${classpath.indexer.version}</version>
            </annotationProcessorPath>
         </annotationProcessorPaths>
         <annotationProcessors>
            <annotationProcessor>ru.joke.classpath.indexer.ClassPathIndexer</annotationProcessor>
         </annotationProcessors>
         <compilerArgs>
            <arg>-ArootProjectOutputDir=${user.dir}/target</arg>
         </compilerArgs>
      </configuration>
   </plugin>
```
For correct indexing to work, you must specify the path to the output directory of the root project 
module using the ```-ArootProjectOutputDir``` argument.
In the example, this is done using the Maven variable ```${user.dir}``` and the ```target``` directory.
If the output directory has a different name, you must specify it (e.g., out, builder, target or another).

When indexing, you can also exclude part of the resources from scanning by providing two additional arguments:
1. ```-AincludedToScanElements```: allows you to explicitly specify masks (as Java regular expressions) for resources that should be included in the index;
2. ```-AexcludedFromScanElements```: allows you to explicitly exclude certain resources using a mask in the form of a Java regular expression.

Using these parameters, you can remove from indexing resources that are a priori not needed at runtime.

## Runtime resource scanning
To perform scanning at runtime, you need to add the following dependency:
```
<dependency>
   <groupId>ru.joke.utils</groupId>
   <artifactId>classpath-scanner</artifactId>
   <version>${classpath.scanner.version}</version>
</dependency>
```
To execute queries, you need to construct a scanning engine using ```ru.joke.classpath.scanner.ClassPathScannerEngines```. 

Example:
```
         final var engine =
                ClassPathScannerEngines.createEngine(
                        "1",
                        ClassPathScannerEngineConfiguration.builder()
                                                            .stateless()
                                                            .defaultScopeFilter(
                                                                    ClassPathScanner.builder()
                                                                                        .begin()
                                                                                            .excludeResourcesInPackages("ru.joke.test3")
                                                                                        .build()
                                                            )
                                                            .disableDefaultScopeOverride()
                                                            .build()
                );

        final var result =
                ClassPathScanner.builder()
                                    .begin()
                                        .begin()
                                            .annotatedBy(TestAnnotation2.class)
                                                .or()
                                            .extendsBy(TestAbstractClass.class)
                                        .end()
                                        .and()
                                            .not().hasModifier(ClassPathResource.Modifier.ABSTRACT)
                                    .build()
                                    .scan(engine);

        for (ClassPathResource classPathResource : result) {
            System.out.println(((ClassResource<?>) classPathResource).asClass().getCanonicalName());
        }
```
More information about the API can be found in the Javadoc for the classes:
- ```ru.joke.classpath.scanner.ClassPathScanner```
- ```ru.joke.classpath.scanner.ClassPathScannerEngines```
- ```ru.joke.classpath.scanner.ClassPathScannerEngine```
- ```ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration```
- ```ru.joke.classpath.scanner.ClassPathScannerBuilder```
