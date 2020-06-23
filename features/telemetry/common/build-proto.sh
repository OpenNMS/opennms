#!/bin/sh
# This script will generate the TelemetryProtos.java file.
# It requires the google protocol buffers compiler
# Reference: https://developers.google.com/protocol-buffers/docs/javatutorial#the-protocol-buffer-api
#
# A few customizations need to be applied after generaton.
#
# The standard OpenNMS header needs to be applied
#
# The following imports are needed:
#    import org.opennms.core.ipc.sink.api.Message;
#    import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
#
# The following class `TelemetryMessage` needs to also implement `TelemetryMessageLogEntry`
#
# The following method needs to be implemented in the `TelemetryMessage` class:
#    @Override
#    public byte[] getByteArray() {
#      return bytes_.toByteArray();
#    }
#
# The following class `TelemetryMessageLog` needs to also implement
#  `Message` and `org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog`
#
protoc -I=./src/main/resources/ --java_out=./src/main/java/ ./src/main/resources/telemetry.proto
