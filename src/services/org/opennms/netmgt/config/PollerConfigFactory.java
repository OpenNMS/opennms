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
// Modifications:
//
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Jan 31: Added code to allow for RRA definitions within poller packages.
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.poller.ExcludeRange;
import org.opennms.netmgt.config.poller.IncludeRange;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.poller.monitors.ServiceMonitor;
import org.opennms.netmgt.utils.IPSorter;
import org.opennms.netmgt.utils.IpListFromUrl;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Poller service from the poller-configuration xml file.
 * 
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class PollerConfigFactory implements PollerConfig {
    /**
     * The singleton instance of this factory
     */
    private static PollerConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * The config class loaded from the config file
     */
    private PollerConfiguration m_config;

    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private Map m_urlIPMap;

    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid repetetive database access.
     */
    private Map m_pkgIpMap;

    /**
     * A mapp of service names to service monitors. Constructed based on data in
     * the configuration file.
     */
    private Map m_svcMonitors = Collections.synchronizedMap(new TreeMap());

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
        m_urlIPMap = new HashMap();

        Enumeration pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package) pkgEnum.nextElement();

            Enumeration urlEnum = pkg.enumerateIncludeUrl();
            while (urlEnum.hasMoreElements()) {
                String urlname = (String) urlEnum.nextElement();

                java.util.List iplist = IpListFromUrl.parse(urlname);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(urlname, iplist);
                }
            }
        }
    }

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private PollerConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream cfgIn = new FileInputStream(configFile);

        m_config = (PollerConfiguration) Unmarshaller.unmarshal(PollerConfiguration.class, new InputStreamReader(cfgIn));
        cfgIn.close();

        createUrlIpMap();

        OpennmsServerConfigFactory.init();
        m_verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();
        m_localServer = OpennmsServerConfigFactory.getInstance().getServerName();

        createPackageIpListMap();
        createServiceMonitors();
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);

        ThreadCategory.getInstance(PollerConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new PollerConfigFactory(cfgFile.getPath());

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized PollerConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * Return the poller configuration object.
     */
    public synchronized PollerConfiguration getConfiguration() {
        return m_config;
    }

    /**
     * This method is used to determine if the named interface is included in
     * the passed package's url includes. If the interface is found in any of
     * the URL files, then a value of true is returned, else a false value is
     * returned.
     * 
     * <pre>
     * The file URL is read and each entry in this file checked. Each line
     *  in the URL file can be one of -
     *  &lt;IP&gt;&lt;space&gt;#&lt;comments&gt;
     *  or
     *  &lt;IP&gt;
     *  or
     *  #&lt;comments&gt;
     * 
     *  Lines starting with a '#' are ignored and so are characters after
     *  a '&lt;space&gt;#' in a line.
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
        java.util.List iplist = (java.util.List) m_urlIPMap.get(url);
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
        Category log = ThreadCategory.getInstance(this.getClass());

        m_pkgIpMap = new HashMap();

        Enumeration pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package) pkgEnum.nextElement();

            //
            // Get a list of ipaddress per package agaist the filter rules from
            // database and populate the package, IP list map.
            //
            Filter filter = new Filter();
            StringBuffer filterRules = new StringBuffer(pkg.getFilter().getContent());

            try {
                if (m_verifyServer) {
                    filterRules.append(" & (serverName == ");
                    filterRules.append('\"');
                    filterRules.append(m_localServer);
                    filterRules.append('\"');
                    filterRules.append(")");
                }

                if (log.isDebugEnabled())
                    log.debug("createPackageIpMap: package is " + pkg.getName() + ". filer rules are  " + filterRules.toString());

                List ipList = filter.getIPList(filterRules.toString());
                if (log.isDebugEnabled())
                    log.debug("createPackageIpMap: package " + pkg.getName() + ": ipList size =  " + ipList.size());

                if (ipList.size() > 0) {
                    if (log.isDebugEnabled())
                        log.debug("createPackageIpMap: package " + pkg.getName() + ". IpList size is " + ipList.size());
                    m_pkgIpMap.put(pkg, ipList);
                }
            } catch (Throwable t) {
                if (log.isEnabledFor(Priority.ERROR)) {
                    log.error("createPackageIpMap: failed to map package: " + pkg.getName() + " to an IP List", t);
                }
            }
        }
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
    public synchronized boolean interfaceInPackage(String iface, org.opennms.netmgt.config.poller.Package pkg) {
        Category log = ThreadCategory.getInstance(this.getClass());

        boolean filterPassed = false;

        // get list of IPs in this package
        java.util.List ipList = (java.util.List) m_pkgIpMap.get(pkg);
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

        long addr = IPSorter.convertToLong(iface);
        Enumeration eincs = pkg.enumerateIncludeRange();
        while (!has_range_include && eincs.hasMoreElements()) {
            IncludeRange rng = (IncludeRange) eincs.nextElement();
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

        Enumeration espec = pkg.enumerateSpecific();
        while (!has_specific && espec.hasMoreElements()) {
            long speca = IPSorter.convertToLong(espec.nextElement().toString());
            if (speca == addr)
                has_specific = true;
        }

        Enumeration eurl = pkg.enumerateIncludeUrl();
        while (!has_specific && eurl.hasMoreElements()) {
            has_specific = interfaceInUrl(iface, (String) eurl.nextElement());
        }

        Enumeration eex = pkg.enumerateExcludeRange();
        while (!has_range_exclude && !has_specific && eex.hasMoreElements()) {
            ExcludeRange rng = (ExcludeRange) eex.nextElement();
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
     * Returns true if the service is part of the package and the status of the
     * service is set to "on". Returns false if the service is not in the
     * package or it is but the status of the service is set to "off".
     * 
     * @param svcName
     *            The service name to lookup.
     * @param pkg
     *            The package to lookup up service.
     */
    public synchronized boolean serviceInPackageAndEnabled(String svcName, org.opennms.netmgt.config.poller.Package pkg) {
        Category log = ThreadCategory.getInstance(this.getClass());

        if (pkg == null) {
            log.warn("serviceInPackageAndEnabled:  pkg argument is NULL!!");
            return false;
        } else {
            if (log.isDebugEnabled())
                log.debug("serviceInPackageAndEnabled: svcName=" + svcName + " pkg=" + pkg.getName());
        }

        boolean result = false;

        Enumeration esvcs = pkg.enumerateService();
        while (result == false && esvcs.hasMoreElements()) {
            Service tsvc = (Service) esvcs.nextElement();
            if (tsvc.getName().equalsIgnoreCase(svcName)) {
                // Ok its in the package. Now check the
                // status of the service
                String status = tsvc.getStatus();
                if (status.equals("on"))
                    result = true;
            }
        }
        return result;
    }

    /**
     * Returns true if the service has a monitor configured, false otherwise.
     * 
     * @param svcName
     *            The service name to lookup.
     */
    public synchronized boolean serviceMonitored(String svcName) {
        boolean result = false;

        Enumeration monitorEnum = m_config.enumerateMonitor();
        while (monitorEnum.hasMoreElements()) {
            Monitor monitor = (Monitor) monitorEnum.nextElement();
            if (monitor.getService().equals(svcName)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Returns the first package that the ip belongs to, null if none.
     * 
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     * 
     * @param ipaddr
     *            the interface to check
     * 
     * @return the first package that the ip belongs to, null if none
     */
    public synchronized org.opennms.netmgt.config.poller.Package getFirstPackageMatch(String ipaddr) {
        Enumeration pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package) pkgEnum.nextElement();

            boolean inPkg = interfaceInPackage(ipaddr, pkg);
            if (inPkg)
                return pkg;
        }

        return null;
    }

    /**
     * Returns true if the ip is part of atleast one package.
     * 
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     * 
     * @param ipaddr
     *            the interface to check
     * 
     * @return true if the ip is part of atleast one package, false otherwise
     */
    public synchronized boolean isPolled(String ipaddr) {
        Enumeration pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package) pkgEnum.nextElement();

            boolean inPkg = interfaceInPackage(ipaddr, pkg);
            if (inPkg)
                return true;
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
     * 
     * @return true if the ip is part of atleast one package and the service is
     *         enabled in this package and monitored, false otherwise
     */
    public synchronized boolean isPolled(String svcName, org.opennms.netmgt.config.poller.Package pkg) {
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
     * 
     * @return true if the ip is part of atleast one package and the service is
     *         enabled in this package and monitored, false otherwise
     */
    public synchronized boolean isPolled(String ipaddr, String svcName) {
        // First make sure there is a service monitor for this service!
        if (!serviceMonitored(svcName)) {
            return false;
        }

        Enumeration pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package) pkgEnum.nextElement();

            //
            // Check if interface is in a package and if the service
            // is enabled for that package
            //
            boolean ipInPkg = interfaceInPackage(ipaddr, pkg);
            if (ipInPkg) {
                boolean svcInPkg = serviceInPackageAndEnabled(svcName, pkg);
                if (svcInPkg) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Retrieves configured RRD step size.
     * 
     * @param pkg
     *            Name of the data collection
     * 
     * @return RRD step size for the specified collection
     */
    public int getStep(org.opennms.netmgt.config.poller.Package pkg) {
        return pkg.getRrd().getStep();
    }

    /**
     * Retrieves configured list of RoundRobin Archive statements.
     * 
     * @param pkg
     *            Name of the data collection
     * 
     * @return list of RRA strings.
     */
    public List getRRAList(org.opennms.netmgt.config.poller.Package pkg) {
        return (List) pkg.getRrd().getRraCollection();

    }

    public Enumeration enumeratePackage() {
        return getConfiguration().enumeratePackage();
    }

    public Enumeration enumerateMonitor() {
        return getConfiguration().enumerateMonitor();
    }

    public int getThreads() {
        return getConfiguration().getThreads();
    }

    /**
     * @param poller
     * @return
     */
    private void createServiceMonitors() {
        Category log = ThreadCategory.getInstance(getClass());

        // Load up an instance of each monitor from the config
        // so that the event processor will have them for
        // new incomming events to create pollable service objects.
        //
        log.debug("start: Loading monitors");

        Enumeration eiter = enumerateMonitor();
        while (eiter.hasMoreElements()) {
            Monitor monitor = (Monitor) eiter.nextElement();
            try {
                if (log.isDebugEnabled()) {
                    log.debug("start: Loading monitor " + monitor.getService() + ", classname " + monitor.getClassName());
                }
                Class mc = Class.forName(monitor.getClassName());
                ServiceMonitor sm = (ServiceMonitor) mc.newInstance();

                // Attempt to initialize the service monitor
                //
                Map properties = null; // properties not currently used
                sm.initialize(this, properties);

                m_svcMonitors.put(monitor.getService(), sm);
            } catch (Throwable t) {
                if (log.isEnabledFor(Priority.WARN)) {
                    log.warn("start: Failed to load monitor " + monitor.getClassName() + " for service " + monitor.getService(), t);
                }
            }
        }
    }

    public Map getServiceMonitors() {
        return m_svcMonitors;
    }

    public ServiceMonitor getServiceMonitor(String svcName) {
        return (ServiceMonitor) getServiceMonitors().get(svcName);
    }

    public String getNextOutageIdSql() {
        return m_config.getNextOutageId();
    }
}
