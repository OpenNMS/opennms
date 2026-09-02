# Install the Prometheus RemoteWrite plugin on every boot. The wait-for-kar
# attribute makes the Karaf extender wait until deploy/opennms-prometheus-remotewrite-plugin.kar
# has been unpacked before installing the feature.
opennms-plugins-prometheus-remotewrite wait-for-kar=opennms-prometheus-remotewrite-plugin
