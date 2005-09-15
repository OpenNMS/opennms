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
// 2004 Dec 21: Changed determination of primary SNMP interface
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
import java.io.Reader;
import java.io.StringWriter;
import java.io.FileWriter;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.ExcludeRange;
import org.opennms.netmgt.config.collectd.IncludeRange;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.utils.IPSorter;
import org.opennms.netmgt.utils.IpListFromUrl;
import org.opennms.netmgt.config.collectd.Package;

/**
 * @author <a href="mailto:jamesz@opennms.com">James Zuo</a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Collection Daemon from the collectd-configuration xml file.
 * 
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 */
public final class CollectdConfigFactory {
    private final static String SELECT_METHOD_MIN = "min";

    private final static String SELECT_METHOD_MAX = "max";

    /**
     * The singleton instance of this factory
     */
    private static CollectdConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * The config class loaded from the config file
     */
    private CollectdConfiguration m_config;

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
     * A boolean flag to indicate If a filter rule against the local NMS server
     * has to be used.
     */
    private static boolean m_verifyServer;

    /**
     * Name of the local NMS server.
     */
    private static String m_localServer;

    /**
     * Go through the configuration and build a mapping of each configured URL
     * to a list of IPs configured in that URL - done at init() time so that
     * repeated file reads can be avoided
     */
    private void createUrlIpMap() {
        m_urlIPMap = new HashMap();

        Enumeration pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            org.opennms.netmgt.config.collectd.Package pkg = (org.opennms.netmgt.config.collectd.Package) pkgEnum.nextElement();

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
     * This method is used to establish package agaist iplist mapping, with
     * which, the iplist is selected per package via the configured filter rules
     * from the database.
     */
    private void createPackageIpListMap() {
        Category log = ThreadCategory.getInstance(this.getClass());

        // Multiple threads maybe asking for the m_pkgIpMap field so create
        // with temp map then assign when finished.
        Map pkgIpMap = new HashMap();

        Enumeration pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            Package pkg = (Package) pkgEnum.nextElement();

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
                if (ipList.size() > 0) {
                    pkgIpMap.put(pkg, ipList);
                }
            } catch (Throwable t) {
                if (log.isEnabledFor(Priority.ERROR)) {
                    log.error("createPackageIpMap: failed to map package: " + pkg.getName() + " to an IP List", t);
                }
            }
        }
        
