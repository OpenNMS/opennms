package org.opennms.features.activemq.eventreceiver;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 */
public class JaxbUtilsUnmarshalProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(JaxbUtilsUnmarshalProcessor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {
		final String object = exchange.getIn().getBody(String.class);
		exchange.getIn().setBody(JaxbUtils.unmarshal(Event.class, object), Event.class);
	}
}
