package org.opennms.netmgt.syslogd;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;

public class CustomSyslogHandler implements SyslogDTOHandler{
	
    @EndpointInject(uri = "seda:handleMessage?size=2000000&blockWhenFull=true", context = "syslogdHandlerKafkaContext")
    private ProducerTemplate template;

    @EndpointInject(uri = "seda:handleMessage?size=2000000&blockWhenFull=true", context = "syslogdHandlerKafkaContext")
    private Endpoint endpoint;
    

	@Override
	public void handleSyslogDTO(SyslogDTO message) {
		template.sendBody(endpoint,message);
		
	}

}
