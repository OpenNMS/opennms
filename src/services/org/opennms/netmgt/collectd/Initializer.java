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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
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
	
			m_nodeInfo = new NodeInfo(m_iface, m_collectionName);
			m_collectionSet.setNodeInfo(m_nodeInfo);

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
					"initialize: initialization completed: nodeid = " + getNodeID()
					+ ", address = " + getHostAddress()
					+ ", primaryIfIndex = " + getPrimaryIfIndex()
					+ ", isSnmpPrimary = " + getIsSnmpPrimary()
					+ ", sysoid = " + getSysoid()
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
						+ getHostAddress());
			}
		}
	}

	private void computeSnmpInfoForInterfaces() {

		PreparedStatement stmt = null;
		try {
			stmt = m_dsConn.prepareStatement(SnmpCollector.SQL_GET_SNMP_INFO);
			stmt.setInt(1, getNodeID());
			ResultSet rs = stmt.executeQuery();
			try {

				while (rs.next()) {
					// Extract retrieved database values from the result set
					int index = rs.getInt(1);
					int type = rs.getInt(2);
					String name = rs.getString(3);
					String descr = rs.getString(4);
					String physAddr = rs.getString(5);
					String collType = rs.getString(6);

					IfInfo ifInfo = (IfInfo)m_ifMap.get(new Integer(index));
					if (ifInfo != null) {
						if ("P".equals(collType)) ifInfo.setCollType(collType);
						continue;
					}

					if (log().isDebugEnabled()) {
						log()
						.debug(
								"initialize: snmpifindex = " + index
								+ ", snmpifname = " + name
								+ ", snmpifdescr = " + descr
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
					if (name != null) {
						label = AlphaNumeric.parseAndReplace(name, SnmpCollector.nonAnRepl);
					} else if (descr != null) {
						label = AlphaNumeric.parseAndReplace(descr, SnmpCollector.nonAnRepl);
					} else {
						log()
						.warn(
								"Interface (ifIndex/nodeId="
								+ index
								+ "/"
								+ getNodeID()
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
					ifInfo = new IfInfo(index, type, label, collType);

					if (index == getPrimaryIfIndex()) {
						ifInfo.setIsPrimary(true);
					} else {
						ifInfo.setIsPrimary(false);
					}

					/*
					 * Retrieve list of mib objects to be collected from the
					 * remote agent for this interface.
					 */
					List oidList = DataCollectionConfigFactory.getInstance()
					.getMibObjectList(m_collectionName, getSysoid(),
							getHostAddress(), type);

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
					m_ifMap.put(new Integer(index), ifInfo);
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
					+ "interfaces for node " + getNodeID()
					+ " from the database.");
		}

		// Add the ifMap object as an attribute of the interface
		m_iface.setIfMap(m_ifMap);

	}

	String getHostAddress() {
		return getIpAddr().getHostAddress();
	}

	private void validateSysObjId() {
		if (getSysoid() == null) {
			throw new RuntimeException("System Object ID for interface "
					+ getHostAddress()
					+ " does not exist in the database.");
		}
	}

	private void logCollectionParms() {
		if (log().isDebugEnabled()) {
			log().debug(
					"initialize: db retrieval info: nodeid = " + getNodeID()
					+ ", address = " + getHostAddress()
					+ ", primaryIfIndex = " + getPrimaryIfIndex()
					+ ", isSnmpPrimary = " + getIsSnmpPrimary()
					+ ", sysoid = " + getSysoid()
					);
		}
	}

	private void validateIsSnmpPrimary() {
		if (getIsSnmpPrimary() != DbIpInterfaceEntry.SNMP_PRIMARY) {
			throw new RuntimeException("Interface "
					+ getHostAddress()
					+ " is not the primary SNMP interface for nodeid "
					+ getNodeID());
		}
	}

	private void validatePrimaryIfIndex() {
		if (getPrimaryIfIndex() == -1) {
			// allow this for nodes without ipAddrTables
			// throw new RuntimeException("Unable to retrieve ifIndex for
			// interface " + ipAddr.getHostAddress());
			if (log().isDebugEnabled()) {
				log().debug(
						"initialize: db retrieval info: node " + getNodeID()
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
		if (getNodeID() == -1) {
			throw new RuntimeException("Unable to retrieve node id "
					+ "for interface " + getHostAddress());
		}
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	private InetAddress getIpAddr() {
		return m_iface.getInetAddress();
	}

	private int getNodeID() {
		return m_iface.getNodeId();
	}

	private int getPrimaryIfIndex() {
		return m_iface.getIfIndex();
	}

	private char getIsSnmpPrimary() {
		String isSnmpPrimary = m_iface.getIpInterface().getIsSnmpPrimary();
		return (isSnmpPrimary == null ? 'N' : isSnmpPrimary.charAt(0));
	}

	String getSysoid() {
		return m_iface.getSysObjectId();
	}

}