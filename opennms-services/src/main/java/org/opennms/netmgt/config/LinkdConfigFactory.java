//
//Copyright (C) 2002 Sortova Consulting Group, Inc. All rights reserved.
//Parts Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//   OpenNMS Licensing <license@opennms.org>
//   http://www.opennms.org/
//   http://www.sortova.com/
//
package org.opennms.netmgt.config;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Category;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.*;
import org.opennms.netmgt.config.linkd.*;
import org.opennms.netmgt.linkd.LinkableNode;
import org.opennms.netmgt.linkd.SnmpCollection;

import org.opennms.protocols.snmp.SnmpObjectId;

public class LinkdConfigFactory {

	/**
	 * Singleton instance
	 */
	private static LinkdConfigFactory instance;

	/**
	 * Object containing all Linkd-configuration objects parsed from the xml
	 * file
	 */
	protected static LinkdConfiguration m_linkdconfiguration;

	/**
	 * Input stream for the general Linkd configuration xml
	 */
	protected static InputStream configIn;

	/**
	 * Boolean indicating if the init() method has been called
	 */
	private static boolean initialized = false;

	/**
	 * The Linkd Location Configuration File
	 */
	private static File m_linkdConfFile;

	/**
	 * The HashMap that associates the snmp primary ip address to Linkable Snmp
	 * nodes
	 */
	private static HashMap<String,LinkableNode> snmpprimaryip2nodes;

	/**
	 * The HashMap that associates the snmp primary ip address to Linkable
	 * SnmpCollection
	 *  
	 */
	private static HashMap<String,SnmpCollection> snmpprimaryip2colls;

	/**
	 * The HashMap that associates the OIDS masks to class name
	 */
	private static HashMap<String,String> oidMask2className;

	/**
	 * The boolean indicating if class name where loaded
	 */
	private static boolean classNameLoaded = false;

	/**
	 * The boolean indicating if hash where loaded
	 */
	private static boolean hashLoaded = false;

	/**
	 * query to select SNMP nodes
	 */
	private static final String SQL_SELECT_SNMP_NODES = "SELECT node.nodeid, nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND issnmpprimary = 'P'";

	/**
	 * update status to D on node maked as Deleted on table Nodes
	 */

	private static final String SQL_UPDATE_ATINTERFACE_D = "UPDATE atinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D' ";

	private static final String SQL_UPDATE_STPNODE_D = "UPDATE stpnode set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

	private static final String SQL_UPDATE_STPINTERFACE_D = "UPDATE stpinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

	private static final String SQL_UPDATE_IPROUTEINTERFACE_D = "UPDATE iprouteinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

	private static final String SQL_UPDATE_DATALINKINTERFACE_D = "UPDATE datalinkinterface set status = 'D' WHERE (nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) OR nodeparentid IN (SELECT nodeid from node WHERE nodetype = 'D' )) AND status <> 'D'";

	private LinkdConfigFactory() {
	}

	static synchronized public LinkdConfigFactory getInstance() {
		if (!initialized)
			return null;

		if (instance == null) {
			instance = new LinkdConfigFactory();
		}

		return instance;
	}

	/**
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws MarshalException
	 * @throws ValidationException
	 * @throws ClassNotFoundException
	 */

	public static synchronized void init() throws IOException,
			FileNotFoundException, MarshalException, ValidationException,
			ClassNotFoundException {

		snmpprimaryip2nodes = new HashMap<String,LinkableNode>();
		snmpprimaryip2colls = new HashMap<String,SnmpCollection>();
		oidMask2className = new HashMap<String,String>();

		File cfgFile = ConfigFileConstants
				.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);

		ThreadCategory.getInstance(LinkdConfigFactory.class).debug(
				"init: config file path: " + cfgFile.getPath());

