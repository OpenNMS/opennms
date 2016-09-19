package org.opennms.netmgt.syslogd;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogObjectToDTOProcessor implements Processor{
	public static final Logger LOG = LoggerFactory.getLogger(SyslogObjectToDTOProcessor.class);

	private final Class<?> m_class;

	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public SyslogObjectToDTOProcessor(Class clazz) {
		m_class = clazz;
	}

	public SyslogObjectToDTOProcessor(String className) throws ClassNotFoundException {
		m_class = Class.forName(className);
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Object object = exchange.getIn().getBody(m_class);
		boolean syslogRawMessageFlag = (boolean)exchange.getIn().getHeader("syslogRawMessageFlag");
		exchange.getIn().setBody(object2dto(object, syslogRawMessageFlag), SyslogDTO.class);
	}
	
	public SyslogDTO object2dto(Object obj, boolean syslogRawMessageFlag) {

		SyslogConnection syslog = (SyslogConnection) obj;

		SyslogDTO syslogDTO = new SyslogDTO();
		syslogDTO.setLocation(syslog.getLocation());
		syslogDTO.setSourceAddress(syslog.getSourceAddress());
		syslogDTO.setSourceport(syslog.getPort());
		syslogDTO.setSystemId(syslog.getSystemId());
		
		if(syslogRawMessageFlag){
			syslogDTO.setBody(syslog.getBytes());
		}

		return syslogDTO;
	}
}
