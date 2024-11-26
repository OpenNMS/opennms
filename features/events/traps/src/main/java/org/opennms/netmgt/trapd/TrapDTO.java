/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpVarBindDTO;
import org.opennms.netmgt.snmp.TrapInformation;

import com.google.common.base.MoreObjects;

@XmlRootElement(name = "trap-dto")
@XmlAccessorType(XmlAccessType.NONE)
public class TrapDTO implements Message {

	@XmlElement(name = "agent-address")
	@XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
	private InetAddress agentAddress;
	@XmlElement(name = "community")
	private String community;
	@XmlElement(name = "version", required=true)
	private String version;
	@XmlElement(name = "timestamp")
	private long timestamp;
	@XmlElement(name = "pdu-length")
	private int pduLength;
	@XmlElement(name = "creation-time")
	private long creationTime;
	@XmlElement(name = "raw-message")
	private byte[] rawMessage;
	@XmlElement(name = "trap-identity")
	private TrapIdentityDTO trapIdentity;
	@XmlElementWrapper(name = "results")
	@XmlElement(name = "result")
	private List<SnmpResult> results = new ArrayList<>();

	@XmlAttribute(name = "trap-address")
	@XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
	private InetAddress trapAddress;


	// No-arg constructor for JAXB
	public TrapDTO() {

	}

	public TrapDTO(TrapInformation trapInfo) {
		setAgentAddress(trapInfo.getAgentAddress());
		setCommunity(trapInfo.getCommunity());
		setVersion(trapInfo.getVersion());
		setTimestamp(trapInfo.getTimeStamp());
		setPduLength(trapInfo.getPduLength());
		setCreationTime(trapInfo.getCreationTime());
		setTrapIdentity(new TrapIdentityDTO(trapInfo.getTrapIdentity()));

		// Map variable bindings
		final List<SnmpResult> results = new ArrayList<>();
		for (int i = 0; i < trapInfo.getPduLength(); i++) {
			final SnmpVarBindDTO varBindDTO = trapInfo.getSnmpVarBindDTO(i);
			if (varBindDTO != null) {
				final SnmpResult snmpResult = new SnmpResult(varBindDTO.getSnmpObjectId(), null, varBindDTO.getSnmpValue());
				results.add(snmpResult);
			}
		}
		setResults(results);
	}

	private void setResults(List<SnmpResult> results) {
		this.results = new ArrayList<>(results);
	}

	public void setAgentAddress(InetAddress agentAddress) {
		this.agentAddress = agentAddress;
	}

	public InetAddress getAgentAddress() {
		return agentAddress;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public String getCommunity() {
		return community;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setPduLength(int pduLength) {
		this.pduLength = pduLength;
	}

	public int getPduLength() {
		return pduLength;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setTrapIdentity(TrapIdentityDTO trapIdentity) {
		this.trapIdentity = trapIdentity;
	}

	public TrapIdentityDTO getTrapIdentity() {
		return trapIdentity;
	}

	public List<SnmpResult> getResults() {
		return results;
	}

	public byte[] getRawMessage() {
		return rawMessage;
	}

	public void setRawMessage(byte[] rawMessage) {
		this.rawMessage = rawMessage;
	}

	public InetAddress getTrapAddress() {
		return trapAddress;
	}

	public void setTrapAddress(InetAddress trapAddress) {
		this.trapAddress = trapAddress;
	}

	@Override
	public int hashCode() {
		return Objects.hash(community, version, timestamp, pduLength, creationTime, rawMessage, trapIdentity, results, agentAddress);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (null == obj) return false;
		if (getClass() != obj.getClass()) return false;
		final TrapDTO other = (TrapDTO) obj;
		boolean equals = Objects.equals(community, other.community)
				&& Objects.equals(version, other.version)
				&& Objects.equals(timestamp, other.timestamp)
				&& Objects.equals(pduLength, other.pduLength)
				&& Objects.equals(creationTime, other.creationTime)
				&& Objects.equals(rawMessage, other.rawMessage)
				&& Objects.equals(trapIdentity, other.trapIdentity)
				&& Objects.equals(results, other.results)
				&& Objects.equals(agentAddress, other.agentAddress);
		return equals;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("agentAddress", agentAddress)
				.add("community", community)
				.add("trapIdentity", trapIdentity)
				.add("creationTime", creationTime)
				.add("pduLength", pduLength)
				.add("timestamp", timestamp)
				.add("version", version)
				.add("rawMessage", rawMessage).toString();
	}
}
