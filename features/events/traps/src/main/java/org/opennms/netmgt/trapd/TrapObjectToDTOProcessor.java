package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JUtils;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.VariableBinding;

public class TrapObjectToDTOProcessor implements Processor{
	public static final Logger LOG = LoggerFactory.getLogger(TrapObjectToDTOProcessor.class);

	private final Class<?> m_class;

	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public TrapObjectToDTOProcessor(Class clazz) {
		m_class = clazz;
	}

	public TrapObjectToDTOProcessor(String className) throws ClassNotFoundException {
		m_class = Class.forName(className);
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Object object = exchange.getIn().getBody(m_class);
		boolean trapRawMessageFlag = (boolean)exchange.getIn().getHeader("trapRawMessageFlag");
		exchange.getIn().setBody(object2dto(object, trapRawMessageFlag), TrapDTO.class);
	}
	
	public TrapDTO object2dto(Object obj, boolean trapRawMessageFlag) {

		TrapDTO trapDTO = new TrapDTO();

		TrapInformation trapInfo = (TrapInformation) obj;

		String version = trapInfo.getVersion();

		Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = null;
		Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = null;

		if (version.equalsIgnoreCase("v1")) {

			v1Trap = (Snmp4JTrapNotifier.Snmp4JV1TrapInformation) obj;
			InetAddress agentAddress = v1Trap.getAgentAddress();
			String community = v1Trap.getCommunity();
			int pduLength = v1Trap.getPduLength();
			// String version = v1Trap.getVersion();
			InetAddress trapAddress = v1Trap.getTrapAddress();
			long timestamp = v1Trap.getTimeStamp();
			PDUv1 pdu = v1Trap.getPduv1();
			byte[] byteArray = null;
			try {
				byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0,
						community, pdu);
			} catch (Exception e) {
				LOG.warn("unable to convert Pdu inot Bytes {}", e.getMessage());
			}

			if(trapRawMessageFlag){
				trapDTO.setBody(byteArray);
			}
			trapDTO.setCommunity(community);
			trapDTO.setPduLength(String.valueOf(pduLength));
			trapDTO.setAgentAddress(agentAddress);
			trapDTO.setTimestamp(timestamp);
			trapDTO.setTrapAddress(trapAddress);
			trapDTO.setVersion(version);

			List<SnmpResult> results = new ArrayList<SnmpResult>();

			SnmpResult snmpResult = null;

			Vector<? extends VariableBinding> vector = pdu
					.getVariableBindings();
			for (int i = 0; i < vector.size(); i++) {
				VariableBinding varBind = (VariableBinding) vector.get(i);

				SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
				SnmpValue value = new Snmp4JValue(varBind.getVariable());
				snmpResult = new SnmpResult(oid, null, value);
				results.add(snmpResult);
			}
			trapDTO.setResults(results);

		} else if (version.equalsIgnoreCase("v2")
				|| version.equalsIgnoreCase("v3")) {

			v2Trap = (Snmp4JTrapNotifier.Snmp4JV2TrapInformation) obj;
			InetAddress agentAddress = v2Trap.getAgentAddress();
			String community = v2Trap.getCommunity();
			int pduLength = v2Trap.getPduLength();
			// String version = v2Trap.getVersion();
			InetAddress trapAddress = v2Trap.getTrapAddress();
			long timestamp = v2Trap.getTimeStamp();
			PDU pdu = v2Trap.getPdu();
			byte[] byteArray = null;
			try {
				byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0,
						community, pdu);
			} catch (Exception e) {
				LOG.warn("unable to convert Pdu inot Bytes {}", e.getMessage());
			}

			if(trapRawMessageFlag){
				trapDTO.setBody(byteArray);
			}
			trapDTO.setCommunity(community);
			trapDTO.setPduLength(String.valueOf(pduLength));
			trapDTO.setAgentAddress(agentAddress);
			trapDTO.setTimestamp(timestamp);
			trapDTO.setTrapAddress(trapAddress);
			trapDTO.setVersion(version);

			List<SnmpResult> results = new ArrayList<SnmpResult>();

			SnmpResult snmpResult = null;

			Vector<? extends VariableBinding> vector = pdu
					.getVariableBindings();
			for (int i = 0; i < vector.size(); i++) {
				VariableBinding varBind = (VariableBinding) vector.get(i);
				SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
				SnmpValue value = new Snmp4JValue(varBind.getVariable());
				snmpResult = new SnmpResult(oid, null, value);
				results.add(snmpResult);
			}
			trapDTO.setResults(results);

		}

		return trapDTO;
	}
}
