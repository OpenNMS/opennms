<?xml version="1.0" encoding="UTF-8"?>
<datasource-configuration xmlns:this="http://xmlns.opennms.org/xsd/config/opennms-datasources"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.opennms.org/xsd/config/opennms-datasources
  http://www.opennms.org/xsd/config/opennms-datasources.xsd ">

  <!--
    DON'T EDIT THIS FILE :: GENERATED WITH CONFD
  -->

  <!--
    Available implementations:

      org.opennms.core.db.C3P0ConnectionFactory
        - Uses C3P0 (http://sourceforge.net/projects/c3p0/).
          This data source is not XA-capable but is available because it was the default
          datasource on OpenNMS 1.12. It is well tested as a reliable database pool.

      org.opennms.core.db.HikariCPConnectionFactory
        - Uses HikariCP (http://brettwooldridge.github.io/HikariCP/), a lightweight and extremely fast connection pool
   -->
  <connection-pool factory="{{getv "opennms/database/connection/poolfactory" "org.opennms.core.db.HikariCPConnectionFactory"}}"
    idleTimeout="{{getv "opennms/database/connection/idletimeout" "600"}}"
    loginTimeout="{{getv "opennms/database/connection/logintimeout" "3"}}"
    minPool="{{getv "opennms/database/connection/minpool" "50"}}"
    maxPool="{{getv "opennms/database/connection/maxpool" "50"}}"
    maxSize="{{getv "opennms/database/connection/maxsize" "50"}}" />

  <jdbc-data-source name="opennms"
                    database-name="{{getv "/opennms/dbname" "opennms"}}"
                    class-name="org.postgresql.Driver"
                    url="jdbc:postgresql://{{getv "/postgres/host" "database"}}:{{getv "/postgres/port" "5432"}}/{{getv "/opennms/dbname" "opennms"}}"
                    user-name="{{getv "/opennms/dbuser" "opennms"}}"
                    password="{{getv "/opennms/dbpass" "opennms"}}" />

  <jdbc-data-source name="opennms-admin"
                    database-name="template1"
                    class-name="org.postgresql.Driver"
                    url="jdbc:postgresql://{{getv "/postgres/host" "database"}}:{{getv "/postgres/port" "5432"}}/template1"
                    user-name="{{getv "/postgres/user" "postgres"}}"
                    password="{{getv "/postgres/password" "postgres"}}"/>
</datasource-configuration>
