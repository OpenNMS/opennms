
# Builde the mavenizer
mvn -DdescriptorId=jar-with-dependencies assembly:assembly

# Run the mavenizer
java -cp target/opennms-mavenizer-1.0-SNAPSHOT-jar-with-dependencies.jar -Dopennms.dir=.. org.opennms.mavenize.Mavenize src/test/resources/opennmsMavenizeSpec.xml




