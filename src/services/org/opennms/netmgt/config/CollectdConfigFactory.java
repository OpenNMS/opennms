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
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Jan 31: Cleaned up some unused imports.
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

import java.io.*;
import java.util.*;
import java.net.InetAddress;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.utils.IpListFromUrl;
import org.opennms.netmgt.utils.IPSorter;

// castor classes generated from the collectd-configuration.xsd
import org.opennms.netmgt.config.collectd.*;
import org.opennms.netmgt.config.server.*;

/**
 * <p>This is the singleton class used to load the configuration for
 * the OpenNMS Collection Daemon from the collectd-configuration.xml.</p>
 *
 * <p>A mapping of the configured URLs to the iplist they contain is
 * built at init() time so as to avoid numerous file reads</p> 
 *
 * <p><strong>Note:</strong>Users of this class should make sure the 
 * <em>init()</em> is called before calling any other method to ensure
 * the config is loaded before accessing other convenience methods</p>
 *
 * @author <a href="mailto:jamesz@blast.com">James Zuo</a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public final class CollectdConfigFactory
{
	private final static String 	SELECT_METHOD_MIN = "min";
	private final static String 	SELECT_METHOD_MAX = "max";
	
	/**
	 * The singleton instance of this factory
	 */
	private static CollectdConfigFactory	m_singleton=null;

	/**
	 * This member is set to true if the configuration file
	 * has been loaded.
	 */
	private static boolean			m_loaded=false;

	/**
	 * The config class loaded from the config file
	 */
	private CollectdConfiguration		m_config;

	/**
	 * A mapping of the configured URLs to a list of the specific IPs
	 * configured in each - so as to avoid file reads
	 */
	private Map				m_urlIPMap;

	/**
	 * A mapping of the configured package to a list of IPs selected 
	 * via filter rules, so as to avoid repetetive database access.
	 */
	private Map				m_pkgIpMap;

        /**
         * A boolean flag to indicate If a filter rule against the local
         * NMS server has to be used.
         */
        private static boolean                  m_verifyServer;

        /**
         * Name of the local NMS server.
         */
        private static String                   m_localServer;
        
	/**
	 * Go through the configuration and build a mapping of each configured
	 * URL to a list of IPs configured in that URL - done at init() time so
	 * that repeated file reads can be avoided
	 */
	private void createUrlIpMap()
	{
		m_urlIPMap = new HashMap();
		
		Enumeration pkgEnum = m_config.enumeratePackage();
		while(pkgEnum.hasMoreElements())
		{
			org.opennms.netmgt.config.collectd.Package pkg = (org.opennms.netmgt.config.collectd.Package)pkgEnum.nextElement();
			
			Enumeration urlEnum = pkg.enumerateIncludeUrl();
			while(urlEnum.hasMoreElements())
			{
				String urlname = (String)urlEnum.nextElement();

				java.util.List iplist = IpListFromUrl.parse(urlname);
				if (iplist.size() > 0)
				{
					m_urlIPMap.put(urlname, iplist);
				}
			}
		}
	}
	
        /**
	 * <p>This method is used to establish package agaist iplist 
         * mapping, with which, the iplist is selected per package
         * via the configured filter rules from the database. </p>
	 */
	private void createPackageIpListMap()
	{
		Category log = ThreadCategory.getInstance(this.getClass());

		m_pkgIpMap = new HashMap();
		
		Enumeration pkgEnum = m_config.enumeratePackage();
		while (pkgEnum.hasMoreElements())
		{
			org.opennms.netmgt.config.collectd.Package pkg = (org.opennms.netmgt.config.collectd.Package)pkgEnum.nextElement();
                        
                        
		        //
		        // Get a list of ipaddress per package agaist the filter rules from
                        // database and populate the package, IP list map.
		        //
		        Filter filter = new Filter();
                        StringBuffer filterRules = new StringBuffer(pkg.getFilter().getContent());
		        
                        try
		        {
                                if (m_verifyServer)
                                {
                                        filterRules.append(" & (serverName == ");
                                        filterRules.append('\"');
                                        filterRules.append(m_localServer);
                                        filterRules.append('\"');
                                        filterRules.append(")");
                                }
		                
                                if (log.isDebugEnabled())
			                log.debug("createPackageIpMap: package is " + pkg.getName() 
                                        + ". filer rules are  "  + filterRules.toString());
                                        
			        List ipList = filter.getIPList(filterRules.toString());
				if (ipList.size() > 0)
				{
					m_pkgIpMap.put(pkg, ipList);
				}
		        }
		        catch (Throwable t)
		        {
			        if(log.isEnabledFor(Priority.ERROR))
			        {
				        log.error("createPackageIpMap: failed to map package: "
				        	  + pkg.getName() + " to an IP List", t);
			        }
                        }
		}
        }

	/**
	 * Private constructor
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	private CollectdConfigFactory(String configFile)
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		InputStream cfgIn = new FileInputStream(configFile);

		m_config = (CollectdConfiguration) Unmarshaller.unmarshal(CollectdConfiguration.class, new InputStreamReader(cfgIn));
		cfgIn.close();

		createUrlIpMap();
                
                OpennmsServerConfigFactory.init();
                m_verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();
                m_localServer  = OpennmsServerConfigFactory.getInstance().getServerName();

                createPackageIpListMap();
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

		File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME); 

		ThreadCategory.getInstance(CollectdConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());
		
		m_singleton = new CollectdConfigFactory(cfgFile.getPath());
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
		m_loaded    = false;

		init();
	}

	/**
	 * <p>Return the singleton instance of this factory<p>
	 *
	 * @return The current factory instance.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the factory
	 * 	has not yet been initialized.
	 */
	public static synchronized CollectdConfigFactory getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");

		return m_singleton;
	}

	/** 
	 * <p>Return the collectd configuration object</p>
	 */
	public synchronized CollectdConfiguration getConfiguration()
	{
		return m_config;
	}

	/**
	 * <p>This method is used to determine if the named interface is
	 * included in the passed package's url includes. If the interface
	 * is found in any of the URL files, then a value of true is returned, else
	 * a false value is returned.</p>
	 *
	 * <pre>The file URL is read and each entry in this file checked. Each line
	 * in the URL file can be one of -
	 * <IP><space>#<comments>
	 * or
	 * <IP>
	 * or
	 * #<comments>
	 *
	 * Lines starting with a '#' are ignored and so are characters after
	 * a '<space>#' in a line.</pre>
	 *
	 * @param addr		The interface to test against the package's URL
	 * @param URL		The url file to read
	 *
	 * @return	True if the interface is included in the url, false otherwise.
	 */
	private boolean interfaceInUrl(String addr, String url)
	{
		boolean bRet = false;

		// get list of IPs in this URL
		java.util.List iplist = (java.util.List)m_urlIPMap.get(url);
		if (iplist != null && iplist.size() > 0)
		{
			bRet = iplist.contains(addr);
		}
			
		return bRet;
	}

	/**
	 * <p>This method is used to determine if the named interface is
	 * included in the passed package definition. If the interface
	 * belongs to the package then a value of true is returned. If
	 * the interface does not belong to the package a false value
	 * is returned.</p>
	 *
	 * <p><strong>Note:</strong>Evaluation of the interface against a
	 * package filter will only work if the IP is already in the database</p>
	 *
	 * @param iface		The interface to test against the package.
	 * @param pkg		The package to check for the inclusion of
	 *				the interface.
	 *
	 * @return	True if the interface is included in the package,
	 *	false otherwise.
	 */
	public synchronized boolean interfaceInPackage(String iface, org.opennms.netmgt.config.collectd.Package pkg)
	{
		Category log = ThreadCategory.getInstance(this.getClass());

		boolean filterPassed = false;

		// get list of IPs in this package 
		java.util.List ipList = (java.util.List)m_pkgIpMap.get(pkg);
		if (ipList != null && ipList.size() > 0)
		{
			filterPassed = ipList.contains(iface);
		}
                
		if (log.isDebugEnabled())
			log.debug("interfaceInPackage: Interface " + iface + " passed filter for package "
				  + pkg.getName() + "?: " + filterPassed);

		if (!filterPassed)
			return false;
			
		//
		// Ensure that the interface is in the specific list or
		// that it is in the include range and is not excluded
		//
		boolean has_specific = false;
		boolean has_range_include = false;
		boolean has_range_exclude = false;

		long addr = IPSorter.convertToLong(iface);
		Enumeration eincs = pkg.enumerateIncludeRange();
		while(!has_range_include && eincs.hasMoreElements())
		{
			IncludeRange rng = (IncludeRange)eincs.nextElement();
			long start = IPSorter.convertToLong(rng.getBegin());
			if(addr > start)
			{
				long end = IPSorter.convertToLong(rng.getEnd());
				if(addr <= end)
				{
					has_range_include = true;
				}
			}
			else if(addr == start)
			{
				has_range_include = true;
			}
		}

		Enumeration espec = pkg.enumerateSpecific();
		while(!has_specific && espec.hasMoreElements())
		{
			long speca = IPSorter.convertToLong(espec.nextElement().toString());
			if(speca == addr)
				has_specific = true;
		}

		Enumeration eurl = pkg.enumerateIncludeUrl();
		while(!has_specific && eurl.hasMoreElements())
		{
			has_specific = interfaceInUrl(iface, (String)eurl.nextElement());
		}

		Enumeration eex = pkg.enumerateExcludeRange();
		while(!has_range_exclude && !has_specific && eex.hasMoreElements())
		{
			ExcludeRange rng = (ExcludeRange)eex.nextElement();
			long start = IPSorter.convertToLong(rng.getBegin());
			if(addr > start)
			{
				long end = IPSorter.convertToLong(rng.getEnd());
				if(addr <= end)
				{
					has_range_exclude = true;
				}
			}
			else if(addr == start)
			{
				has_range_exclude = true;
			}
		}

		return has_specific || (has_range_include && !has_range_exclude);
	}
	
	/**
	 * Returns true if the service is part of the package and the 
	 * status of the service is set to "on".  Returns false if 
	 * the service is not in the package or it is but the status
	 * of the service is set to "off".
	 *
	 * @param svcName	The service name to lookup.
	 * @param pkg		The package to lookup up service.
	 */
	public synchronized boolean serviceInPackageAndEnabled(String svcName, org.opennms.netmgt.config.collectd.Package pkg)
	{
		boolean result = false;
		
		Enumeration esvcs = pkg.enumerateService();
		while(result == false && esvcs.hasMoreElements())
		{
			Service tsvc = (Service)esvcs.nextElement();
			if(tsvc.getName().equalsIgnoreCase(svcName))
			{
				// Ok its in the package.  Now check the
				// status of the service
				String status = tsvc.getStatus();
				if (status.equals("on"))
					result = true;
			}
		}
		return result;
	}
	
	/** 
	 * Returns true if the specified interface is included by 
	 * at least one package which has the specified service 
	 * and that service is enabled (set to "on").
	 *
	 * @param ipAddr	IP address of the interface to lookup
	 * @param svcName	The service name to lookup
	 * 
	 * @return true if Collectd config contains a package which
	 * includes the specified interface and has the specified 
	 * service enabled.
	 */
	public synchronized boolean lookupInterfaceServicePair(String ipAddr, String svcName)
	{
		boolean result = false;
		
		Enumeration pkgs = m_config.enumeratePackage();
		while (pkgs.hasMoreElements() && result == false)
		{
			org.opennms.netmgt.config.collectd.Package pkg = (org.opennms.netmgt.config.collectd.Package)pkgs.nextElement();
			
			// Does the package include the interface?
			//
			if (interfaceInPackage(ipAddr, pkg))
			{
				// Yes, now see if package includes
				// the service and service is enabled
				//
				if (serviceInPackageAndEnabled(svcName, pkg))
				{
					// Thats all we need to know...
					result = true;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * This method is responsbile for determining the node's primary SNMP interface
	 * from the specified list of InetAddress objects.
	 * 
	 * @param interfaceList  List of InetAddress objects representing all the
	 * 			interfaces belonging to a particular node which
	 *			support the "SNMP" service and have a valid ifIndex.
	 * 
	 * @return InetAddress object of the primary SNMP interface or null if none
	 *         of the node's interfaces are eligible.
	 */
	public synchronized InetAddress determinePrimarySnmpInterface(List addressList)
	{
		Category log = ThreadCategory.getInstance(CollectdConfigFactory.class);
		
		InetAddress primaryIf = null;
		
		// For now hard-coding primary interface address selection method to MIN
		String method = SELECT_METHOD_MIN;
		
		// To be selected as the the primary SNMP interface for a node
		// the interface must be included by a Collectd package and
		// that package must include the SNMP service and the service
		// must be enabled.
		//
		// Iterate over interface list and test each  interface
		//
		Iterator iter = addressList.iterator();
		while(iter.hasNext())
		{
			InetAddress ipAddr = (InetAddress)iter.next();
			if (log.isDebugEnabled())
				log.debug("determinePrimarySnmpIf: checking interface " + ipAddr.getHostAddress());
			primaryIf = compareAndSelectPrimaryCollectionInterface("SNMP", ipAddr, primaryIf, method);
		}

		if (log.isDebugEnabled())
			if (primaryIf != null)
				log.debug("determinePrimarySnmpInterface: selected primary SNMP interface: " + primaryIf.getHostAddress());
			else
				log.debug("determinePrimarySnmpInterface: no primary SNMP interface found");
		return primaryIf;
	}
	
	/**
	 * Utility method which compares two InetAddress objects based on the provided method (MIN/MAX)
	 * and returns the InetAddress which is to be considered the primary interface.  
	 * 
	 * NOTE:  In order for an interface to be considered primary it must be included by
	 *        a Collectd package which supports the specified service.  This method will 
	 *        return null if the 'oldPrimary' address is null and the 'currentIf' 
	 *        address does not pass the Collectd package check.
	 *
	 * @param svcName	Service name
	 * @param currentIf	Interface with which to compare the 'oldPrimary' address.
	 * @param oldPrimary	Primary interface to be compared against the 'currentIf' address.
	 * @param method	Comparison method to be used (either "min" or "max")
	 * 
	 * @return InetAddress object of the primary interface based on the provided method
	 *         or null if neither address is eligible to be primary.
	 */
	public synchronized InetAddress compareAndSelectPrimaryCollectionInterface(String svcName, InetAddress currentIf, InetAddress oldPrimary, String method)
	{
		InetAddress newPrimary = null;
		
		if (oldPrimary == null)
		{
			if (lookupInterfaceServicePair(currentIf.getHostAddress(), svcName))
				return currentIf;
			else 
				return oldPrimary;
		}
			
		long current = IPSorter.convertToLong(currentIf.getAddress());
		long primary = IPSorter.convertToLong(oldPrimary.getAddress());
				
		if (method.equals(SELECT_METHOD_MIN))
		{
			// Smallest address wins
			if (current < primary)
			{
				// Replace the primary interface with the current
				// interface only if the current interface is managed!
				if(lookupInterfaceServicePair(currentIf.getHostAddress(), svcName))
					newPrimary = currentIf;
			}
		}
		else 
		{
			// Largest address wins
			if (current > primary)
			{
				// Replace the primary interface with the current
				// interface only if the current interface is managed!
				if(lookupInterfaceServicePair(currentIf.getHostAddress(), svcName))
					newPrimary = currentIf;
			}
		}
		
		if (newPrimary != null)
			return newPrimary;
		else
			return oldPrimary;
	}
}
