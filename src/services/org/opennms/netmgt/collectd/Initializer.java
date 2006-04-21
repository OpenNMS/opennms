/**
 * 
 */
package org.opennms.netmgt.collectd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.netmgt.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.utils.AlphaNumeric;

class Initializer {

	private SnmpCollector m_collector;
	private CollectionInterface m_iface;
	private Map m_parameters;
	private int m_nodeID;
	private int m_primaryIfIndex;
	private char m_isSnmpPrimary;
	private boolean m_snmpv2Supported;
	private String m_sysoid;
	private java.sql.Connection m_dsConn;
	private InetAddress m_ipAddr;
	private String m_collectionName;
	private String m_storageFlag;
	private int m_maxVarsPerPdu;
	private NodeInfo m_nodeInfo;
	private Map m_ifMap;
	private HashMap m_snmppriMap;
	private List m_oidList;
	private List m_dsList;

	public Initializer(SnmpCollector collector, CollectionInterface iface, Map parameters) {
		this.m_collector = collector;
		this.m_iface = iface;
		this.m_parameters = parameters;
	}

	void execute() {
		
		initialize();
		/*
		 * All database calls wrapped in try/finally block so we make certain
		 * that the connection will be closed when we are finished.
		 */
		allocateDBConn();
		try {
			getPrimarySnmpInfo();
	
			getSysObjId();
	
			createNodeInfo();
	
			determineSnmpVersion();
	
			getSnmpInfoForInterfaces();
	
			verifyCollectionIsNecessary();

		} finally {
			closeDBConn();
		}
	
		logCompletion();
	}

	private void initialize() {
		m_ipAddr = m_collector.getInetAddress(m_iface);
		m_collectionName = m_collector.getCollectionName(m_parameters);
		m_storageFlag = m_collector.getStorageFlag(m_collectionName);
		// Add the SNMP storage value as an attribute of the interface
		m_collector.setStorageFlag(m_iface, m_storageFlag);
	
		m_maxVarsPerPdu = m_collector.getMaxVarsPerPdu(m_collectionName);
		m_collector.setMaxVarsPerPdu(m_iface, m_maxVarsPerPdu);
		m_snmppriMap = new HashMap();
		m_ifMap = new TreeMap();
		m_nodeID = -1;
		m_primaryIfIndex = -1;
		m_isSnmpPrimary = DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE;
		m_snmpv2Supported = false;
		m_sysoid = null;
	}
	

	private void logCompletion() {
		if (m_collector.log().isDebugEnabled()) {
			m_collector.log().debug(
					"initialize: address = " + m_ipAddr.getHostAddress()
							+ ", nodeID = " + m_nodeID + ", ifIndex = "
							+ m_primaryIfIndex + ", sysoid = " + m_sysoid);
		}
	
		// Instantiate new SnmpPeer object for this interface
		if (m_collector.log().isDebugEnabled()) {
			m_collector.log().debug(
					"initialize: initialization completed for "
							+ m_ipAddr.getHostAddress());
		}
	}

	private void closeDBConn() {
		// Done with the database so close the connection
		try {
			m_dsConn.close();
		} catch (SQLException e) {
			m_collector.log().info(
					"initialize: SQLException while closing database "
							+ "connection", e);
		}
	}

