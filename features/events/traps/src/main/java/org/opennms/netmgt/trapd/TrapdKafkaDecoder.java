package org.opennms.netmgt.trapd;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.netmgt.snmp.TrapNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.serializer.Decoder;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;

public class TrapdKafkaDecoder implements Decoder<Object>{
	
	public static final Logger LOG = LoggerFactory.getLogger(TrapdKafkaDecoder.class);

//	public TrapdKafkaDecoder(VerifiableProperties verifiableProperties){
//		
//	}
	
    @Override
    public Object fromBytes(byte[] bytes) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
        	System.out.println("######################################################## bytes is : "+bytes);
            return objectMapper.readValue(bytes, TrapNotification.class);
        } catch (IOException e) {
        	System.out.println("e is : "+e);
        	e.printStackTrace();
        	LOG.error(String.format("Json processing failed for object: %s", bytes.toString()), e);
        }
        return null;
    }

}