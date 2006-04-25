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
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.utils.AlphaNumeric;

class Initializer {

	// input parms
	private SnmpCollector m_collector;
	private CollectionInterface m_iface;
	private Map m_parameters;

	// temps
	private java.sql.Connection m_dsConn;
	private String m_collectionName;

	// output parms
	private NodeInfo m_nodeInfo;
	private Map m_ifMap;
	
	private CollectionSet m_collectionSet = new CollectionSet();
	
	void execute(SnmpCollector collector, CollectionInterface iface, Map parameters) {
		
		initialize(collector, iface, parameters);

		m_iface.setCollectionSet(m_collectionSet);

		/*
		 * All database calls wrapped in try/finally block so we make certain
		 * that the connection will be closed when we are finished.
		 */
		allocateDBConn();
		try {
			logCollectionParms();
			
			validateNodeID();
			validatePrimaryIfIndex();
			validateIsSnmpPrimary();
			validateSysObjId();
	
			m_collectionSet.setNodeInfo(m_iface, m_collectionName);

			computeSnmpInfoForInterfaces();
	
			verifyCollectionIsNecessary();
			
		} finally {
			closeDBConn();
		}
	
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

		m_ifMap = new TreeMap();
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
		if (m_nodeInfo.getOidList().isEmpty()) {
			boolean hasInterfaceOids = false;
			Iterator iter = m_ifMap.values().iterator();
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
		OnmsNode node = new OnmsNode();
		node.setId(new Integer(m_iface.getNodeId()));

		PreparedStatement stmt = null;
		try {
			stmt = m_dsConn.prepareStatement(SnmpCollector.SQL_GET_SNMP_INFO);
			stmt.setInt(1, m_iface.getNodeId());
			ResultSet rs = stmt.executeQuery();
			try {

				while (rs.next()) {
					// Extract retrieved database values from the result set
					OnmsSnmpInterface snmpIface = new OnmsSnmpInterface();
					snmpIface.setNode(node);
					snmpIface.setIfIndex(new Integer(rs.getInt(1)));
					snmpIface.setIfType(new Integer(rs.getInt(2)));
					snmpIface.setIfName(rs.getString(3));
					snmpIface.setIfDescr(rs.getString(4));
					snmpIface.setPhysAddr(rs.getString(5));
					String physAddr = snmpIface.getPhysAddr();
					
					
					CollectionType collType = getCollType(snmpIface);

					IfInfo ifInfo = (IfInfo)m_ifMap.get(new Integer(snmpIface.getIfIndex().intValue()));
					if (ifInfo != null) {
						if ("P".equals(collType)) ifInfo.setCollType(collType);
					} else {

						if (log().isDebugEnabled()) {
							log()
							.debug(
									"initialize: snmpifindex = " + snmpIface.getIfIndex().intValue()
									+ ", snmpifname = " + snmpIface.getIfName()
									+ ", snmpifdescr = " + snmpIface.getIfDescr()
									+ ", snmpphysaddr = -"
									+ physAddr + "-");
						}

						/*
						 * Determine the label for this interface. The label will be
						 * used to create the RRD file name which holds SNMP data
						 * retreived from the remote agent. If available ifName is
						 * used to generate the label since it is guaranteed to be
						 * unique. Otherwise ifDescr is used. In either case, all
						 * non alpha numeric characters are converted to underscores
						 * to ensure that the resuling string will make a decent
						 * file name and that RRD won't have any problems using it
						 */
						String label = null;
						if (snmpIface.getIfName() != null) {
							label = AlphaNumeric.parseAndReplace(snmpIface.getIfName(), SnmpCollector.nonAnRepl);
						} else if (snmpIface.getIfDescr() != null) {
							label = AlphaNumeric.parseAndReplace(snmpIface.getIfDescr(), SnmpCollector.nonAnRepl);
						} else {
							log()
							.warn(
									"Interface (ifIndex/nodeId="
									+ snmpIface.getIfIndex().intValue()
									+ "/"
									+ m_iface.getNodeId()
									+ ") has no ifName and no "
									+ "ifDescr...setting to label to 'no_ifLabel'.");
							label = "no_ifLabel";
						}

						/*
						 * In order to assure the uniqueness of the RRD file names
						 * we now append the MAC/physical address to the end of
						 * label if it is available.
						 */
						if (physAddr != null) {
							physAddr = AlphaNumeric.parseAndTrim(physAddr);
							if (physAddr.length() == 12) {
								label = label + "-" + physAddr;
							} else {
								if (log().isDebugEnabled()) {
									log().debug(
											"initialize: physical address len "
											+ "is NOT 12, physAddr="
											+ physAddr);
								}
							}
						}

						if (log().isDebugEnabled()) {
							log().debug("initialize: ifLabel = '" + label + "'");
						}

						// Create new IfInfo object
						ifInfo = new IfInfo(snmpIface.getIfIndex().intValue(), snmpIface.getIfType().intValue(), label, collType);

						if (snmpIface.getIfIndex().intValue() == m_iface.getIfIndex()) {
							ifInfo.setIsPrimary(true);
						} else {
							ifInfo.setIsPrimary(false);
						}

						/*
						 * Retrieve list of mib objects to be collected from the
						 * remote agent for this interface.
						 */
						List oidList = DataCollectionConfigFactory.getInstance()
						.getMibObjectList(m_collectionName, m_iface.getSysObjectId(),
								m_iface.getHostAddress(), snmpIface.getIfType().intValue());

						/*
						 * Now build a list of RRD data source objects from the list
						 * of mib objects
						 */
						List dsList = DataCollectionConfigFactory.buildDataSourceList(m_collectionName, oidList);

						// Set MIB object and data source lists in IfInfo object
						ifInfo.setOidList(oidList);
						ifInfo.setDsList(dsList);

						/*
						 * Add the new IfInfo object to the interface map keyed by
						 * interface index
						 */
						m_ifMap.put(new Integer(snmpIface.getIfIndex().intValue()), ifInfo);
					}
				}
			} finally {
				rs.close();
			}
		} catch (SQLException e) {
			log().debug("initialize: SQL exception!!", e);
			throw new RuntimeException("SQL exception while attempting "
					+ "to retrieve snmp interface info", e);
		} catch (NullPointerException e) {
			/*
			 * Thrown by ResultSet.getString() if database query did not
			 * return anything
			 */
			log().debug("initialize: NullPointerException", e);
			throw new RuntimeException("NullPointerException while "
					+ "attempting to retrieve snmp interface info", e);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				log().info(
						"initialize: an error occured trying to close "
						+ "an SQL statement", e);
			}
		}

		/*
		 * Verify that we did find at least one eligible interface for the
		 * node.
		 */
		if (m_ifMap.size() < 1) {
			throw new RuntimeException("Failed to retrieve any eligible "
					+ "interfaces for node " + m_iface.getNodeId()
					+ " from the database.");
		}

		// Add the ifMap object as an attribute of the interface
		m_iface.setIfMap(m_ifMap);

	}
	
	
	private CollectionType getCollType(OnmsSnmpInterface snmpIface) throws SQLException {
		List ipInterfaces = getIpInterfaces(snmpIface);
		return getMaxCollType(ipInterfaces);		
	}

	private CollectionType getMaxCollType(List ipInterfaces) {
		CollectionType maxCollType = CollectionType.NO_COLLECT;
		for (Iterator it = ipInterfaces.iterator(); it.hasNext();) {
			OnmsIpInterface ipIface = (OnmsIpInterface) it.next();
			maxCollType = maxCollType.max(ipIface.getIsSnmpPrimary());
		}
		return maxCollType;
	}

	private List getIpInterfaces(OnmsSnmpInterface snmpIface) throws SQLException {
		List ipInterfaces = new LinkedList();
		PreparedStatement stmt = null;
		try {
			stmt = m_dsConn.prepareStatement(SnmpCollector.SQL_GET_ISSNMPPRIMARY_FOR_SNMPIF);
			stmt.setInt(1, snmpIface.getNode().getId().intValue());
			stmt.setInt(2, snmpIface.getIfIndex().intValue());
			ResultSet rs = stmt.executeQuery();
			try {
				while (rs.next()) {
					OnmsIpInterface ipIface = new OnmsIpInterface();
					ipIface.setIsSnmpPrimary(CollectionType.get(rs.getString(1)));
					ipInterfaces.add(ipIface);
				}
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
		return ipInterfaces;
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