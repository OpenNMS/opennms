package org.opennms.netmgt.eventd.processor;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.event.Event;

/**
 * Serializes {@link Event} objects to UTF-8 XML bytes using JAXB.
 *
 * <p>Used by {@link FaultEventPublisher} to serialize events for Kafka.
 * This produces the same XML format that daemon containers use via
 * {@code KafkaEventForwarder}.</p>
 */
public class XmlEventSerializer implements Function<Event, byte[]> {
    @Override
    public byte[] apply(Event event) {
        return JaxbUtils.marshal(event).getBytes(StandardCharsets.UTF_8);
    }
}
