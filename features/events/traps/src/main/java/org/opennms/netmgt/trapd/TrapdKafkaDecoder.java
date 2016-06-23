package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
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
import org.snmp4j.PDUv1;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;

import kafka.serializer.Decoder;

public class TrapdKafkaDecoder implements Decoder<Object>{
	
	public static final Logger LOG = LoggerFactory.getLogger(TrapdKafkaDecoder.class);
	
    @Override
    public Object fromBytes(byte[] bytes) {
        ObjectMapper objectMapper = new ObjectMapper();
        TrapNotification snmp4JV2cTrap = null;
        try {        	
        	
        	JsonNode result = objectMapper.readTree(bytes);
        	System.out.println("result is : "+result);
        	
        	String version = result.findValue("version").asText();


			
        	
        	if(version.equalsIgnoreCase("v2") || version.equalsIgnoreCase("v3")){
        		snmp4JV2cTrap = parseV2Information(result);
        	}else if(version.equalsIgnoreCase("v1")){
        		//snmp4JV2cTrap =  parseV1Information(result);
        	}
            
        } catch (IOException e) {
        	//System.out.println("e is : "+e);
        	//e.printStackTrace();
        	LOG.error(String.format("Json processing failed for object: %s", bytes.toString()), e);
        }
        return snmp4JV2cTrap;
        	
    }
    
