/**
 * 
 */
package org.opennms.netmgt.collectd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;

class Initializer {

	// input parms
	private SnmpCollector m_collector;
	private CollectionInterface m_iface;
	private Map m_parameters;

	// temps
	private java.sql.Connection m_dsConn;
	private String m_collectionName;

	// output parms
	private CollectionSet m_collectionSet;
	
	void execute(SnmpCollector collector, CollectionInterface iface, Map parameters) {
		
		initialize(collector, iface, parameters);


		logCollectionParms();
		
		validateNodeID();
		validatePrimaryIfIndex();
		validateIsSnmpPrimary();
		validateSysObjId();

		m_collectionSet = new CollectionSet(m_iface, m_collectionName);
		
		computeSnmpInfoForInterfaces();
		
		verifyCollectionIsNecessary();
			
		m_iface.setCollectionSet(m_collectionSet);
	
		logCompletion();
	}

	private void initialize(SnmpCollector collector, CollectionInterface iface, Map parameters) {
		m_collector = collector;
		m_iface = iface;
		m_parameters = parameters;
		m_collectionName = m_collector.getCollectionName(m_parameters);

		// Add the SNMP storage value as an attribute of the interface
		m_iface.setStorageFlag(m_collector.getStorageFlag(m_collectionName));
	
		m_iface.setMaxVarsPerPdu(m_collector.getMaxVarsPerPdu(m_collectionName));

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

	private void closeDBConn() {
		// Done with the database so close the connection
		try {
			if (m_dsConn != null) m_dsConn.close();
		} catch (SQLException e) {
			log().warn("initialize: SQLException while closing database connection", e);
		}
	}

	private void allocateDBConn() {
		try {
			m_dsConn = DataSourceFactory.getInstance().getConnection();
		} catch (SQLException e) {
			log().error("initialize: Failed getting connection to the database.", e);
			throw new UndeclaredThrowableException(e);
		}
	}

	private void verifyCollectionIsNecessary() {
		/*
		 * Verify that there is something to collect from this primary SMP
		 * interface. If no node objects and no interface objects then throw
		 * exception
		 */
		if (m_collectionSet.getNodeInfo().getOidList().isEmpty()) {
			boolean hasInterfaceOids = false;
			Iterator iter = m_collectionSet.getIfMap().values().iterator();
			while (iter.hasNext() && !hasInterfaceOids) {
				IfInfo ifInfo = (IfInfo) iter.next();
				if (!ifInfo.getOidList().isEmpty()) {
					hasInterfaceOids = true;
				}
			}

			if (!hasInterfaceOids) {
				throw new RuntimeException("collection '" + m_collectionName
						+ "' defines nothing to collect for "
						+ m_iface.getHostAddress());
			}
		}
	}

	private void computeSnmpInfoForInterfaces() {
		OnmsNode node = m_iface.getNode();

		Set snmpIfs = node.getSnmpInterfaces();
		
		for (Iterator it = snmpIfs.iterator(); it.hasNext();) {
			OnmsSnmpInterface snmpIface = (OnmsSnmpInterface) it.next();
			createIfInfoForInterface(snmpIface);
			
		}
		
		validateInterfacesExist();


	}

	private void validateInterfacesExist() {
		/*
		 * FIXME: Should we REALLY refuse to collect at all on interfaces
		 * that have NO snmpInterfaces in the snmpInterface table?
		 * Verify that we did find at least one eligible interface for the
		 * node.
		 */
		if (m_collectionSet.getIfMap().size() < 1) {
			throw new RuntimeException("Failed to retrieve any eligible "
					+ "interfaces for node " + m_iface.getNodeId()
					+ " from the database.");
		}
	}

	private void createIfInfoForInterface(OnmsSnmpInterface snmpIface) {
		logInitializeSnmpIface(snmpIface);

		if (log().isDebugEnabled()) {
			log().debug("initialize: ifLabel = '" + snmpIface.computeLabelForRRD() + "'");
		}

		// Create new IfInfo object
		IfInfo ifInfo = new IfInfo(snmpIface);

		/*
		 * Retrieve list of mib objects to be collected from the
		 * remote agent for this interface.
		 */
		List oidList = DataCollectionConfigFactory.getInstance()
		.getMibObjectList(m_collectionName, m_iface.getSysObjectId(),
				m_iface.getHostAddress(), snmpIface.getIfType().intValue());
		ifInfo.setOidList(oidList);

		/*
		 * Now build a list of RRD data source objects from the list
		 * of mib objects
		 */
		List dsList = DataCollectionConfigFactory.buildDataSourceList(m_collectionName, oidList);
		ifInfo.setDsList(dsList);

		/*
		 * Add the new IfInfo object to the interface map keyed by
		 * interface index
		 */
		m_collectionSet.addIfInfo(ifInfo);
	}

	private void logInitializeSnmpIface(OnmsSnmpInterface snmpIface) {
		if (log().isDebugEnabled()) {
			log()
			.debug(
					"initialize: snmpifindex = " + snmpIface.getIfIndex().intValue()
					+ ", snmpifname = " + snmpIface.getIfName()
					+ ", snmpifdescr = " + snmpIface.getIfDescr()
					+ ", snmpphysaddr = -"+ snmpIface.getPhysAddr() + "-");
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

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

}