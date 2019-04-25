<?xml version="1.0" encoding="UTF-8"?>
<datasource-configuration xmlns:this="http://xmlns.opennms.org/xsd/config/opennms-datasources"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.opennms.org/xsd/config/opennms-datasources
  http://www.opennms.org/xsd/config/opennms-datasources.xsd ">

  <!-- THIS CONIGURATION FILE IS CREATED FROM A TEMPLATE DURING DOCKER BUILD -->

  <!--
    Available implementations:

      org.opennms.core.db.AtomikosDataSourceFactory
        - Uses Atomikos TransactionEssentials (http://www.atomikos.com/Main/TransactionsEssentials)
          This data source is XA-capable so that it works properly with Hibernate 4.

      org.opennms.core.db.C3P0ConnectionFactory
        - Uses C3P0 (http://sourceforge.net/projects/c3p0/).
          This data source is not XA-capable but is available because it was the default
          datasource on OpenNMS 1.12. It is well tested as a reliable database pool.

      org.opennms.core.db.HikariCPConnectionFactory
        - Uses HikariCP (http://brettwooldridge.github.io/HikariCP/), a lightweight and extremely fast connection pool
   -->
  <connection-pool factory="org.opennms.core.db.C3P0ConnectionFactory"
    idleTimeout="600"
    loginTimeout="3"
    minPool="50"
    maxPool="50"
    maxSize="50" />

  <jdbc-data-source name="opennms"
                    database-name="${OPENNMS_DBNAME}"
                    class-name="org.postgresql.Driver"
                    url="jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${OPENNMS_DBNAME}"
                    user-name="${OPENNMS_DBUSER}"
                    password="${OPENNMS_DBPASS}" />

  <jdbc-data-source name="opennms-admin"
                    database-name="template1"
                    class-name="org.postgresql.Driver"
                    url="jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/template1"
                    user-name="${POSTGRES_USER}"
                    password="${POSTGRES_PASSWORD}" />
</datasource-configuration>
