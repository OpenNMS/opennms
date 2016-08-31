package org.opennms.netmgt.syslogd;

import org.opennms.core.camel.MinionDTO;

public class MinionDTOMapper {

	public SyslogDTO mapper(Object obj){
		
		SyslogConnection syslog = (SyslogConnection)obj;
		
		SyslogDTO syslogDTO = new SyslogDTO();
		syslogDTO.setLocation(syslog.getLocation());
		syslogDTO.setSourceAddress(syslog.getSourceAddress());
		syslogDTO.setSourceport(syslog.getPort());
		syslogDTO.setSystemId(syslog.getSystemId());
		
		syslogDTO.setBody(syslog.getBytes());
		
		return syslogDTO;
	}
}