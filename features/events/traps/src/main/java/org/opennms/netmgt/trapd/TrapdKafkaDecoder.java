package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Base64;
import java.util.StringTokenizer;

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
        	System.out.println("result is : "+result); 

        	String version = result.findValue("version").asText();
        	System.out.println("version is : "+version); 

        	String trapAddress = result.findValue("trapAddress").asText();
        	System.out.println("trapAddress : "+trapAddress);

        	JsonNode trapProcessorRoot = result.findValue("trapProcessor");
        	System.out.println("trapProcessorRoot : "+trapProcessorRoot);
        	        	
        	TrapProcessor trapProcessor = new BasicTrapProcessor();
			trapProcessor.setAgentAddress(InetAddressUtils.getInetAddress(trapProcessorRoot.findValue("agentAddress").asText()));
			trapProcessor.setCommunity(trapProcessorRoot.findValue("community").asText());
			trapProcessor.setTimeStamp(trapProcessorRoot.findValue("timeStamp").asLong());
			trapProcessor.setVersion(trapProcessorRoot.findValue("version").asText());
			trapProcessor.setTrapAddress(InetAddressUtils.getInetAddress(trapProcessorRoot.findValue("trapAddress").asText()));

			// Setting TrapIdentity {trapIdentityRoot is : {"generic":6,"specific":0,"enterpriseId":".1.3.6.1.2.1.1.3"}
			JsonNode trapIdentityRoot = trapProcessorRoot.findValue("trapIdentity");
			System.out.println("trapIdentityRoot is : "+trapIdentityRoot);
			int[] ids = convertStringToInts(trapIdentityRoot.findValue("enterpriseId").asText()); 
			SnmpObjId entId = new SnmpObjId(ids, false);
			System.out.println("entId is : "+entId);
			int generic = trapIdentityRoot.findValue("generic").asInt();
			int specific = trapIdentityRoot.findValue("specific").asInt();
			TrapIdentity trapIdentity = new TrapIdentity(entId, generic, specific);
			trapProcessor.setTrapIdentity(trapIdentity);
			
			// Setting VarBind {VarBindRoot is : {"generic":6,"specific":0,"enterpriseId":".1.3.6.1.2.1.1.3"}
			JsonNode varBindRoot = trapProcessorRoot.findValue("varBinds");
			System.out.println("varBindRoot is : "+varBindRoot);         
			String value = new String(Base64.getDecoder().decode(varBindRoot.findValue("value").asText()));
			System.out.println("value is :"+value);
	
			PDU snmp4JV2cTrapPdu = new PDU();
			OID oid = new OID(trapIdentityRoot.findValue("enterpriseId").asText());
			snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));   
			snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
			snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
					new IpAddress(trapAddress)));

			snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(value)));
			snmp4JV2cTrapPdu.setType(varBindRoot.findValue("type").asInt()); // what should be the type?
			System.out.println("type is : "+varBindRoot.findValue("type").asInt());
			
			snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
			InetAddressUtils.getInetAddress(trapAddress), trapProcessorRoot.findValue("community").asText(),
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