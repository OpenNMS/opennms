/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.opennms.core.network.IpListFromUrl;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.config.poller.CriticalService;
import org.opennms.netmgt.config.poller.ExcludeRange;
import org.opennms.netmgt.config.poller.IncludeRange;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.opennms.netmgt.poller.support.DefaultServiceMonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract PollerConfigManager class.</p>
 *
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
abstract public class PollerConfigManager implements PollerConfig {
    private static final Logger LOG = LoggerFactory.getLogger(PollerConfigManager.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    private static final ServiceMonitorRegistry s_serviceMonitorRegistry = new DefaultServiceMonitorRegistry();

    /**
     * <p>Constructor for PollerConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param localServer a {@link java.lang.String} object.
     * @param verifyServer a boolean.
     */
    public PollerConfigManager(final InputStream stream, final String localServer, final boolean verifyServer) {
        m_localServer = localServer;
        m_verifyServer = verifyServer;
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(stream);
            m_config = JaxbUtils.unmarshal(PollerConfiguration.class, isr);
        } finally {
            IOUtils.closeQuietly(isr);
        }
        setUpInternalData();
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
     * <p>setUpInternalData</p>
     */
    protected void setUpInternalData() {
        createUrlIpMap();
        createPackageIpListMap();
        initializeServiceMonitors();
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    @Override
    public abstract void update() throws IOException;

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
    protected PollerConfiguration m_config;
    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private Map<String, List<String>> m_urlIPMap;
    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid repetitive database access.
     */
    private AtomicReference<Map<Package, List<InetAddress>>> m_pkgIpMap = new AtomicReference<Map<Package, List<InetAddress>>>();;
    /**
     * A mapp of service names to service monitors. Constructed based on data in
     * the configuration file.
     */
    private Map<String, ServiceMonitor> m_svcMonitors = new ConcurrentSkipListMap<String, ServiceMonitor>();
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
    
        for(final Package pkg : packages()) {
            for(final String url : includeURLs(pkg)) {
                final List<String> iplist = IpListFromUrl.fetch(url);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(url, iplist);
                }
            }
        }
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws java.io.IOException if any.
     */
    @Override
    public void save() throws IOException {
        try {
            getWriteLock().lock();
            // marshal to a string first, then write the string to the file. This
            // way the original config
            // isn't lost if the XML from the marshal is hosed.
            saveXml(JaxbUtils.marshal(m_config));

            update();
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * Return the poller configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.poller.PollerConfiguration} object.
     */
    @Override
    public PollerConfiguration getConfiguration() {
        try {
            getReadLock().lock();
            return m_config;
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Package getPackage(final String name) {
        try {
            getReadLock().lock();
            for(final Package pkg : packages()) {
                if (pkg.getName().equals(name)) {
                    return pkg;
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public ServiceSelector getServiceSelectorForPackage(final Package pkg) {
        try {
            getReadLock().lock();
            final List<String> svcNames = new LinkedList<String>();
            for(Service svc : services(pkg)) {
                svcNames.add(svc.getName());
            }
            
            final String filter = pkg.getFilter().getContent();
            return new ServiceSelector(filter, svcNames);
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addPackage(final Package pkg) {
        try {
            getWriteLock().lock();
            m_config.addPackage(pkg);
        } finally {
            getWriteLock().unlock();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void addMonitor(final String svcName, final String className) {
        try {
            getWriteLock().lock();
            final Monitor monitor = new Monitor();
            monitor.setService(svcName);
            monitor.setClassName(className);
            m_config.addMonitor(monitor);
        } finally {
            getWriteLock().unlock();
        }
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
     * This method returns the boolean flag pathOutageEnabled to indicate if
     * path outage processing on nodeDown events is enabled
     *
     * @return true if pathOutageEnabled
     */
    @Override
    public boolean isPathOutageEnabled() {
        try {
            getReadLock().lock();
            return Boolean.valueOf(m_config.getPathOutageEnabled());
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * This method returns the configured critical service name.
     *
     * @return the name of the configured critical service, or null if none is
     *         present
     */
    @Override
    public String getCriticalService() {
        try {
            getReadLock().lock();
            CriticalService service = m_config.getNodeOutage().getCriticalService();
            return service == null ? null : service.getName();
        } finally {
            getReadLock().unlock();
        }
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
    @Override
    public boolean shouldPollAllIfNoCriticalServiceDefined() {
        try {
            getReadLock().lock();
            return Boolean.valueOf(m_config.getNodeOutage().getPollAllIfNoCriticalServiceDefined());
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Returns true if node outage processing is enabled.
     *
     * @return a boolean.
     */
    @Override
    public boolean isNodeOutageProcessingEnabled() {
        try {
            getReadLock().lock();
            return m_config.getNodeOutage().getStatus().equals("on");
        } finally {
            getReadLock().unlock();
        }
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
    @Override
    public boolean isServiceUnresponsiveEnabled() {
        try {
            getReadLock().lock();
            return Boolean.valueOf(m_config.getServiceUnresponsiveEnabled());
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * This method is used to establish package agaist iplist mapping, with
     * which, the iplist is selected per package via the configured filter rules
     * from the database.
     */
    private void createPackageIpListMap() {
        getReadLock().lock();
        
        try {
            Map<Package, List<InetAddress>> pkgIpMap = new HashMap<Package, List<InetAddress>>();
            
            for(final Package pkg : packages()) {
        
                // Get a list of ipaddress per package against the filter rules from
                // database and populate the package, IP list map.
                //
                try {
                    List<InetAddress> ipList = getIpList(pkg);
                    LOG.debug("createPackageIpMap: package {}: ipList size = {}", pkg.getName(), ipList.size());
        
                    if (ipList.size() > 0) {
                        pkgIpMap.put(pkg, ipList);
                    }
                    
                } catch (final Throwable t) {
                    LOG.error("createPackageIpMap: failed to map package: {} to an IP List with filter \"{}\"", pkg.getName(), pkg.getFilter().getContent(), t);
                }
                
            }
            
            m_pkgIpMap.set(pkgIpMap);
            
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<InetAddress> getIpList(final Package pkg) {
        try {
            getReadLock().lock();
            final StringBuilder filterRules = new StringBuilder(pkg.getFilter().getContent());
            if (m_verifyServer) {
                filterRules.append(" & (serverName == ");
                filterRules.append('\"');
                filterRules.append(m_localServer);
                filterRules.append('\"');
                filterRules.append(")");
            }
            LOG.debug("createPackageIpMap: package is {}. filter rules are {}", pkg.getName(), filterRules);
            FilterDaoFactory.getInstance().flushActiveIpAddressListCache();
            return FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * This method is used to rebuild the package agaist iplist mapping when
     * needed. When a node gained service event occurs, poller has to determine
     * which package the ip/service combination is in, but if the interface is a
     * newly added one, the package iplist should be rebuilt so that poller
     * could know which package this ip/service pair is in.
     */
    @Override
    public void rebuildPackageIpListMap() {
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
    @Override
    public boolean isInterfaceInPackage(final String iface, final Package pkg) {
        boolean filterPassed = false;
        final InetAddress ifaceAddr = addr(iface);
    
        // get list of IPs in this package
        final List<InetAddress> ipList = m_pkgIpMap.get().get(pkg);
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
        // the range of all valid addresses (0.0.0.0 - 255.255.255.255, ::1 - ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff)
        has_range_include = pkg.getIncludeRanges().size() == 0 && pkg.getSpecifics().size() == 0 && pkg.getIncludeUrls().size() == 0;
        
        final byte[] addr = toIpAddrBytes(iface);

        for (final IncludeRange rng : pkg.getIncludeRanges()) {
            int comparison = new ByteArrayComparator().compare(addr, toIpAddrBytes(rng.getBegin()));
            if (comparison > 0) {
                int endComparison = new ByteArrayComparator().compare(addr, toIpAddrBytes(rng.getEnd()));
                if (endComparison <= 0) {
                    has_range_include = true;
                    break;
                }
            } else if (comparison == 0) {
                has_range_include = true;
                break;
            }
        }

        for (final String spec : pkg.getSpecifics()) {
            if (new ByteArrayComparator().compare(addr, toIpAddrBytes(spec)) == 0) {
                has_specific = true;
                LOG.debug("interfaceInPackage: Interface {} defined as 'specific'", iface);
                break;
            }
        }
    
        for (final String includeUrl : pkg.getIncludeUrls()) {
            if (interfaceInUrl(iface, includeUrl)) {
                has_specific = true;
                LOG.debug("interfaceInPackage: Interface {} exist on {}", iface, includeUrl);
                break;
            }
        }

        if (!has_specific) {
            for (final ExcludeRange rng : pkg.getExcludeRanges()) {
                int comparison = new ByteArrayComparator().compare(addr, toIpAddrBytes(rng.getBegin()));
                if (comparison > 0) {
                    int endComparison = new ByteArrayComparator().compare(addr, toIpAddrBytes(rng.getEnd()));
                    if (endComparison <= 0) {
                        LOG.debug("interfaceInPackage: Interface {} matches an exclude range", iface);
                        has_range_exclude = true;
                        break;
                    }
                } else if (comparison == 0) {
                    LOG.debug("interfaceInPackage: Interface {} matches an exclude range", iface);
                    has_range_exclude = true;
                    break;
                }
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
    @Override
    public boolean isServiceInPackageAndEnabled(final String svcName, final Package pkg) {
        try {
            getReadLock().lock();
            if (pkg == null) {
                LOG.warn("serviceInPackageAndEnabled:  pkg argument is NULL!!");
                return false;
            } else {
                LOG.debug("serviceInPackageAndEnabled: svcName={} pkg={}", svcName, pkg.getName());
            }
        
            for(final Service svc : services(pkg)) {
                if (svc.getName().equalsIgnoreCase(svcName)) {
                    // Ok its in the package. Now check the
                    // status of the service
                    final String status = svc.getStatus();
                    if (status == null || status.equals("on")) {
                        return true;
                    }
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Return the Service object with the given name from the give Package.
     */
    @Override
    public Service getServiceInPackage(final String svcName, final Package pkg) {
        try {
            getReadLock().lock();
            for(final Service svc : services(pkg)) {
                if (svcName.equals(svc.getName())) return svc;
            }
        } finally {
            getReadLock().unlock();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the service has a monitor configured, false otherwise.
     */
    @Override
    public boolean isServiceMonitored(final String svcName) {
        try {
            getReadLock().lock();
            for (final Monitor monitor : monitors()) {
                if (monitor.getService().equals(svcName)) {
                    return true;
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the first package that the ip belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     */
    @Override
    public Package getFirstPackageMatch(final String ipaddr) {
        try {
            getReadLock().lock();
            for(final Package pkg : packages()) {
                if (isInterfaceInPackage(ipaddr, pkg)) {
                    return pkg;
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Package getFirstLocalPackageMatch(final String ipaddr) {
        try {
            getReadLock().lock();
            for(final Package pkg : packages()) {
                if (!pkg.getRemote() && isInterfaceInPackage(ipaddr, pkg)) {
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
     * Returns a list of package names that the ip belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     */
    @Override
    public List<String> getAllPackageMatches(final String ipaddr) {
        List<String> matchingPkgs = new ArrayList<String>();

        try {
            getReadLock().lock();
            for (final Package pkg : packages()) {
                if (isInterfaceInPackage(ipaddr, pkg)) {
                    matchingPkgs.add(pkg.getName());
                }
    
            }
        } finally {
            getReadLock().unlock();
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
    @Override
    public boolean isPolled(final String ipaddr) {
        try {
            getReadLock().lock();
            for(final Package pkg : packages()) {
                if (isInterfaceInPackage(ipaddr, pkg)) return true;
            }
        } finally {
            getReadLock().unlock();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPolledLocally(final String ipaddr) {
        try {
            getReadLock().lock();
            for(final Package pkg : packages()) {
                if (!pkg.getRemote() && isInterfaceInPackage(ipaddr, pkg)) {
                    return true;
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if this package has the service enabled and if there is a
     * monitor for this service.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     */
    @Override
    public boolean isPolled(final String svcName, final Package pkg) {
        if (isServiceInPackageAndEnabled(svcName, pkg)) {
            return isServiceMonitored(svcName);
        }
        return false;
    }

    /**
     * Returns true if the ip is part of atleast one package and if this package
     * has the service enabled and if there is a monitor for this service.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @param svcName
     *            the service to check
     * @return true if the ip is part of atleast one package and the service is
     *         enabled in this package and monitored, false otherwise
     */
    @Override
    public boolean isPolled(final String ipaddr, final String svcName) {
        try {
            getReadLock().lock();
            // First make sure there is a service monitor for this service!
            if (!isServiceMonitored(svcName)) {
                return false;
            }
            for(final Package pkg : packages()) {
                if (isServiceInPackageAndEnabled(svcName, pkg) && isInterfaceInPackage(ipaddr, pkg)) {
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
    public boolean isPolledLocally(final String ipaddr, final String svcName) {
        try {
            getReadLock().lock();
            if (!isServiceMonitored(svcName)) {
                return false;
            }
            for(final Package pkg : packages()) {
                if (isServiceInPackageAndEnabled(svcName, pkg) && isInterfaceInPackage(ipaddr, pkg)) {
                    return true;
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves configured RRD step size.
     */
    @Override
    public int getStep(final Package pkg) {
        try {
            getReadLock().lock();
            return pkg.getRrd().getStep();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves configured list of RoundRobin Archive statements.
     */
    @Override
    public List<String> getRRAList(final Package pkg) {
        try {
            getReadLock().lock();
            return pkg.getRrd().getRras();
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
        try {
            getReadLock().lock();
            return Collections.enumeration(getConfiguration().getPackages());
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * <p>services</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @return a {@link java.lang.Iterable} object.
     */
    private Iterable<Service> services(final Package pkg) {
        try {
            getReadLock().lock();
            return pkg.getServices();
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * <p>includeURLs</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @return a {@link java.lang.Iterable} object.
     */
    private Iterable<String> includeURLs(final Package pkg) {
        try {
            getReadLock().lock();
            return pkg.getIncludeUrls();
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * <p>parameters</p>
     *
     * @param svc a {@link org.opennms.netmgt.config.poller.Service} object.
     * @return a {@link java.lang.Iterable} object.
     */
    @Override
    public Iterable<Parameter> parameters(final Service svc) {
        try {
            getReadLock().lock();
            return svc.getParameters();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>packages</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    private Iterable<Package> packages() {
        try {
            getReadLock().lock();
            return getConfiguration().getPackages();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>monitors</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    private Iterable<Monitor> monitors() {
        try {
            getReadLock().lock();
            return getConfiguration().getMonitors();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    @Override
    public int getThreads() {
        try {
            getReadLock().lock();
            return getConfiguration().getThreads();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * @param poller
     * @return
     */
    private void initializeServiceMonitors() {
        // Load up an instance of each monitor from the config
        // so that the event processor will have them for
        // new incoming events to create pollable service objects.
        //
        LOG.debug("start: Loading monitors");

        final Collection<ServiceMonitorLocator> locators = getServiceMonitorLocators(DistributionContext.DAEMON);
        
        for (final ServiceMonitorLocator locator : locators) {
            try {
                m_svcMonitors.put(locator.getServiceName(), locator.getServiceMonitor(s_serviceMonitorRegistry));
            } catch (Throwable t) {
                LOG.warn("start: Failed to create monitor {} for service {}", locator.getServiceLocatorKey(), locator.getServiceName(), t);
            }
        }
    }

    /**
     * <p>getServiceMonitors</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String, ServiceMonitor> getServiceMonitors() {
        try {
            getReadLock().lock();
            return Collections.unmodifiableMap(m_svcMonitors);
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMonitor getServiceMonitor(final String svcName) {
        try {
            getReadLock().lock();
            return getServiceMonitors().get(svcName);
        } finally {
            getReadLock().unlock();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(final DistributionContext context) {
        List<ServiceMonitorLocator> locators = new ArrayList<ServiceMonitorLocator>();

        try {
            getReadLock().lock();
            for(final Monitor monitor : monitors()) {
                try {
                    final Class<? extends ServiceMonitor> mc = findServiceMonitorClass(monitor);
                    if (isDistributableToContext(mc, context)) {
                        final ServiceMonitorLocator locator = new DefaultServiceMonitorLocator(monitor.getService(), mc);
                        locators.add(locator);
                    }
                    LOG.debug("Loaded monitor for service: {}, class-name: {}", monitor.getService(), monitor.getClassName());
                } catch (final ClassNotFoundException e) {
                    LOG.warn("Unable to load monitor for service: {}, class-name: {}: {}", monitor.getService(), monitor.getClassName(), e.getMessage());
                } catch (ConfigObjectRetrievalFailureException e) {
                    LOG.warn("{} {}", e.getMessage(), e.getRootCause(), e);
                }
            }
        } finally {
            getReadLock().unlock();
        }

        return locators;
        
    }

    private boolean isDistributableToContext(final Class<? extends ServiceMonitor> mc, final DistributionContext context) {
        final List<DistributionContext> supportedContexts = getSupportedDistributionContexts(mc);
        if (supportedContexts.contains(context) || supportedContexts.contains(DistributionContext.ALL)) {
            return true;
        }
        return false;
    }

    private List<DistributionContext> getSupportedDistributionContexts(final Class<? extends ServiceMonitor> mc) {
        final Distributable distributable = mc.getAnnotation(Distributable.class);
        final List<DistributionContext> declaredContexts = 
            distributable == null 
                ? Collections.singletonList(DistributionContext.DAEMON) 
                : Arrays.asList(distributable.value());
       return declaredContexts;
    }

    private Class<? extends ServiceMonitor> findServiceMonitorClass(final Monitor monitor) throws ClassNotFoundException {
        final Class<? extends ServiceMonitor> mc = Class.forName(monitor.getClassName()).asSubclass(ServiceMonitor.class);
        if (!ServiceMonitor.class.isAssignableFrom(mc)) {
            throw new MarshallingResourceFailureException("The monitor for service: "+monitor.getService()+" class-name: "+monitor.getClassName()+" must implement ServiceMonitor");
        }
        return mc;
    }

    @Override
    public ServiceMonitorRegistry getServiceMonitorRegistry() {
        return s_serviceMonitorRegistry;
    }

    /**
     * <p>getNextOutageIdSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getNextOutageIdSql() {
        try {
            getReadLock().lock();
            return m_config.getNextOutageId();
        } finally {
            getReadLock().unlock();
        }
    }

}
