# webapp2diagram

A maven plugin to create diagrams based on webapps. The current version only supports manifestation diagrams and will base it on a packaged ear or war file.
It creates an image that can be included in for i.e. in the project site.

## goals
name | defaultPhase
--- | ---
manifest-diagram | site

## configuration
property | required | default | description 
--- | --- | --- | ---
file | true | ${project.build.directory}\${project.build.finalName}.${project.packaging} | The file to generate a plantUML manifestation diagram for.
name | false | ${project.artifactId} | The name of the file the generated manifestation diagram will receive.
outputDir | false | ${project.build.directory}\site\resources\ear2plantuml\ | The target directory where the manifestation diagram will be stored.
markAsEJB | false |  | Haven't had time to figure out ejb detection. For now let the users mark jar files as such.
excludeDirs | false |  | Not all folders are interesting to include in the diagram. I.e. classes. List directories here that will not be traversed.
filterJars | false |  | List with wildcard expression (i.e. "commons-*" that will be matched against jar files (without extension). It will be included if the jar file matches one of the wildcard expressions.

## usage
```xml
            <plugin>
                <groupId>nl.robinvandenbogaard.plugins</groupId>
                <artifactId>webapp2diagram-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <configuration>
                    <excludeDirs>
                        <param>classes</param>
                        <param>maven</param>
                    </excludeDirs>
                    <filterJars>
                        <param>*cli*</param>
                    </filterJars>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>manifest-diagram</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

## examples
see the directory examples and run `mvn package site` on each project. The result for `webapp-example` is this diagram:
![alt text](https://github.com/robinvandenbogaard/webapp2diagram/blob/master/docs/webapp2diagram-simple-war-example.svg "Manifestation diagram for webapp-example.")
