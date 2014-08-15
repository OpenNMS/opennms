# Event Forwarding over ActiveMQ

To install the ActiveMQ event forwarder between two OpenNMS systems, "dispatcher" and "receiver" Karaf features need to be installed on the two systems. The event channel is a one-way connection from the dispatcher to the receiver.

## Receiver

- Upgrade to the latest build from the `pjsm/2.0` branch.
- Log into Karaf.
- Run this command to start the default ActiveMQ broker (with broker name "opennms") on OpenNMS:

        features:install opennms-activemq

- Verify that ActiveMQ has opened public port 61616 on the receiver system.
- Verify that there are no errors on the log.
- Install the Event Receiver feature that uses the local ActiveMQ broker:

        features:install opennms-activemq-event-receiver

## Dispatcher

- Upgrade to the latest build from the `pjsm/2.0` branch.
- Log into Karaf.
- Run this command to install the default ActiveMQ dispatcher configuration:

        features:install opennms-activemq-dispatcher-config

- Update the configuration with the broker URI of the receiver system: (we should probably clarify these property names in the near future)

        config:edit -f org.apache.activemq.server-dispatcher
        config:propset broker-name [globally unique name for the dispatcher's broker]
        config:propset brokerUri tcp://[receiver IP address]:61616
        config:update

- Set the location name that the Event Forwarder will append as a header to outgoing messages:

        config:edit org.opennms.features.activemq.eventforwarder
        config:propset location [location name]
        config:update

- Install the Event Forwarder feature:

        features:install opennms-activemq-event-forwarder

- Verify that there are no errors in the log.
- Verify that localhost port 61716 has been opened by ActiveMQ for local connections.
- Verify in the Karaf log that the network connector to `brokerUri` has been opened.
- Verify with Karaf Camel commands that events are being forwarded properly.

At this point, the system will be running a local ActiveMQ broker on localhost port 61716 that will forward events to the remote broker running at the `brokerUri` location.
