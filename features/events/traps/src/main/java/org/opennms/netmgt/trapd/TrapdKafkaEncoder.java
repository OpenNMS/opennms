package org.opennms.netmgt.trapd;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
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
        	Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = (Snmp4JTrapNotifier.Snmp4JV2TrapInformation)arg0; 
        	System.out.println(" ######################################################## v2Trap is : "+v2Trap);

        	System.out.println(" ######################################################## v2Trap.toString() : "+v2Trap.toString());

        	
        	return objectMapper.writeValueAsBytes(v2Trap);
            //return objectMapper.writeValueAsString(arg0).getBytes();
//        } catch (JsonProcessingException e) {
//        	LOG.error(String.format("Json processing failed for object: %s", arg0.getClass().getName()), e);
        } catch (Exception e) {
        	System.out.println("########################################################  Exception : "+e);
			e.printStackTrace();
		}
        return "".getBytes();
	}

}
