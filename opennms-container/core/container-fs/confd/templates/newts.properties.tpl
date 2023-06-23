#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#

{{$cassandraPath := "/opennms/cassandra/" -}}

org.opennms.newts.config.hostname={{getv (print $cassandraPath "hostname") "hostname"}}
org.opennms.newts.config.keyspace={{getv (print $cassandraPath "keyspace") "newts"}}
org.opennms.newts.config.port={{getv (print $cassandraPath "port") "9042"}}
org.opennms.newts.config.username={{getv (print $cassandraPath "username") "cassandra"}}
org.opennms.newts.config.password={{getv (print $cassandraPath "password") "cassandra"}}
