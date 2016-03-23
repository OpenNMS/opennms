/**
 * 
 */
package org.opennms.netmgt.trapd;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapProcessor;

/**
 * @author pk015603
 *
 */
public class TrapProcessorImpl implements TrapProcessor{

	private String community;

	private long timeStamp;

	private String version;

	private InetAddress agentAddress;

	private String varBind;

	private InetAddress trapAddress;

	private TrapIdentity trapIdentity;

	private SnmpObjId name;

	private SnmpValue value;

	public String getCommunity() {
		return community;
	}

	@Override
	public void setCommunity(String community) {
		this.community = community;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	public InetAddress getAgentAddress() {
		return agentAddress;
	}

	@Override
	public void setAgentAddress(InetAddress agentAddress) {
		this.agentAddress = agentAddress;
	}

	public String getVarBind() {
		return varBind;
	}

	public InetAddress getTrapAddress() {
		return trapAddress;
	}

	@Override
	public void setTrapAddress(InetAddress trapAddress) {
		this.trapAddress = trapAddress;
	}

	public TrapIdentity getTrapIdentity() {
		return trapIdentity;
	}

	@Override
	public void setTrapIdentity(TrapIdentity trapIdentity) {
		this.trapIdentity = trapIdentity;
	}

	@Override
	public void processVarBind(SnmpObjId name, SnmpValue value) {
		this.name = name;
		this.value = value;
	}
}
