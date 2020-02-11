# Timeseries Integration Layer
The timeseries abstraction layer allows for an easy integration of different timeseries databases. Currently we support:
* in memory (only for testing purposes)
* timescale
* influxdb

In order to use the integration layer set:
* ```org.opennms.timeseries.strategy=integration``` in opennms.properties and enable one of the following implementations:
  * ```org.opennms.timeseries.impl=org.opennms.netmgt.timeseries.impl.timescale.TimescaleStorage```
  * ```org.opennms.timeseries.impl=org.opennms.netmgt.timeseries.impl.influxdb.InfluxdbStorage```
  * ```org.opennms.timeseries.impl=org.opennms.netmgt.timeseries.impl.memory.InMemoryStorage```
  
* start cassandra docker container: ```sudo docker run -p 7199:7199 -p 7000:7000 -p 7001:7001 -p 9160:9160 -p 9042:9042 cassandra:3```
* init newts: ``sudo ./bin/newts init``
  
Based on what you enabled you need to do some more configuration:

## In Memory
nothing 

## Timescale 
* set password for postgres user in nano target/opennms-26.0.0-SNAPSHOT/etc/opennms-datasources.xml to ``password`
* run timescale:
  ``sudo docker run -p 5432:5432 -e POSTGRES_PASSWORD=password timescale/timescaledb:latest-pg11``
* create opennms schema: ``sudo <opennms_home>/bin/install -dis``
* init timescale: ``sudo ./bin/timescale init``

## InfluxDB
* run influxdb: ``sudo docker run -p 9999:9999 quay.io/influxdb/influxdb:2.0.0-beta --reporting-disabled``
* init influxdb: ``sudo ./bin/influxdb init``, it will set up the influxdb instance and return an access token.
* add the token to opennms.properties at: ``org.opennms.timeseries.influxdb.token``

## Open TODOs / discuss with Jesse
* caching strategy and cache priming:
  * they seem to depend on org.opennms.newts.cassandra.search.NewtsCassandraCachePrimer, from the Newts library, do we want to replicate that or is it sufficient to rewrite the cache to store on fetch
  
* clean up opennms.properties
* go with Jesse over applicationContext-timeseries-integration.xml
* consolidate cmd commands, they do the same
* The code makes use of the lombok library. This makes for much faster prototyping by remvoving lots of boilerplate code. Since we haven't agreed on using this library we might need to remove it (shouldn't be a problem)
