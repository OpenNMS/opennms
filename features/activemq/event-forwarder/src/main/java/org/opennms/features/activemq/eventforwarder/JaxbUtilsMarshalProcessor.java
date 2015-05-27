package org.opennms.features.activemq.eventforwarder;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 */
public class JaxbUtilsMarshalProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(JaxbUtilsMarshalProcessor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Object object = exchange.getIn().getBody(Event.class);
		exchange.getIn().setBody(JaxbUtils.marshal(object), String.class);
	}
}
