#!/bin/sh -

# Builde the mavenizer
mvn -DdescriptorId=jar-with-dependencies -Dmaven.test.skip=true \
	assembly:assembly || exit 1

# Run the mavenizer
java -cp target/opennms-mavenizer-1.0-SNAPSHOT-jar-with-dependencies.jar \
	-Dopennms.dir=.. \
	org.opennms.mavenize.Mavenize \
	src/test/resources/opennmsMavenizeSpec.xml || exit 1
