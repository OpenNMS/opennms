== internal-plugins-descriptor

The internal-plugins-descriptor project creates a list (productSpecList.xml) of internal optional plugins packaged with OpenNMS which 
can be installed in a running OpenNMS using either the karaf consol or the Plugin Manager UI. 

This list  is registered with the licenceManager and can then be accessed through the available plugins ReST interface and the AvailablePlugins UI

Plugins are simply Karaf features which have the additional productSpec.xml metadata code defined and a corresponding reference in 
their blueprint.xml definitions. An example blueprint.xml entry for a plugin is shown below.
~~~~
 <!-- register product information for kar descriptor with product registry -->
  <reference id="productRegister" interface="org.opennms.karaf.productpub.ProductRegister" timeout="10000" />

  <bean id="localKarDescriptor" class="org.opennms.karaf.productpub.BundleProductSpecImpl" init-method="registerSpec" destroy-method="unregisterSpec">
    <property name="bundleContext" ref="blueprintBundleContext"></property>
    <property name="productPublisher" ref="productRegister"></property>
    <property name="productMetadataUri" value="/productSpec.xml"></property>
  </bean>
~~~~

The local available plugins list is created at compile time by the internal-plugins-descriptor by scanning the product descriptors for any productSpec.xml files. 
Any productSpec.xml files found in the referenced bundles are added into the list of available plugins in productSpecList.xml.

The product descriptors can either be explicitly referenced in the temporary-features.xml execution of the features-maven-plugin

Alternatively features which contain product descriptors can be referenced in the karaf-maven-plugin configuration and the product descriptors will be found. These features are referenced in the in the <descriptors> tag of the <id>add-features-to-repo</id> task in this projects pom.xml as shown below. 

Example pom.xml entry adding the alarm-change-notifier and opennms-es-rest plugins to the available plugins list
~~~~
         <!-- generates repo which we can traverse to find product descriptor files -->
          <execution>
            <id>add-features-to-repo</id>
            <phase>generate-resources</phase>
            <goals>
              <goal>add-features-to-repo</goal>
            </goals>
            <configuration>
              <!-- this unknown karaf version prevents copying any karaf descriptors into kar - which we do not want -->
              <karafVersion>99999</karafVersion>
              <descriptors>

                <!-- Please define the features containing the plugin product descriptors which -->
                <!-- you want to include in the 'available plugins' list here -->
                
                <descriptor>mvn:org.opennms.plugins/alarm-change-notifier/${project.version}/xml/features</descriptor>
                <descriptor>mvn:org.opennms.plugins/opennms-es-rest/${project.version}/xml/features</descriptor>

                <!-- this descriptor is generated locally to include product descriptors where we don't want to parse the full feature -->
                <descriptor>file:${project.build.directory}/temp-features/features.xml</descriptor>
              </descriptors>
              <features>
                <!-- features you want to include -->
                <!-- <feature>${productName}/${project.version}</feature> -->
                <feature>alarm-change-notifier/${project.version}</feature>
                <feature>opennms-es-rest/${project.version}</feature>

                <!-- locally generated feature containing specific product descriptors -->
                <feature>transientFeature/${project.version}</feature>
              <repository>${project.build.directory}/temp-features-repo</repository>
            </configuration>
          </execution>
        </executions>
      </plugin>
 ~~~~