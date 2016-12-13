package org.opennms.netmgt.syslogd;

import java.util.HashMap;
import java.util.Map;

import kafka.Kafka;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.kafka.KafkaConstants;
import org.opennms.core.camel.RandomPartitionKeyGenerator;
import org.opennms.core.xml.JaxbUtils;

public class CustomSyslogHandler implements SyslogDTOHandler{
	
    @EndpointInject(uri = "seda:handleMessage", context = "syslogdHandlerKafkaContext")
    private ProducerTemplate template;

    @EndpointInject(uri = "seda:handleMessage", context = "syslogdHandlerKafkaContext")
    private Endpoint endpoint;
    
    private RandomPartitionKeyGenerator randomPartitionKeyGenerator;
    

	@Override
	public void handleSyslogDTO(SyslogDTO message) {
		template.sendBody(endpoint,JaxbUtils.marshal(message));
		
	}


	public RandomPartitionKeyGenerator getRandomPartitionKeyGenerator() {
		return randomPartitionKeyGenerator;
	}


	public void setRandomPartitionKeyGenerator(
			RandomPartitionKeyGenerator randomPartitionKeyGenerator) {
		this.randomPartitionKeyGenerator = randomPartitionKeyGenerator;
	}

}
