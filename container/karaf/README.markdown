Upgrading Karaf
===============

To upgrade our container to use a new base Karaf version, follow these steps.

1. Download and unpack the new Karaf tar.gz file.
1. Run this command to see the differences in the Karaf etc files:
        diff -r $KARAF_DIR/etc $SRC_DIR/container/karaf/src/main/filtered-resources/etc
1. Apply any relevent changes to the files inside $SRC_DIR/container/karaf/src/main/filtered-resources/etc.
1. Copy the new Karaf features.xml file into the container project:
        cp $KARAF_DIR/system/org/apache/karaf/assemblies/features/standard/$KARAF_VERSION/standard-$KARAF_VERSION-features.xml $SRC_DIR/container/karaf/src/main/filtered-resources/features/features.xml
1. Restore the HTTP bridge changes to the features.xml file by making all of the changes between the OPENNMS CUSTOMIZATION comment blocks.
1. Update $SRC_DIR/pom.xml with new value for <karafVersion/> property.

See commit dea910701c3f48e367636b507fc575b59e70b843 for an example of an upgrade.
