package org.opennms.netmgt.model.updates;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.model.OnmsNode;

public class IpInterfaceUpdate {

	private Integer m_id;
	private InetAddress m_address;

	private Integer m_nodeId;
	private String m_foreignSource;
	private String m_foreignId;

	private final FieldUpdate<String> m_ipHostName         = new FieldUpdate<String>("ipHostName");
    private final FieldUpdate<String> m_isManaged          = new FieldUpdate<String>("isManaged");
    private final FieldUpdate<PrimaryType> m_isSnmpPrimary = new FieldUpdate<PrimaryType>("isSnmpPrimary");
    private final FieldUpdate<Date> m_ipLastCapsdPoll      = new FieldUpdate<Date>("ipLastCapsdPoll");

    private final Map<String,MonitoredServiceUpdate> m_monitoredServiceUpdates = new HashMap<String,MonitoredServiceUpdate>();

	/*

    private OnmsNode m_node;

    private Set<OnmsMonitoredService> m_monitoredServices = new HashSet<OnmsMonitoredService>();

    private OnmsSnmpInterface m_snmpInterface;

	 */

	public IpInterfaceUpdate(final Integer interfaceId) {
		m_id = interfaceId;
		
		assertInterfaceIdentifiable();
	}

	public IpInterfaceUpdate(final Integer nodeId, final InetAddress address) {
		m_nodeId = nodeId;
		m_address = address;

		assertInterfaceIdentifiable();
	}
	
	public IpInterfaceUpdate(final String foreignSource, final String foreignId, final InetAddress address) {
		m_foreignSource = foreignSource;
		m_foreignId = foreignId;
		m_address = address;

		assertInterfaceIdentifiable();
	}

	public IpInterfaceUpdate(final OnmsIpInterface iface) {
		m_address = iface.getIpAddress();

		final OnmsNode node = iface.getNode();
		if (node != null) {
			m_nodeId = node.getId();
			m_foreignSource = node.getForeignSource();
			m_foreignId = node.getForeignId();
		}
		
		assertInterfaceIdentifiable();
	}

	public IpInterfaceUpdate(final NodeUpdate nodeUpdate, final InetAddress address) {
		m_nodeId = nodeUpdate.getId();
		m_foreignSource = nodeUpdate.getForeignSource();
		m_foreignId = nodeUpdate.getForeignId();
		m_address = address;
		
		assertInterfaceIdentifiable();
	}

	private void assertInterfaceIdentifiable() {
		if (m_id != null) return;
		
		if (m_address == null) {
			throw new IllegalStateException("You must provide an InetAddress");
		}
		if (m_nodeId == null && (m_foreignSource == null || m_foreignId == null)) {
			throw new IllegalStateException("You must provide either a node ID, or a foreign source and foreign ID");
		}
	}

	public InetAddress getAddress() {
		return m_address;
	}

	public String getIpHostName() {
		return m_ipHostName.get();
	}

	public IpInterfaceUpdate setIpHostName(final String ipHostName) {
		m_ipHostName.set(ipHostName);
		return this;
	}

	public String getIsManaged() {
		return m_isManaged.get();
	}

	public IpInterfaceUpdate setIsManaged(final String isManaged) {
		m_isManaged.set(isManaged);
		return this;
	}

	public PrimaryType getIsSnmpPrimary() {
		return m_isSnmpPrimary.get();
	}

	public IpInterfaceUpdate setIsSnmpPrimary(final PrimaryType isSnmpPrimary) {
		m_isSnmpPrimary.set(isSnmpPrimary);
		return this;
	}

	public Date getIpLastCapsdPoll() {
		return m_ipLastCapsdPoll.get();
	}

	public IpInterfaceUpdate setIpLastCapsdPoll(final Date ipLastCapsdPoll) {
		m_ipLastCapsdPoll.set(ipLastCapsdPoll);
		return this;
	}

	public MonitoredServiceUpdate monitoredService(final String serviceName) {
		MonitoredServiceUpdate update = m_monitoredServiceUpdates.get(serviceName);
		if (update == null) {
			update = new MonitoredServiceUpdate(this, serviceName);
			m_monitoredServiceUpdates.put(serviceName, update);
		}
		return update;
	}

	/* same thing, but verbed! */
	public IpInterfaceUpdate addMonitoredService(final String serviceName) {
		monitoredService(serviceName);
		return this;
	}
	
	public OnmsIpInterface apply(final OnmsIpInterface iface) {
		m_ipHostName.apply(iface);
		m_isManaged.apply(iface);
		m_isSnmpPrimary.apply(iface);
		m_ipLastCapsdPoll.apply(iface);

		for (final MonitoredServiceUpdate update : m_monitoredServiceUpdates.values()) {
			update.apply(iface);
		}

		return iface;
	}

}
