/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 26, 2006
 *
 * Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.IpListFromUrl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.linkd.ExcludeRange;
import org.opennms.netmgt.config.linkd.IncludeRange;
import org.opennms.netmgt.config.linkd.LinkdConfiguration;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.config.linkd.Vendor;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.linkd.DiscoveryLink;
import org.opennms.netmgt.linkd.SnmpCollection;
import org.opennms.protocols.snmp.SnmpObjectId;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
abstract public class LinkdConfigManager implements LinkdConfig {

	/**
	 * Object containing all Linkd-configuration objects parsed from the XML
	 * file
	 */
	protected static LinkdConfiguration m_config;

    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private static Map<String, List<String>> m_urlIPMap;

    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid redundant database access.
     */
    private static Map<org.opennms.netmgt.config.linkd.Package, List<String>> m_pkgIpMap;

	/**
	 * The HashMap that associates the OIDS masks to class name
	 */
	private static HashMap<String,String> m_oidMask2className;

    /**
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     * @param reader
     * @param localServer
     * @param verifyServer
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException
     */
	@Deprecated
    public LinkdConfigManager(Reader reader) throws MarshalException, ValidationException, IOException {
        reloadXML(reader);
    }

    public LinkdConfigManager(InputStream stream) throws MarshalException, ValidationException, IOException {
        reloadXML(stream);
    }

    public abstract void update() throws IOException, MarshalException, ValidationException;

