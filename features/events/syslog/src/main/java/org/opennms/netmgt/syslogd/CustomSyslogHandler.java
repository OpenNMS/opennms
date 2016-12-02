package org.opennms.netmgt.syslogd;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;

public class CustomSyslogHandler implements SyslogConnectionHandler{
	
    @EndpointInject(uri = "seda:sendMessage?size=1000000&blockWhenFull=true", context = "syslogdHandlerKafkaContext")
    private ProducerTemplate template;

    @EndpointInject(uri = "seda:sendMessage?size=1000000&blockWhenFull=true", context = "syslogdHandlerKafkaContext")
    private Endpoint endpoint;
    

	@Override
	public void handleSyslogConnection(SyslogConnection message) {
		template.sendBody(endpoint,message);
		
	}

}