        m_pkgIpMap = pkgIpMap;
    }

    /**
     * This method is used to rebuild the package agaist iplist mapping when
     * needed. When a node gained service event occurs, collectd has to
     * determine which package the ip/service combination is in, but if the
     * interface is a newly added one, the package iplist should be rebuilt so
     * that collectd could know which package this ip/service pair is in.
     */
    public synchronized void rebuildPackageIpListMap() {
        createPackageIpListMap();
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
    private CollectdConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream cfgIn = new FileInputStream(configFile);

        m_config = (CollectdConfiguration) Unmarshaller.unmarshal(CollectdConfiguration.class, new InputStreamReader(cfgIn));
        cfgIn.close();

        finishConstruction();
    }

    /**
     * Public constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public CollectdConfigFactory(Reader rdr) throws IOException, MarshalException, ValidationException {
    
        m_config = (CollectdConfiguration) Unmarshaller.unmarshal(CollectdConfiguration.class, rdr);
    
        finishConstruction();
    }

    /**
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    private void finishConstruction() throws IOException, MarshalException, ValidationException {
        createUrlIpMap();
        OpennmsServerConfigFactory.init();
        m_verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();
        m_localServer = OpennmsServerConfigFactory.getInstance().getServerName();
        createPackageIpListMap();
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

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME);

        ThreadCategory.getInstance(CollectdConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new CollectdConfigFactory(cfgFile.getPath());
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
         * Saves the current in-memory configuration to disk and reloads
          */
         public synchronized void saveCurrent()
                 throws MarshalException, IOException, ValidationException
         {
                 File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME);

                 //marshall to a string first, then write the string to the file. This way the original config
                 //isn't lost if the xml from the marshall is hosed.
                 StringWriter stringWriter = new StringWriter();
                 Marshaller.marshal(m_config, stringWriter);
                 if (stringWriter.toString()!=null)
                 {
                         FileWriter fileWriter = new FileWriter(cfgFile);
                         fileWriter.write(stringWriter.toString());
                         fileWriter.flush();
                         fileWriter.close();
                 }

                 reload();
         }


    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized CollectdConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }
    
    public static synchronized void setInstance(CollectdConfigFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /**
     * Return the collectd configuration object.
     */
    public synchronized CollectdConfiguration getConfiguration() {
        return m_config;
    }

         public synchronized  org.opennms.netmgt.config.collectd.Package getPackage(String name) {
                 Enumeration packageEnum=m_config.enumeratePackage();
                 while(packageEnum.hasMoreElements()) {
                         org.opennms.netmgt.config.collectd.Package thisPackage=( org.opennms.netmgt.config.collectd.Package)packageEnum.nextElement();
                         if(thisPackage.getName().equals(name)) {
                                 return thisPackage;
                         }
                 }
                 return null;
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
    public synchronized boolean interfaceInPackage(String iface, Package pkg) {
        Category log = ThreadCategory.getInstance(this.getClass());

        boolean filterPassed = interfaceInFilter(iface, pkg, log);

        if (!filterPassed)
            return false;

        //
        // Ensure that the interface is in the specific list or
        // that it is in the include range and is not excluded
        //

        long addr = IPSorter.convertToLong(iface);
        Enumeration eincs = pkg.enumerateIncludeRange();

        boolean has_range_include = hasIncludeRange(addr, eincs);
        boolean has_specific = hasSpecific(pkg, addr);

        has_specific = hasSpecificUrl(iface, pkg, has_specific);
        boolean has_range_exclude = hasExcludeRange(pkg, addr, has_specific);

        boolean packagePassed = has_specific || (has_range_include && !has_range_exclude);
        log.debug("interfaceInPackage: Interface " + iface + " passed filter and specific/range for package " + pkg.getName() + "?: " + packagePassed);
        return packagePassed;
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
    public synchronized boolean serviceInPackageAndEnabled(String svcName, org.opennms.netmgt.config.collectd.Package pkg) {
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
     * Returns true if the specified interface is included by at least one
     * package which has the specified service and that service is enabled (set
     * to "on").
     * 
     * @param ipAddr
     *            IP address of the interface to lookup
     * @param svcName
     *            The service name to lookup
     * 
     * @return true if Collectd config contains a package which includes the
     *         specified interface and has the specified service enabled.
     */
    public synchronized boolean lookupInterfaceServicePair(String ipAddr, String svcName) {
        boolean result = false;

        Enumeration pkgs = m_config.enumeratePackage();
        while (pkgs.hasMoreElements() && result == false) {
            org.opennms.netmgt.config.collectd.Package pkg = (org.opennms.netmgt.config.collectd.Package) pkgs.nextElement();

            // Does the package include the interface?
            //
            if (interfaceInPackage(ipAddr, pkg)) {
                // Yes, now see if package includes
                // the service and service is enabled
                //
                if (serviceInPackageAndEnabled(svcName, pkg)) {
                    // Thats all we need to know...
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * This method is responsbile for determining the node's primary SNMP
     * interface from the specified list of InetAddress objects.
     * 
     * @param addressList
     *            List of InetAddress objects representing all the interfaces
     *            belonging to a particular node which support the "SNMP"
     *            service and have a valid ifIndex.
     * 
     * @param strict
     *	          Boolean variable which requires an interface to be part of a
     *            Collectd package to be eligible as a primary SNMP interface
     *
     * @return InetAddress object of the primary SNMP interface or null if none
     *         of the node's interfaces are eligible.
     */
    public synchronized InetAddress determinePrimarySnmpInterface(List addressList, boolean strict) {
        Category log = ThreadCategory.getInstance(CollectdConfigFactory.class);

        InetAddress primaryIf = null;

        // For now hard-coding primary interface address selection method to MIN
        String method = SELECT_METHOD_MIN;

        // To be selected as the the primary SNMP interface for a node
        // the interface must be included by a Collectd package if strict
        // is true, and that package must include the SNMP service and
        // the service must be enabled.
        //
        // Iterate over interface list and test each interface
        //
        Iterator iter = addressList.iterator();
        while (iter.hasNext()) {
            InetAddress ipAddr = (InetAddress) iter.next();
            if (log.isDebugEnabled())
                log.debug("determinePrimarySnmpIf: checking interface " + ipAddr.getHostAddress());
            primaryIf = compareAndSelectPrimaryCollectionInterface("SNMP", ipAddr, primaryIf, method, strict);
        }

        if (log.isDebugEnabled())
            if (primaryIf != null)
                log.debug("determinePrimarySnmpInterface: candidate primary SNMP interface: " + primaryIf.getHostAddress());
            else
                log.debug("determinePrimarySnmpInterface: no candidate primary SNMP interface found");
        return primaryIf;
    }

    /**
     * Utility method which compares two InetAddress objects based on the
     * provided method (MIN/MAX) and returns the InetAddress which is to be
     * considered the primary interface.
     * 
     * NOTE: In order for an interface to be considered primary, if strict is
     * true, it must be included by a Collectd package which supports the
     * specified service. This method will return null if the 'oldPrimary'
     * address is null and the 'currentIf' address does not pass the Collectd
     * package check, if strict is true..
     * 
     * @param svcName
     *            Service name
     * @param currentIf
     *            Interface with which to compare the 'oldPrimary' address.
     * @param oldPrimary
     *            Primary interface to be compared against the 'currentIf'
     *            address.
     * @param method
     *            Comparison method to be used (either "min" or "max")
     * @param strict
     *            require interface to be part of a Collectd package
     * 
     * @return InetAddress object of the primary interface based on the provided
     *         method or null if neither address is eligible to be primary.
     */
    public synchronized InetAddress compareAndSelectPrimaryCollectionInterface(String svcName, InetAddress currentIf, InetAddress oldPrimary, String method, boolean strict) {
        InetAddress newPrimary = null;

        if (oldPrimary == null && strict) {
            if (lookupInterfaceServicePair(currentIf.getHostAddress(), svcName))
                return currentIf;
            else
                return oldPrimary;
        }

        if (oldPrimary == null)
            return currentIf;

        long current = IPSorter.convertToLong(currentIf.getAddress());
        long primary = IPSorter.convertToLong(oldPrimary.getAddress());

        if (method.equals(SELECT_METHOD_MIN)) {
            // Smallest address wins
            if (current < primary) {
                // Replace the primary interface with the current
                // interface only if the current interface is managed!
                if (strict) {
                    if (lookupInterfaceServicePair(currentIf.getHostAddress(), svcName))
                        newPrimary = currentIf;
                }
                else {
                    newPrimary = currentIf;
                }
            }
        } else {
            // Largest address wins
            if (current > primary) {
                // Replace the primary interface with the current
                // interface only if the current interface is managed!
                if (strict) {
                    if (lookupInterfaceServicePair(currentIf.getHostAddress(), svcName))
                        newPrimary = currentIf;
                }
                else {
                    newPrimary = currentIf;
                }
            }
        }

        if (newPrimary != null)
            return newPrimary;
        else
            return oldPrimary;
    }


private boolean hasExcludeRange(Package pkg, long addr, boolean has_specific) {
    boolean has_range_exclude = false;
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
    return has_range_exclude;
}

private boolean hasSpecificUrl(String iface, Package pkg, boolean has_specific) {
    Enumeration eurl = pkg.enumerateIncludeUrl();
    while (!has_specific && eurl.hasMoreElements()) {
        has_specific = interfaceInUrl(iface, (String) eurl.nextElement());
    }
    return has_specific;
}

private boolean hasSpecific(Package pkg, long addr) {
    boolean has_specific = false;
    Enumeration espec = pkg.enumerateSpecific();
    while (!has_specific && espec.hasMoreElements()) {
        long speca = IPSorter.convertToLong(espec.nextElement().toString());
        if (speca == addr)
            has_specific = true;
    }
    return has_specific;
}

private boolean hasIncludeRange(long addr, Enumeration eincs) {
    boolean has_range_include = false;
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
    return has_range_include;
}

    private boolean interfaceInFilter(String iface, Package pkg, Category log) {
        boolean filterPassed = false;

        // get list of IPs in this package
        java.util.List ipList = (java.util.List) m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
            filterPassed = ipList.contains(iface);
        } else {
            log.debug("interfaceInFilter: ipList contains no data");
        }

        if (!filterPassed)
            log.debug("interfaceInFilter: Interface " + iface + " passed filter for package " + pkg.getName() + "?: false");
        return filterPassed;
    }

}
