package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.ietf.jgss.Oid;
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
	           	           	            
				LOG.debug("varBindValue is : "+varBindValue);
				oid = new OID(varBindValue);
				snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));   
				snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
				snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
						new IpAddress(trapAddress)));

				int type = Integer.parseInt(field.getValue().findValue("type").asText());
				String value=field.getValue().findValue("value").toString();
				if(type==6)
				{
					if(value.matches("\\[[0-9,]*\\]"))
					{
						int[] arr = Arrays.stream(value.substring(1, value.length()-1).split(","))
								.map(String::trim).mapToInt(Integer::parseInt).toArray();
						OID valueOid=new OID(arr);
						value=valueOid.toString();
					}
				}
				
			        switch (type) {
		            case 2:  snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Integer32(field.getValue().findValue("value").asInt())));
                    		 break;
		            case 4:  snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(new String(Base64.getDecoder().decode(field.getValue().findValue("value").asText())))));
                    		 break;
		            case 5:  snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null()));
                    		 break;
		            case 6:  snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(value)));
                    		 break;
		            case 64: snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new IpAddress(field.getValue().findValue("inetAddress").asText())));
                    		 break;
		            case 67: snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new TimeTicks(field.getValue().findValue("value").asInt())));
		                     break;
		            case 128:snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null(128)));
		                     break;
		            case 129:snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null(129)));
		                     break;
		            case 130:snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null(130)));
		                     break;
		            default: snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(field.getValue().findValue("value").asText())));
		                     break;
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
    	String agentAddress=result.findValue("agentAddress").asText();

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
		snmp4JV1cTrapPdu.setAgentAddress(new IpAddress(trapAddress));
		
		OID enterpriseId=new OID(trapIdentityRoot.findValue("enterpriseId").asText());
		snmp4JV1cTrapPdu.setGenericTrap(generic);
		snmp4JV1cTrapPdu.setSpecificTrap(specific);
		snmp4JV1cTrapPdu.setEnterprise(enterpriseId);
		
		
		JsonNode varBindRoot = trapProcessorRoot.findValue("varBinds");
		LOG.debug("varBindRoot is : "+varBindRoot);
		
		String varBindValue = "";
		OID oid = null;
	    Iterator<Map.Entry<String,JsonNode>> fieldsIterator = varBindRoot.getFields();
	    	while (fieldsIterator.hasNext()) {

	           Map.Entry<String,JsonNode> field = fieldsIterator.next();
	           LOG.debug("Key: " + field.getKey() + "\tValue:" + field.getValue());
	           varBindValue = field.getKey();
	           	            
				LOG.debug("varBindValue is : "+varBindValue);
				oid = new OID(varBindValue);
				snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));   
				snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
				snmp4JV1cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
						new IpAddress(trapAddress)));
				
				int type = Integer.parseInt(field.getValue().findValue("type").asText());
				
				String value=field.getValue().findValue("value").toString();
				if(type==6)
				{
					if(value.matches("\\[[0-9,]*\\]"))
					{
						int[] arr = Arrays.stream(value.substring(1, value.length()-1).split(","))
								.map(String::trim).mapToInt(Integer::parseInt).toArray();
						OID valueOid=new OID(arr);
						value=valueOid.toString();
					}
				}
				
		        switch (type) {
	            case 2:  snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Integer32(field.getValue().findValue("value").asInt())));
                		 break;
	            case 4:  snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(new String(Base64.getDecoder().decode(field.getValue().findValue("value").asText())))));
                		 break;
	            case 5:  snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null()));
                		 break;
	            case 6:  snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(value)));
                		 break;
	            case 64: snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new IpAddress(field.getValue().findValue("inetAddress").asText())));
                		 break;
	            case 67: snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new TimeTicks(field.getValue().findValue("value").asInt())));
	                     break;
	            case 128:snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null(128)));
	                     break;
	            case 129:snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null(129)));
	                     break;
	            case 130:snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null(130)));
	                     break;
	            default: snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(field.getValue().findValue("value").asText())));
	                     break;
	            }
	    	}

		snmp4JV1cTrapPdu.setType(PDU.V1TRAP);
		
		Snmp4JTrapNotifier.Snmp4JV1TrapInformation snmp4JV1cTrap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
		InetAddressUtils.getInetAddress(agentAddress), trapProcessorRoot.findValue("community").asText(),
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