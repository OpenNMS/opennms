package org.opennms.features.elasticsearch.eventforwarder.internal;

import org.apache.camel.Produce;
import org.opennms.core.camel.DefaultDispatcher;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEventForwarder extends DefaultDispatcher implements EventForwarder {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultEventForwarder.class);

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
		if(LOG.isTraceEnabled()) {
			LOG.trace("forwarding event " + event);
		}
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
