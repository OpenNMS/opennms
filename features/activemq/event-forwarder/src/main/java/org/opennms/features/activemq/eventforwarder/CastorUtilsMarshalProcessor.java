package org.opennms.features.activemq.eventforwarder;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 */
public class CastorUtilsMarshalProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(CastorUtilsMarshalProcessor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Object object = exchange.getIn().getBody(Event.class);
		exchange.getIn().setBody(CastorUtils.marshal(object), String.class);
	}
}
