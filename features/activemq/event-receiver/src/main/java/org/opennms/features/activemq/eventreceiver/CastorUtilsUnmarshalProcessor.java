package org.opennms.features.activemq.eventreceiver;

import java.io.ByteArrayInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 */
public class CastorUtilsUnmarshalProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(CastorUtilsUnmarshalProcessor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {
		final String object = exchange.getIn().getBody(String.class);
		exchange.getIn().setBody(CastorUtils.unmarshal(Event.class, new ByteArrayInputStream(object.getBytes("UTF-8"))), Event.class);
	}
}
