package org.opennms.netmgt.trapd;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.serializer.Encoder;

public class TrapdKafkaEncoder implements Encoder<Object>{
	
	public static final Logger LOG = LoggerFactory.getLogger(TrapdKafkaEncoder.class);

	@Override
	public byte[] toBytes(Object arg0) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
        	System.out.println("######################################################## (arg0).getBytes() is : "+arg0);
        	System.out.println(" ######################################################## objectMapper.writeValueAsString(arg0).getBytes() is : "+objectMapper.writeValueAsString(arg0).getBytes());

            return objectMapper.writeValueAsString(arg0).getBytes();
        } catch (JsonProcessingException e) {
        	LOG.error(String.format("Json processing failed for object: %s", arg0.getClass().getName()), e);
        } catch (IOException e) {
			e.printStackTrace();
		}
        return "".getBytes();
	}

}
