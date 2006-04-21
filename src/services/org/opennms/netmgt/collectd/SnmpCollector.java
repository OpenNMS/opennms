//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// 2005 Jan 03: Added support for lame SNMP hosts
// 2003 Oct 20: Added minval and maxval code for mibObj RRDs
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.utils.AlphaNumeric;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.xml.event.Event;

/**
 * <P>
 * The SnmpCollector class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
final class SnmpCollector implements ServiceCollector {
	private final class IfNumberTracker extends SingleInstanceTracker {
		int m_ifNumber = -1;

		private IfNumberTracker() {
			super(SnmpObjId.get(INTERFACES_IFNUMBER), SnmpInstId.INST_ZERO);
		}

		protected void storeResult(SnmpObjId base, SnmpInstId inst,
				SnmpValue val) {
			m_ifNumber = val.toInt();
		}

		public int getIfNumber() {
			return m_ifNumber;
		}
	}

	/**
	 * Name of monitored service.
	 */
	private static final String SERVICE_NAME = "SNMP";

	/**
	 * The character to replace non-alphanumeric characters in Strings where
	 * needed.
	 */
	static final char nonAnRepl = '_';

	/**
	 * The String of characters which are exceptions for
	 * AlphaNumeric.parseAndReplaceExcept in if Aliases
	 */
	private static final String AnReplEx = "-._";

	/**
	 * Value of MIB-II ifAlias oid
	 */
	private static final String IFALIAS_OID = ".1.3.6.1.2.1.31.1.1.1.18";

	/**
	 * SQL statement to retrieve snmpifaliases and snmpifindexes for a given
	 * node.
	 */
	private static final String SQL_GET_SNMPIFALIASES = "SELECT snmpifindex, snmpifalias "
			+ "FROM snmpinterface "
			+ "WHERE nodeid=? "
			+ "AND snmpifalias != ''";

	/**
	 * SQL statement to retrieve most recent forced rescan eventid for a node.
	 */
	private static final String SQL_GET_LATEST_FORCED_RESCAN_EVENTID = "SELECT eventid "
			+ "FROM events "
			+ "WHERE (nodeid=? OR ipaddr=?) "
			+ "AND eventuei='uei.opennms.org/internal/capsd/forceRescan' "
			+ "ORDER BY eventid DESC " + "LIMIT 1";

	/**
	 * SQL statement to retrieve most recent rescan completed eventid for a
	 * node.
	 */
	private static final String SQL_GET_LATEST_RESCAN_COMPLETED_EVENTID = "SELECT eventid "
			+ "FROM events "
			+ "WHERE nodeid=? "
			+ "AND eventuei='uei.opennms.org/internal/capsd/rescanCompleted' "
			+ "ORDER BY eventid DESC " + "LIMIT 1";

	/**
	 * SQL statement to retrieve interface's 'ipinterface' table information.
	 */
	static final String SQL_GET_NODEID = "SELECT nodeid,ifindex,issnmpprimary "
			+ "FROM ipinterface " + "WHERE ipaddr=? " + "AND ismanaged!='D'";

	/**
	 * SQL statement to retrieve interface's 'issnmpprimary' table information.
	 */
	static final String SQL_GET_ISSNMPPRIMARY = "SELECT ifindex,issnmpprimary "
			+ "FROM ipinterface " + "WHERE nodeid=?";

	/**
	 * /** SQL statement to retrieve node's system object id.
	 */
	static final String SQL_GET_NODESYSOID = "SELECT nodesysoid "
			+ "FROM node " + "WHERE nodeid=? " + "AND nodetype!='D'";

	/**
	 * SQL statement to check for SNMPv2 for a node
	 */
	static final String SQL_CHECK_SNMPV2 = "SELECT ifservices.serviceid "
			+ "FROM service, ifservices "
			+ "WHERE servicename='SNMPv2' "
			+ "AND ifservices.serviceid = service.serviceid " + "AND nodeid=?";

	/**
	 * SQL statement to fetch the ifIndex, ifName, and ifDescr values for all
	 * interfaces associated with a node
	 */
	static final String SQL_GET_SNMP_INFO = "SELECT DISTINCT snmpifindex, snmpiftype, snmpifname, "
			+ "snmpifdescr, snmpphysaddr "
			+ "FROM snmpinterface, ipinterface "
			+ "WHERE ipinterface.nodeid=snmpinterface.nodeid "
			+ "AND ifindex = snmpifindex "
			+ "AND ipinterface.nodeid=? "
			+ "AND (ipinterface.ismanaged!='D')";

	/**
	 * Default object to collect if "oid" property not available. This is the
	 * MIB-II System Object ID value.
	 */
	private static final String DEFAULT_OBJECT_IDENTIFIER = ".1.3.6.1.2.1.1.2";

	/**
	 * Object identifier used to retrieve interface count. This is the MIB-II
	 * interfaces.ifNumber value.
	 */
	private static final String INTERFACES_IFNUMBER = ".1.3.6.1.2.1.2.1";

	/**
	 * Valid values for the 'snmpStorageFlag' attribute in datacollection-config
	 * XML file. "primary" = only primary SNMP interface should be collected and
	 * stored "all" = all primary SNMP interfaces should be collected and stored
	 */
	private static String SNMP_STORAGE_PRIMARY = "primary";

	private static String SNMP_STORAGE_ALL = "all";

	private static String SNMP_STORAGE_SELECT = "select";

	/**
	 * This defines the default maximum number of variables the collector is
	 * permitted to pack into a single outgoing PDU. This value is intentionally
	 * kept relatively small in order to communicate successfully with the
	 * largest possible number of agents.
	 */
	private static int DEFAULT_MAX_VARS_PER_PDU = 30;

	/**
	 * Path to SNMP RRD file repository.
	 */
	private String m_rrdPath;

	/**
	 * Local host name
	 */
	private String m_host;

	/* -------------------------------------------------------------- */
	/* Attribute key names */
	/* -------------------------------------------------------------- */

	/**
	 * Interface attribute key used to store the interface's JoeSNMP SnmpPeer
	 * object.
	 */
	static final String SNMP_PEER_KEY = "org.opennms.netmgt.collectd.SnmpCollector.SnmpPeer";

	/**
	 * Interface attribute key used to store the number of interfaces configured
	 * on the remote host.
	 */
	static final String INTERFACE_COUNT_KEY = "org.opennms.netmgt.collectd.SnmpCollector.ifCount";

	/**
	 * Interface attribute key used to store the map of IfInfo objects which
	 * hold data about each interface on a particular node.
	 */
	static String IF_MAP_KEY = "org.opennms.netmgt.collectd.SnmpCollector.ifMap";

	/**
	 * Interface attribute key used to store a NodeInfo object which holds data
	 * about the node being polled.
	 */
	static String NODE_INFO_KEY = "org.opennms.netmgt.collectd.SnmpCollector.nodeInfo";

	/**
	 * Interface attribute key used to store the data collection scheme to be
	 * followed. Possible values are:
	 * <ul>
	 * <li>SNMP_STORAGE_PRIMARY = "primary"</li>
	 * <li>SNMP_STORAGE_ALL = "all"</li>
	 * <li>SNMP_STORAGE_SELECT = "select"</li>
	 * </ul>
	 */
	static String SNMP_STORAGE_KEY = "org.opennms.netmgt.collectd.SnmpCollector.snmpStorage";

	/**
	 * Interface attribute key used to store configured value for the maximum
	 * number of variables permitted in a single outgoing SNMP PDU request.
	 */
	static String MAX_VARS_PER_PDU_STORAGE_KEY = "org.opennms.netmgt.collectd.SnmpCollector.maxVarsPerPdu";

	/**
	 * Returns the name of the service that the plug-in collects ("SNMP").
	 * 
	 * @return The service that the plug-in collects.
	 */
	public String serviceName() {
		return SERVICE_NAME;
	}

	/**
	 * Initialize the service collector. During initialization the SNMP
	 * collector:
	 * <ul>
	 * <li>Initializes various configuration factories.</li>
	 * <li>Verifies access to the database.</li>
	 * <li>Verifies access to RRD file repository.</li>
	 * <li>Verifies access to JNI RRD shared library.</li>
	 * <li>Determines if SNMP to be stored for only the node's primary
	 * interface or for all interfaces.</li>
	 * </ul>
	 * 
	 * @param parameters
	 *            Not currently used.
	 * @exception RuntimeException
	 *                Thrown if an unrecoverable error occurs that prevents the
	 *                plug-in from functioning.
	 */
	public void initialize(Map parameters) {
		determineLocalHostName();

		// Initialize the SnmpPeerFactory
		initSnmpPeerFactory();

		// Initialize the DataCollectionConfigFactory
		initDataCollectionConfig();

		// Make sure we can connect to the database
		initDatabaseConnectionFactory();

		// Get path to RRD repository
		initializeRrdRepository();

	}

	private void initializeRrdRepository() {
		m_rrdPath = DataCollectionConfigFactory.getInstance()
				.getRrdRepository();
		if (m_rrdPath == null) {
			throw new RuntimeException("Configuration error, failed to "
					+ "retrieve path to RRD repository.");
		}

		/*
		 * TODO: make a path utils class that has the below in it strip the
		 * File.separator char off of the end of the path.
		 */
		if (m_rrdPath.endsWith(File.separator)) {
			m_rrdPath = m_rrdPath.substring(0,
					(m_rrdPath.length() - File.separator.length()));
		}
		if (log().isDebugEnabled()) {
			log().debug(
					"initialize: SNMP RRD file repository path: " + m_rrdPath);
		}

		/*
		 * If the RRD file repository directory does NOT already exist, create
		 * it.
		 */
		File f = new File(m_rrdPath);
		if (!f.isDirectory()) {
			if (!f.mkdirs()) {
				throw new RuntimeException("Unable to create RRD file "
						+ "repository, path: " + m_rrdPath);
			}
		}

		initializeRrdInterface();
	}

	private void initializeRrdInterface() {
		try {
			RrdUtils.initialize();
		} catch (RrdException e) {
			log().error("initialize: Unable to initialize RrdUtils", e);
			throw new RuntimeException("Unable to initialize RrdUtils", e);
		}
	}

	private void initDatabaseConnectionFactory() {
		try {
			DataSourceFactory.init();
		} catch (IOException e) {
			log().fatal("initialize: IOException getting database connection", e);
			throw new UndeclaredThrowableException(e);
		} catch (MarshalException e) {
			log().fatal("initialize: Marshall Exception getting database connection", e);
			throw new UndeclaredThrowableException(e);
		} catch (ValidationException e) {
			log().fatal("initialize: Validation Exception getting database connection", e);
			throw new UndeclaredThrowableException(e);
		} catch (SQLException e) {
			log().fatal("initialize: Failed getting connection to the database.", e);
			throw new UndeclaredThrowableException(e);
		} catch (PropertyVetoException e) {
			log().fatal("initialize: Failed getting connection to the database.", e);
			throw new UndeclaredThrowableException(e);
		} catch (ClassNotFoundException e) {
			log().fatal("initialize: Failed loading database driver.", e);
			throw new UndeclaredThrowableException(e);
		}
	}

	private void initDataCollectionConfig() {
		try {
			DataCollectionConfigFactory.reload();
		} catch (MarshalException e) {
			log().fatal("initialize: Failed to load data collection configuration", e);
			throw new UndeclaredThrowableException(e);
		} catch (ValidationException e) {
			log().fatal("initialize: Failed to load data collection configuration", e);
			throw new UndeclaredThrowableException(e);
		} catch (IOException e) {
			log().fatal("initialize: Failed to load data collection configuration", e);
			throw new UndeclaredThrowableException(e);
		}
	}

	private void initSnmpPeerFactory() {
		try {
			SnmpPeerFactory.init();
		} catch (MarshalException e) {
			log().fatal("initialize: Failed to load SNMP configuration", e);
			throw new UndeclaredThrowableException(e);
		} catch (ValidationException e) {
			log().fatal("initialize: Failed to load SNMP configuration", e);
			throw new UndeclaredThrowableException(e);
		} catch (IOException e) {
			log().fatal("initialize: Failed to load SNMP configuration", e);
			throw new UndeclaredThrowableException(e);
		}
	}

	private void determineLocalHostName() {
		// Get local host name (used when generating threshold events)
		try {
			m_host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log().warn("initialize: Unable to resolve local host name.", e);
			m_host = "unresolved.host";
		}
	}

	/**
	 * Responsible for freeing up any resources held by the collector.
	 */
	public void release() {
		// Nothing to release...
	}
	
	/**
	 * Responsible for performing all necessary initialization for the specified
	 * interface in preparation for data collection.
	 * 
	 * @param iface
	 *            Network interface to be prepped for collection.
	 * @param parameters
	 *            Key/value pairs associated with the package to which the
	 *            interface belongs..
	 */
	public void initialize(CollectionInterface iface, Map parameters) {
		
		Initializer initializer = new Initializer(this, iface, parameters);
		initializer.execute();
	}

	void setMaxVarsPerPdu(NetworkInterface iface, int maxVarsPerPdu) {
		// Add max vars per pdu value as an attribute of the interface
		iface.setAttribute(MAX_VARS_PER_PDU_STORAGE_KEY, new Integer(
				maxVarsPerPdu));
		if (log().isDebugEnabled()) {
			log().debug("initialize: maxVarsPerPdu=" + maxVarsPerPdu);
		}
	}

	int getMaxVarsPerPdu(String collectionName) {
		// Retrieve configured value for max number of vars per PDU
		int maxVarsPerPdu = DataCollectionConfigFactory.getInstance()
				.getMaxVarsPerPdu(collectionName);
		if (maxVarsPerPdu == -1) {
			if (log().isEnabledFor(Priority.WARN)) {
				log().warn(
						"initialize: Configuration error, failed to "
								+ "retrieve max vars per pdu from collection: "
								+ collectionName);
			}
			maxVarsPerPdu = DEFAULT_MAX_VARS_PER_PDU;
		} else if (maxVarsPerPdu == 0) {
			/*
			 * Special case, zero indicates "no limit" on number of vars in a
			 * single PDU...so set maxVarsPerPdu to maximum integer value:
			 * Integer.MAX_VALUE. This is a lot easier than building in special
			 * logic to handle a value of zero. Doubt anyone will attempt to
			 * collect over 2 billion oids.
			 */
			maxVarsPerPdu = Integer.MAX_VALUE;
		}
		return maxVarsPerPdu;
	}

	void setStorageFlag(NetworkInterface iface, String storageFlag) {
		iface.setAttribute(SNMP_STORAGE_KEY, storageFlag);
		if (log().isDebugEnabled()) {
			log().debug("initialize: SNMP storage flag: '" + storageFlag + "'");
		}
	}

	String getStorageFlag(String collectionName) {
		String storageFlag = DataCollectionConfigFactory.getInstance()
				.getSnmpStorageFlag(collectionName);
		if (storageFlag == null) {
			if (log().isEnabledFor(Priority.WARN)) {
				log().warn(
						"initialize: Configuration error, failed to "
								+ "retrieve SNMP storage flag for collection: "
								+ collectionName);
			}
			storageFlag = SNMP_STORAGE_PRIMARY;
		}
		return storageFlag;
	}

	/**
	 * Responsible for releasing any resources associated with the specified
	 * interface.
	 * 
	 * @param iface
	 *            Network interface to be released.
	 */
	public void release(CollectionInterface iface) {
		// Nothing to release...
	}

	/**
	 * Perform data collection.
	 * 
	 * @param iface
	 *            Network interface to be data collected.
	 * @param eproxy
	 *            Eventy proxy for sending events.
	 * @param parameters
	 *            Key/value pairs from the package to which the interface
	 *            belongs.
	 */
	public int collect(CollectionInterface iface, EventProxy eproxy, Map parameters) {
		try {
			// Collect node and interface MIB data from the remote agent

			SnmpNodeCollector nodeCollector = null;
			// construct the nodeCollector
			if (!getNodeInfo(iface).getOidList().isEmpty()) {
				nodeCollector = new SnmpNodeCollector(getInetAddress(iface),
						getNodeInfo(iface).getOidList());
			}

			IfNumberTracker ifNumber = null;
			SnmpIfCollector ifCollector = null;
			// construct the ifCollector
			if (hasInterfaceOids(iface)) {
				ifCollector = new SnmpIfCollector(getInetAddress(iface),
						getIfMap(iface));
				ifNumber = new IfNumberTracker();
			}

			collectData(iface, ifNumber, nodeCollector, ifCollector);

			if (hasInterfaceOids(iface)) {
				int savedIfCount = getSavedIfCount(iface);

				int ifCount = ifNumber.getIfNumber();

				saveIfCount(iface, ifCount);

				log().debug(
						"collect: nodeId: " + getNodeInfo(iface).getNodeId()
								+ " interface: " + getHostAddress(iface)
								+ " ifCount: " + ifCount + " savedIfCount: "
								+ savedIfCount);

				/*
				 * If saved interface count differs from the newly retreived
				 * interface count the following must occur: 1. generate
				 * forceRescan event so Capsd will rescan the node, update the
				 * database, and generate the appropriate events back to the
				 * poller.
				 */
				if ((savedIfCount != -1) && (ifCount != savedIfCount)) {
					if (!isForceRescanInProgress(
							getNodeInfo(iface).getNodeId(),
							getHostAddress(iface))) {
						log()
								.info(
										"Number of interfaces on primary SNMP "
												+ "interface "
												+ getHostAddress(iface)
												+ " has changed, generating 'ForceRescan' event.");
						generateForceRescanEvent(getHostAddress(iface),
								getNodeInfo(iface).getNodeId(), eproxy);
					}
				}
			}

			// Update RRD with values retrieved in SNMP collection
			boolean rrdError = updateRRDs(getCollectionName(parameters), iface,
					nodeCollector, ifCollector, parameters, eproxy);

			if (rrdError) {
				log().warn(
						"collect: RRD error during update for "
								+ getHostAddress(iface));
			}

			// return the status of the collection
			return COLLECTION_SUCCEEDED;
		} catch (CollectionError e) {
			if (e.getCause() == null) {
				log().error(e.getMessage());
			} else {
				log().error(e.getMessage(), e.getCause());
			}
			return COLLECTION_FAILED;
		} catch (CollectionWarning e) {
			if (e.getCause() == null) {
				log().warn(e.getMessage());
			} else {
				log().warn(e.getMessage(), e.getCause());
			}
			return COLLECTION_FAILED;
		} catch (Throwable t) {
			log().error(
					"Unexpected error during node SNMP collection for "
							+ getHostAddress(iface), t);
			return COLLECTION_FAILED;
		}
	}

	private void collectData(NetworkInterface iface,
			CollectionTracker ifNumber, SnmpNodeCollector nodeCollector,
			SnmpIfCollector ifCollector) throws CollectionWarning {
		try {
			InetAddress address = getInetAddress(iface);
			List trackers = new ArrayList(3);

			if (ifNumber != null) {
				trackers.add(ifNumber);
			}
			if (nodeCollector != null) {
				trackers.add(nodeCollector);
			}
			if (ifCollector != null) {
				trackers.add(ifCollector);
			}

			SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
					.getAgentConfig(address);

			// now collect the data
			SnmpWalker walker = SnmpUtils.createWalker(agentConfig,
					"SnmpCollectors for " + address.getHostAddress(),
					(CollectionTracker[]) trackers
							.toArray(new CollectionTracker[trackers.size()]));
			walker.start();

			if (log().isDebugEnabled()) {
				log().debug(
						"collect: successfully instantiated "
								+ "SnmpNodeCollector() for "
								+ getHostAddress(iface));
			}

			// wait for collection to finish
			walker.waitFor();

			if (log().isDebugEnabled()) {
				log().debug(
						"collect: node SNMP query for address "
								+ getHostAddress(iface) + " complete.");
			}

			// Was the node collection successful?
			if (walker.failed()) {
				// Log error and return COLLECTION_FAILED
				throw new CollectionWarning("collect: collection failed for "
						+ getHostAddress(iface));
			}

			setMaxVarsPdu(iface, walker.getMaxVarsPerPdu());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CollectionWarning("collect: Collection of node SNMP "
					+ "data for interface " + getHostAddress(iface)
					+ " interrupted!", e);
		}
	}

	private void setMaxVarsPdu(NetworkInterface iface, int maxVarsPerPdu) {
		iface.setAttribute(MAX_VARS_PER_PDU_STORAGE_KEY, new Integer(
				maxVarsPerPdu));
	}

	private void saveIfCount(NetworkInterface iface, int ifCount) {
		/*
		 * Add the interface count to the interface's attributes for retrieval
		 * during poll()
		 */
		iface.setAttribute(INTERFACE_COUNT_KEY, new Integer(ifCount));
	}

	private int getSavedIfCount(NetworkInterface iface) {
		int savedIfCount = -1;
		Integer tmp = (Integer) iface.getAttribute(INTERFACE_COUNT_KEY);
		if (tmp != null) {
			savedIfCount = tmp.intValue();
		}
		return savedIfCount;
	}

	private boolean hasInterfaceOids(NetworkInterface iface)
			throws CollectionError {
		boolean hasInterfaceOids = false;
		Iterator iter = getIfMap(iface).values().iterator();
		while (iter.hasNext() && !hasInterfaceOids) {
			IfInfo ifInfo = (IfInfo) iter.next();
			if (ifInfo.getType() < 1) {
				continue;
			}
			if (!ifInfo.getOidList().isEmpty()) {
				hasInterfaceOids = true;
			}
		}
		return hasInterfaceOids;
	}

	private Map getIfMap(NetworkInterface iface) throws CollectionError {
		Map ifMap = (Map) iface.getAttribute(IF_MAP_KEY);
		if (ifMap == null) {
			throw new CollectionError("Interface map not available for "
					+ "interface " + getHostAddress(iface));
		}
		return ifMap;
	}

	private NodeInfo getNodeInfo(NetworkInterface iface) throws CollectionError {
		NodeInfo nodeInfo = (NodeInfo) iface.getAttribute(NODE_INFO_KEY);
		if (nodeInfo == null) {
			throw new CollectionError("Node info not available for interface "
					+ getHostAddress(iface));
		}
		return nodeInfo;
	}

	private String getHostAddress(NetworkInterface iface) {
		return getInetAddress(iface).getHostAddress();
	}

	InetAddress getInetAddress(NetworkInterface iface) {

		if (iface.getType() != NetworkInterface.TYPE_IPV4)
			throw new RuntimeException("Unsupported interface type, "
					+ "only TYPE_IPV4 currently supported");



		InetAddress ipaddr = (InetAddress) iface.getAddress();
		return ipaddr;
	}

	String getCollectionName(Map parameters) {
		String collectionName = ParameterMap.getKeyedString(parameters,
				"collection", "default");
		return collectionName;
	}

	/**
	 * This method is responsible for building an RRDTool style 'update' command
	 * which is issued via the RRD JNI interface in order to push the latest
	 * SNMP-collected values into the interface's RRD database.
	 * 
	 * @param collectionName
	 *            SNMP data Collection name from 'datacollection-config.xml'
	 * @param iface
	 *            NetworkInterface object of the interface currently being
	 *            polled
	 * @param nodeCollector
	 *            Node level MIB data collected via SNMP for the polled
	 *            interface
	 * @param ifCollector
	 *            Interface level MIB data collected via SNMP for the polled
	 *            interface
	 * @throws CollectionError
	 * @exception RuntimeException
	 *                Thrown if the data source list for the interface is null.
	 */
	private boolean updateRRDs(String collectionName, NetworkInterface iface,
			SnmpNodeCollector nodeCollector, SnmpIfCollector ifCollector,
			Map parms, EventProxy eproxy) throws CollectionError {
		// Log4j category
		InetAddress ipaddr = getInetAddress(iface);

		// Retrieve SNMP storage attribute
		String snmpStorage = getSnmpStorage(iface);

		// Get primary interface index from NodeInfo object
		NodeInfo nodeInfo = getNodeInfo(iface);
		int nodeId = nodeInfo.getNodeId();
		int primaryIfIndex = nodeInfo.getPrimarySnmpIfIndex();

		// Retrieve interface map attribute
		Map ifMap = getIfMap(iface);

		/*
		 * Write relevant collected SNMP statistics to RRD database First the
		 * node level RRD info will be updated. Secondly the interface level RRD
		 * info will be updated.
		 */
		boolean rrdError = false;

		// Node data
		if (nodeCollector != null) {
			log().debug("updateRRDs: processing node-level collection...");

			/*
			 * Build path to node RRD repository. createRRD() will make the
			 * appropriate directories if they do not already exist.
			 */
			String nodeRepository = m_rrdPath + File.separator
					+ String.valueOf(nodeId);

			SNMPCollectorEntry nodeEntry = nodeCollector.getEntry();

			/*
			 * Iterate over the node datasource list and issue RRD update
			 * commands to update each datasource which has a corresponding
			 * value in the collected SNMP data.
			 */
			Iterator iter = nodeInfo.getDsList().iterator();
			while (iter.hasNext()) {
				DataSource ds = (DataSource) iter.next();

				try {
					String dsVal = getRRDValue(ds, nodeEntry);
					if (dsVal == null) {
						// Do nothing, no update is necessary
						if (log().isDebugEnabled()) {
							log().debug(
									"updateRRDs: Skipping update, no "
											+ "data retrieved for nodeId: "
											+ nodeId + " datasource: "
											+ ds.getName());
						}
					} else {
						// createRRD(collectionName, ipaddr, nodeRepository,
						// ds);
						if (ds.performUpdate(collectionName, ipaddr
								.getHostAddress(), nodeRepository,
								ds.getName(), dsVal)) {
							log().warn(
									"updateRRDs: ds.performUpdate() "
											+ "failed for node: " + nodeId
											+ " datasource: " + ds.getName());
							rrdError = true;
						}
					}
				} catch (IllegalArgumentException e) {
					log().warn("getRRDValue: " + e.getMessage());

					// Set rrdError flag
					rrdError = true;
					log().warn(
							"updateRRDs: call to getRRDValue() failed "
									+ "for node: " + nodeId + " datasource: "
									+ ds.getName());
				}

			} // end while(more datasources)
		} // end if(nodeCollector != null)

		// Interface-specific data
		boolean forceRescan = false;
		boolean rescanPending = false;
		Map SnmpIfAliasMap = new HashMap();

		if (ifCollector != null) {
			String domain = ParameterMap.getKeyedString(parms, "domain",
					"default");
			String storeByNodeID = ParameterMap.getKeyedString(parms,
					"storeByNodeID", "normal");
			String storeByIfAlias = ParameterMap.getKeyedString(parms,
					"storeByIfAlias", "false");
			String storFlagOverride = ParameterMap.getKeyedString(parms,
					"storFlagOverride", "false");
			String ifAliasComment = ParameterMap.getKeyedString(parms,
					"ifAliasComment", null);

			if (log().isDebugEnabled() && storeByIfAlias.equals("true")) {
				log()
						.debug(
								"domain:storeByNodeID:storeByIfAlias:"
										+ "storFlagOverride:ifAliasComment = "
										+ domain + ":" + storeByNodeID + ":"
										+ storeByIfAlias + ":"
										+ storFlagOverride + ":"
										+ ifAliasComment);
			}

			/*
			 * Retrieve list of SNMP collector entries generated for the remote
			 * node's interfaces.
			 */
			List snmpCollectorEntries = ifCollector.getEntries();
			if (snmpCollectorEntries == null
					|| snmpCollectorEntries.size() == 0) {
				log().warn(
						"updateRRDs: No data retrieved for the interface "
								+ ipaddr.getHostAddress());
			}

			// get the snmpIfAliases
			if (isForceRescanInProgress(nodeId, ipaddr.getHostAddress())) {
				rescanPending = true;
			} else {
				SnmpIfAliasMap = getIfAliasesFromDb(nodeId);
			}

			// Iterate over the SNMP collector entries
			Iterator iter = snmpCollectorEntries.iterator();
			while (iter.hasNext()) {
				SNMPCollectorEntry ifEntry = (SNMPCollectorEntry) iter.next();

				int ifIndex = ifEntry.getIfIndex().intValue();
				String ifIdx = Integer.toString(ifIndex);

				// get the ifAlias if one exists
				String aliasVal = getRRDIfAlias(ifIdx, ifEntry);
				if (aliasVal != null && !aliasVal.equals("")) {
					aliasVal = aliasVal.trim();

					/*
					 * Check DB to see if ifAlias is current and flag a forced
					 * rescan if not.
					 */
					if (!rescanPending) {
						if (SnmpIfAliasMap.get(ifIdx) == null
								|| !SnmpIfAliasMap.get(ifIdx).equals(aliasVal)) {
							rescanPending = true;
							forceRescan = true;
							if (log().isDebugEnabled()) {
								log().debug(
										"Forcing rescan.  IfAlias " + aliasVal
												+ " for index " + ifIdx
												+ " does not match DB value: "
												+ SnmpIfAliasMap.get(ifIdx));
							}
						}
					}
					if (ifAliasComment != null) {
						int si = aliasVal.indexOf(ifAliasComment);
						if (si > -1) {
							aliasVal = aliasVal.substring(0, si).trim();
						}
					}
					if (aliasVal != null && !aliasVal.equals("")) {
						aliasVal = AlphaNumeric.parseAndReplaceExcept(aliasVal,
								nonAnRepl, AnReplEx);
					}
				}

				boolean override = true;
				if (storFlagOverride.equals("false") || (aliasVal == null)
						|| aliasVal.equals("")) {
					override = false;
				}
				String byNode = storeByNodeID;

				/*
				 * Are we storing SNMP data for all interfaces or primary
				 * interface only? If only storing for primary interface only
				 * proceed if current ifIndex is equal to the ifIndex of the
				 * primary SNMP interface.
				 */
				if (snmpStorage.equals(SNMP_STORAGE_PRIMARY)) {
					if (ifIndex != primaryIfIndex) {
						if (override) {
							if (log().isDebugEnabled()) {
								log()
										.debug(
												"updateRRDs: storFlagOverride "
														+ "= true. Storing SNMP data for "
														+ "non-primary interface "
														+ ifIdx);
							}
						} else {
							if (log().isDebugEnabled()) {
								log()
										.debug(
												"updateRRDs: only storing "
														+ "SNMP data for primary interface ("
														+ primaryIfIndex
														+ "), skipping ifIndex: "
														+ ifIdx);
							}
							continue;
						}
						if (byNode.equals("normal")) {
							byNode = "false";
						}
					}
				}

				/*
				 * Use ifIndex to lookup the IfInfo object from the interface
				 * map.
				 */
				IfInfo ifInfo = (IfInfo) ifMap.get(new Integer(ifIndex));
				if (ifInfo == null) {
					// no data needed for this interface
					continue;
				}

				if (snmpStorage.equals(SNMP_STORAGE_SELECT)) {
					if (ifInfo.getCollType() == null
							|| ifInfo.getCollType().equals("N")) {
						if (override) {
							if (log().isDebugEnabled()) {
								log()
										.debug(
												"updateRRDs: storFlagOverride "
														+ "= true. Storing SNMP data for "
														+ "interface " + ifIdx
														+ " with CollType = "
														+ ifInfo.getCollType());
							}
						} else {
							if (log().isDebugEnabled()) {
								log()
										.debug(
												"updateRRDs: selectively storing "
														+ "SNMP data for primary interface ("
														+ primaryIfIndex
														+ "), skipping ifIndex: "
														+ ifIdx
														+ " because collType = "
														+ ifInfo.getCollType());
							}
							continue;
						}
						if (byNode.equals("normal")) {
							byNode = "false";
						}
					}
				}
				if (byNode.equals("normal")) {
					byNode = "true";
				}

				if (ifInfo.getDsList() == null) {
					throw new RuntimeException("Data Source list not "
							+ "available for primary IP addr "
							+ ipaddr.getHostAddress() + " and ifIndex "
							+ ifInfo.getIndex());
				}

				/*
				 * Iterate over the interface datasource list and issue RRD
				 * update commands to update each datasource which has a
				 * corresponding value in the collected SNMP data.
				 */
				Iterator i = ifInfo.getDsList().iterator();
				while (i.hasNext()) {
					DataSource ds = (DataSource) i.next();

					/*
					 * Build path to interface RRD repository. createRRD() will
					 * make the appropriate directories if they do not already
					 * exist.
					 */
					String ifRepository = m_rrdPath + File.separator
							+ String.valueOf(nodeId) + File.separator
							+ ifInfo.getLabel();

					try {
						String dsVal = getRRDValue(ds, ifEntry);

						// Build RRD update command
						if (dsVal == null) {
							// Do nothing, no update is necessary
							if (log().isDebugEnabled()) {
								log().debug(
										"updateRRDs: Skipping update, "
												+ "no data retrieved for "
												+ "node/ifindex: " + nodeId
												+ "/" + ifIndex
												+ " datasource: "
												+ ds.getName());
							}
						} else {
							/*
							 * Call createRRD() to create RRD if it doesn't
							 * already exist.
							 */
							// createRRD(collectionName, ipaddr, ifRepository,
							// ds);
							if (byNode.equals("true")) {
								if (ds.performUpdate(collectionName, ipaddr
										.getHostAddress(), ifRepository, ds
										.getName(), dsVal)) {
									log()
											.warn(
													"updateRRDs: "
															+ "ds.performUpdate() failed for "
															+ "node/ifindex: "
															+ nodeId + "/"
															+ ifIndex
															+ " datasource: "
															+ ds.getName());
									rrdError = true;
								}
							}
							if (storeByIfAlias.equals("true")) {
								if ((aliasVal != null) && !aliasVal.equals("")) {
									ifRepository = m_rrdPath + File.separator
											+ domain + File.separator
											+ aliasVal;
									if (ds.performUpdate(collectionName, ipaddr
											.getHostAddress(), ifRepository, ds
											.getName(), dsVal)) {
										log()
												.warn(
														"updateRRDs: "
																+ "ds.performUpdate() failed for "
																+ "node/ifindex/domain/alias: "
																+ nodeId
																+ "/"
																+ ifIndex
																+ "/"
																+ domain
																+ "/"
																+ aliasVal
																+ " datasource: "
																+ ds.getName());
										rrdError = true;
									}
								}
							}

						}
					} catch (IllegalArgumentException e) {
						log().warn("buildRRDUpdateCmd: " + e.getMessage());

						// Set rrdError flag
						rrdError = true;
						log().warn(
								"updateRRDs: call to buildRRDUpdateCmd() "
										+ "failed for node/ifindex: " + nodeId
										+ "/" + ifIndex + " datasource: "
										+ ds.getName());
					}

				} // end while(more datasources)
			} // end while(more SNMP collector entries)
		} // end if(ifCollector != null)

		if (forceRescan) {
			generateForceRescanEvent(ipaddr.getHostAddress(), nodeInfo
					.getNodeId(), eproxy);
		}
		return rrdError;
	}

	private String getSnmpStorage(NetworkInterface iface) {
		String snmpStorage = (String) iface.getAttribute(SNMP_STORAGE_KEY);
		return snmpStorage;
	}

	/**
	 * @param ds
	 * @param collectorEntry
	 * @param log
	 * @param dsVal
	 * @return
	 * @throws Exception
	 */
	public String getRRDValue(DataSource ds, SNMPCollectorEntry collectorEntry)
			throws IllegalArgumentException {
		Category log = log();

		// Make sure we have an actual object id value.
		if (ds.getOid() == null) {
			return null;
		}

		String instance = null;
		if (ds.getInstance().equals(MibObject.INSTANCE_IFINDEX)) {
			instance = collectorEntry.getIfIndex().toString();
		} else {
			instance = ds.getInstance();
		}

		String fullOid = SnmpObjId.get(ds.getOid(), instance).toString();

		SnmpValue snmpVar = collectorEntry.getValue(fullOid);
		if (snmpVar == null) {
			// No value retrieved matching this oid
			return null;
		}

		if (log.isDebugEnabled()) {
			log.debug("issueRRDUpdate: name:oid:value - " + ds.getName() + ":"
					+ fullOid + ":" + snmpVar.toString());
		}

		return ds.getStorableValue(snmpVar);
	}

	/**
	 * @param oid
	 * @param instance
	 * @param collectorEntry
	 * @return
	 * @throws Exception
	 */
	public String getRRDIfAlias(String instance,
			SNMPCollectorEntry collectorEntry) throws IllegalArgumentException {
		Category log = log();
		if (instance.equals("")) {
			return null;
		}

		String fullOid = IFALIAS_OID + "." + instance;

		String snmpVar = collectorEntry.getDisplayString(fullOid);

		if (snmpVar == null || snmpVar.equals("")) {
			// No value retrieved matching this oid
			return null;
		}

		if (log.isDebugEnabled()) {
			log.debug("getRRDIfAlias: ifAlias = " + snmpVar);
		}
		return snmpVar;
	}

	/**
	 * This method is responsible for building a list of RRDDataSource objects
	 * from the provided list of MibObject objects.
	 * 
	 * @param collectionName
	 *            Collection name
	 * @param oidList
	 *            List of MibObject objects defining the oid's to be collected
	 *            via SNMP.
	 * 
	 * @return list of RRDDataSource objects
	 */
	List buildDataSourceList(String collectionName, List oidList) {
		// Log4j category
		Category log = log();

		/*
		 * Retrieve the RRD expansion data source list which contains all the
		 * expansion data source's. Use this list as a basis for building a data
		 * source list for the current interface.
		 */
		List dsList = new LinkedList();

		/*
		 * Loop through the MIB object list to be collected for this interface
		 * and add a corresponding RRD data source object. In this manner each
		 * interface will have RRD files create which reflect only the data
		 * sources pertinent to it.
		 */
		Iterator o = oidList.iterator();
		while (o.hasNext()) {
			MibObject obj = (MibObject) o.next();
			DataSource ds = DataSource.dataSourceForMibObject(obj,
					collectionName);
			if (ds != null) {
				// Add the new data source to the list
				dsList.add(ds);
			} else if (log.isEnabledFor(Priority.WARN)) {
				log.warn("buildDataSourceList: Data type '" + obj.getType()
						+ "' not supported.");
				log.warn("buildDataSourceList: MIB object '" + obj.getAlias()
						+ "' will not be mapped to a data source.");
			}
		}

		return dsList;
	}

	/**
	 * This method is responsible for retrieving an array of ifaliases indexed
	 * by ifindex for the specified node
	 * 
	 * @param int
	 *            nodeID the nodeID of the node being checked
	 */
	private Map getIfAliasesFromDb(int nodeID) {
		Category log = ThreadCategory.getInstance(getClass());
		java.sql.Connection dsConn = null;
		Map ifAliasMap = new HashMap();
		if (log.isDebugEnabled()) {
			log.debug("building ifAliasMap for node " + nodeID);
		}

		try {
			dsConn = DataSourceFactory.getInstance().getConnection();

			PreparedStatement stmt = dsConn
					.prepareStatement(SQL_GET_SNMPIFALIASES);
			stmt.setInt(1, nodeID);
			try {
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					ifAliasMap.put(rs.getString(1), rs.getString(2));
				}
			} catch (SQLException e) {
				throw e;
			}
		} catch (SQLException e) {
			log.error("Failed getting connection to the database.", e);
			throw new UndeclaredThrowableException(e);
		} finally {
			// Done with the database so close the connection
			try {
				dsConn.close();
			} catch (SQLException e) {
				log
						.info("SQLException while closing database connection",
								e);
			}
		}
		return ifAliasMap;
	}

	/**
	 * This method is responsible for determining if a forced rescan has been
	 * started, but is not yet complete for the given nodeID
	 * 
	 * @param int
	 *            nodeID the nodeID of the node being checked
	 */
	private boolean isForceRescanInProgress(int nodeID, String addr) {
		Category log = ThreadCategory.getInstance(getClass());
		java.sql.Connection dsConn = null;
		boolean force = true;

		try {
			dsConn = DataSourceFactory.getInstance().getConnection();

			PreparedStatement stmt1 = dsConn
					.prepareStatement(SQL_GET_LATEST_FORCED_RESCAN_EVENTID);
			PreparedStatement stmt2 = dsConn
					.prepareStatement(SQL_GET_LATEST_RESCAN_COMPLETED_EVENTID);
			stmt1.setInt(1, nodeID);
			stmt1.setString(2, addr);
			stmt2.setInt(1, nodeID);
			try {
				// Issue database query
				ResultSet rs1 = stmt1.executeQuery();
				if (rs1.next()) {
					int forcedRescanEventId = rs1.getInt(1);
					try {
						ResultSet rs2 = stmt2.executeQuery();
						if (rs2.next()) {
							if (rs2.getInt(1) > forcedRescanEventId) {
								force = false;
							} else {
								if (log.isDebugEnabled()) {
									log.debug("Rescan already pending on "
											+ "node " + nodeID);
								}
							}
						}
					} catch (SQLException e) {
						throw e;
					} finally {
						stmt2.close();
					}
				} else {
					force = false;
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				stmt1.close();
			}
		} catch (SQLException e) {
			log.error("Failed getting connection to the database.", e);
			throw new UndeclaredThrowableException(e);
		} finally {
			// Done with the database so close the connection
			try {
				dsConn.close();
			} catch (SQLException e) {
				log
						.info("SQLException while closing database connection",
								e);
			}
		}
		return force;
	}

	/**
	 * This method is responsible for building a Capsd forceRescan event object
	 * and sending it out over the EventProxy.
	 * 
	 * @param ifAddress
	 *            interface address to which this event pertains
	 * @param eventProxy
	 *            proxy over which an event may be sent to eventd
	 */
	private void generateForceRescanEvent(String ifAddress, int nodeId,
			EventProxy eventProxy) {
		// Log4j category
		Category log = log();

		if (log.isDebugEnabled()) {
			log.debug("generateForceRescanEvent: interface = " + ifAddress);
		}

		// create the event to be sent
		Event newEvent = new Event();

		newEvent.setUei(EventConstants.FORCE_RESCAN_EVENT_UEI);

		newEvent.setSource("SNMPServiceMonitor");

		newEvent.setInterface(ifAddress);

		newEvent.setService(SERVICE_NAME);

		if (m_host != null) {
			newEvent.setHost(m_host);
		}

		newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

		newEvent.setNodeid(nodeId);

		// Send event via EventProxy
		try {
			eventProxy.send(newEvent);
		} catch (EventProxyException e) {
			log.error("generateForceRescanEvent: Unable to send "
					+ "forceRescan event.", e);
		}
	}

	Category log() {
		return ThreadCategory.getInstance(SnmpCollector.class);
	}
}
