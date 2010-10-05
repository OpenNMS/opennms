//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 06: Eliminate a warning. - dj@opennms.org
// 2006 Apr 27: Added support for pathOutageEnabled
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.IpListFromUrl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.rancid.adapter.ExcludeRange;
import org.opennms.netmgt.config.rancid.adapter.IncludeRange;
import org.opennms.netmgt.config.rancid.adapter.Mapping;
import org.opennms.netmgt.config.rancid.adapter.Package;
import org.opennms.netmgt.config.rancid.adapter.PolicyManage;
import org.opennms.netmgt.config.rancid.adapter.RancidConfiguration;
import org.opennms.netmgt.config.rancid.adapter.Schedule;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.filter.FilterDaoFactory;

/**
 * <p>Abstract RancidAdapterConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@openms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:antonio@openms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:antonio@openms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
abstract public class RancidAdapterConfigManager implements RancidAdapterConfig {
     
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
   public RancidAdapterConfigManager(InputStream reader,String serverName, boolean verifyServer) throws MarshalException, ValidationException, IOException {
        m_localServer = serverName;
        m_verifyServer = verifyServer;
        reloadXML(reader);
    }

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
    private Map<Package, List<String>> m_pkgIpMap;
    
    /**
     * A mapping between policyManage Name and Package
     */
    private Map<Package, PolicyManage> m_pkgPolicyMap;
    
     /**
      * <p>Constructor for RancidAdapterConfigManager.</p>
      */
     public RancidAdapterConfigManager() {
     }
    

    /**
     * <p>reloadXML</p>
     *
     * @param reader a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected synchronized void reloadXML(InputStream reader) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(RancidConfiguration.class, reader);
        createPolicyNamePkgMap();
        createUrlIpMap();
        createPackageIpListMap();
    }
    
    /**
     * Go throw the rancid configuration and find a map from 
     * policy name and packages
     * 
     */
    private void createPolicyNamePkgMap() {
        m_pkgPolicyMap = new HashMap<Package, PolicyManage>();
        if (hasPolicies()) {
            for (PolicyManage pm: policies() ) {
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
            for (Package pkg: packages() ) {        
                for(String url : includeURLs(pkg)) {
        
                    List<String> iplist = IpListFromUrl.parse(url);
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
        m_pkgIpMap = new HashMap<Package, List<String>>();
        
        
        if (hasPolicies()) {
            for (Package pkg: packages() ) {
                // Get a list of ipaddress per package agaist the filter rules from
                // database and populate the package, IP list map.
                //
                try {
                    List<String> ipList = getIpList(pkg);
                    if (log().isDebugEnabled())
                        log().debug("createPackageIpMap: package " + pkg.getName() + ": ipList size =  " + ipList.size());
        
                    if (ipList.size() > 0) {
                        if (log().isDebugEnabled())
                            log().debug("createPackageIpMap: package " + pkg.getName() + ". IpList size is " + ipList.size());
                        m_pkgIpMap.put(pkg, ipList);
                    }
                } catch (Throwable t) {
                    log().error("createPackageIpMap: failed to map package: " + pkg.getName() + " to an IP List: " + t, t);
                }
    
            }
        }
    }
    
    private List<String> getIpList(Package pkg) {
        StringBuffer filterRules = new StringBuffer(pkg.getFilter().getContent());
        if (m_verifyServer) {
            filterRules.append(" & (serverName == ");
            filterRules.append('\"');
            filterRules.append(m_localServer);
            filterRules.append('\"');
            filterRules.append(")");
        }
        if (log().isDebugEnabled())
            log().debug("createPackageIpMap: package is " + pkg.getName() + ". filer rules are  " + filterRules.toString());
        List<String> ipList = FilterDaoFactory.getInstance().getIPList(filterRules.toString());
        return ipList;
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
    private synchronized boolean interfaceInPackage(String iface, Package pkg) {
        ThreadCategory log = log();
    
        boolean filterPassed = false;
    
        // get list of IPs in this package
        List<String> ipList = m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
            filterPassed = ipList.contains(iface);
        }
    
        if (log.isDebugEnabled())
            log.debug("interfaceInPackage: Interface " + iface + " passed filter for package " + pkg.getName() + "?: " + filterPassed);
    
        if (!filterPassed)
            return false;
    
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
        
        long addr = InetAddressUtils.toIpAddrLong(iface);
        
        Enumeration<IncludeRange> eincs = pkg.enumerateIncludeRange();
        while (!has_range_include && eincs.hasMoreElements()) {
            IncludeRange rng = eincs.nextElement();
            long start = InetAddressUtils.toIpAddrLong(rng.getBegin());
            if (addr > start) {
                long end = InetAddressUtils.toIpAddrLong(rng.getEnd());
                if (addr <= end) {
                    has_range_include = true;
                }
            } else if (addr == start) {
                has_range_include = true;
            }
        }
    
        Enumeration<String> espec = pkg.enumerateSpecific();
        while (!has_specific && espec.hasMoreElements()) {
            long speca = InetAddressUtils.toIpAddrLong(espec.nextElement());
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
            long start = InetAddressUtils.toIpAddrLong(rng.getBegin());
            if (addr > start) {
                long end = InetAddressUtils.toIpAddrLong(rng.getEnd());
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
    private boolean interfaceInUrl(String addr, String url) {
        boolean bRet = false;
    
        // get list of IPs in this URL
        List<String> iplist = m_urlIPMap.get(url);
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
    private synchronized List<String> getAllPackageMatches(String ipaddr) {
    
        List<String> matchingPkgs = new ArrayList<String>();

        for(Package pkg : packages()) {

            boolean inPkg = interfaceInPackage(ipaddr, pkg);
            if (inPkg) {
                matchingPkgs.add(pkg.getName());
            }

        }
    
        return matchingPkgs;
    }
    
    

    /** {@inheritDoc} */
    public long getDelay(String ipaddr) {
        if (hasPolicyManage(ipaddr) && getPolicyManageWithoutTesting(ipaddr).hasDelay())   
            return getPolicyManageWithoutTesting(ipaddr).getDelay();
        return getConfiguration().getDelay();
    }
    
    /** {@inheritDoc} */
    public int getRetries(String ipaddr) {
        if (hasPolicyManage(ipaddr) && getPolicyManage(ipaddr).hasRetries())
            return getPolicyManageWithoutTesting(ipaddr).getRetries();
        return getConfiguration().getRetries();
    }
            
    /** {@inheritDoc} */
    public boolean useCategories(String ipaddr) {
        if (hasPolicyManage(ipaddr) && getPolicyManage(ipaddr).hasUseCategories())
            return getPolicyManageWithoutTesting(ipaddr).getUseCategories();
        return getConfiguration().getUseCategories();
    }

    /** {@inheritDoc} */
    public String getType(String sysoid){
        if (sysoid != null) {
            for (Mapping map: mappings()) {
                if (sysoid.startsWith(map.getSysoidMask()))
                return map.getType();
            }
        }
        return getConfiguration().getDefaultType();
    }
    

    /** {@inheritDoc} */
    public boolean isCurTimeInSchedule(String ipaddr){
        if (hasSchedule(ipaddr)) {
            Calendar cal = new GregorianCalendar();
            for(Schedule schedule: getSchedules(ipaddr)) {
                if (isTimeInSchedule(cal, schedule)) return true;
            }
            return false;
        }
        return true;
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
    private synchronized boolean isTimeInSchedule(Calendar cal, Schedule schedule) {
        return BasicScheduleUtils.isTimeInSchedule(cal, schedule);

    }


    private boolean hasPolicies() {
        return (getConfiguration().getPolicies() != null);
    }
 
    private boolean hasPolicyManage(String ipaddress) {
        return (getAllPackageMatches(ipaddress).size() > 0);
    }

    private PolicyManage getPolicyManage(String ipaddr) {
       if (hasPolicyManage(ipaddr)) {
           return getPolicyManageWithoutTesting(ipaddr);
       }
       return null;
    }

    private PolicyManage getPolicyManageWithoutTesting(String ipaddr) {
        String pkgname = getAllPackageMatches(ipaddr).get(0);
        Iterator<Entry<Package,PolicyManage>> ite = m_pkgPolicyMap.entrySet().iterator();
        while (ite.hasNext()) {
            Entry<Package,PolicyManage> entry = ite.next();
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
    public boolean hasSchedule(String ipaddress) {
        if (hasPolicyManage(ipaddress))   
            return (getPolicyManageWithoutTesting(ipaddress).getScheduleCount() > 0);
        return false;
    }


    /**
     * <p>getSchedules</p>
     *
     * @param ipaddress a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Schedule> getSchedules(String ipaddress){
        if (hasPolicyManage(ipaddress))   
               return getPolicyManageWithoutTesting(ipaddress).getScheduleCollection();
        return new ArrayList<Schedule>();
    }
    

    /**
     * <p>packages</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Package> packages() {
        List<Package> pkgs = new ArrayList<Package>();
        if (hasPolicies()) {
            for (PolicyManage pm: policies() ) {
                pkgs.add(pm.getPackage());
            }
        }
        return pkgs;
    }

    /**
     * <p>mappings</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Mapping> mappings() {
        return getConfiguration().getMappingCollection();
    }

    /**
     * <p>policies</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<PolicyManage> policies() {
        return getConfiguration().getPolicies().getPolicyManageCollection();
    }

    /**
     * <p>includeURLs</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.rancid.adapter.Package} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<String> includeURLs(Package pkg) {
        return pkg.getIncludeUrlCollection();
    }

    /**
     * Return the Rancid Adapter configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.rancid.adapter.RancidConfiguration} object.
     */
    public synchronized RancidConfiguration getConfiguration() {
        return m_config;
    }

    
 
    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }

    /**
     * This method is used to rebuild the package agaist iplist mapping when
     * needed. When a node gained service event occurs, poller has to determine
     * which package the ip/service combination is in, but if the interface is a
     * newly added one, the package iplist should be rebuilt so that poller
     * could know which package this ip/service pair is in.
     */
    public synchronized void rebuildPackageIpListMap() {
        createPackageIpListMap();
    }
    
    
}
