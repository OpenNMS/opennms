# set up test

## Timescale 
* set password for postgres user in nano target/opennms-26.0.0-SNAPSHOT/etc/opennms-datasources.xml to ``password`
* run timescale:
  ``sudo docker run -p 5432:5432 -e POSTGRES_PASSWORD=password timescale/timescaledb:latest-pg11|run timescale docker container``
* make sure the timescale plugin ist installed: ``select * from pg_extension;``
* create opennms schema: ``sudo <opennms_home>/bin/install -dis``
* init timescale: ``sudo ./bin/timescale init``

## Newts / cassandra
* set ```org.opennms.timeseries.strategy=timescale``` in opennms.properties
* start cassandra docker container: ```sudo docker run -p 7199:7199 -p 7000:7000 -p 7001:7001 -p 9160:9160 -p 9042:9042 cassandra:3```
* init newts: ``sudo ./bin/newts init``

## TODOs
* Create structure to store metadata in postgres
* remove all Newts stuff from Abstraction layer
