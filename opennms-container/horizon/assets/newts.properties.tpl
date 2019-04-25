# NEWTS CASSANDRA CONFIGURATION
#
# additional configuration to allow OpenNMS to use cassandra / newts
# This file is placed in OPENNMS-HOME/etc/opennms.properties.d and
# overwrites values defined in openms.properties
#

# Configure storage strategy
org.opennms.rrd.storeByForeignSource=true

###### Time Series Strategy ####
# Use this property to set the strategy used to persist and retrieve time series metrics:
# Supported values are:
#   rrd (default)
#   newts
#   evaluate (for sizing purposes only)
#   tcp (export metrics using protobuf messages over TCP)
org.opennms.timeseries.strategy=newts

### Disables the processing of counter wraps, replacing these with NaNs instead.
org.opennms.newts.nan_on_counter_wrap=true

###### Newts #####
# Use these properties to configure persistence using Newts
# Note that Newts must be enabled using the 'org.opennms.timeseries.strategy' property
# for these to take effect.
#
org.opennms.newts.config.hostname=${OPENNMS_CASSANDRA_HOSTNAMES}
org.opennms.newts.config.keyspace=${OPENNMS_CASSANDRA_KEYSPACE}
org.opennms.newts.config.port=${OPENNMS_CASSANDRA_PORT}
org.opennms.newts.config.username=${OPENNMS_CASSANDRA_USERNAME}
org.opennms.newts.config.password=${OPENNMS_CASSANDRA_PASSWORD}


###### Time Series Strategy ####
#
# TIPP: It is recommended to set the following properties in a dedicated configuration file, e.g. in
# ./etc-overlay/opennms.properties.d/newts.properties
#
# org.opennms.newts.config.read_consistency=ONE
# org.opennms.newts.config.write_consistency=ANY
#
###### Depends the Cassandra cluster's batch_size_fail_threshold_in_kb property
# org.opennms.newts.config.max_batch_size=16
# org.opennms.newts.config.ring_buffer_size=8192
#
###### One year in seconds
# org.opennms.newts.config.ttl=31540000
#
###### Seven days in seconds
# org.opennms.newts.config.resource_shard=604800
#
###### Local In-Memory cache (default)
# org.opennms.newts.config.cache.strategy=org.opennms.netmgt.newts.support.GuavaSearchableResourceMetadataCache
# org.opennms.newts.config.cache.max_entries=8192
#
###### External Redis cache
# org.opennms.newts.config.cache.strategy=org.opennms.netmgt.newts.support.RedisResourceMetadataCache
# org.opennms.newts.config.cache.redis_hostname=localhost
# org.opennms.newts.config.cache.redis_port=6379
