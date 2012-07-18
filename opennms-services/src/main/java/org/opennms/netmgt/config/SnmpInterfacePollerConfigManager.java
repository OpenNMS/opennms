/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.IpListFromUrl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.snmpinterfacepoller.CriticalService;
import org.opennms.netmgt.config.snmpinterfacepoller.ExcludeRange;
import org.opennms.netmgt.config.snmpinterfacepoller.IncludeRange;
import org.opennms.netmgt.config.snmpinterfacepoller.Interface;
import org.opennms.netmgt.config.snmpinterfacepoller.Package;
import org.opennms.netmgt.config.snmpinterfacepoller.SnmpInterfacePollerConfiguration;
import org.opennms.netmgt.filter.FilterDaoFactory;

/**
 * <p>Abstract SnmpInterfacePollerConfigManager class.</p>
 *
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
abstract public class SnmpInterfacePollerConfigManager implements SnmpInterfacePollerConfig {

    /**
     * <p>Constructor for SnmpInterfacePollerConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param localServer a {@link java.lang.String} object.
     * @param verifyServer a boolean.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public SnmpInterfacePollerConfigManager(InputStream stream, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
        m_localServer = localServer;
        m_verifyServer = verifyServer;
        reloadXML(stream);
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public abstract void update() throws IOException, MarshalException, ValidationException;

    /**
     * <p>saveXml</p>
     *
     * @param xml a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXml(String xml) throws IOException;

    /**
     * The config class loaded from the config file
     */
    private SnmpInterfacePollerConfiguration m_config;
 
    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private Map<String, List<String>> m_urlIPMap;
    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid repetitive database access.
     */
    private Map<Package, List<InetAddress>> m_pkgIpMap;


    private Map<String,Map<String,Interface>> m_pkgIntMap;

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
     * Go through the poller configuration and build a mapping of each
     * configured URL to a list of IPs configured in that URL - done at init()
     * time so that repeated file reads can be avoided
     */
    private void createUrlIpMap() {
        m_urlIPMap = new HashMap<String, List<String>>();
    
        for(Package pkg : packages()) {
    
            for(String url : includeURLs(pkg)) {
    
                List<String> iplist = IpListFromUrl.parse(url);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(url, iplist);
                }
            }

        }
    }

    /**
     * <p>reloadXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected synchronized void reloadXML(InputStream stream) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(SnmpInterfacePollerConfiguration.class, stream);
        createUrlIpMap();
        createPackageIpListMap();
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void save() throws MarshalException, IOException, ValidationException {
    
        // Marshal to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);
        saveXml(stringWriter.toString());
    
        update();
    }

    /**
     * Return the poller configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.snmpinterfacepoller.SnmpInterfacePollerConfiguration} object.
     */
    public synchronized SnmpInterfacePollerConfiguration getConfiguration() {
        return m_config;
    }

    /**
     * <p>getPackage</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.snmpinterfacepoller.Package} object.
     */
    public synchronized Package getPackage(final String name) {
        
        for(Package pkg : packages()) {

            if (pkg.getName().equals(name)) {
                return pkg;
            }
        }
        return null;
    }
    
    /**
     * <p>addPackage</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.snmpinterfacepoller.Package} object.
     */
    public synchronized void addPackage(Package pkg) {
        m_config.addPackage(pkg);
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
        List<String> iplist = m_urlIPMap.get(url);
        if (iplist != null && iplist.size() > 0) {
            bRet = iplist.contains(addr);
        }
    
        return bRet;
    }

    /**
     * This method returns the configured critical service name.
     *
     * @return the name of the configured critical service, or null if none is
     *         present
     */
    public synchronized String[] getCriticalServiceIds() {
        CriticalService[] cs = m_config.getNodeOutage().getCriticalService();
        String[] criticalServiceNames = new String[cs.length];
        for (int i =0 ; i< cs.length; i++) {
            criticalServiceNames[i] = cs[i].getName();
        }
        return criticalServiceNames;
   }

     /**
     * This method is used to establish package against IP list mapping, with
     * which, the IP list is selected per package via the configured filter rules
     * from the database.
     */
    private void createPackageIpListMap() {
        m_pkgIpMap = new HashMap<Package, List<InetAddress>>();
        m_pkgIntMap = new HashMap<String, Map<String, Interface>>();
        
        for(Package pkg : packages()) {
    
            Map<String, Interface> interfaceMap = new HashMap<String, Interface>();
            for (Interface interf: pkg.getInterfaceCollection()) {
                interfaceMap.put(interf.getName(),interf);
            }
            m_pkgIntMap.put(pkg.getName(), interfaceMap);
            // Get a list of IP addresses per package against the filter rules from
            // database and populate the package, IP list map.
            //
            try {
                List<InetAddress> ipList = getIpList(pkg);
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

    /**
     * <p>getIpList</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.snmpinterfacepoller.Package} object.
     * @return a {@link java.util.List} object.
     */
    public List<InetAddress> getIpList(Package pkg) {
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
        List<InetAddress> ipList = FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());
        return ipList;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }

    /**
     * This method is used to rebuild the package against IP list mapping when
     * needed. When a node gained service event occurs, poller has to determine
     * which package the IP/service combination is in, but if the interface is a
     * newly added one, the package IP list should be rebuilt so that poller
     * could know which package this IP/service pair is in.
     */
    public synchronized void rebuildPackageIpListMap() {
        createPackageIpListMap();
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
    public synchronized boolean interfaceInPackage(String iface, Package pkg) {
        final InetAddress ifaceAddr = addr(iface);
        ThreadCategory log = log();
    
        boolean filterPassed = false;
    
        // get list of IPs in this package
        List<InetAddress> ipList = m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
			filterPassed = ipList.contains(ifaceAddr);
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
        
        for (IncludeRange rng : pkg.getIncludeRangeCollection()) {
            if (isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                has_range_include = true;
                break;
            }
        }

        byte[] addr = toIpAddrBytes(iface);

        for (String spec : pkg.getSpecificCollection()) {
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
     * Returns the first package that the ip belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @return the first package that the IP belongs to, null if none
     */
    public synchronized Package getPackageForAddress(String ipaddr) {
        
        for(Package pkg : packages()) {
    
            if (interfaceInPackage(ipaddr, pkg)) {
                return pkg;
            }
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
    public synchronized List<String> getAllPackageMatches(String ipaddr) {
    
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
    public synchronized String getPackageName(String ipaddr) {
        for(Package pkg : packages()) {
            
            if (interfaceInPackage(ipaddr, pkg)) {
                return pkg.getName();
            }
        }    
        return null;
    }

    /** {@inheritDoc} */
    public synchronized Set<String> getInterfaceOnPackage(String pkgName) {
        if (m_pkgIntMap.containsKey(pkgName))
            return Collections.unmodifiableSet(m_pkgIntMap.get(pkgName).keySet());
        Set<String> retval = Collections.emptySet();
        return Collections.unmodifiableSet(retval);
    }

    /** {@inheritDoc} */
    public synchronized boolean getStatus(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).getStatus().equals("on");
    }
    
    /** {@inheritDoc} */
    public synchronized long getInterval(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).getInterval();
        
    }
    /** {@inheritDoc} */
    public synchronized String getCriteria(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).getCriteria();
    }
    /** {@inheritDoc} */
    public synchronized boolean hasPort(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).hasPort();
    }
    /** {@inheritDoc} */
    public synchronized int getPort(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).getPort();
    }
    /** {@inheritDoc} */
    public synchronized boolean hasTimeout(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).hasTimeout();
    }
    /** {@inheritDoc} */
    public synchronized int getTimeout(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).getTimeout();
    }
    /** {@inheritDoc} */
    public synchronized boolean hasRetries(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).hasRetry();
    }
    /** {@inheritDoc} */
    public synchronized int getRetries(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).getRetry();
    }
    /** {@inheritDoc} */
    public synchronized boolean hasMaxVarsPerPdu(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).hasMaxVarsPerPdu();
    }
    /** {@inheritDoc} */
    public synchronized int getMaxVarsPerPdu(String pkgName,String pkgInterfaceName) {
        return m_pkgIntMap.get(pkgName).get(pkgInterfaceName).getMaxVarsPerPdu();        
    }

    /**
     * <p>enumeratePackage</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    public Enumeration<Package> enumeratePackage() {
        return getConfiguration().enumeratePackage();
    }
    
     
     /**
      * <p>packages</p>
      *
      * @return a {@link java.lang.Iterable} object.
      */
     public Iterable<Package> packages() {
        return getConfiguration().getPackageCollection();
    }

    /**
     * <p>includeURLs</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.snmpinterfacepoller.Package} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<String> includeURLs(Package pkg) {
        return pkg.getIncludeUrlCollection();
    }
     
    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads() {
        return getConfiguration().getThreads();
    }

    /**
     * <p>getThreads</p>
     *
     * @return a long.
     */
    public long getInterval() {
        return getConfiguration().getInterval();
    }

    /**
     * <p>getService</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getService() {
        return getConfiguration().getService();
    }
    
}
