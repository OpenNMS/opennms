//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Sep 17: Fixed an SQL parameter problem.
// 2003 Sep 16: Changed rescan information to let OpenNMS handle duplicate IPs.
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Aug 27: Fixed <range> tag. Bug #655
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.lang.*;

import java.io.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Enumeration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.capsd.Plugin;
import org.opennms.netmgt.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.capsd.DbIfServiceEntry;

import org.opennms.netmgt.utils.IPSorter;

import org.opennms.netmgt.config.CollectdConfigFactory;


// castor classes generated from the capsd-configuration.xsd
import org.opennms.netmgt.config.capsd.*;

/**
 * <p>This is the singleton class used to load the configuration for
 * the OpenNMS Capsd service from the capsd-configuration.xml.</p>
 *
 * <p><strong>Note:</strong>Users of this class should make sure the 
 * <em>init()</em> is called before calling any other method to ensure
 * the config is loaded before accessing other convenience methods</p>
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public final class CapsdConfigFactory
{
	/**
	 * The string indicating the start of the comments in a
	 * line containing the IP address in a file URL
	 */
	private final static String COMMENT_STR		= " #";

	/**
	 * This character at the start of a line indicates a comment line in a URL file
	 */
	private final static char COMMENT_CHAR		= '#';
	
	/**
	 * This member is set to true if the configuration file
	 * has been loaded.
	 */
	private static boolean				m_loaded=false;

	/**
	 * The singleton instance of this factory
	 */
	private static CapsdConfigFactory		m_singleton=null;

	/**
	 * The config class loaded from the config file
	 */
	private CapsdConfiguration			m_config;

	/**
	 * Maps url names to a list of addresses specified
	 * by the url.  Initialized at factory construction.
	 */
	 private Map 					m_urlMap;
	 
	/**
	 * The SQL statement used to retrieve all non-deleted/non-forced unamanaged IP interfaces
	 * from the 'ipInterface' table.
	 */
	final static String	SQL_DB_RETRIEVE_IP_INTERFACE = "SELECT nodeid,ipaddr,ismanaged FROM ipinterface WHERE ipaddr!='0.0.0.0' AND isManaged!='D' AND isManaged!='F'";
	
	/** 
	 * SQL statement to retrieve all non-deleted IP addresses from the ipInterface table 
	 * which support SNMP.
	 */
	private static String SQL_DB_RETRIEVE_SNMP_IP_INTERFACES = "SELECT DISTINCT ipinterface.nodeid,ipinterface.ipaddr,ipinterface.ifindex,ipinterface.issnmpprimary FROM ipinterface,ifservices,service WHERE ipinterface.ismanaged!='D' AND ipinterface.ipaddr=ifservices.ipaddr AND ifservices.serviceid=service.serviceid AND service.servicename='SNMP'";
	
	/** 
	 * SQL statement used to update the 'isSnmpPrimary' field of the ipInterface table.
	 */
	private static String SQL_DB_UPDATE_SNMP_PRIMARY_STATE = "UPDATE ipinterface SET issnmpprimary=? WHERE nodeid=? AND ipaddr=? AND ismanaged!='D'";
	/**
	 * The SQL statement used to retrieve all non-deleted/non-forced unamanaged services
	 * for a nodeid/ip from the 'ifservices' table.
	 */
	final static String	SQL_DB_RETRIEVE_IF_SERVICES = "SELECT serviceid, status FROM ifservices WHERE nodeid=? AND ipaddr=? AND status!='D' AND status!='F'";
	
	/**
	 * The SQL statement which updates the 'isManaged' field in the
	 * ipInterface table for a specific node/ipAddr pair
	 */
	final static String	SQL_DB_UPDATE_IP_INTERFACE = "UPDATE ipinterface SET ismanaged=? WHERE nodeid=? AND ipaddr=? AND isManaged!='D' AND isManaged!='F'";

	/**
	 * The SQL statement which updates the 'status' field in the
	 * ifServices table for a specific node/ipAddr pair
	 */
	final static String	SQL_DB_UPDATE_ALL_SERVICES_FOR_NIP = "UPDATE ifservices SET status=? WHERE nodeid=? AND ipaddr=? AND status!='D' AND status!='F'";
 
	/*
	 * The SQL statement which updates the 'status' field in the
	 * ifServices table for a specific node/ipAddr/service
	 */
	final static String	SQL_DB_UPDATE_SERVICE_FOR_NIP = "UPDATE ifservices SET status=? WHERE nodeid=? AND ipaddr=? AND serviceid=? AND status!='D' AND status!='F'";
 
	/** 
	 * The SQL statement used to determine if an IP address is already
	 * in the ipInterface table and there is known.
	 */
	private static final String	RETRIEVE_IPADDR_SQL = "SELECT ipaddr FROM ipinterface WHERE ipaddr=? AND ismanaged!='D'";
	 
	/** 
	 * The SQL statement used to determine if an IP address is already
	 * in the ipInterface table and if so what its parent nodeid is.
	 */
	private static final String	RETRIEVE_IPADDR_NODEID_SQL = "SELECT nodeid FROM ipinterface WHERE ipaddr=? AND ismanaged!='D'";
	
	/**
	 * The SQL statement used to load the currenly 
	 * defined service table.
	 */
	private static final String	SVCTBL_LOAD_SQL	= "SELECT serviceID, serviceName FROM service";

	/**
	 * The SQL statement used to add a new entry into the
	 * service table
	 */
	private static final String	SVCTBL_ADD_SQL	= "INSERT INTO service (serviceID, serviceName) VALUES (?,?)";

	/**
	 * The SQL statement used to get the next value for 
	 * a service identifier. This is a sequence defined
	 * in the database.
	 */
	private static final String	NEXT_SVC_ID_SQL	= "SELECT nextval('serviceNxtId')";
	
	/**  
	 * The SQL statement used to delete all entries from the outage
	 * table which refer to the specified serviceId
	 */
	private static final String 	DELETE_OUTAGES_SQL = "DELETE FROM outages WHERE serviceID=?";
	
	/**  
	 * The SQL statement used to mark all ifservices table entries
       	 * which refer to the specified serviceId as deleted.
	 */
	private static final String 	DELETE_IFSERVICES_SQL = "UPDATE ifservices SET status='D' WHERE serviceID=?";
	
	/**
	 * The map of service identifiers, mapped by the
	 * service id and name. The identifier keys are integers
	 * and the names are string. The integers map to strings
	 * and the strings map to integers.
	 *
	 */
	private static final Map	m_serviceIds	= new HashMap();

	/**
	 * The map of capsd plugins. The plugins are indexed by
	 * both their class name and the protocols that they support.
	 */
	private static final Map	m_plugins	= new TreeMap();

	/**
	 * The integer value that is used to represent the 
	 * protocol scan configuration. If this value is used
	 * the the plugin should be used to scan the address.
	 */
	public static final Integer	SCAN		= new Integer(0);

	/**
	 * This integer value is used to represent that the protocol
	 * plugin should not be used to scan the interface.
	 */
	public static final Integer	SKIP		= new Integer(1);

	/**
	 * This integer value is used to represent the state when
	 * the protocol should be automatically set, with any
	 * status checks.
	 */
	public static final Integer	AUTO_SET	= new Integer(2);

	/**
	 * This class is used to encapsulate the basic protocol
	 * information read from the config file. The information
	 * includes the plugin, the protocol name, the merged parameters
	 * to the plugin, and the action to be taken.
	 *
	 * @author <a href="mailto:weave@opennms.org">Weave</a>
	 * @author <a href="http://www.opennms.org/">OpenNMS</a>
	 *
	 */
	public final static class ProtocolInfo
	{
		/**
		 * The plugin used to poll the interface
		 */
		private Plugin	m_plugin;

		/**
		 * The name of the protocol supported by the plugin.
		 */
		private String	m_protocol;

		/**
		 * the map or parameters passed to the plugin
		 */
		private Map	m_parameters;

		/**
		 * The integer value that represents the action
		 * that should be taken to poll the interface.
		 */
		private Integer	m_action;
		
		/**
		 * Constructs  a new protocol information element.
		 * 
		 * @param proto		The protocol supported.
		 * @param plugin	The plugin module
		 * @param params	The parameters for the plugin.
		 * @param action	The action to take.
		 */
		public ProtocolInfo(String proto, Plugin plugin, Map params, Integer action)
		{
			m_plugin = plugin;
			m_protocol = proto;
			m_parameters = params;
			m_action = action;
		}

		/**
		 * Returns the protocol name
		 */
		public String getProtocol()
		{
			return m_protocol;
		}

		/**
		 * Returns the plugin module
		 */
		public Plugin getPlugin()
		{
			return m_plugin;
		}

		/**
		 * Returns the input parameters for the plugin
		 */
		public Map getParameters()
		{
			return m_parameters;
		}

		/**
		 * Returns true if the configuration has this
		 * particular module set as automaticall enabled.
		 *
		 */
		public boolean autoEnabled()
		{
			return m_action == CapsdConfigFactory.AUTO_SET;
		}
	} // end ProtocolInfo

	/**
	* <P>LightWeightIfEntry is designed to hold specific information
	* about an IP interface in the database such as its IP address,
	* its parent node id, and its managed status and represents
	* a lighter weight version of the DbIpInterfaceEntry class.</P>
	*/
	private static final class LightWeightIfEntry
	{
		/** 
		 * Represents NULL value for 'ifIndex' field in the ipInterface table
		 */
		final static int	NULL_IFINDEX   = -1;
	
		private int 	m_nodeId;
		private int     m_ifIndex;
		private String	m_address;
		private char 	m_managementState;
		private char    m_snmpPrimaryState;
		private boolean m_primaryStateChanged;

		/**
		 * <P>Constructs a new LightWeightIfEntry object.</P>
		*
		* @param nodeId			Interface's parent node id
		* @param ifIndex		Interface's index
		* @param address		Interface's ip address
		* @param managementState	Interface's management state
		* @param snmpPrimaryState	Interface's primary snmp interface state 
		*/
		public LightWeightIfEntry(int nodeId, int ifIndex, String address, char managementState, char snmpPrimaryState)
		{
			m_nodeId = nodeId;
			m_ifIndex = ifIndex;
			m_address = address;
			m_managementState = managementState;
			m_snmpPrimaryState = snmpPrimaryState;
			m_primaryStateChanged = false;
		}

		/**
		 * <P>Returns the IP address of the interface.</P>
		 */
		public String getAddress()
		{
			return m_address;
		}
	
		/**
		 * <P>Returns the parent node id of the interface.</P>
		 */
		public int getNodeId()
		{
			return m_nodeId;
		}
		
		/**
		 * <P>Returns the ifIndex of the interface.</P>
		 */
		public int getIfIndex()
		{
			return m_ifIndex;
		}

		/**
		 * 
		 */
		public char getManagementState()
		{
			return m_managementState;
		}
		
		/**
		 * 
		 */
		public char getSnmpPrimaryState()
		{
			return m_snmpPrimaryState;
		}
		
		/** 
		 * 
		 */
		public void setSnmpPrimaryState(char state)
		{
			if (state != m_snmpPrimaryState)
			{
				m_snmpPrimaryState = state;
				m_primaryStateChanged = true;
			}
		}
		
		/** 
		 * 
		 */
		public boolean hasSnmpPrimaryStateChanged()
		{
			return m_primaryStateChanged;
		}
	}
	
	/**
	 * Constructs a new CapsdConfigFactory object for access to the
	 * Capsd configuration information.
	 * 
	 * In addition to loading the configuration from the capsd-configuration.xml
	 * file the constructor also inserts any plugins defined in the
	 * configuration but not in the database into the 'service' table.
	 *
	 * @param	configFile	The configuration file to load.
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	private CapsdConfigFactory(String configFile)
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		InputStream cfgIn = new FileInputStream(configFile);

		m_config = (CapsdConfiguration) Unmarshaller.unmarshal(CapsdConfiguration.class, new InputStreamReader(cfgIn));
		cfgIn.close();
		
		// now load the plugins!
		// Map by protocol name and also by class name!
		//
		Enumeration eprotos = m_config.enumerateProtocolPlugin();
		while(eprotos.hasMoreElements())
		{
			ProtocolPlugin plugin = (ProtocolPlugin)eprotos.nextElement();
			try
			{
				if(m_plugins.containsKey(plugin.getClassName()))
				{
					Object oplugin = m_plugins.get(plugin.getClassName());
					m_plugins.put(plugin.getProtocol(), oplugin);
				}
				else
				{
					Class cplugin = Class.forName(plugin.getClassName());
					Object oplugin = cplugin.newInstance();
	
					// map them
					//
					m_plugins.put(plugin.getClassName(), oplugin);
					m_plugins.put(plugin.getProtocol(), oplugin);
				}
			}
			catch(Throwable t)
			{
				ThreadCategory.getInstance(getClass()).error("CapsdConfigFactory: failed to load plugin for protocol " + plugin.getProtocol() + ", class-name = " 
					 + plugin.getClassName(), t);
			}
		}
		
		// load address from any specified URLs
		//
		
		
		// Load addresses from any urls which have been specified
		//
		m_urlMap = new HashMap();
		
		Enumeration e = m_config.enumerateIpManagement();
		while(e.hasMoreElements())
		{
			IpManagement mgt = (IpManagement)e.nextElement();
			
			Enumeration f = mgt.enumerateIncludeUrl();
			while(f.hasMoreElements())
			{
				String url = f.nextElement().toString();
				if (!m_urlMap.containsKey(url))
					m_urlMap.put(url, getAddressesFromURL(url));
			}
		}
	}

	/**
	 * Load the config from the default config file and create the 
	 * singleton instance of this factory.
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	public static synchronized void init()
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		if (m_loaded)
		{
			// init already called - return
			// to reload, reload() will need to be called
			return;
		}

		File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);

		ThreadCategory.getInstance(CapsdConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());
		
		m_singleton = new CapsdConfigFactory(cfgFile.getPath());
		
		m_loaded = true;
	}

	/**
	 * Reload the config from the default config file
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read/loaded
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	public static synchronized void reload()
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		m_singleton = null;
		m_loaded = false;

		init();
	}

	/**
	 * <p>Return the singleton instance of this factory</p>
	 *
	 * @return The current factory instance.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the factory
	 * 	has not yet been initialized.
	 */
	public static synchronized CapsdConfigFactory getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");
			
		return m_singleton;
	}

	/** 
	 * <p>Return the Capsd configuration object</p>
	 */
	public CapsdConfiguration getConfiguration()
	{
		return m_config;
	}

	/**
	 * This method is responsible for sync'ing the content of the 'service' table
	 * with the protocols listed in the caspd-configuration.xml file.  
	 * 
	 * First a list of services currently contained in the 'service' table in 
	 * the database is built.
	 * 
	 * Next, the list of services defined in capsd-configuration.xml is iterated
	 * over and if any services are defined but do not yet exist in the 'service'
	 * table they are added to the table.
	 * 
	 * Finally, the list of services in the database is iterated over and if 
	 * any service exists in the database but is no longer listed in the
	 * capsd-configuration.xml file then that the following occurs:
	 *    
	 * 	1. All 'outage' table entries which refer to the service are
	 *   	   deleted.
	 * 	2. All 'ifServices' table entries which refer to the service
	 * 	   are deleted.
	 *
	 * Note that the 'service' table entry will remain in the database
	 * since events most likely exist which refer to the service.
	 */
	public void syncServices(Connection conn)
		throws SQLException
	{ 
		Category log = ThreadCategory.getInstance();
		if (conn == null)
		{
			log.error("CapsdConfigFactory.syncServices: Sync failed...must have valid database connection.");
			return;
		}
		
		// synchronize configured services list with
		// the database
		//
		
		PreparedStatement insStmt = conn.prepareStatement(SVCTBL_ADD_SQL);
		PreparedStatement nxtStmt = conn.prepareStatement(NEXT_SVC_ID_SQL);
		PreparedStatement loadStmt= conn.prepareStatement(SVCTBL_LOAD_SQL);
		PreparedStatement delFromOutagesStmt = null;
		PreparedStatement delFromIfServicesStmt = null;

		try
		{
			// go ahead and load the table first if it 
			// can be loaded.
			//
			List serviceNames = new ArrayList();
			ResultSet rs = loadStmt.executeQuery();
			while(rs.next())
			{
				Integer id = new Integer(rs.getInt(1));
				String name = rs.getString(2);

				m_serviceIds.put(id, name);
				m_serviceIds.put(name, id);
				serviceNames.add(name);
			}
			rs.close();

			// Build list of configured protocols from the loaded
			// configuration
			List protocols = new ArrayList();
			Enumeration eplugin = m_config.enumerateProtocolPlugin();
			while(eplugin.hasMoreElements())
			{
				ProtocolPlugin plugin = (ProtocolPlugin)eplugin.nextElement();
				protocols.add(plugin.getProtocol());
			}
				
			// now iterate over the configured protocols
			// and make sure that each is represented in the database.
			//
			Iterator protos = protocols.iterator();
			while(protos.hasNext())
			{
				String protocol = (String)protos.next();
				if(!serviceNames.contains(protocol))
				{
					// get the next identifier
					//
					int id = -1;
					rs = nxtStmt.executeQuery();
					rs.next();
					id = rs.getInt(1);
					rs.close();

					insStmt.setInt(1, id);
					insStmt.setString(2, protocol);
					insStmt.executeUpdate();

					Integer xid = new Integer(id);
					m_serviceIds.put(xid, protocol);
					m_serviceIds.put(protocol, xid);
					
					serviceNames.add(protocol);
				}
			}
			
			// now iterate over the services from the 'service' table
			// and determine if any no longer exist in the list of
			// configured protocols
			//
			Iterator s = serviceNames.iterator();
			while(s.hasNext())
			{
				String service = (String)s.next();
				if (!protocols.contains(service))
				{
					if (log.isDebugEnabled())
						log.debug("syncServices: service " + service + " exists in the database but not in the Capsd config file.");
					
					// Delete 'outage' table entries which refer to the service
					Integer id = (Integer)m_serviceIds.get(service);
					
					if (log.isDebugEnabled())
						log.debug("syncServices: deleting all references to service id " + id + " from the Outages table.");
					delFromOutagesStmt = conn.prepareStatement(DELETE_OUTAGES_SQL);
					delFromOutagesStmt.setInt(1, id.intValue());
					delFromOutagesStmt.executeUpdate();
					
					// Delete 'ifServices' table entries which refer to the service
					if (log.isDebugEnabled())
						log.debug("syncServices: deleting all references to service id " + id + " from the IfServices table.");
					delFromIfServicesStmt = conn.prepareStatement(DELETE_IFSERVICES_SQL);
					delFromIfServicesStmt.setInt(1, id.intValue());
					delFromIfServicesStmt.executeUpdate();
				}
			}
		}
		finally
		{
			if (insStmt != null)
				insStmt.close();
			if (nxtStmt != null)
				nxtStmt.close();
			if (loadStmt != null)
				loadStmt.close();
			if (delFromOutagesStmt != null)
				delFromOutagesStmt.close();
			if (delFromIfServicesStmt != null)
				delFromIfServicesStmt.close();
		}
	}
	
	/**
	 * <p>Responsible for syncing up the 'isManaged' field of the ipInterface table
	 * and the 'status' field of the ifServices table based on the capsd and
	 * poller configurations. Note that the 'sync' only takes place for interfaces
	 * and services that are not deleted or force unmanaged</p>
	 *
	 * <pre>Here is how the statuses are set:
	 * If an interface is 'unmanaged' based on the capsd configuration,
	 *     ipManaged='U' and status='U'
	 *
	 * If an interface is 'managed' based on the capsd configuration,
	 *   1. If the interface is not in any pacakge, ipManaged='N' and status ='N'
	 *   2. If the interface in atleast one package but the service is not polled by
	 *      by any of the packages, ipManaged='M' and status='N'
	 *   3. If the interface in atleast one package and the service is polled by a
	 *      package that this interface belongs to, ipManaged='M' and status'=A'
	 *</pre>
	 *
	 * @param 	conn	Connection to the database.
	 *
	 * @exception SQLException		Thrown if an error occurs while syncing 
	 * 					the database.
	 */
	public void syncManagementState(Connection conn)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance();

		if (conn == null)
		{
			log.error("CapsdConfigFactory.syncManagementState: Sync failed...must have valid database connection.");
			return;
		}
		
		// Get default management state.
		//
		String temp = m_config.getManagementPolicy();
		boolean managed_by_default = (temp == null || temp.equalsIgnoreCase("managed"));
		if (log.isDebugEnabled())
			log.debug("syncManagementState: managed_by_default: " + managed_by_default);
		
		//
		// Retrieve list of interfaces and their managed status from the database
		// NOTE:  Interfaces with an 'isManaged' field equal to 'D' (Deleted) or
		// 'F' (Forced Unmanaged) are 
		// not eligible to be managed and will not be included in the interfaces
		// retrieved from the database.  Likewise, interfaces with IP address of
		// '0.0.0.0' will also be excluded by the SQL query.
		//
		
		//prepare the SQL statement to query the database
		PreparedStatement ipRetStmt = conn.prepareStatement(SQL_DB_RETRIEVE_IP_INTERFACE);
		
		ArrayList ifList = new ArrayList();
		ResultSet result = null;
		try
		{
			//run the statement
			result = ipRetStmt.executeQuery();
		
			// Build array list of CapsdInterface objects representing each
			// of the interfaces retrieved from the database
			while (result.next())
			{
				// Node Id
				int nodeId = result.getInt(1);
				
				// IP address
				String address = result.getString(2);
				if (address == null)
				{
					log.warn("invalid ipInterface table entry, no IP address, skipping...");
					continue;
				}
				
				// Management State
				char managedState = DbIpInterfaceEntry.STATE_UNKNOWN;
				String str = result.getString(3);
				if(str != null)
					managedState = str.charAt(0);
				
				ifList.add(new LightWeightIfEntry(nodeId, 
								LightWeightIfEntry.NULL_IFINDEX, 
								address, 
								managedState, 
								DbIpInterfaceEntry.SNMP_UNKNOWN));
			}
		}
		finally
		{
			result.close();
			ipRetStmt.close();
		}
		
		// For efficiency, prepare the SQL statements in advance
		PreparedStatement ifUpdateStmt = conn.prepareStatement(SQL_DB_UPDATE_IP_INTERFACE);
		PreparedStatement allSvcUpdateStmt = conn.prepareStatement(SQL_DB_UPDATE_ALL_SERVICES_FOR_NIP);

		PreparedStatement svcRetStmt = conn.prepareStatement(SQL_DB_RETRIEVE_IF_SERVICES);
		PreparedStatement svcUpdateStmt = conn.prepareStatement(SQL_DB_UPDATE_SERVICE_FOR_NIP);
		
		// get a handle to the PollerConfigFactory
		PollerConfigFactory pollerCfgFactory = PollerConfigFactory.getInstance();

		try
		{
			// Loop through interface list and determine if there has been a change in 
			// the managed status of the interface based on the newly loaded package
			// configuration data.
			Iterator iter = ifList.iterator();
			while (iter.hasNext())
			{
				LightWeightIfEntry ifEntry = (LightWeightIfEntry)iter.next();

				String ipaddress = ifEntry.getAddress();
				
				// Convert to InetAddress object
				//
				InetAddress ifAddress = null;
				try
				{
					ifAddress = InetAddress.getByName(ipaddress);
				}
				catch (UnknownHostException uhE)
				{
					log.warn("Failed converting ip address " + ipaddress + " to InetAddress.");
					continue;
				}
				
				// Check interface address against Capsd config information to determine 
				// if interface management state should be managed or unmanaged.
				boolean address_is_unmanaged = this.isAddressUnmanaged(ifAddress);
				if (log.isDebugEnabled())
				{
					log.debug("syncManagementState: " + ipaddress + " unmanaged based on capsd config?: " + address_is_unmanaged);
				}
				
				if (address_is_unmanaged)
				{
					// Interface not managed, check current
					// management state for this interface.
					if (ifEntry.getManagementState() != DbIpInterfaceEntry.STATE_UNMANAGED)
					{
						// Update management state to unmanaged for the 
						// interface as well as for its services.

						// Update the 'ipInterface' table
						ifUpdateStmt.setString(1, new String(new char[] { DbIpInterfaceEntry.STATE_UNMANAGED }));
						ifUpdateStmt.setInt(2, ifEntry.getNodeId());
						ifUpdateStmt.setString(3, ipaddress);
						ifUpdateStmt.executeUpdate();

						// Update the 'ifServices' table
						allSvcUpdateStmt.setString(1, new String(new char[] { DbIfServiceEntry.STATUS_UNMANAGED }));
						allSvcUpdateStmt.setInt(2, ifEntry.getNodeId());
						allSvcUpdateStmt.setString(3, ipaddress);
						allSvcUpdateStmt.executeUpdate();
					
						if (log.isDebugEnabled())
						{
							log.debug("syncManagementState: update completed for node/interface: " + 
							ifEntry.getNodeId() + "/" + ipaddress
							+ " to unmanaged");
						}
					}
				}
				else
				{
					// Interface should be managed - check the status against
					// poller config to see if interface will be polled
					// 
					// NOTE: Try to avoid re-evaluating the ip against filters for
					// each service, try to get the first package here and use
					// that for service evaluation
					//
					org.opennms.netmgt.config.poller.Package ipPkg = pollerCfgFactory.getFirstPackageMatch(ipaddress);
					boolean ipToBePolled = false;
					if (ipPkg != null)
						ipToBePolled = true;

					if (log.isDebugEnabled())
						log.debug("syncManagementState: " + ipaddress + " to be polled based on poller config?: " + ipToBePolled);

					if ((ifEntry.getManagementState() == DbIpInterfaceEntry.STATE_MANAGED  && ipToBePolled) ||
					   (ifEntry.getManagementState() == DbIpInterfaceEntry.STATE_NOT_POLLED && !ipToBePolled))
					{
						// current status is right
						if (log.isDebugEnabled())
							log.debug("syncManagementState: " + ipaddress + " - no change in status");
					}
					else
					{
						if (ipToBePolled)
							ifUpdateStmt.setString(1, new String(new char[] { DbIpInterfaceEntry.STATE_MANAGED }));
						else
							ifUpdateStmt.setString(1, new String(new char[] { DbIpInterfaceEntry.STATE_NOT_POLLED }));

						ifUpdateStmt.setInt(2, ifEntry.getNodeId());
						ifUpdateStmt.setString(3, ipaddress);
						ifUpdateStmt.executeUpdate();

						if (log.isDebugEnabled())
						{
							log.debug("syncManagementState: update completed for node/interface: " + 
							ifEntry.getNodeId() + "/" + ipaddress);
						}
					}

					// get services for this nodeid/ip and update
					svcRetStmt.setInt(1, ifEntry.getNodeId());
					svcRetStmt.setString(2, ipaddress);

					ResultSet svcRS = svcRetStmt.executeQuery();
					while(svcRS.next())
					{
						int svcId = svcRS.getInt(1);

						char svcStatus = DbIfServiceEntry.STATUS_UNKNOWN;
						String str = svcRS.getString(2);
						if(str != null)
							svcStatus = str.charAt(0);
				
						String svcName = (String)getServiceIdentifier(new Integer(svcId));
						//
						// try the first package that had the ip first, if
						// service is not enabled, try all packages
						//
						boolean svcToBePolled = false;
						if (ipPkg != null)
						{
							svcToBePolled = pollerCfgFactory.isPolled(svcName, ipPkg);
							if (!svcToBePolled)
								svcToBePolled = pollerCfgFactory.isPolled(ipaddress, svcName);
						}

						if (log.isDebugEnabled())
							log.debug("syncManagementState: " + ipaddress + "/" + svcName + " to be polled based on poller config?: " + svcToBePolled);

						if ((svcStatus == DbIfServiceEntry.STATUS_ACTIVE  && svcToBePolled) ||
						    (svcStatus == DbIfServiceEntry.STATUS_NOT_POLLED && !ipToBePolled))
						{
							// current status is right
							if (log.isDebugEnabled())
								log.debug("syncManagementState: " + ifEntry.getNodeId() + "/" + ipaddress  + "/" + svcName + " - no change in status");
						}
						else
						{
							// Update the 'ifServices' table
							if (svcToBePolled)
								svcUpdateStmt.setString(1, new String(new char[] { DbIfServiceEntry.STATUS_ACTIVE }));
							else
								svcUpdateStmt.setString(1, new String(new char[] { DbIfServiceEntry.STATUS_NOT_POLLED }));
							svcUpdateStmt.setInt(2, ifEntry.getNodeId());
							svcUpdateStmt.setString(3, ipaddress);
							svcUpdateStmt.setInt(4, svcId);
							svcUpdateStmt.executeUpdate();
					
							if (log.isDebugEnabled())
							{
								log.debug("syncManagementState: update completed for node/interface/svc: " + 
								ifEntry.getNodeId() + "/" + ipaddress
								+ "/" + svcName);
							}
						}

					} // end ifservices result
				} // interface managed
			} // end while
		}
		finally
		{
			// Close the prepared statements...
			try
			{
				ifUpdateStmt.close();
				allSvcUpdateStmt.close();
				svcRetStmt.close();
				svcUpdateStmt.close();
			}
			catch (Exception e)
			{
				if(log.isDebugEnabled())
				{
					log.debug("Exception while closing prepared statements", e);
				}
			}
		}
	}
	
	/**
	 * <p>Responsible for syncing up the 'isPrimarySnmp' field of the ipInterface table
	 * based on the capsd and collectd configurations. Note that the 'sync' only takes 
	 * place for interfaces that are not deleted</p>
	 *
	 * @param 	conn	Connection to the database.
	 *
	 * @exception SQLException		Thrown if an error occurs while syncing 
	 * 					the database.
	 */
	public synchronized void syncSnmpPrimaryState(Connection conn)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance();

		if (conn == null)
		{
			throw new IllegalArgumentException("Sync failed...must have valid database connection.");
		}
		
		//
		// Retrieve all non-deleted SNMP-supporting IP interfaces from the 
		// ipInterface table and build a map of nodes to interface entry list
		//
		if (log.isDebugEnabled())
			log.debug("syncSnmpPrimaryState: building map of nodes to interfaces...");
			
		Map nodes = new HashMap();
		
		//prepare the SQL statement to query the database
		PreparedStatement ipRetStmt = conn.prepareStatement(SQL_DB_RETRIEVE_SNMP_IP_INTERFACES);
		ResultSet result = null;
		try
		{
			//run the statement
			result = ipRetStmt.executeQuery();
		
			// Iterate over result set and build map of interface
			// entries keyed by node id.
			List ifList = new ArrayList();
			while (result.next())
			{
				// Node Id
				int nodeId = result.getInt(1);
				
				// IP address
				String address = result.getString(2);
				if (address == null)
				{
					log.warn("invalid ipInterface table entry, no IP address, skipping...");
					continue;
				}
				
				// ifIndex
				int ifIndex = result.getInt(3);
				if(result.wasNull() || ifIndex < 1)
				{
					log.debug("ipInterface table entry for address " + address + " does not have a valid ifIndex ");
					ifIndex = LightWeightIfEntry.NULL_IFINDEX;
				}
				
				// Primary SNMP State
				char primarySnmpState = DbIpInterfaceEntry.SNMP_UNKNOWN;
				String str = result.getString(4);
				if(str != null)
					primarySnmpState = str.charAt(0);
				
				// New node or existing node?
				ifList = (List)nodes.get(new Integer(nodeId));
				if (ifList == null)
				{
					// Create new interface entry list
					ifList = new ArrayList();
					ifList.add(new LightWeightIfEntry(nodeId, ifIndex, address, DbIpInterfaceEntry.STATE_UNKNOWN, primarySnmpState));
					
					// Add interface entry list to the map
					nodes.put(new Integer(nodeId), ifList);
				}
				else
				{
					// Just add the current interface to the
					// node's interface list
					ifList.add(new LightWeightIfEntry(nodeId, ifIndex, address, DbIpInterfaceEntry.STATE_UNKNOWN, primarySnmpState));
				}
			}
		}
		finally
		{
			try
			{
				result.close();
				ipRetStmt.close();
			}
			catch (Exception e)
			{
				// Ignore
			}
		}
		
		// Iterate over the nodes in the map and determine what the primary SNMP 
		// interface for each node should be.  Keep track of those interfaces
		// whose primary SNMP interface state has changed so that the database
		// can be updated accordingly.
		//
		if (log.isDebugEnabled())
			log.debug("syncSnmpPrimaryState: iterating over nodes in map and checking primary SNMP interface, node count: " + nodes.size());
		Iterator niter = nodes.keySet().iterator();
		while (niter.hasNext())
		{
			// Get the nodeid (key)
			Integer nId = (Integer)niter.next();
			log.debug("building SNMP address list for node " + nId);
			
			// Lookup the interface list (value)
			List ifEntries = (List)nodes.get(nId);
			
			// From the interface entries build a list of InetAddress objects
			// eligible to be the primary SNMP interface for the node.
			//
			List addressList = new ArrayList();
			Iterator iter = ifEntries.iterator();
			while(iter.hasNext())
			{
				LightWeightIfEntry lwIf = (LightWeightIfEntry)iter.next();
				
				// Skip interfaces which do not have a valid (non-null) ifIndex
				// as they are not eligible to be the primary SNMP interface
				if (lwIf.getIfIndex() == LightWeightIfEntry.NULL_IFINDEX)
				{
					log.debug("skipping address " + lwIf.getAddress() + ": does not have a valid ifIndex.");
					continue;
				}
					
				try
				{
					InetAddress addr = InetAddress.getByName(lwIf.getAddress());
					addressList.add(addr);
				}
				catch(UnknownHostException uhe)
				{
					log.warn("Unknown host exception for " + lwIf.getAddress(), uhe);
				}
			}
			
			// Determine primary SNMP interface from list of possible addresses
			//
			InetAddress primarySnmpIf = CollectdConfigFactory.getInstance().determinePrimarySnmpInterface(addressList);
			if (log.isDebugEnabled())
				log.debug("syncSnmpPrimaryState: primary SNMP interface for node " + nId + " is: " + primarySnmpIf );
				
			// Iterate back over interface list and update primary SNMP interface state 
			// for this node...if the primary SNMP interface state has changed update
			// the database to reflect the new state.
			iter = ifEntries.iterator();
			while(iter.hasNext())
			{
				LightWeightIfEntry lwIf = (LightWeightIfEntry)iter.next();
				
				if (lwIf.getIfIndex() == LightWeightIfEntry.NULL_IFINDEX)
				{
					lwIf.setSnmpPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
				}
				else if (primarySnmpIf == null || !lwIf.getAddress().equals(primarySnmpIf.getHostAddress()))
				{
					lwIf.setSnmpPrimaryState(DbIpInterfaceEntry.SNMP_SECONDARY);
				}
				else
				{
					lwIf.setSnmpPrimaryState(DbIpInterfaceEntry.SNMP_PRIMARY);
				}
				
				// Has SNMP primary state changed?
				if (lwIf.hasSnmpPrimaryStateChanged())
				{
					if (log.isDebugEnabled())
						log.debug("syncSnmpPrimaryState: updating " + 
							lwIf.getNodeId() + "/" + lwIf.getAddress() + 
							", marking with state: " + lwIf.getSnmpPrimaryState());
							
					//prepare the SQL statement to query the database
					PreparedStatement updateStmt = conn.prepareStatement(SQL_DB_UPDATE_SNMP_PRIMARY_STATE);
					updateStmt.setString(1, new String(new char[] { lwIf.getSnmpPrimaryState() }));
					updateStmt.setInt(2, lwIf.getNodeId());
					updateStmt.setString(3, lwIf.getAddress());
					
					try
					{
						//run the statement
						updateStmt.executeUpdate();
					}
					finally
					{
						try
						{
							updateStmt.close();
						}
						catch (Exception e)
						{
							// Ignore
						}
					}
				}
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("syncSnmpPrimaryState: sync completed.");
	}
	
	/**
	 * This method is used to convert the passed IP address
	 * to a <code>long</code> value. The address is converted
	 * in network byte order (big endin). This is compatable
	 * with the number format of the JVM, and thus the 
	 * return longs can be compared with other converted 
	 * IP Addresses to determine inclusion.
	 *
	 * @param addr	The IP address to convert.
	 *
	 * @return The converted IP address.
	 *
	 * @deprecated Use org.opennms.netmgt.utils.IPSorter.convertToLong() instead.
	 */
	private static long toLong(InetAddress addr)
	{
		byte[] baddr = addr.getAddress();
		
		return ((((long)baddr[0] & 0xffL) << 24) |
			(((long)baddr[1] & 0xffL) << 16) |
			(((long)baddr[2] & 0xffL) <<  8) |
			((long)baddr[3] & 0xffL));
	}

	/**
	 * Returns the list of protocol plugins and the associated
	 * actions for the named address. The currently loaded configuration
	 * is used to find, build, and return the protocol information.
	 * The returns information has all the necessary element to
	 * check the address for capabilities.
	 *
	 * @param address	The address to get protocol information for.
	 *
	 * @return The array of protocol information instances for the 
	 * 	address.
	 *
	 */
	public ProtocolInfo[] getProtocolSpecification(InetAddress address)
	{
		// get a logger
		//
		Category log = ThreadCategory.getInstance(CapsdConfigFactory.class);

		// The list of protocols that will be turned into
		// and array and returned to the caller. These are
		// of type ProtocolInfo
		//
		List lprotos = new ArrayList(m_config.getProtocolPluginCount());

		// go through all the defined plugins
		//
		Enumeration eplugins = m_config.enumerateProtocolPlugin();
		PLUGINLOOP: while(eplugins.hasMoreElements())
		{
			ProtocolPlugin plugin = (ProtocolPlugin)eplugins.nextElement();
			boolean found = false;

			// Loop through the specific and ranges to find out
			// if there is a particular protocol specification
			//
			Enumeration epluginconf = plugin.enumerateProtocolConfiguration();
			PLUGINCONFLOOP: while(epluginconf.hasMoreElements())
			{
				ProtocolConfiguration pluginConf = (ProtocolConfiguration) epluginconf.nextElement();

				// Check specifics first
				//
				Enumeration espec = pluginConf.enumerateSpecific();
				while(espec.hasMoreElements() && !found)
				{
					String saddr = (String)espec.nextElement();
					try
					{
						InetAddress taddr = InetAddress.getByName(saddr);
						if(taddr.equals(address))
						{
							found = true;
						}
					}
					catch(UnknownHostException e)
					{
						log.warn("CapsdConfigFactory: failed to convert address " + saddr + " to InetAddress", e);
					}
				}

				// check the ranges
				//
				long laddr = IPSorter.convertToLong(address.getAddress());
				Enumeration erange = pluginConf.enumerateRange();
				while(erange.hasMoreElements() && !found)
				{
					Range rng = (Range)erange.nextElement();

					InetAddress start = null;
					try
					{
						start = InetAddress.getByName(rng.getBegin());
					}
					catch(UnknownHostException e)
					{
						log.warn("CapsdConfigFactory: failed to convert address " + rng.getBegin() + " to InetAddress", e);
						continue;
					}

					InetAddress stop = null;
					try
					{
						stop = InetAddress.getByName(rng.getEnd());
					}
					catch(UnknownHostException e)
					{
						log.warn("CapsdConfigFactory: failed to convert address " + rng.getEnd() + " to InetAddress", e);
						continue;
					}

					if(toLong(start) <= laddr && laddr <= toLong(stop))
					{
						found = true;
					}
				}

				// if it has not be found yet then it's not
				// in this particular plugin conf, check the 
				// next
				//
				if(!found)
					continue;
				
				// if found then build protocol
				// specification if on, else next protocol.
				//
				String scan = null;
				if((scan = pluginConf.getScan()) != null)
				{
					if(scan.equals("enable"))
					{
						lprotos.add(new ProtocolInfo(plugin.getProtocol(),
									     (Plugin)m_plugins.get(plugin.getProtocol()),
									     null,
									     AUTO_SET));
						continue PLUGINLOOP;
					}
					else if(scan.equals("off"))
					{
						continue PLUGINLOOP;
					}
				}
				else if((scan = plugin.getScan()) != null)
				{
					if(scan.equals("off"))
						continue PLUGINLOOP;
				}

				// it's either on specifically, or by default
				// so map it parameters
				//
				Map params = new TreeMap();
				
				// add the default first
				//
				Enumeration eparams = plugin.enumerateProperty();
				while(eparams.hasMoreElements())
				{
					Property p = (Property)eparams.nextElement();
					params.put(p.getKey(), p.getValue());
				}

				eparams = pluginConf.enumerateProperty();
				while(eparams.hasMoreElements())
				{
					Property p = (Property)eparams.nextElement();
					params.put(p.getKey(), p.getValue());
				}

				lprotos.add(new ProtocolInfo(plugin.getProtocol(),
							     (Plugin)m_plugins.get(plugin.getProtocol()),
							     params,
							     SCAN));
			} // end ProtocolConfiguration loop

			if(!found) // use default config
			{
				// if found then build protocol
				// specification if on, else next protocol.
				//
				String scan = null;
				if((scan = plugin.getScan()) != null)
				{
					if(scan.equals("off"))
						continue PLUGINLOOP;
				}

				// it's either on specifically, or by default
				// so map it parameters
				//
				Map params = new TreeMap();
				
				// add the default first
				//
				Enumeration eparams = plugin.enumerateProperty();
				while(eparams.hasMoreElements())
				{
					Property p = (Property)eparams.nextElement();
					params.put(p.getKey(), p.getValue());
				}

				lprotos.add(new ProtocolInfo(plugin.getProtocol(),
							     (Plugin)m_plugins.get(plugin.getProtocol()),
							     params,
							     SCAN));
			}

		} // end ProtocolPlugin

		// copy the protocol information to
		// the approriate array and return that
		// result
		//
		ProtocolInfo[] result = new ProtocolInfo[lprotos.size()];
		
		return (ProtocolInfo[]) lprotos.toArray(result);
	}

	/**
	 * Returns the protocol identifier from the service
	 * table that was loaded during class initialization.
	 * The identifier is used determines the result. If
	 * a String is passed then the integer value is
	 * returned. If an interger value is passed then the
	 * string protocol name is returned.
	 *
	 * @param key	The value used to lookup the result in
	 * 	in the preloaded map.
	 *
	 * @return The result of the lookup, either a String or
	 * 	an Integer.
	 *
	 */
	public Object getServiceIdentifier(Object key)
	{
		return m_serviceIds.get(key);
	}

	/**
	 * Finds the SMB authentication object using the netbios name.
	 *
	 * The target of the search.
	 */
	public SmbAuth getSmbAuth(String target)
	{
		SmbConfig cfg = m_config.getSmbConfig();
		if(cfg != null)
		{
			Enumeration es = cfg.enumerateSmbAuth();
			while(es.hasMoreElements())
			{
				SmbAuth a = (SmbAuth)es.nextElement();
				if(a.getContent() != null && a.getContent().equalsIgnoreCase(target))
					return a;
			}
		}
		return null;
	}

	/**
	 * Checks the configuration to determine if 
	 * the target is managed or unmanaged.
	 *
	 * @param target	The target to check against.
	 */
	public boolean isAddressUnmanaged(InetAddress target)
	{
		Category log = ThreadCategory.getInstance(CapsdConfigFactory.class);
		String temp = m_config.getManagementPolicy();
		boolean managed_by_default = (temp == null || temp.equalsIgnoreCase("managed"));

		boolean found_denial = false;
		boolean found_accept = false;
		Enumeration e = m_config.enumerateIpManagement();
		while(e.hasMoreElements() && !found_denial)
		{
			IpManagement mgt = (IpManagement)e.nextElement();
			Enumeration f = mgt.enumerateSpecific();
			while(f.hasMoreElements())
			{
				String saddr = f.nextElement().toString();
				try
				{
					InetAddress addr = InetAddress.getByName(saddr);
					if(addr.equals(target))
					{
						if(mgt.getPolicy() == null || mgt.getPolicy().equalsIgnoreCase("managed"))
							found_accept = true;
						else
							found_denial = true;

						break;
					}
				}
				catch(UnknownHostException ex)
				{
					log.info("Failed to convert management address " + saddr + " to an InetAddress", ex);
				}
			}

			// now check the ranges
			//
			f = mgt.enumerateRange();
			while(!found_denial && f.hasMoreElements())
			{
				Range rng = (Range)f.nextElement();
				try
				{
					InetAddress saddr = InetAddress.getByName(rng.getBegin());
					InetAddress eaddr = InetAddress.getByName(rng.getEnd());

					long start = toLong(saddr);
					long stop  = toLong(eaddr);
					long tgt   = toLong(target);
					if(start <= tgt && tgt <= stop)
					{
						if(mgt.getPolicy() == null || mgt.getPolicy().equalsIgnoreCase("managed"))
							found_accept = true;
						else
							found_denial = true;

						break;
					}
				}
				catch(UnknownHostException ex)
				{
					log.info("Failed to convert management addresses (" + rng.getBegin() + ", " + rng.getEnd() + ")", ex);
				}
			}
			
			// now check urls
			//
			f = mgt.enumerateIncludeUrl();
			boolean match = false;
			while(!found_denial && !match && f.hasMoreElements())
			{
				String url = f.nextElement().toString();
				
				// Retrieve address list from url map
				List addresses = (List)m_urlMap.get(url);
				
				// Iterate over addresses looking for target match
				Iterator iter = addresses.iterator();
				while(iter.hasNext())
				{
					String saddr = (String)iter.next();
					try
					{
						InetAddress addr = InetAddress.getByName(saddr);
						if(addr.equals(target))
						{
							if(mgt.getPolicy() == null || mgt.getPolicy().equalsIgnoreCase("managed"))
								found_accept = true;
							else
								found_denial = true;
							
							match = true;
							break;
						}
					}
					catch(UnknownHostException ex)
					{
						log.info("Failed to convert management address " + saddr + " to an InetAddress", ex);
					}
				}
			}
					
			
		}

		boolean result = !managed_by_default;
		if(found_denial)
			result = true;
		else if(found_accept)
			result = false;

		return result;
	}
	
	/**
	 * The file URL is read and a 'specific IP' is added for each entry
	 * in this file. Each line in the URL file can be one of -
	 * <IP><space>#<comments>
	 * or
	 * <IP>
	 * or
	 * #<comments>
	 *
	 * Lines starting with a '#' are ignored and so are characters after
	 * a '<space>#' in a line.
	 *
	 * @param url		the URL file
	 * 
	 * @return List of addresses retrieved from the URL
	 */
	private List getAddressesFromURL(String url)
	{
		List addrList = new ArrayList();

		try
		{
			// open the file indicated by the url
			URL fileURL = new URL(url);
			
			File file = new File(fileURL.getFile());
		
			//check to see if the file exists
			if(file.exists())
			{
				BufferedReader buffer = new BufferedReader(new FileReader(file));
			
				String ipLine = null;
				String specIP =null;
		
				// get each line of the file and turn it into a specific address
				while( (ipLine = buffer.readLine()) != null )
				{
					ipLine = ipLine.trim();
					if (ipLine.length() == 0 || ipLine.charAt(0) == COMMENT_CHAR)
					{
						// blank line or skip comment
						continue;
					}

					// check for comments after IP
					int comIndex = ipLine.indexOf(COMMENT_STR);
					if (comIndex == -1)
					{
						specIP = ipLine;
					}
					else 
					{
						specIP = ipLine.substring(0, comIndex);
						ipLine = ipLine.trim();
					}

					addrList.add(specIP);
					
					specIP = null;
				}
			
				buffer.close();
			}
			else
			{
				// log something
				ThreadCategory.getInstance().warn("URL does not exist: " + url.toString());
			}
		}
		catch(MalformedURLException e)
		{
			ThreadCategory.getInstance().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
		}
		catch(FileNotFoundException e)
		{
			ThreadCategory.getInstance().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
		}
		catch(IOException e)
		{
			ThreadCategory.getInstance().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
		}
		
		return addrList;
	}
	
	/**
	 * 
	 */
	public boolean isInterfaceInDB(Connection dbConn, InetAddress ifAddress)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance(CapsdConfigFactory.class);
		
		boolean result = false;
		
		log.debug("isInterfaceInDB: attempting to lookup interface " + ifAddress.getHostAddress() + " in the database.");
		
		// Set connection as read-only
		//
		//dbConn.setReadOnly(true);
	
		PreparedStatement s = dbConn.prepareStatement(RETRIEVE_IPADDR_SQL);
		s.setString(1, ifAddress.getHostAddress());
		
		ResultSet rs = s.executeQuery();
		if(rs.next())
			result = true;
		
		// Close result set and statement
		//
		rs.close();
		s.close();

		return result;
	}
	
	/**
	 * 
	 */
	public int getInterfaceDbNodeId(Connection dbConn, InetAddress ifAddress, int ifIndex)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance(CapsdConfigFactory.class);
		
		log.debug("getInterfaceDbNodeId: attempting to lookup interface " + ifAddress.getHostAddress() 
                        + "/ifindex: " + ifIndex + " in the database.");
		
		// Set connection as read-only
		//
		//dbConn.setReadOnly(true);
	
                StringBuffer qs = new StringBuffer(RETRIEVE_IPADDR_NODEID_SQL);
                if (ifIndex != -1)
                        qs.append(" AND ifindex=?");
                
		PreparedStatement s = dbConn.prepareStatement(qs.toString());
		s.setString(1, ifAddress.getHostAddress());
	
                if (ifIndex != -1)
                        s.setInt(2, ifIndex);
                        
		ResultSet rs = s.executeQuery();
		int nodeid = -1;
		if(rs.next())
		{
			nodeid = rs.getInt(1);
		}
		
		// Close result set and statement
		//
		rs.close();
		s.close();
			
		return nodeid;
	}
	
	/**
	 * 
	 */
	public long getRescanFrequency()
	{
		long frequency = -1;
		
		if (m_config.hasRescanFrequency())
			frequency = m_config.getRescanFrequency();
		else
		{
			ThreadCategory.getInstance(CapsdConfigFactory.class).warn("Capsd configuration file is missing rescan interval, defaulting to 24 hour interval.");
			frequency = 86400000; // default is 24 hours
		}
		
		return frequency;
	}
	
	/**
	 * 
	 */
	public long getInitialSleepTime()
	{
		long sleep = -1;
		
		if (m_config.hasInitialSleepTime())
			sleep = m_config.getInitialSleepTime();
		else
		{
			ThreadCategory.getInstance(CapsdConfigFactory.class).warn("Capsd configuration file is missing rescan interval, defaulting to 24 hour interval.");
			sleep = 300000; // default is 5 minutes
		}
		
		return sleep;
	}
	
	/**
	 * 
	 */
	public int getMaxSuspectThreadPoolSize()
	{
		return m_config.getMaxSuspectThreadPoolSize();
	}
	
	/**
	 * 
	 */
	public int getMaxRescanThreadPoolSize()
	{
		return m_config.getMaxRescanThreadPoolSize();
	}
	
	/** 
	 * Defines Capsd's behavior when, during a protocol scan, it gets a 
	 * java.net.NoRouteToHostException exception.  If abort rescan property is
	 * set to "true" then Capsd will not perform any additional protocol
	 * scans.
	 */
	public boolean getAbortProtocolScansFlag()
	{
		boolean abortFlag = false;
		
		String abortProperty = m_config.getAbortProtocolScansIfNoRoute();
		if (abortProperty != null && abortProperty.equals("true"))
			abortFlag = true;
			
		return abortFlag;
	}
}
