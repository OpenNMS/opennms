Integrating JasperServer
------------------------
JasperServer a good tool to create customized reports based on the OpenNMS database.
We have created some reports for availability of applications and nodes and also about
the asset-informations. This feature is a little improvement and the OpenNMS community
was very helpful.

Directorys (-d-) and Files (-f-)
--------------------------------
 -d- etc                 --> contains a plain jasperclient.enable configuration-file
 -f- maven-jasperlibs.sh --> install the libs in m2-Repository look inside for 2.1 or 3.0
 -f- compile.sh          --> mv to OpenNMS source-base and help to compile with the right arguments
 -f- READM.txt           --> welcome ;)

Requirements:
-------------
  1. Get the source from OpenNMS http://sourceforge.net/project/showfiles.php?group_id=4141&package_id=118141
     The source-patch is prepared for usage in OpenNMS unstable SNAPSHOT 1.7.0 If you would like use it in other versions
     modify the version-tag in opennms-webapp/pom.xml
  2. Install JasperServer 3.0 and be sure it works
  3. Create a useraccount for OpenNMS for securityreasons give it read-only access because the credentials are
     stored configurationfile as plain text.
  4. Be sure you can compile OpenNMS source successfull. 
  5. It is recommended to install Acrobat Reader 8 or higher with Webbrowser-plugin to access the reports as PDF.
     If you would like use links in your PDF-Reports

  To install the jar files manually in your .m2-repository download the jars listed in ./maven-jasperlibs.sh and copy
  them to a lib-directory where maven-jasperlibs.sh is or modify the parameter:

      Dfile=lib/jasperreports/jasperreports/3.0.0/jasperreports-3.0.0.jar

Which libs are required:

  - jasperserver-common-ws-3.0.0.jar
  - jasperserver-ireport-plugin-3.0.0.jar
  - axis-1.4patched.jar
  - jaxrpc-1.0.jar
  - saaj-api-1.3.jar

This JARs can not be downloaded. Manual installation required with ./maven-jasperlisbs.sh

To compile OpenNMS i use the following command: 

  ./build.sh clean && ./build.sh -Dopennms.home=$OPENNMS_HOME install assembly:attached

Patch the source
----------------
  1. Extract the tar.bz2
  2. copy opennms-webapp folder into source
  3. install additional jars in your maven-repository with ./maven-jasperlibs.sh
  4. re-compile the source for example with 
     ./build.sh clean && ./build.sh -Dopennms.home=$OPENNMS_HOME install assembly:attached
  5. copy the configuration-file jasperclient.enable to $OPENNMS_HOME/etc
  6. configure your settings in jasperclient.enable and uncomment
  7. Login to your OpenNMS Web Console and navigate to Reports/JasperServer-Reporting now 
     you should get access to your reports as PDF-Documents.
  8. gl&hf


Greetings and contacts
----------------------
Thanks to the OpenNMS Community. Feel free to contact me and do not hesitate to give me
informations for feature- and/or code-improvements.
My contacts

  IRC: irc.freenode.org
  Channel: #opennms
  Nickname: _indigo
  Mail: opennms@open-factory.org
  Blog: http://opennms-me.blogspot.com/