	private void allocateDBConn() {
		m_dsConn = null;
		try {
			m_dsConn = DataSourceFactory.getInstance().getConnection();
		} catch (SQLException e) {
			m_collector.log().error(
					"initialize: Failed getting connection to the database.",
					e);
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
						+ m_ipAddr.getHostAddress());
			}
		}
	}

	private void getSnmpInfoForInterfaces() {

		PreparedStatement stmt = null;

		try {
			stmt = m_dsConn.prepareStatement(SnmpCollector.SQL_GET_SNMP_INFO);
			stmt.setInt(1, m_nodeID);
			ResultSet rs = stmt.executeQuery();

			addIfaceToSnmpPrimaryMap();

			while (rs.next()) {
				// Extract retrieved database values from the result set
				int index = rs.getInt(1);
				int type = rs.getInt(2);
				String name = rs.getString(3);
				String descr = rs.getString(4);
				String physAddr = rs.getString(5);
				if (m_collector.log().isDebugEnabled()) {
					m_collector.log()
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
					m_collector.log()
					.warn(
							"Interface (ifIndex/nodeId="
							+ index
							+ "/"
							+ m_nodeID
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
						if (m_collector.log().isDebugEnabled()) {
							m_collector.log().debug(
									"initialize: physical address len "
									+ "is NOT 12, physAddr="
									+ physAddr);
						}
					}
				}

				if (m_collector.log().isDebugEnabled()) {
					m_collector.log().debug("initialize: ifLabel = '" + label + "'");
				}

				// Create new IfInfo object

				String collType = (String) m_snmppriMap.get(rs.getString(1));

				IfInfo ifInfo = new IfInfo(index, type, label, collType);

				if (index == m_primaryIfIndex) {
					ifInfo.setIsPrimary(true);
				} else {
					ifInfo.setIsPrimary(false);
				}

				/*
				 * Retrieve list of mib objects to be collected from the
				 * remote agent for this interface.
				 */
				m_oidList = DataCollectionConfigFactory.getInstance()
				.getMibObjectList(m_collectionName, m_sysoid,
						m_ipAddr.getHostAddress(), type);

				/*
				 * Now build a list of RRD data source objects from the list
				 * of mib objects
				 */
				m_dsList = m_collector.buildDataSourceList(m_collectionName, m_oidList);

				// Set MIB object and data source lists in IfInfo object
				ifInfo.setOidList(m_oidList);
				ifInfo.setDsList(m_dsList);

				/*
				 * Add the new IfInfo object to the interface map keyed by
				 * interface index
				 */
				m_ifMap.put(new Integer(index), ifInfo);
			}
			rs.close();
		} catch (SQLException e) {
			m_collector.log().debug("initialize: SQL exception!!", e);
			throw new RuntimeException("SQL exception while attempting "
					+ "to retrieve snmp interface info", e);
		} catch (NullPointerException e) {
			/*
			 * Thrown by ResultSet.getString() if database query did not
			 * return anything
			 */
			m_collector.log().debug("initialize: NullPointerException", e);
			throw new RuntimeException("NullPointerException while "
					+ "attempting to retrieve snmp interface info", e);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				m_collector.log().info(
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
					+ "interfaces for node " + m_nodeID
					+ " from the database.");
		}

		// Add the ifMap object as an attribute of the interface
		m_iface.setAttribute(SnmpCollector.IF_MAP_KEY, m_ifMap);

	}

	private void addIfaceToSnmpPrimaryMap() throws SQLException {
		PreparedStatement stmt1 = null;
		try {

		/*
		 * The following code does a database lookup on the ipinterface
		 * table and builds a Map of ifIndex and issnmpprimary values.
		 * The issnmpprimary value can then be checked to see if SNMP
		 * collection needs to be done on it.
		 */
		stmt1 = m_dsConn.prepareStatement(SnmpCollector.SQL_GET_ISSNMPPRIMARY);
		stmt1.setInt(1, m_nodeID); // interface address
		ResultSet rs1 = null;
		try {
			rs1 = stmt1.executeQuery();

		if (m_collector.log().isDebugEnabled()) {
			m_collector.log().debug(
					"initialize: Attempting to get issnmpprimary "
							+ "information for node: " + m_nodeID);
		}


		while (rs1.next()) {
			String snmppriIfIndex = rs1.getString(1);
			String snmppriCollType = rs1.getString(2);

			String currSNMPPriValue = (String) m_snmppriMap
					.get(snmppriIfIndex);

			if (currSNMPPriValue == null) {
				m_snmppriMap.put(snmppriIfIndex, snmppriCollType);
			} else if (currSNMPPriValue.equals("P")) {
				continue;
			} else if (currSNMPPriValue.equals("S")
					&& snmppriCollType.equals("P")) {
				m_snmppriMap.put(snmppriIfIndex, snmppriCollType);
			} else if (currSNMPPriValue.equals("C")
					&& (snmppriCollType.equals("P") || snmppriCollType
							.equals("S"))) {
				m_snmppriMap.put(snmppriIfIndex, snmppriCollType);
			} else {
				m_snmppriMap.put(snmppriIfIndex, snmppriCollType);
			}
		}
		
		} finally {
		rs1.close();
		}
		} finally {
			stmt1.close();
		}
	}

	private void determineSnmpVersion() {
		/*
		 * Prepare & execute the SQL statement for retrieving the SNMP
		 * version of node
		 */
		{
			
			PreparedStatement stmt = null;

		try {
			stmt = m_dsConn.prepareStatement(SnmpCollector.SQL_CHECK_SNMPV2);
			stmt.setInt(1, m_nodeID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				rs.getInt(1);
				m_snmpv2Supported = true;
			} else {
				m_snmpv2Supported = false;
			}
			rs.close();
		} catch (SQLException e) {
			m_collector.log().debug("initialize: SQL exception!!", e);
			throw new RuntimeException("SQL exception while attempting "
					+ "to retrieve snmp version information", e);
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				m_collector.log().info(
						"initialize: an error occured while closing "
								+ "an SQL statement", e);
			}
		}
		}

		if (m_collector.log().isDebugEnabled()) {
			m_collector.log().debug(
					"initialize: address = " + m_ipAddr.getHostAddress()
							+ ", nodeid = " + m_nodeID
							+ ", primaryIfIndex = " + m_primaryIfIndex
							+ ", isSnmpPrimary = " + m_isSnmpPrimary
							+ ", SNMPversion = "
							+ (m_snmpv2Supported ? "SNMPv2" : "SNMPv1"));
		}
	}

	private void createNodeInfo() {
		// Create the NodeInfo obect for this node
		m_nodeInfo = new NodeInfo(m_nodeID, m_primaryIfIndex);

		/*
		 * Retrieve list of mib objects to be collected from the remote
		 * agent which are to be stored in the node-level RRD file. These
		 * objects pertain to the node itself not any individual interfaces.
		 */
		List oidList = DataCollectionConfigFactory.getInstance()
				.getMibObjectList(m_collectionName, m_sysoid,
						m_ipAddr.getHostAddress(), -1);
		m_nodeInfo.setOidList(oidList);
		List dsList = m_collector.buildDataSourceList(m_collectionName, oidList);
		m_nodeInfo.setDsList(dsList);

		// Add the NodeInfo object as an attribute of the interface
		m_iface.setAttribute(SnmpCollector.NODE_INFO_KEY, m_nodeInfo);
	}

	private void getSysObjId() {
		/*
		 * Prepare & execute the SQL statement to get the node's system
		 * object id (sysoid)
		 */
		PreparedStatement stmt = null;

		try {
			stmt = m_dsConn.prepareStatement(SnmpCollector.SQL_GET_NODESYSOID);
			stmt.setInt(1, m_nodeID); // node ID
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				m_sysoid = rs.getString(1);
			} else {
				m_sysoid = null;
			}
			rs.close();
		} catch (SQLException e) {
			m_collector.log().debug("initialize: SQL exception retrieving the node id",
					e);
			throw new RuntimeException("SQL exception while attempting "
					+ "to retrieve interface's node id", e);
		} catch (NullPointerException e) {
			/*
			 * Thrown by ResultSet.getString() if database query did not
			 * return anything.
		      * XXX this is a bad hack
			 */
			m_collector.log().debug("initialize: NullPointerException", e);
			m_sysoid = null;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				m_collector.log().info(
						"initialize: an error occured trying to close "
								+ "an SQL statement", e);
			}
		}

		if (m_sysoid == null) {
			throw new RuntimeException("System Object ID for interface "
					+ m_ipAddr.getHostAddress()
					+ " does not exist in the database.");
		}

		/*
		 * Our implmentation requires that all sysObjectID's must have a
		 * leading period ('.'). Add the leading period if it is not
		 * present.
		 */
		if (!m_sysoid.startsWith(".")) {
			String period = ".";
			period.concat(m_sysoid);
			m_sysoid = period;
		}
	}

	private void getPrimarySnmpInfo() {
		/*
		 * Prepare & execute the SQL statement to get the 'nodeid' from the
		 * ipInterface table 'nodeid' will be used to retrieve the node's
		 * system object id from the node table. In addition to nodeid, the
		 * interface's ifIndex and isSnmpPrimary fields are also retrieved.
		 */
		PreparedStatement stmt = null;
		try {
			stmt = m_dsConn.prepareStatement(SnmpCollector.SQL_GET_NODEID);
			stmt.setString(1, m_ipAddr.getHostAddress()); // interface
			// address
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				m_nodeID = rs.getInt(1);
				if (rs.wasNull()) {
					m_nodeID = -1;
				}

				m_primaryIfIndex = rs.getInt(2);
				if (rs.wasNull()) {
					m_primaryIfIndex = -1;
				}

				String str = rs.getString(3);
				if (str != null) {
					m_isSnmpPrimary = str.charAt(0);
				}
			} else {
				m_nodeID = -1;
				m_primaryIfIndex = -1;
				m_isSnmpPrimary = DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE;
			}

			rs.close();
		} catch (SQLException e) {
			m_collector.log().debug("initialize: SQL exception", e);
			throw new RuntimeException("SQL exception while attempting "
					+ "to retrieve node id for interface "
					+ m_ipAddr.getHostAddress());
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				// Ignore
			}
		}

		if (m_collector.log().isDebugEnabled()) {
			m_collector.log().debug(
					"initialize: db retrieval info: nodeid = " + m_nodeID
					+ ", address = " + m_ipAddr.getHostAddress()
					+ ", primaryIfIndex = " + m_primaryIfIndex
					+ ", isSnmpPrimary = " + m_isSnmpPrimary);
		}

		/*
		 * RuntimeException is thrown if any of the following are true: -
		 * node id is invalid - primaryIfIndex is invalid - Interface is not
		 * the primary SNMP interface for the node
		 */
		if (m_nodeID == -1) {
			throw new RuntimeException("Unable to retrieve node id "
					+ "for interface " + m_ipAddr.getHostAddress());
		}

		if (m_primaryIfIndex == -1) {
			// allow this for nodes without ipAddrTables
			// throw new RuntimeException("Unable to retrieve ifIndex for
			// interface " + ipAddr.getHostAddress());
			if (m_collector.log().isDebugEnabled()) {
				m_collector.log().debug(
						"initialize: db retrieval info: node " + m_nodeID
						+ " does not have a legitimate "
						+ "primaryIfIndex.  Assume node does not "
						+ "supply ipAddrTable and continue...");
			}
		}

		if (m_isSnmpPrimary != DbIpInterfaceEntry.SNMP_PRIMARY) {
			throw new RuntimeException("Interface "
					+ m_ipAddr.getHostAddress()
					+ " is not the primary SNMP interface for nodeid "
					+ m_nodeID);
		}
	}
	
}