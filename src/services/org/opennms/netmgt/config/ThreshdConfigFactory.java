//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.utils.IPSorter;
import org.opennms.netmgt.utils.IpListFromUrl;

/**
 * This is the singleton class used to load the configuration for
 * the OpenNMS Thresholding Daemon from the threshd-configuration xml file.
 *
 * A mapping of the configured URLs to the iplist they contain is
 * built at init() time so as to avoid numerous file reads.
 *
 * <strong>Note:</strong>Users of this class should make sure the 
 * <em>init()</em> is called before calling any other method to ensure
 * the config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:jamesz@opennms.com>James Zuo</a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public final class ThreshdConfigFactory
{
	/**
	 * The singleton instance of this factory
	 */
	private static ThreshdConfigFactory	m_singleton=null;

	/**
	 * This member is set to true if the configuration file
	 * has been loaded.
	 */
	private static boolean			m_loaded=false;

	/**
	 * The config class loaded from the config file
	 */
	private ThreshdConfiguration		m_config;

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
         * A boolean flag to indicate If a filter rule agaist the local 
         * OpenNMS server has to be used.
         */
        private static boolean                  m_verifyServer;

        /**
         * The name of the local OpenNMS server
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
			org.opennms.netmgt.config.threshd.Package pkg = (org.opennms.netmgt.config.threshd.Package)pkgEnum.nextElement();
			
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
	 * This method is used to establish package agaist an iplist 
         * iplist mapping, with which, the iplist is selected per package
         * via the configured filter rules from the database.
	 */
	private void createPackageIpListMap()
	{
		Category log = ThreadCategory.getInstance(this.getClass());

		m_pkgIpMap = new HashMap();
		
		Enumeration pkgEnum = m_config.enumeratePackage();
		while (pkgEnum.hasMoreElements())
		{
			org.opennms.netmgt.config.threshd.Package pkg = (org.opennms.netmgt.config.threshd.Package)pkgEnum.nextElement();
                        
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
         * This nethod is used to rebuild the package agaist iplist mapping when needed.
         * When a node gained service event occurs, threshd has to determine which package
         * the ip/service combination is in, but if the interface is a newly added one, the
         * package iplist should be rebuilt so that threshd could know which package this
         * ip/service pair is in.
         */
        public synchronized void rebuildPackageIpListMap()
        {
                createPackageIpListMap();
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
	private ThreshdConfigFactory(String configFile)
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		InputStream cfgIn = new FileInputStream(configFile);

		m_config = (ThreshdConfiguration)Unmarshaller.unmarshal(ThreshdConfiguration.class, new InputStreamReader(cfgIn));
		cfgIn.close();

		createUrlIpMap();
                
                OpennmsServerConfigFactory.init();
                m_verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();
                m_localServer = OpennmsServerConfigFactory.getInstance().getServerName();
                
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

		File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME); 

		ThreadCategory.getInstance(ThreshdConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());
		
		m_singleton = new ThreshdConfigFactory(cfgFile.getPath());
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
	 * Return the singleton instance of this factory.
	 *
	 * @return The current factory instance.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the factory
	 * 	has not yet been initialized.
	 */
	public static synchronized ThreshdConfigFactory getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");

		return m_singleton;
	}

	/** 
	 * Return the threshd configuration object.
	 */
	public synchronized ThreshdConfiguration getConfiguration()
	{
		return m_config;
	}

	/**
	 * This method is used to determine if the named interface is
	 * included in the passed package's url includes. If the interface
	 * is found in any of the URL files, then a value of true is returned, else
	 * a false value is returned.
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
	 * @param url		The url file to read
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
	 * This method is used to determine if the named interface is
	 * included in the passed package definition. If the interface
	 * belongs to the package then a value of true is returned. If
	 * the interface does not belong to the package a false value
	 * is returned.
	 *
	 * <strong>Note:</strong>Evaluation of the interface against a
	 * package filter will only work if the IP is already in the database.
	 *
	 * @param iface		The interface to test against the package.
	 * @param pkg		The package to check for the inclusion of
	 *				the interface.
	 *
	 * @return	True if the interface is included in the package,
	 *	false otherwise.
	 */
	public synchronized boolean interfaceInPackage(String iface, org.opennms.netmgt.config.threshd.Package pkg)
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
	public synchronized boolean serviceInPackageAndEnabled(String svcName, org.opennms.netmgt.config.threshd.Package pkg)
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
}
