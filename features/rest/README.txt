REST to OSGI

First of all checkout the source of the RestToOSGi branch.
Build and deploy OpenNMS from that code base.

Start your OpenNMS and ssh to the KARAF
cmd> ssh -p 8101 admin@opennms

To server JAXRS Services the OpenNMS KARAF container requires the CXF feature.
--> A feature in this context is a preconfigured set of OSGi bundles for KARAF.
--> KARAF is able to install features from maven resources, too.

This call adds the apache cxf KARAF feature as maven source to KARAF.
karaf> features:addurl mvn:org.apache.cxf.karaf/apache-cxf/2.6.5/xml/features

KARAF is now able to install parts of cxf from maven repositories.

This call lists all features KARAF is aware of at the moment.
karaf> features:list

For this project we require cxf-core and cxf-jaxrs.

karaf> features:install cxf-core
karaf> features:install cxf-jaxrs

Check the that the status of this two features is installed now.
karaf> features:list

--> The KARAF shell supports many linux commands like grep

karaf> features:list | grep cxf

The cxf framework is now providing JAXRS services in KARAF.
By default cxf offers JAXRS with a "cxf/" path extention.
To overwrite this we drop are cfg file into the opennms/etc folder.
The required file is located next to this readme and is called:
org.apache.cxf.osgi.cfg
This file will cause KARAF to overwirte the listed properties for the org.apache.cxf.osgi bundle.
The cxf framework will now provide it's servlet at localhost:8980/opennms/rest2/

After this steps the OpenNMS KARAF is ready for the new Rest OSGi bundles.

The now bundles are located in featuers/rest in the OpenNMS source. To make the new bundles availible in OpenNMS KARAF we add them as jars.
karaf> osgi:install file:/your/path/to/features/rest/core/target/core-1.11.4-SNAPSHOT.jar
karaf> osgi:install file:/your/path/to/features/rest/addon/target/addon-1.11.4-SNAPSHOT.jar

KARAF will return a bundle id for each installed bundle. To start them run start.
karaf> start {bundleID}

Check if the bundle was started correctly.

karaf> list

If the status is not Created check the logs.
karaf> log:display

Now check in your browser if the rest service is working.
browser> http://localhost:8980/opennms/rest2/nodes/
browser> http://localhost:8980/opennms/rest2/nodes/{nodeID}

--> If this urls are not working check cxf vs rest2. KARAF may need a restart.

If you make any changes on the rest/core bundle, just run mvn clean install and remote the bundle from KARAF and install it again.
karaf> uninstall {bundleID}
karaf> osgi:install file:/your/path/to/features/rest/core/target/core-1.11.4-SNAPSHOT.jar
karaf> start {newBundleID}

--> osgi:install -s file:....... will start the bundle directly.

What is implemented at the moment?

The core bundle contains a sample that uses the OpenNMS Node DAO wired by the blueprint. All required dependencies in the pom.xml are provided ...
The addon bundle is not realy working. It could also cause problems.

The JSON output of the core bundle follows the same pattern as the original OpenNMS rest services




