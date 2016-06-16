package org.opennms.netmgt.trapd;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import org.opennms.netmgt.snmp.TrapNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Deepak
 */
public class TrapdKafkaDeSerializer {
	public static final Logger LOG = LoggerFactory.getLogger(TrapdKafkaDeSerializer.class);

	private final Class<?> m_class;

	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public TrapdKafkaDeSerializer(Class clazz) {
		m_class = clazz;
	}

	public TrapdKafkaDeSerializer(String className) throws ClassNotFoundException {
		m_class = Class.forName(className);
	}

    public TrapNotification process(final  byte[] bytes) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return (TrapNotification)in.readObject();
    }
}

