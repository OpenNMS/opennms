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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.IpListFromUrl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.poller.ExcludeRange;
import org.opennms.netmgt.config.poller.IncludeRange;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.netmgt.dao.CastorObjectRetrievalFailureException;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.springframework.dao.PermissionDeniedDataAccessException;

/**
 * <p>Abstract PollerConfigManager class.</p>
 *
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
abstract public class PollerConfigManager implements PollerConfig {

    /**
     * <p>Constructor for PollerConfigManager.</p>
     *
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     * @param reader a {@link java.io.Reader} object.
     * @param localServer a {@link java.lang.String} object.
     * @param verifyServer a boolean.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public PollerConfigManager(Reader reader, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
        m_localServer = localServer;
        m_verifyServer = verifyServer;
        reloadXML(reader);
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
    private PollerConfiguration m_config;
    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private Map<String, List<String>> m_urlIPMap;
    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid repetetive database access.
     */
    private Map<Package, List<String>> m_pkgIpMap;
    /**
     * A mapp of service names to service monitors. Constructed based on data in
     * the configuration file.
     */
    private Map<String, ServiceMonitor> m_svcMonitors = Collections.synchronizedMap(new TreeMap<String, ServiceMonitor>());
    /**
     * A boolean flag to indicate If a filter rule agaist the local OpenNMS
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
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected synchronized void reloadXML(Reader reader) throws MarshalException, ValidationException, IOException {
        m_config = (PollerConfiguration) Unmarshaller.unmarshal(PollerConfiguration.class, reader);
        createUrlIpMap();
        createPackageIpListMap();
        createServiceMonitors();
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
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

    /**
     * Return the poller configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.poller.PollerConfiguration} object.
     */
    public synchronized PollerConfiguration getConfiguration() {
        return m_config;
    }

    /** {@inheritDoc} */
    public synchronized Package getPackage(final String name) {
        
        for(Package pkg : packages()) {

            if (pkg.getName().equals(name)) {
                return pkg;
            }
        }
        return null;
    }
    
    /** {@inheritDoc} */
    public ServiceSelector getServiceSelectorForPackage(Package pkg) {
        
        List<String> svcNames = new LinkedList<String>();
        for(Service svc : services(pkg)) {
            svcNames.add(svc.getName());
        }
        
        String filter = pkg.getFilter().getContent();
        return new ServiceSelector(filter, svcNames);
    }

    /** {@inheritDoc} */
    public synchronized void addPackage(Package pkg) {
        m_config.addPackage(pkg);
    }
    
    /** {@inheritDoc} */
    public synchronized void addMonitor(String svcName, String className) {
        Monitor monitor = new Monitor();
        monitor.setService(svcName);
        monitor.setClassName(className);
        m_config.addMonitor(monitor);
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
     * This method returns the boolean flag xmlrpc to indicate if notification
     * to external xmlrpc server is needed.
     *
     * @return true if need to notify an external xmlrpc server
     */
    public synchronized boolean getXmlrpc() {
        String flag = m_config.getXmlrpc();
        if (flag.equals("true"))
            return true;
        else
            return false;
    }

    /**
     * This method returns the boolean flag pathOutageEnabled to indicate if
     * path outage processing on nodeDown events is enabled
     *
     * @return true if pathOutageEnabled
     */
    public synchronized boolean pathOutageEnabled() {
        String flag = m_config.getPathOutageEnabled();
        if (flag.equals("true"))
            return true;
        else
            return false;
    }

    /**
     * This method returns the configured critical service name.
     *
     * @return the name of the configured critical service, or null if none is
     *         present
     */
    public synchronized String getCriticalService() {
        return m_config.getNodeOutage().getCriticalService().getName();
    }

    /**
     * This method returns the configured value of the
     * 'pollAllIfNoCriticalServiceDefined' flag.
     *
     * A value of true causes the poller's node outage code to poll all the
     * services on an interface if a status change has occurred and there is no
     * critical service defined on the interface.
     *
     * A value of false causes the poller's node outage code to not poll all the
     * services on an interface in this situation.
     * </p>
     *
     * @return true or false based on configured value
     */
    public synchronized boolean pollAllIfNoCriticalServiceDefined() {
        String flag = m_config.getNodeOutage().getPollAllIfNoCriticalServiceDefined();
        if (flag.equals("true"))
            return true;
        else
            return false;
    }

    /**
     * Returns true if node outage processing is enabled.
     *
     * @return a boolean.
     */
    public synchronized boolean nodeOutageProcessingEnabled() {
        String status = m_config.getNodeOutage().getStatus();
        if (status.equals("on"))
            return true;
        else
            return false;
    }

    /**
     * Returns true if serviceUnresponsive behavior is enabled. If enabled a
     * serviceUnresponsive event is generated for TCP-based services if the
     * service monitor is able to connect to the designated port but times out
     * before receiving the expected response. If disabled, an outage will be
     * generated in this scenario.
     *
     * @return a boolean.
     */
    public synchronized boolean serviceUnresponsiveEnabled() {
        String enabled = m_config.getServiceUnresponsiveEnabled();
        if (enabled.equals("true"))
            return true;
        else
            return false;
    }

    /**
     * This method is used to establish package agaist iplist mapping, with
     * which, the iplist is selected per package via the configured filter rules
     * from the database.
     */
    private void createPackageIpListMap() {
        m_pkgIpMap = new HashMap<Package, List<String>>();
        
        for(Package pkg : packages()) {
    
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

    /** {@inheritDoc} */
    public List<String> getIpList(Package pkg) {
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

    private Category log() {
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

    /**
     * {@inheritDoc}
     *
     * This method is used to determine if the named interface is included in
     * the passed package definition. If the interface belongs to the package
     * then a value of true is returned. If the interface does not belong to the
     * package a false value is returned.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     */
    public synchronized boolean interfaceInPackage(String iface, Package pkg) {
        Category log = log();
    
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
     * {@inheritDoc}
     *
     * Returns true if the service is part of the package and the status of the
     * service is set to "on". Returns false if the service is not in the
     * package or it is but the status of the service is set to "off".
     */
    public synchronized boolean serviceInPackageAndEnabled(String svcName, Package pkg) {
        if (pkg == null) {
            log().warn("serviceInPackageAndEnabled:  pkg argument is NULL!!");
            return false;
        } else {
            if (log().isDebugEnabled())
                log().debug("serviceInPackageAndEnabled: svcName=" + svcName + " pkg=" + pkg.getName());
        }
    
        for(Service svc : services(pkg)) {
            if (svc.getName().equalsIgnoreCase(svcName)) {
                // Ok its in the package. Now check the
                // status of the service
                String status = svc.getStatus();
                if (status == null || status.equals("on")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Return the Service object with the given name from the give Package.
     */
    public synchronized Service getServiceInPackage(String svcName, Package pkg) {
        
        for(Service svc : services(pkg)) {
            if (svcName.equals(svc.getName()))
                return svc;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the service has a monitor configured, false otherwise.
     */
    public synchronized boolean serviceMonitored(String svcName) {
        boolean result = false;
    
        for(Monitor monitor : monitors()) {

            if (monitor.getService().equals(svcName)) {
                result = true;
                break;
            }
        }
    
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the first package that the ip belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     */
    public synchronized Package getFirstPackageMatch(String ipaddr) {
        
        for(Package pkg : packages()) {
    
            if (interfaceInPackage(ipaddr, pkg)) {
                return pkg;
            }
        }
    
        return null;
    }

    /** {@inheritDoc} */
    public Package getFirstLocalPackageMatch(String ipaddr) {
        for(Package pkg : packages()) {
            if (!pkg.getRemote() && interfaceInPackage(ipaddr, pkg)) {
                return pkg;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Returns a list of package names that the ip belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
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

    /**
     * {@inheritDoc}
     *
     * Returns true if the ip is part of atleast one package.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     */
    public synchronized boolean isPolled(String ipaddr) {
        
        for(Package pkg : packages()) {
            
            boolean inPkg = interfaceInPackage(ipaddr, pkg);
            if (inPkg)
                return true;
        }
    
        return false;
    }

    /** {@inheritDoc} */
    public boolean isPolledLocally(String ipaddr) {
        for(Package pkg : packages()) {
            if (!pkg.getRemote() && interfaceInPackage(ipaddr, pkg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this package has the service enabled and if there is a
     * monitor for this service.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     *
     * @param svcName
     *            the service to check
     * @param pkg
     *            the package to check
     * @return true if the ip is part of atleast one package and the service is
     *         enabled in this package and monitored, false otherwise
     */
    public synchronized boolean isPolled(String svcName, Package pkg) {
        // Check if the service is enabled for this package and
        // if there is a monitor for that service
        //
        boolean svcInPkg = serviceInPackageAndEnabled(svcName, pkg);
        if (svcInPkg) {
            return serviceMonitored(svcName);
        }
    
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the ip is part of atleast one package and if this package
     * has the service enabled and if there is a monitor for this service.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     */
    public synchronized boolean isPolled(String ipaddr, String svcName) {
        // First make sure there is a service monitor for this service!
        if (!serviceMonitored(svcName)) {
            return false;
        }
    
        for(Package pkg : packages()) {
            if (serviceInPackageAndEnabled(svcName, pkg) && interfaceInPackage(ipaddr, pkg)) {
                return true;
            }
        }
    
        return false;
    }

    /** {@inheritDoc} */
    public boolean isPolledLocally(String ipaddr, String svcName) {
        if (!serviceMonitored(svcName)) {
            return false;
        }
        
        for(Package pkg : packages()) {
            if (serviceInPackageAndEnabled(svcName, pkg) && interfaceInPackage(ipaddr, pkg)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves configured RRD step size.
     */
    public int getStep(Package pkg) {
        return pkg.getRrd().getStep();
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves configured list of RoundRobin Archive statements.
     */
    public List<String> getRRAList(Package pkg) {
        return pkg.getRrd().getRraCollection();
    
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
     * <p>enumerateMonitor</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    public Enumeration<Monitor> enumerateMonitor() {
        return getConfiguration().enumerateMonitor();
    }
    
    /**
     * <p>services</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Service> services(Package pkg) {
        return pkg.getServiceCollection();
    }
    
    /**
     * <p>includeURLs</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<String> includeURLs(Package pkg) {
        return pkg.getIncludeUrlCollection();
    }
    
    /**
     * <p>parameters</p>
     *
     * @param svc a {@link org.opennms.netmgt.config.poller.Service} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Parameter> parameters(Service svc) {
        return svc.getParameterCollection();
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
     * <p>monitors</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Monitor> monitors() {
        return getConfiguration().getMonitorCollection();
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
     * @param poller
     * @return
     */
    private synchronized void createServiceMonitors() {
        Category log = ThreadCategory.getInstance(getClass());
    
        // Load up an instance of each monitor from the config
        // so that the event processor will have them for
        // new incomming events to create pollable service objects.
        //
        log.debug("start: Loading monitors");

        
        Collection<ServiceMonitorLocator> locators = getServiceMonitorLocators(DistributionContext.DAEMON);
        
        for (ServiceMonitorLocator locator : locators) {
            try {
                m_svcMonitors.put(locator.getServiceName(), locator.getServiceMonitor());
            } catch (Throwable t) {
                if (log.isEnabledFor(Level.WARN)) {
                    log.warn("start: Failed to create monitor " + locator.getServiceLocatorKey() + " for service " + locator.getServiceName(), t);
                }
            }
        }
    
    }

    /**
     * <p>getServiceMonitors</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public synchronized Map<String, ServiceMonitor> getServiceMonitors() {
        return m_svcMonitors;
    }

    /** {@inheritDoc} */
    public synchronized ServiceMonitor getServiceMonitor(String svcName) {
        return getServiceMonitors().get(svcName);
    }
    
    /** {@inheritDoc} */
    public synchronized Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context) {
        List<ServiceMonitorLocator> locators = new ArrayList<ServiceMonitorLocator>();
        for(Monitor monitor : monitors()) {
            try {
                Class<? extends ServiceMonitor> mc = findServiceMonitorClass(monitor);
                if (isDistributableToContext(mc, context)) {
                    ServiceMonitorLocator locator = new DefaultServiceMonitorLocator(monitor.getService(), mc);
                    locators.add(locator);
                }
            } catch (ClassNotFoundException e) {
                log().warn("Unable to location monitor for service: "+monitor.getService()+" class-name: "+monitor.getClassName(), e);
            } catch (CastorObjectRetrievalFailureException e) {
                log().warn(e.getMessage(), e.getRootCause());
            }
            
        }
        
        return locators;
        
    }

    private boolean isDistributableToContext(Class<? extends ServiceMonitor> mc, DistributionContext context) {
        List<DistributionContext> supportedContexts = getSupportedDistributionContexts(mc);
        if (supportedContexts.contains(context) || supportedContexts.contains(DistributionContext.ALL)) {
            return true;
        }
        return false;
    }

    private List<DistributionContext> getSupportedDistributionContexts(Class<? extends ServiceMonitor> mc) {
        Distributable distributable = mc.getAnnotation(Distributable.class);
        List<DistributionContext> declaredContexts = 
            distributable == null 
                ? Collections.singletonList(DistributionContext.DAEMON) 
                : Arrays.asList(distributable.value());
       return declaredContexts;
    }

    private Class<? extends ServiceMonitor> findServiceMonitorClass(Monitor monitor) throws ClassNotFoundException {
        Class<? extends ServiceMonitor> mc = Class.forName(monitor.getClassName()).asSubclass(ServiceMonitor.class);
        if (!ServiceMonitor.class.isAssignableFrom(mc)) {
            throw new CastorDataAccessFailureException("The monitor for service: "+monitor.getService()+" class-name: "+monitor.getClassName()+" must implement ServiceMonitor");
        }
        return mc;
    }



    /**
     * <p>getNextOutageIdSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNextOutageIdSql() {
        return m_config.getNextOutageId();
    }

	/**
	 * <p>releaseAllServiceMonitors</p>
	 */
	public void releaseAllServiceMonitors() {
		Iterator<ServiceMonitor> iter = getServiceMonitors().values().iterator();
	    while (iter.hasNext()) {
	        ServiceMonitor sm = iter.next();
	        sm.release();
	    }
	}

    /** {@inheritDoc} */
    public void saveResponseTimeData(String locationMonitor, OnmsMonitoredService monSvc, double responseTime, Package pkg) {
        
        String svcName = monSvc.getServiceName();
        
        Service svc = getServiceInPackage(svcName, pkg);
        
        String dsName = getServiceParameter(svc, "ds-name");
        if (dsName == null) {
            return;
        }
        
        String rrdRepository = getServiceParameter(svc, "rrd-repository");
        if (rrdRepository == null) {
            return;
        }
        
        String rrdDir = rrdRepository+File.separatorChar+"distributed"+File.separatorChar+locationMonitor+File.separator+monSvc.getIpAddress();

        try {
            RrdUtils.initialize();
            File rrdFile = new File(rrdDir, dsName);
            if (!rrdFile.exists()) {
                RrdUtils.createRRD(locationMonitor, rrdDir, dsName, getStep(pkg), "GAUGE", 600, "U", "U", getRRAList(pkg));
            }
            RrdUtils.updateRRD(locationMonitor, rrdDir, dsName, System.currentTimeMillis(), String.valueOf(responseTime));
        } catch (RrdException e) {
            throw new PermissionDeniedDataAccessException("Unable to store rrdData from "+locationMonitor+" for service "+monSvc, e);
        }
        
    }
    
    

    private String getServiceParameter(Service svc, String key) {
        
        for(Parameter parm : parameters(svc)) {

            if (key.equals(parm.getKey())) {
            	if (parm.getValue() != null) {
            		return parm.getValue();
            	} else if (parm.getAnyObject() != null) {
            		return parm.getAnyObject().toString();
            	}
            }
        }
        return null;
    }
    
}
