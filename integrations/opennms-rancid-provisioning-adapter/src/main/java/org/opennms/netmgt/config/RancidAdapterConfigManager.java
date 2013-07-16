/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.isInetAddressInRange;
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.IpListFromUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.rancid.adapter.ExcludeRange;
import org.opennms.netmgt.config.rancid.adapter.IncludeRange;
import org.opennms.netmgt.config.rancid.adapter.Mapping;
import org.opennms.netmgt.config.rancid.adapter.Package;
import org.opennms.netmgt.config.rancid.adapter.PolicyManage;
import org.opennms.netmgt.config.rancid.adapter.RancidConfiguration;
import org.opennms.netmgt.config.rancid.adapter.Schedule;
import org.opennms.netmgt.filter.FilterDaoFactory;

/**
 * <p>Abstract RancidAdapterConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@openms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
abstract public class RancidAdapterConfigManager implements RancidAdapterConfig {
    private static final Logger LOG = LoggerFactory.getLogger(RancidAdapterConfigManager.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
     
    /**
     * The config class loaded from the config file
     */
     private RancidConfiguration m_config;

    /**
     * A boolean flag to indicate If a filter rule against the local OpenNMS
     * server has to be used.
     */
    private static boolean m_verifyServer;

    /**
     * The name of the local OpenNMS server
     */
    private static String m_localServer;

    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private Map<String, List<String>> m_urlIPMap;
    
    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid database access.
     */
    private Map<Package, List<InetAddress>> m_pkgIpMap;
    
    /**
     * A mapping between policyManage Name and Package
     */
    private Map<Package, PolicyManage> m_pkgPolicyMap;
    
    /**
     * <p>Constructor for RancidAdapterConfigManager.</p>
     *
     * @author <a href="mailto:antonio@opennms.org">Antonio Russo</a>
     * @param reader a {@link java.io.InputStream} object.
     * @param verifyServer a boolean.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @param serverName a {@link java.lang.String} object.
     */
    public RancidAdapterConfigManager(final InputStream reader,final String serverName, final boolean verifyServer) throws MarshalException, ValidationException, IOException {
         m_localServer = serverName;
         m_verifyServer = verifyServer;
         reloadXML(reader);
     }

     /**
      * <p>Constructor for RancidAdapterConfigManager.</p>
      */
     public RancidAdapterConfigManager() {
     }
    
     public Lock getReadLock() {
         return m_readLock;
     }
     
     public Lock getWriteLock() {
         return m_writeLock;
     }

    /**
     * <p>reloadXML</p>
     *
     * @param reader a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected void reloadXML(final InputStream reader) throws MarshalException, ValidationException, IOException {
        getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshal(RancidConfiguration.class, reader);
            createPolicyNamePkgMap();
            createUrlIpMap();
            createPackageIpListMap();
        } finally {
            getWriteLock().unlock();
        }
    }
    
    /**
     * Go throw the rancid configuration and find a map from 
     * policy name and packages
     * 
     */
    private void createPolicyNamePkgMap() {
        m_pkgPolicyMap = new HashMap<Package, PolicyManage>();
        if (hasPolicies()) {
            for (final PolicyManage pm : policies() ) {
                m_pkgPolicyMap.put(pm.getPackage(),pm);
            }
        }
    }
    
    /**
     * Go through the rancid adapter configuration and build a mapping of each
     * configured URL to a list of IPs configured in that URL - done at init()
     * time so that repeated file reads can be avoided
     */
    private void createUrlIpMap() {
        m_urlIPMap = new HashMap<String, List<String>>();
    
        if (hasPolicies()) {
            for (final Package pkg: packages() ) {        
                for(final String url : includeURLs(pkg)) {
                    final List<String> iplist = IpListFromUrl.parse(url);
                    if (iplist.size() > 0) {
                        m_urlIPMap.put(url, iplist);
                    }
                }
    
            }
    
        }
    }
    
    /**
     * This method is used to establish package against iplist mapping, with
     * which, the iplist is selected per package via the configured filter rules
     * from the database.
     */
    private void createPackageIpListMap() {
        getWriteLock().lock();
        try {
            m_pkgIpMap = new HashMap<Package, List<InetAddress>>();
            
            if (hasPolicies()) {
                for (final Package pkg: packages() ) {
                    // Get a list of ipaddress per package agaist the filter rules from
                    // database and populate the package, IP list map.
                    //
                    try {
                        final List<InetAddress> ipList = getIpList(pkg);
                        LOG.debug("createPackageIpMap: package {}: ipList size = {}", pkg.getName(), ipList.size());
            
                        if (ipList.size() > 0) {
                            m_pkgIpMap.put(pkg, ipList);
                        }
                    } catch (final Throwable t) {
                        LOG.error("createPackageIpMap: failed to map package: {} to an IP List with filter \"{}\"", pkg.getName(), pkg.getFilter().getContent(), t);
                    }
        
                }
            }
        } finally {
            getWriteLock().unlock();
        }
    }
    
    private List<InetAddress> getIpList(final Package pkg) {
        final StringBuffer filterRules = new StringBuffer(pkg.getFilter().getContent());
        if (m_verifyServer) {
            filterRules.append(" & (serverName == ");
            filterRules.append('\"');
            filterRules.append(m_localServer);
            filterRules.append('\"');
            filterRules.append(")");
        }
        LOG.debug("createPackageIpMap: package is {}. filter rules are {}", pkg.getName(), filterRules);
        return FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());
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
     * TODO: Factor this method out so that it can be reused? Or use an existing
     * utility method if one exists?
     * 
     * @param iface
     *            The interface to test against the package.
     * @param pkg
     *            The package to check for the inclusion of the interface.
     * 
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    private boolean interfaceInPackage(final String iface, final Package pkg) {
        boolean filterPassed = false;
        final InetAddress ifaceAddr = addr(iface);
    
        // get list of IPs in this package
        final List<InetAddress> ipList = m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
			filterPassed = ipList.contains(ifaceAddr);
        }
    
        LOG.debug("interfaceInPackage: Interface {} passed filter for package {}?: {}", iface, pkg.getName(), Boolean.valueOf(filterPassed));
    
        if (!filterPassed) return false;
    
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
        
        for (IncludeRange rng : pkg.getIncludeRange()) {
            if (isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                has_range_include = true;
                break;
            }
        }

        byte[] addr = toIpAddrBytes(iface);

        for (String spec : pkg.getSpecific()) {
            byte[] speca = toIpAddrBytes(spec);
            if (new ByteArrayComparator().compare(speca, addr) == 0) {
                has_specific = true;
                break;
            }
        }

        Enumeration<String> eurl = pkg.enumerateIncludeUrl();
        while (!has_specific && eurl.hasMoreElements()) {
            has_specific = interfaceInUrl(iface, eurl.nextElement());
        }
    
        for (ExcludeRange rng : pkg.getExcludeRangeCollection()) {
            if (isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                has_range_exclude = true;
                break;
            }
        }
    
        return has_specific || (has_range_include && !has_range_exclude);
    }

    /**
     * This method is used to determine if the named interface is included in
     * the passed package's url includes. If the interface is found in any of
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
     *            The url file to read
     * 
     * @return True if the interface is included in the url, false otherwise.
     */
    private boolean interfaceInUrl(final String addr, final String url) {
        boolean bRet = false;
    
        // get list of IPs in this URL
        final List<String> iplist = m_urlIPMap.get(url);
        if (iplist != null && iplist.size() > 0) {
            bRet = iplist.contains(addr);
        }
    
        return bRet;
    }
    
    /**
     * Returns a list of package names that the ip belongs to, null if none.
     *                
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     *
     * @param ipaddr
     *            the interface to check
     *
     * @return a list of package names that the ip belongs to, null if none
     */
    private List<String> getAllPackageMatches(final String ipaddr) {
        final List<String> matchingPkgs = new ArrayList<String>();

        for(final Package pkg : packages()) {
            if (interfaceInPackage(ipaddr, pkg)) {
                matchingPkgs.add(pkg.getName());
            }
        }
    
        return matchingPkgs;
    }
    
    

    /** {@inheritDoc} */
    @Override
    public long getDelay(final String ipaddr) {
        getReadLock().lock();
        try {
            if (hasPolicyManage(ipaddr) && getPolicyManageWithoutTesting(ipaddr).hasDelay()) {
                return getPolicyManageWithoutTesting(ipaddr).getDelay();
            }
            return getConfiguration().getDelay();
        } finally {
            getReadLock().unlock();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public int getRetries(final String ipaddr) {
        getReadLock().lock();
        try {
            if (hasPolicyManage(ipaddr) && getPolicyManage(ipaddr).hasRetries()) {
                return getPolicyManageWithoutTesting(ipaddr).getRetries();
            }
            return getConfiguration().getRetries();
        } finally {
            getReadLock().unlock();
        }
    }
            
    /** {@inheritDoc} */
    @Override
    public boolean useCategories(final String ipaddr) {
        getReadLock().lock();
        try {
            if (hasPolicyManage(ipaddr) && getPolicyManage(ipaddr).hasUseCategories()) {
                return getPolicyManageWithoutTesting(ipaddr).getUseCategories();
            }
            return getConfiguration().getUseCategories();
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getType(final String sysoid) {
        getReadLock().lock();
        try {
            if (sysoid != null) {
                for (final Mapping map: mappings()) {
                    if (sysoid.startsWith(map.getSysoidMask()))
                    return map.getType();
                }
            }
            return getConfiguration().getDefaultType();
        } finally {
            getReadLock().unlock();
        }
    }
    

    /** {@inheritDoc} */
    @Override
    public boolean isCurTimeInSchedule(final String ipaddr) {
        getReadLock().lock();
        try {
            if (hasSchedule(ipaddr)) {
                final Calendar cal = new GregorianCalendar();
                for(final Schedule schedule : getSchedules(ipaddr)) {
                    if (isTimeInSchedule(cal, schedule)) return true;
                }
                return false;
            }
            return true;
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * Return if time is part of specified outage.
     * 
     * @param cal
     *            the calendar to lookup
     * @param outage
     *            the outage
     * 
     * @return true if time is in outage
     */
    private boolean isTimeInSchedule(final Calendar cal, final Schedule schedule) {
        return BasicScheduleUtils.isTimeInSchedule(cal, BasicScheduleUtils.getRancidSchedule(schedule));
    }


    private boolean hasPolicies() {
        return (getConfiguration().getPolicies() != null);
    }
 
    private boolean hasPolicyManage(final String ipaddress) {
        return (getAllPackageMatches(ipaddress).size() > 0);
    }

    private PolicyManage getPolicyManage(final String ipaddr) {
       if (hasPolicyManage(ipaddr)) {
           return getPolicyManageWithoutTesting(ipaddr);
       }
       return null;
    }

    private PolicyManage getPolicyManageWithoutTesting(final String ipaddr) {
        final String pkgname = getAllPackageMatches(ipaddr).get(0);
        final Iterator<Entry<Package,PolicyManage>> ite = m_pkgPolicyMap.entrySet().iterator();
        while (ite.hasNext()) {
            final Entry<Package,PolicyManage> entry = ite.next();
            if (entry.getKey().getName().equals(pkgname)) {
                return entry.getValue();
            }
        }        
        return null;
    }


    /**
     * <p>hasSchedule</p>
     *
     * @param ipaddress a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasSchedule(final String ipaddress) {
        getReadLock().lock();
        try {
            if (hasPolicyManage(ipaddress)) {
                return (getPolicyManageWithoutTesting(ipaddress).getScheduleCount() > 0);
            }
            return false;
        } finally {
            getReadLock().unlock();
        }
    }


    /**
     * <p>getSchedules</p>
     *
     * @param ipaddress a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Schedule> getSchedules(final String ipaddress) {
        getReadLock().lock();
        try {
            if (hasPolicyManage(ipaddress)) {
                return getPolicyManageWithoutTesting(ipaddress).getScheduleCollection();
            }
            return new ArrayList<Schedule>();
        } finally {
            getReadLock().unlock();
        }
    }
    

    /**
     * <p>packages</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Package> packages() {
        getReadLock().lock();
        try {
            final List<Package> pkgs = new ArrayList<Package>();
            if (hasPolicies()) {
                for (final PolicyManage pm : policies() ) {
                    pkgs.add(pm.getPackage());
                }
            }
            return pkgs;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>mappings</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Mapping> mappings() {
        getReadLock().lock();
        try {
            return getConfiguration().getMappingCollection();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>policies</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<PolicyManage> policies() {
        getReadLock().lock();
        try {
            return getConfiguration().getPolicies().getPolicyManageCollection();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>includeURLs</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.rancid.adapter.Package} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<String> includeURLs(final Package pkg) {
        getReadLock().lock();
        try {
            return pkg.getIncludeUrlCollection();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the Rancid Adapter configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.rancid.adapter.RancidConfiguration} object.
     */
    public RancidConfiguration getConfiguration() {
        getReadLock().lock();
        try {
            return m_config;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * This method is used to rebuild the package against iplist mapping when
     * needed. When a node gained service event occurs, poller has to determine
     * which package the ip/service combination is in, but if the interface is a
     * newly added one, the package iplist should be rebuilt so that poller
     * could know which package this ip/service pair is in.
     */
    public void rebuildPackageIpListMap() {
        createPackageIpListMap();
    }
    
}
