//
//Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
//Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//   OpenNMS Licensing       <license@opennms.org>
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
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.*;
import org.opennms.netmgt.config.linkd.*;
import org.opennms.netmgt.linkd.LinkableSnmpNode;


public class LinkdConfigFactory {

	private static final String LOG4J_CATEGORY = "OpenNMS.Linkd";

	/**
	 * Singleton instance
	 */
	private static LinkdConfigFactory instance;

	/**
	 * Object containing all Buildings and Room objects parsed from the xml file
	 */
	protected static LinkdConfiguration m_linkdconfiguration;

	/**
	 * Input stream for the general Asset Location configuration xml
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
	 * The HashMap that associates the sysoidmask to vlanoid
	 */
	private static HashMap sysOidMask2vlanOid;
	
	/**
	 * The HashMap that associates the snmp primary ip addressto Linkable Snmp nodes
	 */
	private static HashMap snmpprimaryip2nodes;

	/**
	 * queries to select SNMP nodes
	 */
	private static final String SQL_SELECT_SNMP_NODES_1 = "SELECT node.nodeid, nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND issnmpprimary = 'P'";

	private static final String SQL_SELECT_SNMP_NODES_2 = "SELECT node.nodeid, nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND node.nodeid NOT IN (SELECT DISTINCT(nodeid) FROM ipinterface WHERE issnmpprimary = 'P') AND node.nodeid IN (SELECT DISTINCT(nodeid) FROM snmpinterface) AND ipaddr <> '0.0.0.0'";

	/**
	 * update status to D on node maked as Deleted on table Nodes
	 */

	private static final String SQL_UPDATE_ATINTERFACE_D = "UPDATE atinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D' ";

	private static final String SQL_UPDATE_STPNODE_D = "UPDATE stpnode set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

	private static final String SQL_UPDATE_STPINTERFACE_D = "UPDATE stpinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

	private static final String SQL_UPDATE_IPROUTEINTERFACE_D = "UPDATE iprouteinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

	private static final String SQL_UPDATE_DATALINKINTERFACE_D = "UPDATE datalinkinterface set status = 'D' WHERE (nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) OR nodeparentid IN (SELECT nodeid from node WHERE nodetype = 'D' )) AND status <> 'D'";
	
	/**
	 * Query to select info for specific node
	 */
	private static final String SQL_SELECT_SNMP_NODE = "SELECT node.nodeid, nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodeid = ? AND nodetype = 'A' AND issnmpprimary = 'P'";



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

	public static synchronized void init()
		throws
			IOException,
			FileNotFoundException,
			MarshalException,
			ValidationException,
			ClassNotFoundException {
		
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();

		sysOidMask2vlanOid = new HashMap();
		snmpprimaryip2nodes = new HashMap();

        File cfgFile = ExtendedConfigFileConstants.getFile(ExtendedConfigFileConstants.LINKD_CONF_FILE_NAME);

        ThreadCategory.getInstance(LinkdConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());

		if (!initialized) {
			reload();
			initialized = true;
		}
		
	}

	public static synchronized void reload()
		throws IOException, MarshalException, ValidationException {
		m_linkdConfFile =
			ExtendedConfigFileConstants.getFile(
			        ExtendedConfigFileConstants.LINKD_CONF_FILE_NAME);
		InputStream configIn = new FileInputStream(m_linkdConfFile);

		m_linkdconfiguration =
			(LinkdConfiguration) Unmarshaller.unmarshal(
				LinkdConfiguration.class,
				new InputStreamReader(configIn));
	}

