package org.opennms.features.activemq.eventforwarder.internal;

import org.apache.camel.InOnly;
import org.apache.camel.Produce;
import org.opennms.core.camel.DefaultDispatcher;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

@InOnly
public class DefaultEventForwarder extends DefaultDispatcher implements EventForwarder {

	@Produce(property="endpointUri")
	EventForwarder m_proxy;

	public DefaultEventForwarder(final String endpointUri) {
		super(endpointUri);
	}

	/**
	 * Send the incoming {@link Event} message into the Camel route
	 * specified by the {@link #m_endpointUri} property.
	 */
	@Override
	public void sendNow(Event event) {
		m_proxy.sendNow(event);
	}

	/**
	 * Send the incoming {@link Log} message into the Camel route
	 * specified by the {@link #m_endpointUri} property.
	 */
	@Override
	public void sendNow(Log eventLog) {
		for (Event event : eventLog.getEvents().getEventCollection()) {
			m_proxy.sendNow(event);
		}
	}
}
