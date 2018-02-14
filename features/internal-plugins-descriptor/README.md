== internal-plugins-descriptor

The internal-plugins-descriptor project creates a list of internal optional plugins packaged with OpenNMS which 
can be installed in a running OpenNMS using either the karaf consol or the Plugin Manager UI. 

Plugins are simply Karaf features which have the additional productSpec.xml metadata code defined and a corresponding reference in 
their blueprint.xml definitions. An example blueprint.xml entry for a plugin is shown below
~~~~
 <!-- register product information for kar descriptor with product registry -->
  <reference id="productRegister" interface="org.opennms.karaf.productpub.ProductRegister" timeout="10000" />

  <bean id="localKarDescriptor" class="org.opennms.karaf.productpub.BundleProductSpecImpl" init-method="registerSpec" destroy-method="unregisterSpec">
    <property name="bundleContext" ref="blueprintBundleContext"></property>
    <property name="productPublisher" ref="productRegister"></property>
    <property name="productMetadataUri" value="/productSpec.xml"></property>
  </bean>
~~~~

The available plugins list is created at compile time by the internal-plugins-descriptor by scanning the features referenced
 in the <descriptors> tag of the <id>add-features-to-repo</id> task in this projects pom.xml as shown below. 
  
Any productSpec.xml files found in the referenced bundles are added into the list of available plugins.

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

              </descriptors>
              <features>
                <!-- <feature>${productName}/${project.version}</feature> -->
                <feature>alarm-change-notifier/${project.version}</feature>
                <feature>opennms-es-rest/${project.version}</feature>
              </features>
              <repository>${project.build.directory}/temp-features-repo</repository>
            </configuration>
          </execution>
        </executions>
      </plugin>
 ~~~~