package org.opennms.netmgt.model.updates;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public class NodeUpdate {
	private boolean m_applied = false;

	private Integer m_id;
    private String m_foreignSource;
    private String m_foreignId;

    private final FieldUpdate<String> m_type            = new FieldUpdate<String>("type");
    private final FieldUpdate<String> m_sysObjectId     = new FieldUpdate<String>("sysObjectId");
    private final FieldUpdate<String> m_sysName         = new FieldUpdate<String>("sysName");
    private final FieldUpdate<String> m_sysDescription  = new FieldUpdate<String>("sysDescription");
    private final FieldUpdate<String> m_sysLocation     = new FieldUpdate<String>("sysLocation");
    private final FieldUpdate<String> m_sysContact      = new FieldUpdate<String>("sysContact");
    private final FieldUpdate<String> m_label           = new FieldUpdate<String>("label");
    private final FieldUpdate<String> m_labelSource     = new FieldUpdate<String>("labelSource");
    private final FieldUpdate<String> m_netBiosName     = new FieldUpdate<String>("netBiosName");
    private final FieldUpdate<String> m_netBiosDomain   = new FieldUpdate<String>("netBiosDomain");
    private final FieldUpdate<String> m_operatingSystem = new FieldUpdate<String>("operatingSystem");
    private final FieldUpdate<Date>   m_lastCapsdPoll   = new FieldUpdate<Date>("lastCapsdPoll");

	private final Map<InetAddress,IpInterfaceUpdate> m_ipInterfaces = new HashMap<InetAddress,IpInterfaceUpdate>();
	private final Map<String,CategoryUpdate> m_categoryUpdates = new HashMap<String,CategoryUpdate>();

	private AssetRecordUpdate m_assetRecordUpdate;


	/*
	 *
    private OnmsNode m_parent;
    private OnmsDistPoller m_distPoller;
    private OnmsAssetRecord m_assetRecord;
    private Set<OnmsIpInterface> m_ipInterfaces = new LinkedHashSet<OnmsIpInterface>();
    private Set<OnmsSnmpInterface> m_snmpInterfaces = new LinkedHashSet<OnmsSnmpInterface>();
    private Set<OnmsArpInterface> m_arpInterfaces = new LinkedHashSet<OnmsArpInterface>();
    private Set<OnmsArpInterface> m_arpInterfacesBySource = new LinkedHashSet<OnmsArpInterface>();
    private Set<OnmsCategory> m_categories = new LinkedHashSet<OnmsCategory>();
	private PathElement m_pathElement;
	 */

	public NodeUpdate(final Integer id) {
		m_id = id;

		assertNodeIdentifiable();
	}
	
	public NodeUpdate(final String foreignSource, final String foreignId) {
		m_foreignSource = foreignSource;
		m_foreignId = foreignId;

		assertNodeIdentifiable();
	}

	public NodeUpdate(final Integer nodeId, final String foreignSource, final String foreignId) {
		m_id = nodeId;
		m_foreignSource = foreignSource;
		m_foreignId = foreignId;
		
		assertNodeIdentifiable();
	}

	private void assertNodeIdentifiable() {
		if (m_id == null && (m_foreignSource == null || m_foreignId == null)) {
			throw new IllegalStateException("You must set either the ID, or the foreignSource and foreignId");
		}
	}

	public Integer getId() {
		return m_id;
	}

	public String getForeignSource() {
		return m_foreignSource;
	}

	public String getForeignId() {
		return m_foreignId;
	}

	public String getLabel() {
		return m_label.get();
	}
	
	public NodeUpdate setLabel(final String label) {
		assertNotApplied("label");
		m_label.set(label);
		return this;
	}

	public String getType() {
		return m_type.get();
	}

	public NodeUpdate setType(final String type) {
		assertNotApplied("type");
		m_type.set(type);
		return this;
	}

	public String getSysObjectId() {
		return m_sysObjectId.get();
	}

	public NodeUpdate setSysObjectId(final String sysObjectId) {
		assertNotApplied("sysObjectId");
		m_sysObjectId.set(sysObjectId);
		return this;
	}

	public String getSysName() {
		return m_sysName.get();
	}

	public NodeUpdate setSysName(final String sysName) {
		assertNotApplied("sysName");
		m_sysName.set(sysName);
		return this;
	}

	public String getSysDescription() {
		return m_sysDescription.get();
	}

	public NodeUpdate setSysDescription(final String sysDescription) {
		assertNotApplied("sysDescription");
		m_sysDescription.set(sysDescription);
		return this;
	}

	public String getSysLocation() {
		return m_sysLocation.get();
	}

	public NodeUpdate setSysLocation(final String sysLocation) {
		assertNotApplied("sysLocation");
		m_sysLocation.set(sysLocation);
		return this;
	}

	public String getSysContact() {
		return m_sysContact.get();
	}

	public NodeUpdate setSysContact(final String sysContact) {
		assertNotApplied("sysContact");
		m_sysContact.set(sysContact);
		return this;
	}

	public String getLabelSource() {
		return m_labelSource.get();
	}

	public NodeUpdate setLabelSource(final String labelSource) {
		assertNotApplied("labelSource");
		m_labelSource.set(labelSource);
		return this;
	}

	public String getNetBiosName() {
		return m_netBiosName.get();
	}

	public NodeUpdate setNetBiosName(final String netBiosName) {
		assertNotApplied("netBiosName");
		m_netBiosName.set(netBiosName);
		return this;
	}

	public String getNetBiosDomain() {
		return m_netBiosDomain.get();
	}

	public NodeUpdate setNetBiosDomain(final String netBiosDomain) {
		assertNotApplied("netBiosDomain");
		m_netBiosDomain.set(netBiosDomain);
		return this;
	}

	public String getOperatingSystem() {
		return m_operatingSystem.get();
	}

	public NodeUpdate setOperatingSystem(final String operatingSystem) {
		assertNotApplied("operatingSystem");
		m_operatingSystem.set(operatingSystem);
		return this;
	}

	public Date getLastCapsdPoll() {
		return m_lastCapsdPoll.get();
	}

	public NodeUpdate setLastCapsdPoll(final Date lastCapsdPoll) {
		assertNotApplied("lastCapsdPoll");
		m_lastCapsdPoll.set(lastCapsdPoll);
		return this;
	}

	public IpInterfaceUpdate ipAddress(final InetAddress address) {
		IpInterfaceUpdate update = m_ipInterfaces.get(address);
		if (update == null) {
			update = new IpInterfaceUpdate(this, address);
			m_ipInterfaces.put(address, update);
		}
		return update;
	}

	public CategoryUpdate category(final String categoryName) {
		CategoryUpdate update = m_categoryUpdates.get(categoryName);
		if (update == null) {
			update = new CategoryUpdate(this, categoryName);
			m_categoryUpdates.put(categoryName, update);
		}
		return update;
	}

	public AssetRecordUpdate assetRecord() {
		if (m_assetRecordUpdate == null) {
			m_assetRecordUpdate = new AssetRecordUpdate();
		}
		return m_assetRecordUpdate;
	}

	private void assertNotApplied(final String field) {
		if (m_applied) {
			throw new IllegalStateException("Cannot set field '" + field + "', node update has already been applied.");
		}
	}

	public OnmsNode apply(final OnmsNode node) {
		m_applied = true;
		m_type.apply(node);
		m_sysObjectId.apply(node);
		m_sysName.apply(node);
		m_sysDescription.apply(node);
		m_sysLocation.apply(node);
		m_sysContact.apply(node);
		m_label.apply(node);
		m_labelSource.apply(node);
		m_netBiosName.apply(node);
		m_netBiosDomain.apply(node);
		m_operatingSystem.apply(node);
		m_lastCapsdPoll.apply(node);
		
		if (m_assetRecordUpdate != null) {
			m_assetRecordUpdate.apply(node.getAssetRecord());
		}

		for (final IpInterfaceUpdate update : m_ipInterfaces.values()) {
			OnmsIpInterface iface = node.getIpInterfaceByIpAddress(update.getAddress());
			if (iface == null) {
				iface = new OnmsIpInterface(update.getAddress(), node);
			}
			update.apply(iface);
		}

		for (final CategoryUpdate update : m_categoryUpdates.values()) {
			update.apply(node);
		}
		return node;
	}

}