	/**
	 * 
	 * @return Int Initial Sleep Time
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public long getInitialSleepTime()
		throws IOException, MarshalException, ValidationException {

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

	public long getSnmpPollInterval()
		throws IOException, MarshalException, ValidationException {

		updateFromFile();

		long snmppollinterval = 900000;

		if (m_linkdconfiguration.hasSnmp_poll_interval()) {
			snmppollinterval = m_linkdconfiguration.getSnmp_poll_interval();
		}

		return snmppollinterval;
	}

	public long getDiscoveryLinkInterval()
	throws IOException, MarshalException, ValidationException {

	updateFromFile();

	long discoverylinkinterval = 3600000;

	if (m_linkdconfiguration.hasSnmp_poll_interval()) {
		discoverylinkinterval = m_linkdconfiguration.getDiscovery_link_interval();
	}

	return discoverylinkinterval;
}

	
	public int getThreads()
	throws IOException, MarshalException, ValidationException {

	updateFromFile();

	int threads = 5;

	if (m_linkdconfiguration.hasThreads()) {
		threads = m_linkdconfiguration.getThreads();
	}

	return threads;
}


	
	public HashMap getVlanOidFromSysOidMask()
	throws IOException, MarshalException, ValidationException {

	updateFromFile();
    Vendor[] vendors = m_linkdconfiguration.getVlans().getVendor();
	for (int i=0; i < vendors.length;i++) {
 		sysOidMask2vlanOid.put((String)vendors[i].getSysoidMask(),(String)vendors[i].getVlanoid());
	}

	return sysOidMask2vlanOid;
}

	public synchronized void saveCurrent()
		throws
			MarshalException,
			ValidationException,
			IOException,
			ClassNotFoundException {

		//marshall to a string first, then write the string to the file. This way the original config
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

	private static void updateFromFile()
		throws IOException, MarshalException, ValidationException {
				reload();
		}
	
	public HashMap getLinkableSnmpNodes(Connection dbConn) throws SQLException, UnknownHostException {

        Category log = ThreadCategory.getInstance();

		snmpprimaryip2nodes.clear();

		try {
			sysOidMask2vlanOid = getVlanOidFromSysOidMask();
		} catch (Throwable t) {
			if (log.isEnabledFor(Priority.WARN)) {
				log.warn("getLinkableSnmpNodes: Failed to load sysoidmash2vlanoid from linkd configuration file " + t);
			}
		}
		Set ks = sysOidMask2vlanOid.keySet();

		PreparedStatement ps = dbConn.prepareStatement(SQL_SELECT_SNMP_NODES_1);

		ResultSet rs = ps.executeQuery();
		if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: searching snmp primary ip nodes with query " + SQL_SELECT_SNMP_NODES_1);

		while (rs.next()) {
			int nodeid = rs.getInt("nodeid");
			String ipaddr = rs.getString("ipaddr");
			String sysoid = rs.getString("nodesysoid");
			if (sysoid == null) sysoid = "-1";
			LinkableSnmpNode node = new LinkableSnmpNode(nodeid, ipaddr, sysoid);
			node.setSnmpPeer(SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName(ipaddr)));
			Iterator it = ks.iterator();
			while (it.hasNext()) {
				String sysoidmask = (String) it.next();
				if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: searching vlanoid for node/sysoid " + nodeid + "/" + sysoid + "; matching sys oid mask: "+ sysoidmask );
				if (node.getSysOid().startsWith(sysoidmask)) {
					node
							.setVlanOid((String) sysOidMask2vlanOid
									.get(sysoidmask));
					if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: found vlanoid: " + node.getVlanOid() + " for node " + nodeid);
					break;
				}
			}
			snmpprimaryip2nodes.put(ipaddr, node);
		}

		rs.close();
		ps.close();

		int firstQuerySize = snmpprimaryip2nodes.size();
		if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: found " + firstQuerySize + " snmp primary ip nodes with previus query");

		//select snmp device active but no polled		
		ps = dbConn.prepareStatement(SQL_SELECT_SNMP_NODES_2);

		rs = ps.executeQuery();
		if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: searching snmp primary ip nodes with query " + SQL_SELECT_SNMP_NODES_2);
		while (rs.next()) {
			int nodeid = rs.getInt("nodeid");
			String ipaddr = rs.getString("ipaddr");
			String sysoid = rs.getString("nodesysoid");
			if (sysoid == null) sysoid = "-1";
			LinkableSnmpNode node = new LinkableSnmpNode(nodeid, ipaddr, sysoid);
			node.setSnmpPeer(SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName(ipaddr)));
			Iterator it = ks.iterator();
			while (it.hasNext()) {
				String sysoidmask = (String) it.next();
				if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: searching vlanoid for node/sysoid " + nodeid + "/" + sysoid + "; matching sys oid mask: "+ sysoidmask );
				if (node.getSysOid().startsWith(sysoidmask)) {
					node
							.setVlanOid((String) sysOidMask2vlanOid
									.get(sysoidmask));
					if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: found vlanoid: " + node.getVlanOid() + " for node " + nodeid);
					break;
				}
			}
			snmpprimaryip2nodes.put(ipaddr, node);
		}

		rs.close();
		ps.close();
		int secondQuerySize = snmpprimaryip2nodes.size() - firstQuerySize;

		if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: found " + secondQuerySize + " snmp primary ip nodes with previus query");
		if(log.isDebugEnabled()) log.debug("getLinkableSnmpNodes: globally " + snmpprimaryip2nodes.size() + " snmp primary ip nodes");
		return snmpprimaryip2nodes;
		
	}
	
	public void updateDeletedNodes(Connection dbConn) throws SQLException {

        Category log = ThreadCategory.getInstance();

		// update atinterface
		int i = 0;
		PreparedStatement ps = dbConn.prepareStatement(SQL_UPDATE_ATINTERFACE_D);
		i = ps.executeUpdate();
		if (log.isEnabledFor(Priority.WARN)) {
			log.warn("updateDeletedNodes: " + SQL_UPDATE_ATINTERFACE_D + "rows: " + i);
		}

		// update stpnode
		ps = dbConn.prepareStatement(SQL_UPDATE_STPNODE_D);
		i = ps.executeUpdate();
		if (log.isEnabledFor(Priority.WARN)) {
			log.warn("updateDeletedNodes: " + SQL_UPDATE_STPNODE_D + "rows: " + i);
		}

		// update stpinterface
		ps = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE_D);
		i = ps.executeUpdate();
		if (log.isEnabledFor(Priority.WARN)) {
			log.warn("updateDeletedNodes: " + SQL_UPDATE_STPINTERFACE_D + "rows: " + i);
		}

		// update iprouteinterface
		ps = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE_D);
		i = ps.executeUpdate();
		if (log.isEnabledFor(Priority.WARN)) {
			log.warn("updateDeletedNodes: " + SQL_UPDATE_IPROUTEINTERFACE_D + "rows: " + i);
		}

		// update datalinkinterface
		ps = dbConn.prepareStatement(SQL_UPDATE_DATALINKINTERFACE_D);
		i = ps.executeUpdate();
		if (log.isEnabledFor(Priority.WARN)) {
			log.warn("updateDeletedNodes: " + SQL_UPDATE_DATALINKINTERFACE_D + "rows: " + i);
		}
	
	}
	
	public LinkableSnmpNode GetLinkableSnmpNode(Connection dbConn, int nodeid)throws SQLException, UnknownHostException {
        Category log = ThreadCategory.getInstance(getClass());

        LinkableSnmpNode node = null;
		try {
			sysOidMask2vlanOid = getVlanOidFromSysOidMask();
		} catch (Throwable t) {
			if (log.isEnabledFor(Priority.WARN)) {
				log.warn("init: Failed to load sysoidmash2vlanoid from linkd configuration file " + t);
			}
		}

		Set ks = sysOidMask2vlanOid.keySet();

		PreparedStatement ps = dbConn.prepareStatement(SQL_SELECT_SNMP_NODE);
		ps.setInt(1,nodeid);
		ResultSet rs = ps.executeQuery();
		
		// FIXME this result must be unique! 
		while (rs.next()) {
			String ipaddr = rs.getString("ipaddr");
			node = new LinkableSnmpNode(rs.getInt("nodeid"), ipaddr, rs
					.getString("nodesysoid"));
			node.setSnmpPeer(SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName(ipaddr)));
			Iterator it = ks.iterator();
			while (it.hasNext()) {
				String sysoidmask = (String) it.next();
				if (node.getSysOid().startsWith(sysoidmask)) {
					node
							.setVlanOid((String) sysOidMask2vlanOid
									.get(sysoidmask));
					break;
				}
			}
		}
		rs.close();
		ps.close();

		return node;
	}

}
