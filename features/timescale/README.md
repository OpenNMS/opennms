# set up test

## Timescale 
* set password for postgres user in nano target/opennms-26.0.0-SNAPSHOT/etc/opennms-datasources.xml to ``password`
* run timescale:
  ``sudo docker run -p 5432:5432 -e POSTGRES_PASSWORD=password timescale/timescaledb:latest-pg11``
* create opennms schema: ``sudo <opennms_home>/bin/install -dis``
* init timescale: ``sudo ./bin/timescale init``
* set ```org.opennms.timeseries.strategy=timescale``` in opennms.properties

## Newts / cassandra
* start cassandra docker container: ```sudo docker run -p 7199:7199 -p 7000:7000 -p 7001:7001 -p 9160:9160 -p 9042:9042 cassandra:3```
* init newts: ``sudo ./bin/newts init``

## Hints
* make sure the timescale plugin ist installed: ``select * from pg_extension;``

## TODOs
* Understand difference between index and insert
* Reading metadata from postgres
* remove all Newts stuff from Abstraction layer
* The code makes heavy use of the lombok library. This makes for much faster prototyping by remvoving lots of boilerplate code. Since we haven't agreed on using this library we might need to remove it (shouldn't be a problem)
