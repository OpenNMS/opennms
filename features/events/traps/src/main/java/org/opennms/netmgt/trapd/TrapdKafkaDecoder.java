package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.net.InetAddress;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;

import kafka.serializer.Decoder;

public class TrapdKafkaDecoder implements Decoder<Object>{
	
	public static final Logger LOG = LoggerFactory.getLogger(TrapdKafkaDecoder.class);

//	public TrapdKafkaDecoder(VerifiableProperties verifiableProperties){
//		
//	}
	
	
    @Override
    public Object fromBytes(byte[] bytes) {
        ObjectMapper objectMapper = new ObjectMapper();
        TrapNotification snmp4JV2cTrap = null;
        try {
        	//System.out.println("######################################################## bytes is : "+bytes);
        	//Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = objectMapper.readValue(bytes, Snmp4JTrapNotifier.Snmp4JV2TrapInformation.class);
        	//System.out.println("####################################################### v2Trap is : "+v2Trap);
        	//System.out.println("####################################################### v2Trap.toString is : "+v2Trap.toString());
        	//objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);

        	//Object result = objectMapper.readValue(bytes, Snmp4JTrapNotifier.Snmp4JV2TrapInformation.class);

        	
        	
        	JsonNode result = objectMapper.readTree(bytes);
        	String version = result.findValue("version").toString();

        	System.out.println("result is : "+result); 
        	System.out.println("version is : "+version); 

        	String agentAddress = result.findValue("agentAddress").toString();
        	String community = result.findValue("community").toString();
        	System.out.println("agentAddress : "+agentAddress);
        	System.out.println("community : "+community);

        	JsonNode trapProcessor1 = result.findValue("trapProcessor");
        	System.out.println("trapProcessor1 : "+trapProcessor1);
        	
        	TrapProcessor trapProcessor = new BasicTrapProcessor();
			trapProcessor.setCommunity(trapProcessor1.get("community").toString());
			trapProcessor.setTimeStamp(Long.parseLong(trapProcessor1.get("timeStamp").toString()));
			trapProcessor.setVersion(trapProcessor1.get("version").toString());

			
			PDU snmp4JV2cTrapPdu = new PDU();
			OID oid = new OID(".1.3.6.1.2.1.1.3.0");
			snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
			snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
			snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
					new IpAddress("127.0.0.1")));

			snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Major")));
			snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);
			
			snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
			InetAddressUtils.ONE_TWENTY_SEVEN, community,
			snmp4JV2cTrapPdu, trapProcessor);
			
        	
//        	if(version.equalsIgnoreCase("v2") || version.equalsIgnoreCase("v3")){
//        		return parseV2Information(result);
//        	}else if(version.equalsIgnoreCase("v1")){
//        		return parseV1Information(result);
//        	}
			System.out.println("snmp4JV2cTrap is : "+snmp4JV2cTrap);
            
        } catch (IOException e) {
        	//System.out.println("e is : "+e);
        	//e.printStackTrace();
        	LOG.error(String.format("Json processing failed for object: %s", bytes.toString()), e);
        }
        return snmp4JV2cTrap;
        	
    }
    
//    public TrapNotification parseV2Information(JsonNode jsonRoot){
//    	System.out.println("Start - parseV2Information()");
//    	TrapNotification trapV2Information = null;
//    	
//    	String agentAddress = jsonRoot.findValue("agentAddress").toString();
//    	String community = jsonRoot.findValue("community").toString();
//    	
//    	JsonNode trapProcessor = jsonRoot.findValue("trapProcessor");
//    	System.out.println("trapProcessor : "+trapProcessor);
//    	
//		PDU snmp4JV2cTrapPdu = new PDU();
//		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
//		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
//		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
//		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
//				new IpAddress("127.0.0.1")));
//
//		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Major")));
//		snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);
//		
//		TrapNotification snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
//		InetAddressUtils.ONE_TWENTY_SEVEN, "public",
//		snmp4JV2cTrapPdu, new BasicTrapProcessor());
//				
//		
//    	
//    	return trapV2Information;
//    }
//    
//    public TrapNotification parseV1Information(JsonNode jsonRoot){
//    	System.out.println("Start - parseV1Information()");
//    	TrapNotification trapV1Information = null;
//    	
//    	return trapV1Information;
//    }

}