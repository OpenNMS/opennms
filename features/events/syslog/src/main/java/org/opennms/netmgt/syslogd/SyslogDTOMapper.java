package org.opennms.netmgt.syslogd;

import org.opennms.core.camel.MinionDTO;
import org.opennms.core.utils.InetAddressUtils;

public class SyslogDTOMapper {

	public SyslogDTO object2dto(Object obj){
		
		SyslogConnection syslog = (SyslogConnection)obj;
		
		SyslogDTO syslogDTO = new SyslogDTO();
		syslogDTO.setLocation(syslog.getLocation());
		syslogDTO.setSourceAddress(syslog.getSourceAddress());
		syslogDTO.setSourceport(syslog.getPort());
		syslogDTO.setSystemId(syslog.getSystemId());
		
		syslogDTO.setBody(syslog.getBytes());
		
		return syslogDTO;
	}
	
	public SyslogConnection dto2object(Object obj){
				
		SyslogDTO syslogDTO = (SyslogDTO)obj;
		
		SyslogConnection syslog = new SyslogConnection();

		syslog.setLocation(syslogDTO.getFromMap(MinionDTO.LOCATION));
		syslog.setSourceAddress(InetAddressUtils.getInetAddress(syslogDTO.getFromMap(MinionDTO.SOURCE_ADDRESS)));
		syslog.setPort(Integer.parseInt(syslogDTO.getFromMap(MinionDTO.SOURCE_PORT)));
		syslog.setSystemId(syslogDTO.getFromMap(MinionDTO.SYSTEM_ID));
		
		syslog.setBytes(syslogDTO.getBody());
		
		return syslog;
	}
}