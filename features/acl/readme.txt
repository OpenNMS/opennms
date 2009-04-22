Databases and configurations
Create acl-test and acl db with the sql scripts inside /db folder
Configure the acl_test database properties in /src/test/java/org/opennms/acl/conf/config.properties
Configure the acl database properties in /src/main/webapp/WEB-INF/conf/config.properties
Configure the logs path in /src/main/webapp/WEB-INF/conf/log4j.properties

Start webapp
type in the shell: mvn jetty:run &
open your browser at http://localhost:8080/opennms-acl
log with username:admin password adminadmin
to stop the webapp type in the shell: mvn jetty:stop



