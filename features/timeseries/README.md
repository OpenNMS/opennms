# set up test

## Timescale 
* set password for postgres user in nano target/opennms-26.0.0-SNAPSHOT/etc/opennms-datasources.xml to ``password`
* run timescale:
  ``sudo docker run -p 5432:5432 -e POSTGRES_PASSWORD=password timescale/timescaledb:latest-pg11``
* create opennms schema: ``sudo <opennms_home>/bin/install -dis``
* init timescale: ``sudo ./bin/timescale init``
* set ```org.opennms.timeseries.strategy=timescale``` in opennms.properties

## InfluxDB
* run influxdb: ``sudo docker run -p 9999:9999 quay.io/influxdb/influxdb:2.0.0-beta --reporting-disabled``
* init influxdb: ``sudo ./bin/influxdb init``

## Newts / cassandra
* start cassandra docker container: ```sudo docker run -p 7199:7199 -p 7000:7000 -p 7001:7001 -p 9160:9160 -p 9042:9042 cassandra:3```
* init newts: ``sudo ./bin/newts init``

## Hints
* make sure the timescale plugin ist installed: ``select * from pg_extension;``

## TODOs
* finish influxdb
* make configurable which timeseries to use
* remove all Newts stuff from Abstraction layer
* set up module structure properly
* clean up opennms.properties
* consolidate cmd commands, they do the same
* The code makes use of the lombok library. This makes for much faster prototyping by remvoving lots of boilerplate code. Since we haven't agreed on using this library we might need to remove it (shouldn't be a problem)
