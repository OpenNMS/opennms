package org.opennms.features.elasticsearch.eventforwarder.internal;

import org.apache.camel.Produce;
import org.opennms.core.camel.DefaultDispatcher;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAlarmForwarder extends DefaultDispatcher implements CamelAlarmForwarder {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultAlarmForwarder.class);

	@Produce(property="endpointUri")
	CamelAlarmForwarder m_proxy;

	public DefaultAlarmForwarder(final String endpointUri) {
		super(endpointUri);
	}

	@Override
	public void sendNow(NorthboundAlarm alarm) {
		if(LOG.isTraceEnabled()) {
			LOG.trace("forwarding alarm " + alarm);
		}
		m_proxy.sendNow(alarm);
	}
}
