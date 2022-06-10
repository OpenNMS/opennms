

To upgrade our container to use a new base Karaf version, follow these steps.

1. Download and unpack the new Karaf tar.gz file.
2. Run these commands to see the differences in the Karaf etc files:

        diff -r $KARAF_DIR/etc $SRC_DIR/container/karaf/src/main/filtered-resources/etc
        diff -r $KARAF_DIR/etc $SRC_DIR/features/container/minion/src/main/filtered-resources/etc
        diff -r $KARAF_DIR/etc $SRC_DIR/features/container/sentinel/src/main/filtered-resources/etc

3. Apply any relevant changes to the files inside each etc file. 

4. Update the ```karaf-maven-plugin``` configuration inside ```$SRC_DIR/container/karaf/pom.xml``` so that it matches the default Karaf assembly from <https://github.com/apache/karaf/blob/karaf-$KARAF_VERSION/assemblies/apache-karaf/pom.xml> with our additions.
5. Update the ```karaf-maven-plugin``` configuration inside ```$SRC_DIR/features/minion/container/karaf/pom.xml``` so that it matches the default Karaf assembly from <https://github.com/apache/karaf/blob/karaf-$KARAF_VERSION/assemblies/apache-karaf/pom.xml> with our additions.
6. Update the list of repo features inside ```$SRC_DIR/container/features/pom.xml```, ```$SRC_DIR/core/test-api/karaf/pom.xml```, and ```$SRC_DIR/opennms-full-assembly/pom.xml```.
7. Update the Karaf version inside ```$SRC_DIR/core/test-api/karaf/src/main/java/org/opennms/core/test/karaf/KarafTestCase.java```.
8. Update ```$SRC_DIR/pom.xml``` with new value for ```<karafVersion>``` property.
9. Update ```$SRC_DIR/smoke-test/pom.xml``` with new value for ```<karafVersion>``` property.

See commit dea910701c3f48e367636b507fc575b59e70b843 for an example of an upgrade.