    protected abstract void saveXml(String xml) throws IOException;

    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }

	public synchronized org.opennms.netmgt.config.linkd.Package getPackage(String name) {
        for (org.opennms.netmgt.config.linkd.Package thisPackage : m_config.getPackageCollection()) {
            if (thisPackage.getName().equals(name)) {
                return thisPackage;
            }
        }
        return null;
    }
    
    private void createUrlIpMap() {

        m_urlIPMap = new HashMap<String, List<String>>();

        for (org.opennms.netmgt.config.linkd.Package pkg : m_config.getPackageCollection()) {
            for (String urlname : pkg.getIncludeUrlCollection()) {
                java.util.List<String> iplist = IpListFromUrl.parse(urlname);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(urlname, iplist);
                }
            }
        }
    }

    /**
     * This method is used to establish package against IP list mapping, with
     * which, the IP list is selected per package via the configured filter rules
     * from the database.
     */
    public void createPackageIpListMap() {
        m_pkgIpMap = new HashMap<org.opennms.netmgt.config.linkd.Package, List<String>>();

        for (org.opennms.netmgt.config.linkd.Package pkg : m_config.getPackageCollection()) {
            //
            // Get a list of IP addresses per package against the filter rules from
            // database and populate the package, IP list map.
            //
            try {
                List<String> ipList = getIpList(pkg);
                if (log().isDebugEnabled())
                    log().debug("createPackageIpMap: package " + pkg.getName() + ": ipList size =  " + ipList.size());
    
                if (ipList.size() > 0) {
                    m_pkgIpMap.put(pkg, ipList);
                }
            } catch (Throwable t) {
                log().error("createPackageIpMap: failed to map package: " + pkg.getName() + " to an IP List: " + t, t);
            }
        }
    }

    public List<String> getIpList(Package pkg) {
        StringBuffer filterRules = new StringBuffer(pkg.getFilter().getContent());
        
        if (log().isDebugEnabled())
            log().debug("createPackageIpMap: package is " + pkg.getName() + ". filer rules are  " + filterRules.toString());
        List<String> ipList = FilterDaoFactory.getInstance().getIPList(filterRules.toString());
        return ipList;
    }

	public boolean useIpRouteDiscovery() {

		boolean discoveryUsingRoutes = true;

		if (m_config.hasUseIpRouteDiscovery()) {
			discoveryUsingRoutes = m_config.getUseIpRouteDiscovery();
		}

		return discoveryUsingRoutes;
	}

	public boolean saveRouteTable() {

		boolean downloadRoutes = true;
		
		if (m_config.hasSaveRouteTable()) {
			downloadRoutes = m_config.getSaveRouteTable();
		}
		
		return downloadRoutes;
	}

	public boolean useCdpDiscovery() {

		boolean discoveryUsingCdp = true;

		if (m_config.hasUseCdpDiscovery()) {
			discoveryUsingCdp = m_config.getUseCdpDiscovery();
		}

		return discoveryUsingCdp;
	}
	
	public boolean useBridgeDiscovery() {

		boolean discoveryUsingBridge = true;

		if (m_config.hasUseBridgeDiscovery()) {
			discoveryUsingBridge = m_config.getUseBridgeDiscovery();
		}

		return discoveryUsingBridge;
	}

	public boolean saveStpNodeTable() {

		boolean download = true;
		
		if (m_config.hasSaveStpNodeTable()) {
			download = m_config.getSaveStpNodeTable();
		}
		
		return download;
	}
	
	public boolean enableDiscoveryDownload() {
		boolean enable=false;
		if (m_config.hasEnableDiscoveryDownload()) 
			enable = m_config.getEnableDiscoveryDownload();
		return enable;
	}	
	
	public boolean saveStpInterfaceTable() {

		boolean download = true;
		
		if (m_config.hasSaveStpInterfaceTable()) {
			download = m_config.getSaveStpInterfaceTable();
		}
		
		return download;
	}

	public long getInitialSleepTime() {


		long initialSleepTime = 1800000;

		if (m_config.hasInitial_sleep_time()) {
			initialSleepTime = m_config.getInitial_sleep_time();
		}

		return initialSleepTime;
	}

	public long getSnmpPollInterval() {

		long snmppollinterval = 900000;

		if (m_config.hasSnmp_poll_interval()) {
			snmppollinterval = m_config.getSnmp_poll_interval();
		}

		return snmppollinterval;
	}

	public long getDiscoveryLinkInterval() {

		long discoverylinkinterval = 3600000;

		if (m_config.hasSnmp_poll_interval()) {
			discoverylinkinterval = m_config
					.getDiscovery_link_interval();
		}

		return discoverylinkinterval;
	}

	public int getThreads() {

		int threads = 5;

		if (m_config.hasThreads()) {
			threads = m_config.getThreads();
		}

		return threads;
	}

    /**
     * Return the linkd configuration object.
     */
    public synchronized LinkdConfiguration getConfiguration() {
        return m_config;
    }

    /**
	 * 
	 * @return boolean auto-discovery
	 */

	public boolean autoDiscovery() {

		boolean autodiscovery = false; 
		if (m_config.hasAutoDiscovery()) {
			autodiscovery = m_config.getAutoDiscovery();
		}

		return autodiscovery;
	}

	/**
	 * 
	 * @return boolean enable-vlan-discovery
	 */

	public boolean enableVlanDiscovery() {

		boolean vlandiscovery = true; 
		if (m_config.hasEnableVlanDiscovery()) {
			vlandiscovery = m_config.getEnableVlanDiscovery();
		}

		return vlandiscovery;
	}

	private void getClassNames() throws IOException, MarshalException,
			ValidationException {

		m_oidMask2className = new HashMap<String,String>();

		List<String> excludedOids = new ArrayList<String>();

		Vendor[] vendors = m_config.getVlans().getVendor();
		for (int i = 0; i < vendors.length; i++) {
			SnmpObjectId curRootSysOid = new SnmpObjectId(vendors[i]
					.getSysoidRootMask());
			String curClassName = vendors[i].getClassName();

			String[] specifics = vendors[i].getSpecific();
			for (int js = 0; js < specifics.length; js++) {
				SnmpObjectId oidMask = new SnmpObjectId(specifics[js]);
				oidMask.prepend(curRootSysOid);
				m_oidMask2className.put(oidMask.toString(), curClassName);
				if (log().isDebugEnabled())
					log().debug("getClassNames:  adding class " + curClassName
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
						if (log().isDebugEnabled())
							log().debug("getClassNames:  signing excluded class "
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
							m_oidMask2className.put(oidMask.toString(),
									curClassName);
							if (log().isDebugEnabled())
								log().debug("getClassNames:  adding class "
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
	}

	
	public String getClassName(String sysoid) {

		String defaultClassName = null;
		for (String oidMask : m_oidMask2className.keySet()) {
			if (sysoid.startsWith(oidMask)) {
				return m_oidMask2className.get(oidMask);
			}
		}

		return defaultClassName;

	}

	public boolean hasClassName(String sysoid) {

	    for (String oidMask : m_oidMask2className.keySet()) {
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
	
    /**
     * This method is used to determine if the named interface is included in
     * the passed package definition. If the interface belongs to the package
     * then a value of true is returned. If the interface does not belong to the
     * package a false value is returned.
     * 
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     * 
     * @param iface
     *            The interface to test against the package.
     * @param pkg
     *            The package to check for the inclusion of the interface.
     * 
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    public synchronized boolean interfaceInPackage(String iface, org.opennms.netmgt.config.linkd.Package pkg) {
        ThreadCategory log = log();
    
        boolean filterPassed = false;
    
        // get list of IPs in this package
        java.util.List<String> ipList = m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
            filterPassed = ipList.contains(iface);
        }
    
        if (log.isDebugEnabled())
            log.debug("interfaceInPackage: Interface " + iface + " passed filter for package " + pkg.getName() + "?: " + filterPassed);
    
        if (!filterPassed)
            return false;
        return interfaceInPackageRange(iface, pkg);
    }
    
    public synchronized boolean interfaceInPackageRange(String iface, org.opennms.netmgt.config.linkd.Package pkg) {
    
        //
        // Ensure that the interface is in the specific list or
        // that it is in the include range and is not excluded
        //
        boolean has_specific = false;
        boolean has_range_include = false;
        boolean has_range_exclude = false;
 
        // if there are NO include ranges then treat act as if the user include
        // the range 0.0.0.0 - 255.255.255.255
        has_range_include = pkg.getIncludeRangeCount() == 0 && pkg.getSpecificCount() == 0;
        
        long addr = IPSorter.convertToLong(iface);
        Enumeration<IncludeRange> eincs = pkg.enumerateIncludeRange();
        while (!has_range_include && eincs.hasMoreElements()) {
            IncludeRange rng = eincs.nextElement();
            long start = IPSorter.convertToLong(rng.getBegin());
            if (addr > start) {
                long end = IPSorter.convertToLong(rng.getEnd());
                if (addr <= end) {
                    has_range_include = true;
                }
            } else if (addr == start) {
                has_range_include = true;
            }
        }
    
        Enumeration<String> espec = pkg.enumerateSpecific();
        while (!has_specific && espec.hasMoreElements()) {
            long speca = IPSorter.convertToLong(espec.nextElement());
            if (speca == addr)
                has_specific = true;
        }
    
        Enumeration<String> eurl = pkg.enumerateIncludeUrl();
        while (!has_specific && eurl.hasMoreElements()) {
            has_specific = interfaceInUrl(iface, eurl.nextElement());
        }
    
        Enumeration<ExcludeRange> eex = pkg.enumerateExcludeRange();
        while (!has_range_exclude && !has_specific && eex.hasMoreElements()) {
            ExcludeRange rng = eex.nextElement();
            long start = IPSorter.convertToLong(rng.getBegin());
            if (addr > start) {
                long end = IPSorter.convertToLong(rng.getEnd());
                if (addr <= end) {
                    has_range_exclude = true;
                }
            } else if (addr == start) {
                has_range_exclude = true;
            }
        }
    
        return has_specific || (has_range_include && !has_range_exclude);
    }
    
    /**
     * This method is used to determine if the named interface is included in
     * the passed package's URL includes. If the interface is found in any of
     * the URL files, then a value of true is returned, else a false value is
     * returned.
     * 
     * <pre>
     * 
     *  The file URL is read and each entry in this file checked. Each line
     *   in the URL file can be one of -
     *   &lt;IP&gt;&lt;space&gt;#&lt;comments&gt;
     *   or
     *   &lt;IP&gt;
     *   or
     *   #&lt;comments&gt;
     *  
     *   Lines starting with a '#' are ignored and so are characters after
     *   a '&lt;space&gt;#' in a line.
     *  
     * </pre>
     * 
     * @param addr
     *            The interface to test against the package's URL
     * @param url
     *            The URL file to read
     * 
     * @return True if the interface is included in the URL, false otherwise.
     */
    private boolean interfaceInUrl(String addr, String url) {
        boolean bRet = false;
    
        // get list of IPs in this URL
        java.util.List<String> iplist = m_urlIPMap.get(url);
        if (iplist != null && iplist.size() > 0) {
            bRet = iplist.contains(addr);
        }
    
        return bRet;
    }
    
    @Deprecated
    protected synchronized void reloadXML(Reader reader) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(LinkdConfiguration.class, reader);
        createUrlIpMap();
        createPackageIpListMap();
        getClassNames();
        
    }

    protected synchronized void reloadXML(InputStream stream) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(LinkdConfiguration.class, stream);
        createUrlIpMap();
        createPackageIpListMap();
        getClassNames();
        
    }
    /**
     * Saves the current in-memory configuration to disk and reloads
     */
    public synchronized void save() throws MarshalException, IOException, ValidationException {
    
        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);
        saveXml(stringWriter.toString());
    
        update();
    }

    public List<SnmpCollection> getSnmpCollections(String ipaddr, String sysoid) {
	
    	List<SnmpCollection> snmpcolls = new ArrayList<SnmpCollection>();
		
    	Iterator<String> ite = getAllPackageMatches(ipaddr).iterator();
    	
    	while (ite.hasNext()) {
			snmpcolls.add(populateSnmpCollection(createCollection(ipaddr),getPackage(ite.next()),sysoid));
    	}

    	return snmpcolls;
    }

    public SnmpCollection getSnmpCollection(String ipaddr, String sysoid,String pkgName) {
    	
    	Iterator<String> ite = getAllPackageMatches(ipaddr).iterator();
    	while (ite.hasNext()) {
    		if (ite.next().equals(pkgName))
    				return populateSnmpCollection(createCollection(ipaddr),getPackage(pkgName),sysoid);
    	}
    	return null;
    }

    private SnmpCollection createCollection(String ipaddr) {
		SnmpCollection coll = null;
    	try {
			coll = new SnmpCollection(SnmpPeerFactory
					.getInstance()
					.getAgentConfig(InetAddress.getByName(ipaddr)));
		} catch (Throwable t) {
			log().error(
					"getSnmpCollection: Failed to load snmpcollection parameter from snmp configuration file "
							+ t);
		}

		return coll;
    }
    
    private SnmpCollection populateSnmpCollection(SnmpCollection coll, Package pkg,String sysoid) {
   		
    	coll.setPackageName(pkg.getName());
		
   		coll.setInitialSleepTime(getInitialSleepTime());
    		
   		if (pkg.hasSnmp_poll_interval()) coll.setPollInterval(pkg.getSnmp_poll_interval());
   		else coll.setPollInterval(getSnmpPollInterval());

   		if ( pkg.hasEnableVlanDiscovery() && pkg.getEnableVlanDiscovery() && hasClassName(sysoid)) {
			coll.setVlanClass(getClassName(sysoid));
			if (log().isDebugEnabled())
				log().debug(
							"populateSnmpCollection: found class to get Vlans: "
									+ coll.getVlanClass());
		} else if (!pkg.hasEnableVlanDiscovery() && enableVlanDiscovery() && hasClassName(sysoid)) {
   				coll.setVlanClass(getClassName(sysoid));
				if (log().isDebugEnabled())
					log().debug(
							"populateSnmpCollection: found class to get Vlans: "
									+ coll.getVlanClass());
		} else {
				if (log().isDebugEnabled())
					log()
							.debug(
									"populateSnmpCollection: no class found to get Vlans or VlanDiscoveryDisabled for Package: " + pkg.getName());
		}
			
		if (pkg.hasUseCdpDiscovery()) coll.collectCdpTable(pkg.getUseCdpDiscovery());
		else coll.collectCdpTable(useCdpDiscovery());
		
		boolean condition1 = false;
		boolean condition2 = false;

		if (pkg.hasUseIpRouteDiscovery()) condition1 = pkg.getUseIpRouteDiscovery();
		else condition1 = useIpRouteDiscovery();
		
		if (pkg.hasSaveRouteTable()) condition2 = pkg.getSaveRouteTable();
		else condition2 = saveRouteTable();

		coll.SaveIpRouteTable(condition2);
		coll.collectIpRouteTable(condition1 || condition2);

		if (pkg.hasUseBridgeDiscovery()) condition1 = pkg.getUseBridgeDiscovery();
		else condition1 = useBridgeDiscovery();

		coll.collectBridgeForwardingTable(condition1);

		if (pkg.hasSaveStpNodeTable()) condition2 = pkg.getSaveStpNodeTable();
		else condition2 = saveStpNodeTable();

		coll.saveStpNodeTable(condition2);
		coll.collectStpNode(condition1 || condition2);
		
		if (pkg.hasSaveStpInterfaceTable()) condition2 = pkg.getSaveStpInterfaceTable();
		else condition2 = saveStpInterfaceTable();
		
		coll.saveStpInterfaceTable(condition2);
		coll.collectStpTable(condition1 || condition2);

		return coll;
    }
    
    
    /**
     * Returns the first package that the IP belongs to, null if none.
     * 
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     * 
     * @param ipaddr
     *            the interface to check
     * 
     * @return the first package that the ip belongs to, null if none
     */
    public synchronized org.opennms.netmgt.config.linkd.Package getFirstPackageMatch(String ipaddr) {
        for (org.opennms.netmgt.config.linkd.Package pkg : m_config.getPackageCollection()) {
            boolean inPkg = interfaceInPackage(ipaddr, pkg);
            if (inPkg)
                return pkg;
        }
    
        return null;
    }

    /**
     * Returns a list of package names that the IP belongs to, null if none.
     *                
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param ipaddr
     *            the interface to check
     *
     * @return a list of package names that the ip belongs to, null if none
     */
    public synchronized List<String> getAllPackageMatches(String ipaddr) {
        List<String> matchingPkgs = new ArrayList<String>();
        
        for (org.opennms.netmgt.config.linkd.Package pkg : m_config.getPackageCollection()) {
            String pkgName = pkg.getName();
            boolean inPkg = interfaceInPackage(ipaddr, pkg);
            if (inPkg) {
                matchingPkgs.add(pkgName);
            }
        }
    
        return matchingPkgs;
    }

    public Enumeration<Package> enumeratePackage() {
        return getConfiguration().enumeratePackage();
    }
    
    public DiscoveryLink getDiscoveryLink(String pkgName) {
		DiscoveryLink discoveryLink = new DiscoveryLink();

		Package pkg = getPackage(pkgName);
		
		discoveryLink.setPackageName(pkg.getName());

		if (pkg.hasSnmp_poll_interval()) discoveryLink.setSnmpPollInterval(pkg.getSnmp_poll_interval());
		else discoveryLink.setSnmpPollInterval(getSnmpPollInterval());
		
		if (pkg.hasDiscovery_link_interval()) discoveryLink.setDiscoveryInterval(pkg.getDiscovery_link_interval());
		else discoveryLink.setDiscoveryInterval(getDiscoveryLinkInterval());
		
		discoveryLink.setInitialSleepTime(getInitialSleepTime());
		
		if (pkg.hasUseBridgeDiscovery()) discoveryLink.setDiscoveryUsingBridge(pkg.getUseBridgeDiscovery());
		else discoveryLink.setDiscoveryUsingBridge(useBridgeDiscovery());
		if (pkg.hasUseCdpDiscovery()) discoveryLink.setDiscoveryUsingCdp(pkg.getUseCdpDiscovery());
		else discoveryLink.setDiscoveryUsingCdp(useCdpDiscovery());
		if (pkg.hasUseIpRouteDiscovery()) discoveryLink.setDiscoveryUsingCdp(pkg.getUseIpRouteDiscovery());
		else discoveryLink.setDiscoveryUsingRoutes(useIpRouteDiscovery());
		if (pkg.hasEnableDiscoveryDownload()) discoveryLink.setEnableDownloadDiscovery(pkg.getEnableDiscoveryDownload());
		else discoveryLink.setEnableDownloadDiscovery(enableDiscoveryDownload());
		if (pkg.hasForceIpRouteDiscoveryOnEthernet()) discoveryLink.setForceIpRouteDiscoveryOnEtherNet(pkg.getForceIpRouteDiscoveryOnEthernet());
		else discoveryLink.setForceIpRouteDiscoveryOnEtherNet(forceIpRouteDiscoveryOnEthernet());
		return discoveryLink;
    }

	private boolean forceIpRouteDiscoveryOnEthernet() {
		boolean forceIpRouteDiscoveryOnEthernet = false;

		if (m_config.hasForceIpRouteDiscoveryOnEthernet()) {
			forceIpRouteDiscoveryOnEthernet = m_config.getForceIpRouteDiscoveryOnEthernet();
		}

		return forceIpRouteDiscoveryOnEthernet;
	}
}