#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#

# Configure storage strategy
org.opennms.rrd.storeByForeignSource={{getv "/opennms/rrd/storebyforeignsource" "true"}}
org.opennms.timeseries.strategy={{getv "/opennms/timeseries/strategy" "rrd"}}
org.opennms.rrd.interfaceJar={{getv "/opennms/rrd/interfacejar" "/usr/share/java/jrrd2.jar"}}
org.opennms.rrd.strategyClass={{getv "/opennms/rrd/strategyclass" "org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy"}}
opennms.library.jrrd2={{getv "/opennms/library/jrrd2" "/usr/lib64/libjrrd2.so"}}
