package org.opennms.netmgt.syslogd;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.camel.MinionDTO;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogDTOToObjectProcessor implements Processor{
	public static final Logger LOG = LoggerFactory.getLogger(SyslogObjectToDTOProcessor.class);

	private final Class<?> m_class;

	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public SyslogDTOToObjectProcessor(Class clazz) {
		m_class = clazz;
	}

	public SyslogDTOToObjectProcessor(String className) throws ClassNotFoundException {
		m_class = Class.forName(className);
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Object object = exchange.getIn().getBody(m_class);
		exchange.getIn().setBody(dto2object(object), SyslogConnection.class);
	}
	
	public SyslogConnection dto2object(Object obj) {

		SyslogDTO syslogDTO = (SyslogDTO) obj;

		SyslogConnection syslog = new SyslogConnection();

		syslog.setLocation(syslogDTO.getFromMap(MinionDTO.LOCATION));
		syslog.setSourceAddress(InetAddressUtils.getInetAddress(syslogDTO
				.getFromMap(MinionDTO.SOURCE_ADDRESS)));
		syslog.setPort(Integer.parseInt(syslogDTO
				.getFromMap(MinionDTO.SOURCE_PORT)));
		syslog.setSystemId(syslogDTO.getFromMap(MinionDTO.SYSTEM_ID));

		syslog.setBytes(syslogDTO.getBody());

		return syslog;
	}
}
