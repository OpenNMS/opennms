/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.opennms.core.utils.InetAddressUtils.isInetAddressInRange;
import static org.opennms.core.utils.InetAddressUtils.str;
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.IpListFromUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.config.linkd.ExcludeRange;
import org.opennms.netmgt.config.linkd.Filter;
import org.opennms.netmgt.config.linkd.IncludeRange;
import org.opennms.netmgt.config.linkd.Iproutes;
import org.opennms.netmgt.config.linkd.LinkdConfiguration;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.config.linkd.Vendor;
import org.opennms.netmgt.config.linkd.Vlans;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.protocols.snmp.SnmpObjectId;

/**
 * <p>Abstract LinkdConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
abstract public class LinkdConfigManager implements LinkdConfig {
    private static final Logger LOG = LoggerFactory.getLogger(LinkdConfigManager.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
	public static final String DEFAULT_IP_ROUTE_CLASS_NAME = "org.opennms.netmgt.linkd.snmp.IpCidrRouteTable";

    /**
	 * Object containing all Linkd-configuration objects parsed from the XML
	 * file
	 */
	protected static LinkdConfiguration m_config;

    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private static Map<String, List<String>> m_urlIPMap = new HashMap<String, List<String>>();

    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid redundant database access.
     */
    private static Map<org.opennms.netmgt.config.linkd.Package, List<InetAddress>> m_pkgIpMap = new HashMap<org.opennms.netmgt.config.linkd.Package, List<InetAddress>>();

	/**
	 * The HashMap that associates the OIDS masks to class name for Vlans
	 */
	private static Map<String,String> m_oidMask2VlanclassName = new HashMap<String,String>();

	/**
	 * The HashMap that associates the OIDS masks to class name for IpRoutes
	 */
	 private static Map<String,String> m_oidMask2IpRouteclassName = new HashMap<String,String>();
	 
    /**
     * <p>Constructor for LinkdConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public LinkdConfigManager() {
    }

    @Override
    public Lock getReadLock() {
        return m_readLock;
    }
    
    @Override
    public Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * Whether autodiscovery is enabled in linkd-config (default: false)
     */
    @Override
    public boolean isAutoDiscoveryEnabled() {
        getReadLock().lock();
        try {
            if (m_config.hasAutoDiscovery()) return m_config.getAutoDiscovery();
        } finally {
            getReadLock().unlock();
        }
        return false;
    }

    /**
     * Whether vlan discovery is enabled in linkd-config (default: true)
     */
    @Override
    public boolean isVlanDiscoveryEnabled() {
        getReadLock().lock();
        try {
            if (m_config.hasEnableVlanDiscovery()) return m_config.getEnableVlanDiscovery();
        } finally {
            getReadLock().unlock();
        }
        return true;
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
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    @Override
    public boolean isInterfaceInPackage(final InetAddress iface, final org.opennms.netmgt.config.linkd.Package pkg) {
        boolean filterPassed = false;
    
        getReadLock().lock();
        
        try {
            // get list of IPs in this package
            final List<InetAddress> ipList = m_pkgIpMap.get(pkg);
            if (ipList != null && ipList.size() > 0) {
				filterPassed = ipList.contains(iface);
            }
        
            LOG.debug("interfaceInPackage: Interface {} passed filter for package {}?: {}", str(iface), pkg.getName(), (filterPassed? "True":"False"));
        
            if (!filterPassed) return false;
    
            return isInterfaceInPackageRange(iface, pkg);
        } finally {
            getReadLock().unlock();
        }
    }
    
    @Override
    public boolean isInterfaceInPackageRange(final InetAddress iface, final org.opennms.netmgt.config.linkd.Package pkg) {
        if (pkg == null) return false;

        //
        // Ensure that the interface is in the specific list or
        // that it is in the include range and is not excluded
        //
        boolean has_specific = false;
        boolean has_range_include = false;
        boolean has_range_exclude = false;
 
        getReadLock().lock();
        try {
            byte[] addr = iface.getAddress();
    
            // if there are NO include ranges then treat act as if the user include
            // the range 0.0.0.0 - 255.255.255.255
            has_range_include = pkg.getIncludeRangeCount() == 0 && pkg.getSpecificCount() == 0;
    
            // Specific wins; if we find one, return immediately.
            for (final String spec : pkg.getSpecificCollection()) {
                final byte[] speca = toIpAddrBytes(spec);
                if (new ByteArrayComparator().compare(addr, speca) == 0) {
                    has_specific = true;
                    break;
                }
            }
            if (has_specific) return true;
    
            for (final String url : pkg.getIncludeUrlCollection()) {
                has_specific = isInterfaceInUrl(iface, url);
                if (has_specific) break;
            }
            if (has_specific) return true;
    
            if (!has_range_include) {
                for (final IncludeRange rng : pkg.getIncludeRangeCollection()) {
                    if (isInetAddressInRange(iface.getAddress(), rng.getBegin(), rng.getEnd())) {
                        has_range_include = true;
                        break;
                    }
                }
            }
    
            for (final ExcludeRange rng : pkg.getExcludeRangeCollection()) {
                if (isInetAddressInRange(iface.getAddress(), rng.getBegin(), rng.getEnd())) {
                    has_range_exclude = true;
                    break;
                }
            }
    
            return has_range_include && !has_range_exclude;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>enumeratePackage</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    @Override
    public Enumeration<Package> enumeratePackage() {
        getReadLock().lock();
        try {
            return getConfiguration().enumeratePackage();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the linkd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.linkd.LinkdConfiguration} object.
     */
    @Override
    public LinkdConfiguration getConfiguration() {
        getReadLock().lock();
        try {
            return m_config;
        } finally {
            getReadLock().unlock();
        }
    }

	/** {@inheritDoc} */
    @Override
	public org.opennms.netmgt.config.linkd.Package getPackage(final String name) {
	    getReadLock().lock();
	    try {
            for (final org.opennms.netmgt.config.linkd.Package thisPackage : m_config.getPackageCollection()) {
                final String n = thisPackage.getName();
                if (n != null && n.equals(name)) {
                    return thisPackage;
                }
            }
	    } finally {
	        getReadLock().unlock();
	    }
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public List<InetAddress> getIpList(final Package pkg) {
        getReadLock().lock();
        
        try {
            if (pkg == null) return null;
    
            final Filter filter = pkg.getFilter();
            if (filter == null) return null;
    
            final StringBuffer filterRules = new StringBuffer(filter.getContent());
    
            LOG.debug("getIpList: package is {}. filter rules are: {}", pkg.getName(), filterRules);
            return FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getIpRouteClassName(final String sysoid) {
        getReadLock().lock();
        try {
            for (final String oidMask : m_oidMask2IpRouteclassName.keySet()) {
                if (sysoid.startsWith(oidMask)) {
                    return m_oidMask2IpRouteclassName.get(oidMask);
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return DEFAULT_IP_ROUTE_CLASS_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public String getVlanClassName(final String sysoid) {
        getReadLock().lock();
        try {
            for (final String oidMask : m_oidMask2VlanclassName.keySet()) {
                if (sysoid.startsWith(oidMask)) {
                    return m_oidMask2VlanclassName.get(oidMask);
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the first package that the IP belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     */
    @Override
    public org.opennms.netmgt.config.linkd.Package getFirstPackageMatch(final InetAddress ipaddr) {
        getReadLock().lock();
        try {
            for (final org.opennms.netmgt.config.linkd.Package pkg : m_config.getPackageCollection()) {
                if (isInterfaceInPackage(ipaddr, pkg)) {
                    return pkg;
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Returns a list of package names that the IP belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     */
    @Override
    public List<String> getAllPackageMatches(final InetAddress ipaddr) {
        final List<String> matchingPkgs = new ArrayList<String>();
        
        getReadLock().lock();
        try {
            for (final org.opennms.netmgt.config.linkd.Package pkg : m_config.getPackageCollection()) {
                final String pkgName = pkg.getName();
                if (isInterfaceInPackage(ipaddr, pkg)) {
                    matchingPkgs.add(pkgName);
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return matchingPkgs;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasClassName(final String sysoid) {
        getReadLock().lock();
        try {
            for (final String oidMask : m_oidMask2VlanclassName.keySet()) {
                if (sysoid.startsWith(oidMask)) {
                    return true;
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getDefaultIpRouteClassName() {
        return DEFAULT_IP_ROUTE_CLASS_NAME;
    }

    /**
     * <p>useIpRouteDiscovery</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean useIpRouteDiscovery() {
        if (m_config.hasUseIpRouteDiscovery()) return m_config.getUseIpRouteDiscovery();
        return true;
    }

    /**
     * <p>saveRouteTable</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean saveRouteTable() {
        if (m_config.hasSaveRouteTable()) return m_config.getSaveRouteTable();
        return true;
    }

    /**
     * <p>useCdpDiscovery</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean useCdpDiscovery() {
        if (m_config.hasUseCdpDiscovery()) return m_config.getUseCdpDiscovery();
        return true;
    }
    
    /**
     * <p>useBridgeDiscovery</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean useBridgeDiscovery() {
        if (m_config.hasUseBridgeDiscovery()) return m_config.getUseBridgeDiscovery();
        return true;
    }

    /**
     * <p>useLldpDiscovery</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean useLldpDiscovery() {
        if (m_config.hasUseLldpDiscovery()) return m_config.getUseLldpDiscovery();
        return true;
    }

    /**
     * <p>useOspfDiscovery</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean useOspfDiscovery() {
        if (m_config.hasUseOspfDiscovery()) return m_config.getUseOspfDiscovery();
        return true;
    }

    /**
     * <p>saveStpNodeTable</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean saveStpNodeTable() {
        if (m_config.hasSaveStpNodeTable()) return m_config.getSaveStpNodeTable();
        return true;
    }
    
    /**
     * <p>enableDiscoveryDownload</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean enableDiscoveryDownload() {
        if (m_config.hasEnableDiscoveryDownload()) return m_config.getEnableDiscoveryDownload();
        return false;
    }   
    
    /**
     * <p>saveStpInterfaceTable</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean saveStpInterfaceTable() {
        if (m_config.hasSaveStpInterfaceTable()) return m_config.getSaveStpInterfaceTable();
        return true;
    }

    @Override
    public long getInitialSleepTime() {
        if (m_config.hasInitial_sleep_time()) return m_config.getInitial_sleep_time();
        return 1800000;
    }

    @Override
    public long getSnmpPollInterval() {
        if (m_config.hasSnmp_poll_interval()) return m_config.getSnmp_poll_interval();
        return 900000;
    }

    @Override
    public long getDiscoveryLinkInterval() {
        if (m_config.hasSnmp_poll_interval()) return m_config.getDiscovery_link_interval();
        return 3600000;
    }

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    @Override
    public int getThreads() {
        if (m_config.hasThreads()) return m_config.getThreads();
        return 5;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasIpRouteClassName(final String sysoid) {
        for (final String oidMask : m_oidMask2IpRouteclassName.keySet()) {
            if (sysoid.startsWith(oidMask)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>update</p>
     *
     */
    @Override
    public abstract void update(); 

    /**
     * This method is used to establish package against IP list mapping, with
     * which, the IP list is selected per package via the configured filter rules
     * from the database.
     */
    protected void updatePackageIpListMap() {
        m_pkgIpMap.clear();
        getWriteLock().lock();
        try {
            for (final org.opennms.netmgt.config.linkd.Package pkg : m_config.getPackageCollection()) {
                //
                // Get a list of IP addresses per package against the filter rules from
                // database and populate the package, IP list map.
                //
                try {
                    final List<InetAddress> ipList = getIpList(pkg);
                    LOG.trace("createPackageIpMap: package {}: ipList size = {}", pkg.getName(), ipList.size());
    
                    if (ipList != null && ipList.size() > 0) {
                        m_pkgIpMap.put(pkg, ipList);
                    }
                } catch (final Throwable t) {
                    LOG.error("createPackageIpMap: failed to map package: {} to an IP list", pkg.getName(), t);
                }
            }
        } finally {
            getWriteLock().unlock();
        }
    }

    
   protected void updateUrlIpMap() {
       m_urlIPMap.clear();
        for (final org.opennms.netmgt.config.linkd.Package pkg : m_config.getPackageCollection()) {
            if (pkg == null) continue;
            for (final String urlname : pkg.getIncludeUrlCollection()) {
                final java.util.List<String> iplist = IpListFromUrl.parse(urlname);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(urlname, iplist);
                }
            }
        }
    }

    protected void updateIpRouteClassNames() {
        m_oidMask2IpRouteclassName.clear();
        getWriteLock().lock();
        try {
            final Iproutes iproutes = m_config.getIproutes();
            if (iproutes == null) {
                LOG.info("no iproutes found in config");
                return;
            }

            for (final Vendor vendor : iproutes.getVendorCollection()) {
                final SnmpObjectId curRootSysOid = new SnmpObjectId(vendor.getSysoidRootMask());
                final String curClassName = vendor.getClassName();

                for (final String specific : vendor.getSpecific()) {
                    final SnmpObjectId oidMask = new SnmpObjectId(specific);
                    oidMask.prepend(curRootSysOid);
                    m_oidMask2IpRouteclassName.put(oidMask.toString(), curClassName);
                    LOG.debug("initializeIpRouteClassNames:  adding class {} for oid {}", curClassName, oidMask);
                }
            }
        } finally {
            getWriteLock().unlock();
        }
    }

	protected void updateVlanClassNames() {
	    m_oidMask2VlanclassName.clear();
	    getWriteLock().lock();
	    try {
    		final Vlans vlans = m_config.getVlans();
    		if (vlans == null) {
    		    LOG.info("initializeVlanClassNames: no vlans found in config");
    		}
    
            final List<String> excludedOids = new ArrayList<String>();
    		for (final Vendor vendor : vlans.getVendorCollection()) {
    		    final SnmpObjectId curRootSysOid = new SnmpObjectId(vendor.getSysoidRootMask());
    		    final String curClassName = vendor.getClassName();
    
    			for (final String specific : vendor.getSpecific()) {
    			    final SnmpObjectId oidMask = new SnmpObjectId(specific);
    				oidMask.prepend(curRootSysOid);
    				m_oidMask2VlanclassName.put(oidMask.toString(), curClassName);
    				LOG.debug("initializeVlanClassNames:  adding class {} for oid {}", curClassName, oidMask);
    			}
    
    			for (final ExcludeRange excludeRange : vendor.getExcludeRangeCollection()) {
    			    final SnmpObjectId snmpBeginOid = new SnmpObjectId(excludeRange.getBegin());
    			    final SnmpObjectId snmpEndOid = new SnmpObjectId(excludeRange.getEnd());
    				final SnmpObjectId snmpRootOid = getRootOid(snmpBeginOid);
    				if (snmpBeginOid.getLength() == snmpEndOid.getLength() && snmpRootOid.isRootOf(snmpEndOid)) {
    				    final SnmpObjectId snmpCurOid = new SnmpObjectId(snmpBeginOid);
    					while (snmpCurOid.compare(snmpEndOid) <= 0) {
    						excludedOids.add(snmpCurOid.toString());
    						LOG.debug("initializeVlanClassNames:  signing excluded class {} for oid {}", curClassName, curRootSysOid.toString().concat(snmpCurOid.toString()));
    						int lastCurCipher = snmpCurOid.getLastIdentifier();
    						lastCurCipher++;
    						int[] identifiers = snmpCurOid.getIdentifiers();
    						identifiers[identifiers.length - 1] = lastCurCipher;
    						snmpCurOid.setIdentifiers(identifiers);
    					}
    				}
    			}
    
    			for (final IncludeRange includeRange : vendor.getIncludeRangeCollection()) {
    			    final SnmpObjectId snmpBeginOid = new SnmpObjectId(includeRange.getBegin());
    				final SnmpObjectId snmpEndOid = new SnmpObjectId(includeRange.getEnd());
    				final SnmpObjectId rootOid = getRootOid(snmpBeginOid);
    				if (snmpBeginOid.getLength() == snmpEndOid.getLength() && rootOid.isRootOf(snmpEndOid)) {
    					final SnmpObjectId snmpCurOid = new SnmpObjectId(snmpBeginOid);
    					while (snmpCurOid.compare(snmpEndOid) <= 0) {
    						if (!excludedOids.contains(snmpBeginOid.toString())) {
    							final SnmpObjectId oidMask = new SnmpObjectId(snmpBeginOid);
    							oidMask.prepend(curRootSysOid);
    							m_oidMask2VlanclassName.put(oidMask.toString(), curClassName);
    							LOG.debug("initializeVlanClassNames:  adding class {} for oid {}", curClassName, oidMask);
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
	    } finally {
	        getWriteLock().unlock();
	    }
	}

    
	private SnmpObjectId getRootOid(final SnmpObjectId snmpObj) {
        getReadLock().lock();
        try {
    	    final int[] identifiers = snmpObj.getIdentifiers();
    		final int[] rootIdentifiers = new int[identifiers.length - 1];
    		for (int i = 0; i < identifiers.length - 1; i++) {
    			rootIdentifiers[i] = identifiers[i];
    		}
    		return new SnmpObjectId(rootIdentifiers);
        } finally {
            getReadLock().unlock();
        }
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
    private boolean isInterfaceInUrl(final InetAddress addr, final String url) {
        getReadLock().lock();

        try {
            // get list of IPs in this URL
            final List<String> iplist = m_urlIPMap.get(url);
            if (iplist != null && iplist.size() > 0) {
                return iplist.contains(InetAddressUtils.str(addr));
            }
        } finally {
            getReadLock().unlock();
        }
        return false;
    }

    /**
     * <p>saveXml</p>
     *
     * @param xml a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXml(final String xml) throws IOException;
    
    @Override
	public boolean forceIpRouteDiscoveryOnEthernet() {
		if (m_config.hasForceIpRouteDiscoveryOnEthernet()) return m_config.getForceIpRouteDiscoveryOnEthernet();
		return false;
	}
}
