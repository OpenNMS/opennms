package org.opennms.netmgt.trapd;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.snmp.TrapNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Camel {@link Processor} uses {@link JaxbUtils} to unmarshal classes
 * from XML String representations.
 * 
 * @author Seth
 */
public class TrapdKafkaDeSerializer implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(TrapdKafkaDeSerializer.class);

	private final Class<?> m_class = TrapNotification.class;

	@Override
	public void process(final Exchange exchange) throws Exception {
		byte[] bytes = exchange.getIn().getBody(byte[].class);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
		TrapNotification trapNotification = (TrapNotification)in.readObject();
		exchange.getIn().setBody(trapNotification, m_class);
	}
}

