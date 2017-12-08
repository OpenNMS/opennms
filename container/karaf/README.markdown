Upgrading Karaf
===============

To upgrade our container to use a new base Karaf version, follow these steps.

1. Download and unpack the new Karaf tar.gz file.
1. Run these commands to see the differences in the Karaf etc files:

        diff -r $KARAF_DIR/etc $SRC_DIR/container/karaf/src/main/filtered-resources/etc
        diff -r $KARAF_DIR/etc $SRC_DIR/features/minion/container/karaf/src/main/filtered-resources/etc
        diff -r $KARAF_DIR/etc $SRC_DIR/smoke-test/src/test/resources

1. Apply any relevant changes to the files inside each etc file.
1. Copy the new Karaf "standard" features.xml file into the features project:

        cp $KARAF_DIR/system/org/apache/karaf/features/standard/$KARAF_VERSION/standard-$KARAF_VERSION-features.xml $SRC_DIR/container/features/src/main/resources/karaf/standard.xml

1. Restore the HTTP bridge changes to the features.xml file by making all of the changes between the OPENNMS CUSTOMIZATION comment blocks.
1. Copy the new Karaf "spring" features.xml file into the features project:

        cp $KARAF_DIR/system/org/apache/karaf/features/spring/$KARAF_VERSION/spring-$KARAF_VERSION-features.xml $SRC_DIR/container/features/src/main/resources/karaf/spring.xml

1. Remove the unmodified standard features repo from the spring.xml file by making all of the changes between the OPENNMS CUSTOMIZATION comment blocks.
1. Copy the new Karaf "spring-legacy" features.xml file into the features project:

        cp $KARAF_DIR/system/org/apache/karaf/features/spring-legacy/$KARAF_VERSION/spring-legacy-$KARAF_VERSION-features.xml $SRC_DIR/container/features/src/main/resources/karaf/spring-legacy.xml

1. Remove the unmodified standard features repo from the spring-legacy.xml file by making all of the changes between the OPENNMS CUSTOMIZATION comment blocks.
1. Update the ```karaf-maven-plugin``` configuration inside ```$SRC_DIR/container/karaf/pom.xml``` so that it matches the default Karaf assembly from <https://github.com/apache/karaf/blob/karaf-$KARAF_VERSION/assemblies/apache-karaf/pom.xml> with our additions.
1. Update the ```karaf-maven-plugin``` configuration inside ```$SRC_DIR/features/minion/container/karaf/pom.xml``` so that it matches the default Karaf assembly from <https://github.com/apache/karaf/blob/karaf-$KARAF_VERSION/assemblies/apache-karaf/pom.xml> with our additions.
1. Update the list of repo features inside ```$SRC_DIR/container/features/pom.xml```, ```$SRC_DIR/core/test-api/karaf/pom.xml```, and ```$SRC_DIR/opennms-full-assembly/pom.xml```.
1. Update the Karaf version inside ```$SRC_DIR/core/test-api/karaf/src/main/java/org/opennms/core/test/karaf/KarafTestCase.java```.
1. Update ```$SRC_DIR/pom.xml``` with new value for ```<karafVersion>``` property.
1. Update ```$SRC_DIR/smoke-test/pom.xml``` with new value for ```<karafVersion>``` property.

See commit dea910701c3f48e367636b507fc575b59e70b843 for an example of an upgrade.