		if (!initialized) {
			reload();
			initialized = true;
		}

	}

	public static synchronized void reload() throws IOException,
			MarshalException, ValidationException {
		m_linkdConfFile = ConfigFileConstants
				.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
		InputStream configIn = new FileInputStream(m_linkdConfFile);

		m_linkdconfiguration = (LinkdConfiguration) Unmarshaller.unmarshal(
				LinkdConfiguration.class, new InputStreamReader(configIn));
	}

	/**
	 * 
	 * @return Int Initial Sleep Time
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public long getInitialSleepTime() throws IOException, MarshalException,
			ValidationException {

		updateFromFile();

		long initialSleepTime = 1800000;

		if (m_linkdconfiguration.hasInitial_sleep_time()) {
			initialSleepTime = m_linkdconfiguration.getInitial_sleep_time();
		}

		return initialSleepTime;
	}

	/**
	 * 
	 * @return Int SnmpPollInterval
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public long getSnmpPollInterval() throws IOException, MarshalException,
			ValidationException {

		updateFromFile();

		long snmppollinterval = 900000;

		if (m_linkdconfiguration.hasSnmp_poll_interval()) {
			snmppollinterval = m_linkdconfiguration.getSnmp_poll_interval();
		}

		return snmppollinterval;
	}

	/**
	 * 
	 * @return Int DiscoveryLinkInterval
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public long getDiscoveryLinkInterval() throws IOException,
			MarshalException, ValidationException {

		updateFromFile();

		long discoverylinkinterval = 3600000;

		if (m_linkdconfiguration.hasSnmp_poll_interval()) {
			discoverylinkinterval = m_linkdconfiguration
					.getDiscovery_link_interval();
		}

		return discoverylinkinterval;
	}

	/**
	 * 
	 * @return Int Threads
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public int getThreads() throws IOException, MarshalException,
			ValidationException {

		updateFromFile();

		int threads = 5;

		if (m_linkdconfiguration.hasThreads()) {
			threads = m_linkdconfiguration.getThreads();
		}

		return threads;
	}

	/**
	 * 
	 * @return boolean auto-discovery
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public boolean autoDiscovery() throws IOException, MarshalException,
			ValidationException {

		updateFromFile();

		boolean autodiscovery = true; 
		if (m_linkdconfiguration.hasAutoDiscovery()) {
			autodiscovery = m_linkdconfiguration.getAutoDiscovery();
		}

		return autodiscovery;
	}

	/**
	 * 
	 * @return boolean enable-vlan-discovery
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	private boolean enableVlanDiscovery() throws IOException, MarshalException,
			ValidationException {

		updateFromFile();

		boolean vlandiscovery = true; 
		if (m_linkdconfiguration.hasEnableVlanDiscovery()) {
			vlandiscovery = m_linkdconfiguration.getEnableVlanDiscovery();
		}

		return vlandiscovery;
	}

	public HashMap<String,SnmpCollection> getSnmpColls(Connection dbConn) throws SQLException,
			UnknownHostException {
		if (!hashLoaded)
			getNodesInfo(dbConn);
		return snmpprimaryip2colls;
	}

	public HashMap<String,LinkableNode> getLinkableNodes(Connection dbConn) throws SQLException,
			UnknownHostException {
		if (!hashLoaded)
			getNodesInfo(dbConn);
		return snmpprimaryip2nodes;
	}

	public synchronized void saveCurrent() throws MarshalException,
			ValidationException, IOException, ClassNotFoundException {

		//marshall to a string first, then write the string to the file. This
		// way the original config
		//isn't lost if the xml from the marshall is hosed.
		StringWriter stringWriter = new StringWriter();
		Marshaller.marshal(m_linkdconfiguration, stringWriter);
		if (stringWriter.toString() != null) {
			FileWriter fileWriter = new FileWriter(m_linkdConfFile);
			fileWriter.write(stringWriter.toString());
			fileWriter.flush();
			fileWriter.close();
		}

		reload();
	}

	private static void updateFromFile() throws IOException, MarshalException,
			ValidationException {
		reload();
	}

	private void getClassNames() throws IOException, MarshalException,
			ValidationException {

		Category log = ThreadCategory.getInstance(LinkdConfigFactory.class);
		if (classNameLoaded)
			return;

		updateFromFile();
		oidMask2className.clear();
		List<String> excludedOids = new ArrayList<String>();

		Vendor[] vendors = m_linkdconfiguration.getVlans().getVendor();
		for (int i = 0; i < vendors.length; i++) {
			SnmpObjectId curRootSysOid = new SnmpObjectId(vendors[i]
					.getSysoidRootMask());
			String curClassName = vendors[i].getClassName();

			String[] specifics = vendors[i].getSpecific();
			for (int js = 0; js < specifics.length; js++) {
				SnmpObjectId oidMask = new SnmpObjectId(specifics[js]);
				oidMask.prepend(curRootSysOid);
				oidMask2className.put(oidMask.toString(), curClassName);
				if (log.isDebugEnabled())
					log.debug("getClassNames:  adding class " + curClassName
							+ " for oid " + oidMask.toString());

			}

			ExcludeRange[] excludeds = vendors[i].getExcludeRange();
			for (int je = 0; je < excludeds.length; je++) {
				SnmpObjectId snmpBeginOid = new SnmpObjectId(excludeds[je]
						.getBegin());
				SnmpObjectId snmpEndOid = new SnmpObjectId(excludeds[je]
						.getEnd());
				SnmpObjectId snmpRootOid = getRootOid(snmpBeginOid);
				if (snmpBeginOid.getLength() == snmpEndOid.getLength()
						&& snmpRootOid.isRootOf(snmpEndOid)) {
					SnmpObjectId snmpCurOid = new SnmpObjectId(snmpBeginOid);
					while (snmpCurOid.compare(snmpEndOid) <= 0) {
						excludedOids.add(snmpCurOid.toString());
						if (log.isDebugEnabled())
							log.debug("getClassNames:  signing excluded class "
									+ curClassName
									+ " for oid "
									+ curRootSysOid.toString().concat(
											snmpCurOid.toString()));
						int lastCurCipher = snmpCurOid.getLastIdentifier();
						lastCurCipher++;
						int[] identifiers = snmpCurOid.getIdentifiers();
						identifiers[identifiers.length - 1] = lastCurCipher;
						snmpCurOid.setIdentifiers(identifiers);
					}
				}
			}

			IncludeRange[] includeds = vendors[i].getIncludeRange();
			for (int ji = 0; ji < includeds.length; ji++) {
				SnmpObjectId snmpBeginOid = new SnmpObjectId(includeds[ji]
						.getBegin());
				SnmpObjectId snmpEndOid = new SnmpObjectId(includeds[ji]
						.getEnd());
				SnmpObjectId rootOid = getRootOid(snmpBeginOid);
				if (snmpBeginOid.getLength() == snmpEndOid.getLength()
						&& rootOid.isRootOf(snmpEndOid)) {
					SnmpObjectId snmpCurOid = new SnmpObjectId(snmpBeginOid);
					while (snmpCurOid.compare(snmpEndOid) <= 0) {
						if (!excludedOids.contains(snmpBeginOid.toString())) {
							SnmpObjectId oidMask = new SnmpObjectId(
									snmpBeginOid);
							oidMask.prepend(curRootSysOid);
							oidMask2className.put(oidMask.toString(),
									curClassName);
							if (log.isDebugEnabled())
								log.debug("getClassNames:  adding class "
										+ curClassName + " for oid "
										+ oidMask.toString());
						}
						int lastCipher = snmpBeginOid.getLastIdentifier();
						lastCipher++;
						int[] identifiers = snmpBeginOid.getIdentifiers();
						identifiers[identifiers.length - 1] = lastCipher;
						snmpCurOid.setIdentifiers(identifiers);
					}
				}
			}
		}
		classNameLoaded = true;
	}

	private void getNodesInfo(Connection dbConn) throws SQLException,
			UnknownHostException {

		Category log = ThreadCategory.getInstance(LinkdConfigFactory.class);

		snmpprimaryip2nodes.clear();
		snmpprimaryip2colls.clear();

		try {
			if (!classNameLoaded)
				getClassNames();
		} catch (Throwable t) {
			log.error("getNodesInfo: cannot find vlan hash class " + t);
		}

		PreparedStatement ps = dbConn.prepareStatement(SQL_SELECT_SNMP_NODES);

		ResultSet rs = ps.executeQuery();
		if (log.isDebugEnabled())
			log
					.debug("getNodesInfo: execute query: \" "
							+ SQL_SELECT_SNMP_NODES + "\"");

		while (rs.next()) {
			int nodeid = rs.getInt("nodeid");
			String ipaddr = rs.getString("ipaddr");
			String sysoid = rs.getString("nodesysoid");
			if (sysoid == null)
				sysoid = "-1";
			if (log.isDebugEnabled())
				log.debug("getNodesInfo: found node element: nodeid " + nodeid
						+ " ipaddr " + ipaddr + " sysoid " + sysoid);

			LinkableNode node = new LinkableNode(nodeid, ipaddr);
			snmpprimaryip2nodes.put(ipaddr, node);

			SnmpCollection coll = new SnmpCollection(SnmpPeerFactory
					.getInstance().getAgentConfig(InetAddress.getByName(ipaddr)));

			try {
				if (enableVlanDiscovery() && hasClassName(sysoid)) {
					coll.setVlanClass(getClassName(sysoid));
					if (log.isDebugEnabled())
						log.debug("getNodesInfo: found class to get Vlans: "
								+ coll.getVlanClass());
				} else {
					if (log.isDebugEnabled())
						log.debug("getNodesInfo: no class found to get Vlans or VlanDiscoveryDisabled ");
				}
			} catch (Throwable t) {
					log
							.error("getNodesInfo: Failed to load vlan classes from linkd configuration file "
									+ t);
			}
			snmpprimaryip2colls.put(ipaddr, coll);

		}

		rs.close();
		ps.close();

		if (log.isDebugEnabled())
			log.debug("getNodesInfo: found " + snmpprimaryip2nodes.size()

			+ " snmp primary ip nodes");
		hashLoaded = true;
	}

	public SnmpCollection getSnmpCollection(Connection dbConn, int nodeid)
			throws SQLException, UnknownHostException {

		Category log = ThreadCategory.getInstance(getClass());

		try {
			if (!classNameLoaded)
				getClassNames();
		} catch (Throwable t) {
			log.error("getSnmpCollection: cannot find vlan hash class " + t);
		}

		SnmpCollection coll = null;

		/**
		 * Query to select info for specific node
		 */
		String SQL_SELECT_SNMP_NODE = "SELECT nodesysoid, ipaddr FROM node "
				+ "LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid "
				+ "WHERE node.nodeid = ? AND nodetype = 'A' AND issnmpprimary = 'P'";

		PreparedStatement stmt = dbConn.prepareStatement(SQL_SELECT_SNMP_NODE);
		stmt.setInt(1, nodeid);
		if (log.isDebugEnabled())
			log.debug("getSnmpCollection: execute '" + SQL_SELECT_SNMP_NODE + "' with nodeid ="+nodeid);

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			String sysoid = rs.getString("nodesysoid");
			if (sysoid == null)
				sysoid = "-1";
			String ipaddr = rs.getString("ipaddr");
			if (log.isDebugEnabled())
				log.debug("getSnmpCollection: found nodeid "
						+ nodeid + " ipaddr " + ipaddr + " sysoid " + sysoid);

			coll = new SnmpCollection(SnmpPeerFactory.getInstance().getAgentConfig(
					InetAddress.getByName(ipaddr)));

			try {
				if (enableVlanDiscovery() && hasClassName(sysoid)) {
					coll.setVlanClass(getClassName(sysoid));
					if (log.isDebugEnabled())
						log
								.debug("getSnmpCollection: found class to get Vlans: "
										+ coll.getVlanClass());
				} else {
					if (log.isDebugEnabled())
						log
								.debug("getSnmpCollection: no class found to get Vlans");
				}
			} catch (Throwable t) {
					log
							.error("getSnmpCollection: Failed to load vlan classes from linkd configuration file "
									+ t);
			}

		}
		rs.close();
		stmt.close();

		return coll;

	}

	public InetAddress getSnmpPrimaryIp(Connection dbConn, int nodeid)
			throws SQLException, UnknownHostException {

		Category log = ThreadCategory.getInstance(getClass());

		/**
		 * Query to select info for specific node
		 */
		String SQL_SELECT_SNMP_NODE = "SELECT ipaddr FROM node "
				+ "LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid "
				+ "WHERE node.nodeid = ? AND issnmpprimary = 'P'";

		String ipaddr = null;
		PreparedStatement stmt = dbConn.prepareStatement(SQL_SELECT_SNMP_NODE);
		stmt.setInt(1, nodeid);
		if (log.isDebugEnabled())
			log.debug("getSnmpPrimaryIp: SQL statement = " + stmt.toString());

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			ipaddr = rs.getString("ipaddr");
			if (ipaddr == null)
				return null;
			if (log.isDebugEnabled())
				log.debug("getSnmpPrimaryIp: found node element: nodeid " + nodeid
						+ " ipaddr " + ipaddr);


		}
		rs.close();
		stmt.close();

		return 	InetAddress.getByName(ipaddr);

	}

	public void updateDeletedNodes(Connection dbConn) throws SQLException {

		Category log = ThreadCategory.getInstance(LinkdConfigFactory.class);

		// update atinterface
		int i = 0;
		PreparedStatement ps = dbConn
				.prepareStatement(SQL_UPDATE_ATINTERFACE_D);
		i = ps.executeUpdate();
		if (log.isInfoEnabled()) {
			log.info("updateDeletedNodes: execute '" + SQL_UPDATE_ATINTERFACE_D
					+ "' updated rows: " + i);
		}

		// update stpnode
		ps = dbConn.prepareStatement(SQL_UPDATE_STPNODE_D);
		i = ps.executeUpdate();
		if (log.isInfoEnabled()) {
			log.info("updateDeletedNodes: execute '" + SQL_UPDATE_STPNODE_D + "' updated rows: "
					+ i);
		}

		// update stpinterface
		ps = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE_D);
		i = ps.executeUpdate();
		if (log.isInfoEnabled()) {
			log.info("updateDeletedNodes: execute '" + SQL_UPDATE_STPINTERFACE_D
					+ "' updated rows: " + i);
		}

		// update iprouteinterface
		ps = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE_D);
		i = ps.executeUpdate();
		if (log.isInfoEnabled()) {
			log.info("updateDeletedNodes: execute '" + SQL_UPDATE_IPROUTEINTERFACE_D
					+ "'updated rows: " + i);
		}

		// update datalinkinterface
		ps = dbConn.prepareStatement(SQL_UPDATE_DATALINKINTERFACE_D);
		i = ps.executeUpdate();
		if (log.isInfoEnabled()) {
			log.info("updateDeletedNodes: execute '" + SQL_UPDATE_DATALINKINTERFACE_D
					+ "' updated rows: " + i);
		}

	}

	private String getClassName(String sysoid) {

		String defaultClassName = null;
		Set ks = oidMask2className.keySet();
		Iterator ite = ks.iterator();
		while (ite.hasNext()) {
			String oidMask = (String) ite.next();
			if (sysoid.startsWith(oidMask)) {
				return (String) oidMask2className.get(oidMask);
			}
		}

		return defaultClassName;

	}

	private boolean hasClassName(String sysoid) {

		Set ks = oidMask2className.keySet();
		Iterator ite = ks.iterator();
		while (ite.hasNext()) {
			String oidMask = (String) ite.next();
			if (sysoid.startsWith(oidMask)) {
				return true;
			}
		}

		return false;
	}

	private SnmpObjectId getRootOid(SnmpObjectId snmpObj) {
		int[] identifiers = snmpObj.getIdentifiers();
		int[] rootIdentifiers = new int[identifiers.length - 1];
		for (int i = 0; i < identifiers.length - 1; i++) {
			rootIdentifiers[i] = identifiers[i];
		}
		return new SnmpObjectId(rootIdentifiers);

	}
}