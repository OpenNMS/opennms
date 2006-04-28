/**
 * 
 */
package org.opennms.netmgt.collectd;

import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;

class Initializer {

	// input parms
	private SnmpCollector m_collector;
	private CollectionInterface m_iface;

	// temps
	private String m_collectionName;

	// output parms
	private CollectionSet m_collectionSet;
	
	void execute(SnmpCollector collector, CollectionInterface iface, Map parameters) {
		
		m_collector = collector;
		m_iface = iface;
		m_collectionName = m_collector.getCollectionName(parameters);
		
		// Add the SNMP storage value as an attribute of the interface
		m_iface.setStorageFlag(m_collector.getStorageFlag(m_collectionName));
		m_iface.setMaxVarsPerPdu(m_collector.getMaxVarsPerPdu(m_collectionName));

		logCollectionParms();
		
		validateIsSnmpPrimary();
		validateSysObjId();

		m_collectionSet = new CollectionSet(m_iface, m_collectionName);
		m_iface.setCollectionSet(m_collectionSet);
		
		verifyCollectionIsNecessary();
	
		logCompletion();
	}

	private void logCompletion() {
		
		if (log().isDebugEnabled()) {
			log().debug(
					"initialize: initialization completed: nodeid = " + m_iface.getNodeId()
					+ ", address = " + m_iface.getHostAddress()
					+ ", primaryIfIndex = " + m_iface.getIfIndex()
					+ ", isSnmpPrimary = " + m_iface.getCollectionType()
					+ ", sysoid = " + m_iface.getSysObjectId()
					);
		}

	}

	private void verifyCollectionIsNecessary() {

		/*
		 * Verify that there is something to collect from this primary SMP
		 * interface. If no node objects and no interface objects then throw
		 * exception
		 */
		if (!m_collectionSet.hasDataToCollect()) {
			throw new RuntimeException("collection '" + m_collectionName
					+ "' defines nothing to collect for "
					+ m_iface.getHostAddress());
		}
	}

	private void validateSysObjId() {
		if (m_iface.getSysObjectId() == null) {
			throw new RuntimeException("System Object ID for interface "
					+ m_iface.getHostAddress()
					+ " does not exist in the database.");
		}
	}

	private void logCollectionParms() {
		if (log().isDebugEnabled()) {
			log().debug(
					"initialize: db retrieval info: nodeid = " + m_iface.getNodeId()
					+ ", address = " + m_iface.getHostAddress()
					+ ", primaryIfIndex = " + m_iface.getIfIndex()
					+ ", isSnmpPrimary = " + m_iface.getCollectionType()
					+ ", sysoid = " + m_iface.getSysObjectId()
					);
		}
	}

	private void validateIsSnmpPrimary() {
		if (!CollectionType.PRIMARY.equals(m_iface.getCollectionType())) {
			throw new RuntimeException("Interface "
					+ m_iface.getHostAddress()
					+ " is not the primary SNMP interface for nodeid "
					+ m_iface.getNodeId());
		}
	}

	private void validatePrimaryIfIndex() {
		if (m_iface.getIfIndex() == -1) {
			// allow this for nodes without ipAddrTables
			// throw new RuntimeException("Unable to retrieve ifIndex for
			// interface " + ipAddr.getHostAddress());
			if (log().isDebugEnabled()) {
				log().debug(
						"initialize: db retrieval info: node " + m_iface.getNodeId()
						+ " does not have a legitimate "
						+ "primaryIfIndex.  Assume node does not "
						+ "supply ipAddrTable and continue...");
			}
		}
	}

	private void validateNodeID() {
		/*
		 * RuntimeException is thrown if any of the following are true: -
		 * node id is invalid - primaryIfIndex is invalid - Interface is not
		 * the primary SNMP interface for the node
		 */
		if (m_iface.getNodeId() == -1) {
			throw new RuntimeException("Unable to retrieve node id "
					+ "for interface " + m_iface.getHostAddress());
		}
	}

	Category log() {
		return ThreadCategory.getInstance(getClass());
	}

}