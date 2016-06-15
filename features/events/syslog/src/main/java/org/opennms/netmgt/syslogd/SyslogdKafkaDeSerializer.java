package org.opennms.netmgt.syslogd;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Camel {@link Processor} uses {@link JaxbUtils} to unmarshal classes
 * from XML String representations.
 * 
 * @author Seth
 */
public class SyslogdKafkaDeSerializer implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(SyslogdKafkaDeSerializer.class);

	private final Class<?> m_class;

	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public SyslogdKafkaDeSerializer(Class clazz) {
		m_class = clazz;
	}

	public SyslogdKafkaDeSerializer(String className) throws ClassNotFoundException {
		m_class = Class.forName(className);
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		byte[] bytes = exchange.getIn().getBody(byte[].class);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
		SyslogConnection syslogConnection = (SyslogConnection)in.readObject();
		exchange.getIn().setBody(syslogConnection, m_class);
	}
}

