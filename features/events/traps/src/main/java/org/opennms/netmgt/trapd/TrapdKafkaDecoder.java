package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.SnmpObjId;
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
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
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
        TrapNotification snmp4JTrap = null;
        try {        	
        	
        	JsonNode result = objectMapper.readTree(bytes);
        	LOG.debug("result is : "+result);
        	
        	String version = result.findValue("version").asText();
        	
        	if(version.equalsIgnoreCase("v2") || version.equalsIgnoreCase("v3")){
        		snmp4JTrap = parseV2Information(result);
        	}else if(version.equalsIgnoreCase("v1")){
        		snmp4JTrap =  parseV1Information(result);
        	}
            
        } catch (IOException e) {
        	LOG.error(String.format("Json processing failed for object: %s", bytes.toString()), e);
        } catch (Exception e) {
        	LOG.error(String.format("Json processing failed for object: %s", bytes.toString()), e);
        }
        return snmp4JTrap;
        	
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
		LOG.debug("varBindRoot is : "+varBindRoot);
		
		String varBindValue = "";
		OID oid = null;
	    Iterator<Map.Entry<String,JsonNode>> fieldsIterator = varBindRoot.getFields();
	    	while (fieldsIterator.hasNext()) {

	           Map.Entry<String,JsonNode> field = fieldsIterator.next();
	           LOG.debug("Key: " + field.getKey() + "\tValue:" + field.getValue());
	           varBindValue = field.getKey();
	           
	           String type = field.getValue().findValue("type").asText();
	           LOG.debug("type is : "+type); 
	           	            
				LOG.debug("varBindValue is : "+varBindValue);
				oid = new OID(varBindValue);
				snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));   
				snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
				snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
						new IpAddress(trapAddress)));
				
		           if(type!=null && type.equalsIgnoreCase("67")){
		        	   LOG.debug("{67}value is : "+field.getValue().findValue("value").asInt());
		        	   snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new TimeTicks(field.getValue().findValue("value").asInt())));
		           }else if(type!=null && type.equalsIgnoreCase("6")){
		        	   LOG.debug("{6}value is : "+field.getValue().findValue("value").toString());
		        	   snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(field.getValue().findValue("value").toString())));
		           }else if(type!=null && type.equalsIgnoreCase("64")){
		        	   LOG.debug("{64}value is : "+field.getValue().findValue("inetAddress").asText());
		        	   snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new IpAddress(field.getValue().findValue("inetAddress").asText())));
		           }else if(type!=null && type.equalsIgnoreCase("4")){
		        	   LOG.debug("{4}value is : "+new String(Base64.getDecoder().decode(field.getValue().findValue("value").asText())));
		        	   snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(new String(Base64.getDecoder().decode(field.getValue().findValue("value").asText())))));
		           }else if(type!=null && type.equalsIgnoreCase("2")){
		        	   LOG.debug("{2}value is : "+field.getValue().findValue("value").asInt());
		        	   snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Integer32(field.getValue().findValue("value").asInt())));
		           }else if(type!=null && type.equalsIgnoreCase("5")){
		        	   LOG.debug("{5}value is : "+field.getValue().findValue("value"));
		        	   snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null()));
		           }else if(type!=null && (type.equalsIgnoreCase("128") || type.equalsIgnoreCase("129") || type.equalsIgnoreCase("130"))){
		        	   LOG.debug("{128 or 129 or 130 }value is : "+field.getValue().findValue("value"));
		        	   snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null(Integer.parseInt(type))));
		           }else{
		        	   LOG.debug("{Any}value is : "+field.getValue().findValue("value").asText());
		        	   snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(field.getValue().findValue("value").asText())));
		           }
	    	}

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
		LOG.debug("varBindRoot is : "+varBindRoot);
		
		String varBindValue = "";
		OID oid = null;
	    Iterator<Map.Entry<String,JsonNode>> fieldsIterator = varBindRoot.getFields();
	    	while (fieldsIterator.hasNext()) {

	           Map.Entry<String,JsonNode> field = fieldsIterator.next();
	           LOG.debug("Key: " + field.getKey() + "\tValue:" + field.getValue());
	           varBindValue = field.getKey();
	           
	           String type = field.getValue().findValue("type").asText();
	           LOG.debug("type is : "+type); 
	           	            
				LOG.debug("varBindValue is : "+varBindValue);
				oid = new OID(varBindValue);
				snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));   
				snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
				snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
						new IpAddress(trapAddress)));
				
		           if(type!=null && type.equalsIgnoreCase("67")){
		        	   LOG.debug("{67}value is : "+field.getValue().findValue("value").asInt());
		        	   snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new TimeTicks(field.getValue().findValue("value").asInt())));
		           }else if(type!=null && type.equalsIgnoreCase("6")){
		        	   LOG.debug("{6}value is : "+field.getValue().findValue("value").toString());
		        	   snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(field.getValue().findValue("value").toString())));
		           }else if(type!=null && type.equalsIgnoreCase("64")){
		        	   LOG.debug("{64}value is : "+field.getValue().findValue("inetAddress").asText());
		        	   snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new IpAddress(field.getValue().findValue("inetAddress").asText())));
		           }else if(type!=null && type.equalsIgnoreCase("4")){
		        	   LOG.debug("{4}value is : "+new String(Base64.getDecoder().decode(field.getValue().findValue("value").asText())));
		        	   snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(new String(Base64.getDecoder().decode(field.getValue().findValue("value").asText())))));
		           }else if(type!=null && type.equalsIgnoreCase("2")){
		        	   LOG.debug("{2}value is : "+field.getValue().findValue("value").asInt());
		        	   snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Integer32(field.getValue().findValue("value").asInt())));
		           }else if(type!=null && type.equalsIgnoreCase("5")){
		        	   LOG.debug("{5}value is : "+field.getValue().findValue("value"));
		        	   snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null()));
		           }else if(type!=null && (type.equalsIgnoreCase("128") || type.equalsIgnoreCase("129") || type.equalsIgnoreCase("130"))){
		        	   LOG.debug("{128 or 129 or 130 }value is : "+field.getValue().findValue("value"));
		        	   snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null(Integer.parseInt(type))));
		           }else{
		        	   LOG.debug("{Any}value is : "+field.getValue().findValue("value").asText());
		        	   snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(field.getValue().findValue("value").asText())));
		           }
	    	}

		snmp4JV1cTrapPdu.setType(PDU.V1TRAP);
		
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