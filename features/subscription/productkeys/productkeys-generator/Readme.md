This project is generated in a forked maven build which calls pom-exec.xml to avoid inheritance of incorrect artifact ids.

=== Main Repository Settings

You should ensure the values of the deploy repositories match the main build so that licence authenticator artifacts go in the same repo as the main OpenNMS build

check the following settings in pom-exec.xml

----
  <!-- This setting should match the OpenNMS main pom.xml -->
  <distributionManagement>
    <repository>
      <id>opennms-repo</id>
      <name>OpenNMS Maven Repository</name>
      <url>scpexe://repo.opennms.org/var/www/sites/opennms.org/site/repo/maven2</url>
    </repository>
    <snapshotRepository>
      <id>opennms-snapshots</id>
      <name>OpenNMS Snapshot Maven Repository</name>
      <url>scpexe://repo.opennms.org/var/www/sites/opennms.org/site/repo/snapshots</url>
    </snapshotRepository>
  </distributionManagement>
----

=== Licence Repository Settings

You should set up a sepaarate private repository for licences.
The following settings should be adjusted in the licence-spec projects
----
Set correctly in 
licence-spec
licence-spec-feature 

  <distributionManagement>
    <repository>
      <id>osgi-plugins-licence-specs</id>
      <url>http://admin:admin123@localhost:28081/nexus/content/repositories/osgi-plugins-licence-specs</url>
    </repository>
    <snapshotRepository>
      <id>osgi-plugins-licence-specs-snapshots</id>
      <url>http://admin:admin123@localhost:28081/nexus/content/repositories/osgi-plugins-licence-specs-snapshots</url>
    </snapshotRepository>
  </distributionManagement>
----