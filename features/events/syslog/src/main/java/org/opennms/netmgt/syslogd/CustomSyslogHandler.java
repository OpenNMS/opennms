package org.opennms.netmgt.syslogd;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.opennms.core.xml.JaxbUtils;

public class CustomSyslogHandler implements SyslogDTOHandler{
	
    @EndpointInject(uri = "seda:handleMessage", context = "syslogdHandlerKafkaContext")
    private ProducerTemplate template;

    @EndpointInject(uri = "seda:handleMessage", context = "syslogdHandlerKafkaContext")
    private Endpoint endpoint;
    

	@Override
	public void handleSyslogDTO(SyslogDTO message) {
		template.sendBody(endpoint,JaxbUtils.marshal(message));
		
	}

}