    public TrapInformation parseV2Information(JsonNode result){
    	
    	String trapAddress = result.findValue("trapAddress").asText();

    	JsonNode trapProcessorRoot = result.findValue("trapProcessor");
    	        	
    	TrapProcessor trapProcessor = new BasicTrapProcessor();
		trapProcessor.setAgentAddress(InetAddressUtils.getInetAddress(trapProcessorRoot.findValue("agentAddress").asText()));
		trapProcessor.setCommunity(trapProcessorRoot.findValue("community").asText());
		trapProcessor.setTimeStamp(trapProcessorRoot.findValue("timeStamp").asLong());
		trapProcessor.setVersion(trapProcessorRoot.findValue("version").asText());
		trapProcessor.setTrapAddress(InetAddressUtils.getInetAddress(trapProcessorRoot.findValue("trapAddress").asText()));

		// Setting TrapIdentity {trapIdentityRoot is : {"generic":6,"specific":0,"enterpriseId":".1.3.6.1.2.1.1.3"}
		JsonNode trapIdentityRoot = trapProcessorRoot.findValue("trapIdentity");
		int[] ids = convertStringToInts(trapIdentityRoot.findValue("enterpriseId").asText()); 
		SnmpObjId entId = new SnmpObjId(ids, false);
		int generic = trapIdentityRoot.findValue("generic").asInt();
		int specific = trapIdentityRoot.findValue("specific").asInt();
		TrapIdentity trapIdentity = new TrapIdentity(entId, generic, specific);
		trapProcessor.setTrapIdentity(trapIdentity);
		
		// Setting VarBind 
		PDU snmp4JV2cTrapPdu = new PDU();
		
		JsonNode varBindRoot = trapProcessorRoot.findValue("varBinds");
		System.out.println("varBindRoot is : "+varBindRoot);
		
		String varBindValue = "";
	    Iterator<Map.Entry<String,JsonNode>> fieldsIterator = varBindRoot.getFields();
	    	while (fieldsIterator.hasNext()) {

	           Map.Entry<String,JsonNode> field = fieldsIterator.next();
	           System.out.println("Key: " + field.getKey() + "\tValue:" + field.getValue());
	           varBindValue = field.getKey();
	    	}
		
		String bytesValue = new String(Base64.getDecoder().decode(varBindRoot.findValue("value").asText()));
		System.out.println("bytesValue is : "+bytesValue);
		System.out.println("varBindValue is : "+varBindValue);
		OID oid = new OID(varBindValue);
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));   
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
				new IpAddress(trapAddress)));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(bytesValue)));
		
		System.out.println("type is : "+varBindRoot.findValue("type"));
		
		//snmp4JV2cTrapPdu.setType(varBindRoot.findValue("type").asInt()); // what should be the type? 
		snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);
		
		Snmp4JTrapNotifier.Snmp4JV2TrapInformation snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
		InetAddressUtils.getInetAddress(trapAddress), trapProcessorRoot.findValue("community").asText(),
		snmp4JV2cTrapPdu, trapProcessor);
		
		return snmp4JV2cTrap;
    }
    
    public TrapInformation parseV1Information(JsonNode result){
    	
    	String trapAddress = result.findValue("trapAddress").asText();

    	JsonNode trapProcessorRoot = result.findValue("trapProcessor");
    	        	
    	TrapProcessor trapProcessor = new BasicTrapProcessor();
		trapProcessor.setAgentAddress(InetAddressUtils.getInetAddress(trapProcessorRoot.findValue("agentAddress").asText()));
		trapProcessor.setCommunity(trapProcessorRoot.findValue("community").asText());
		trapProcessor.setTimeStamp(trapProcessorRoot.findValue("timeStamp").asLong());
		trapProcessor.setVersion(trapProcessorRoot.findValue("version").asText());
		trapProcessor.setTrapAddress(InetAddressUtils.getInetAddress(trapProcessorRoot.findValue("trapAddress").asText()));

		// Setting TrapIdentity {trapIdentityRoot is : {"generic":6,"specific":0,"enterpriseId":".1.3.6.1.2.1.1.3"}
		JsonNode trapIdentityRoot = trapProcessorRoot.findValue("trapIdentity");
		int[] ids = convertStringToInts(trapIdentityRoot.findValue("enterpriseId").asText()); 
		SnmpObjId entId = new SnmpObjId(ids, false);
		int generic = trapIdentityRoot.findValue("generic").asInt();
		int specific = trapIdentityRoot.findValue("specific").asInt();
		TrapIdentity trapIdentity = new TrapIdentity(entId, generic, specific);
		trapProcessor.setTrapIdentity(trapIdentity);
		
		// Setting VarBind 
		PDUv1 snmp4JV1cTrapPdu = new PDUv1();
		
		JsonNode varBindRoot = trapProcessorRoot.findValue("varBinds");
		System.out.println(varBindRoot);
		
		String bytesValue = new String(Base64.getDecoder().decode(varBindRoot.findValue("value").asText()));
		System.out.println("bytesValue is : "+bytesValue);
		
		OID oid = new OID(trapIdentityRoot.findValue("enterpriseId").asText());
		snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));   
		snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
				new IpAddress(trapAddress)));

		snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(bytesValue))); // we should read this from object
		//snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid2), new OctetString(value2))); // we should read this from object
		
		System.out.println("type is : "+varBindRoot.findValue("type"));
		
		//snmp4JV2cTrapPdu.setType(varBindRoot.findValue("type").asInt()); // what should be the type? 
		snmp4JV1cTrapPdu.setType(PDU.NOTIFICATION);
		
		
		Snmp4JTrapNotifier.Snmp4JV1TrapInformation snmp4JV1cTrap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
		InetAddressUtils.getInetAddress(trapAddress), trapProcessorRoot.findValue("community").asText(),
		snmp4JV1cTrapPdu, trapProcessor);
		
		return snmp4JV1cTrap;
    }
    
    private static int[] convertStringToInts(String oid) {
    	oid = oid.trim();
        if (oid.startsWith(".")) {
            oid = oid.substring(1);
        }
        
        final StringTokenizer tokenizer = new StringTokenizer(oid, ".");
        int[] ids = new int[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            try {
                String tok = tokenizer.nextToken();
                long value = Long.parseLong(tok);
                ids[index] = (int)value;
                if (value < 0)
                    throw new IllegalArgumentException("String "+oid+" could not be converted to a SnmpObjId. It has a negative for subId "+index);
                index++;
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("String "+oid+" could not be converted to a SnmpObjId at subId "+index);
            }
        }
        return ids;
    }
}