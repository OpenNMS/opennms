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
import static org.opennms.core.utils.InetAddressUtils.isInetAddressInRange;
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.opennms.core.network.IpListFromUrl;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ServiceStatus;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Thresholding Daemon from the threshd-configuration xml file.
 *
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:jamesz@opennms.com>James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class ThreshdConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ThreshdConfigFactory.class);

    /**
     * The config class loaded from the config file
     */
    protected ThreshdConfiguration m_config;

    /**
     * A mapping of the configured URLs to a list of the specific IPs configured in each - so as to avoid file reads
     */
    private Map<String, List<String>> m_urlIPMap;

    /**
     * A mapping of the configured package to a list of IPs selected via filter rules, so as to avoid repetitive database access.
     */
    private Map<Package, List<InetAddress>> m_pkgIpMap;

    /**
     * The singleton instance of this factory
     */
    private static ThreshdConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * <p>Constructor for ThreshdConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public ThreshdConfigFactory(InputStream stream) throws IOException {
        try (final Reader reader = new InputStreamReader(stream)) {
            m_config = JaxbUtils.unmarshal(ThreshdConfiguration.class, reader);
        }

        createUrlIpMap();

        createPackageIpListMap();
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }
        

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME);

        LOG.debug("init: config file path: {}", cfgFile.getPath());

        InputStream stream = null;
        try {
            stream = new FileInputStream(cfgFile);
            m_singleton = new ThreshdConfigFactory(stream);
            m_loaded = true;
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws IOException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    public void reloadXML() throws IOException {
        /* FIXME: THIS IS WAY WRONG! Should only reload the xml not recreate the instance
         otherwise references to the old instance will still linger */
        reload();
    }

    protected void saveXML(String xmlString) throws IOException {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME);
        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
        fileWriter.write(xmlString);
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized ThreshdConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param configFactory a {@link org.opennms.netmgt.config.ThreshdConfigFactory} object.
     */
    public static void setInstance(ThreshdConfigFactory configFactory) {
    	m_loaded = true;
    	m_singleton = configFactory;
    }

    /**
     * Go through the configuration and build a mapping of each configured URL to a list of IPs configured in that URL - done at init() time so that repeated file reads can be
     * avoided
     */
    protected void createUrlIpMap() {
        m_urlIPMap = new HashMap<String, List<String>>();

        for (Package pkg : m_config.getPackages()) {
            for (String urlname : pkg.getIncludeUrls()) {
                java.util.List<String> iplist = IpListFromUrl.fetch(urlname);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(urlname, iplist);
                }
            }
        }
    }

    /**
     * This method is used to establish package against an iplist iplist mapping, with which, the iplist is selected per package via the configured filter rules from the database.
     */
    protected void createPackageIpListMap() {

        m_pkgIpMap = new HashMap<Package, List<InetAddress>>();

        for (final org.opennms.netmgt.config.threshd.Package pkg : m_config.getPackages()) {
            //
            // Get a list of ipaddress per package agaist the filter rules from
            // database and populate the package, IP list map.
            //
            final StringBuilder filterRules = new StringBuilder();
            if (pkg.getFilter().getContent().isPresent()) {
                filterRules.append(pkg.getFilter().getContent().get());
            }
            try {
                LOG.debug("createPackageIpMap: package is {}. filer rules are {}", filterRules, pkg.getName());

                FilterDaoFactory.getInstance().flushActiveIpAddressListCache();
                List<InetAddress> ipList = FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());
                if (ipList.size() > 0) {
                    m_pkgIpMap.put(pkg, ipList);
                }
            } catch (Throwable t) {
                LOG.error("createPackageIpMap: failed to map package: {} to an IP List with filter \"{}\"", pkg.getName(), pkg.getFilter().getContent().orElse(null), t);
            }
        }
    }

    /**
     * This nethod is used to rebuild the package agaist iplist mapping when needed. When a node gained service event occurs, threshd has to determine which package the ip/service
     * combination is in, but if the interface is a newly added one, the package iplist should be rebuilt so that threshd could know which package this ip/service pair is in.
     */
    public synchronized void rebuildPackageIpListMap() {
        createPackageIpListMap();
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws java.io.IOException
     *             if any.
     */
    public synchronized void saveCurrent() throws IOException {
        // marshall to a string first, then write the string to the file. This way the original config
        final String xmlString = JaxbUtils.marshal(m_config);
        if (xmlString != null) {
            saveXML(xmlString);
            reloadXML();
        }
    }

    /**
     * Return the threshd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.threshd.ThreshdConfiguration} object.
     */
    public synchronized ThreshdConfiguration getConfiguration() {
        return m_config;
    }

    /**
     * <p>
     * getPackage
     * </p>
     *
     * @param name
     *            a {@link java.lang.String} object.
     * @return a org$opennms$netmgt$config$threshd$Package object.
     */
    public synchronized org.opennms.netmgt.config.threshd.Package getPackage(String name) {
        for (org.opennms.netmgt.config.threshd.Package thisPackage : m_config.getPackages()) {
            if (thisPackage.getName().equals(name)) {
                return thisPackage;
            }
        }
        return null;
    }

    /**
     * This method is used to determine if the named interface is included in the passed package's url includes. If the interface is found in any of the URL files, then a value of
     * true is returned, else a false value is returned.
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
     * @return True if the interface is included in the url, false otherwise.
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

    /**
     * This method is used to determine if the named interface is included in the passed package definition. If the interface belongs to the package then a value of true is
     * returned. If the interface does not belong to the package a false value is returned. <strong>Note: </strong>Evaluation of the interface against a package filter will only
     * work if the IP is already in the database.
     *
     * @param iface
     *            The interface to test against the package.
     * @param pkg
     *            The package to check for the inclusion of the interface.
     * @return True if the interface is included in the package, false otherwise.
     */
    public synchronized boolean interfaceInPackage(String iface, org.opennms.netmgt.config.threshd.Package pkg) {

        final InetAddress ifaceAddr = addr(iface);
        boolean filterPassed = false;

        // get list of IPs in this package
        java.util.List<InetAddress> ipList = m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
            filterPassed = ipList.contains(ifaceAddr);
        }

        LOG.debug("interfaceInPackage: Interface {} passed filter for package {}?: {}", filterPassed, iface, pkg.getName());

        if (!filterPassed)
            return false;

        //
        // Ensure that the interface is in the specific list or
        // that it is in the include range and is not excluded
        //
        boolean has_specific = false;
        boolean has_range_include = false;
        boolean has_range_exclude = false;

        has_range_include = pkg.getIncludeRanges().size() == 0 && pkg.getSpecifics().size() == 0;

        for (IncludeRange rng : pkg.getIncludeRanges()) {
            if (isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                has_range_include = true;
                break;
            }
        }

        byte[] addr = toIpAddrBytes(iface);

        for (String spec : pkg.getSpecifics()) {
            byte[] speca = toIpAddrBytes(spec);
            if (new ByteArrayComparator().compare(speca, addr) == 0) {
                has_specific = true;
                break;
            }
        }

        final Iterator<String> eurl = pkg.getIncludeUrls().iterator();
        while (!has_specific && eurl.hasNext()) {
            has_specific = interfaceInUrl(iface, eurl.next());
        }

        for (ExcludeRange rng : pkg.getExcludeRanges()) {
            if (isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                has_range_exclude = true;
                break;
            }
        }

        return has_specific || (has_range_include && !has_range_exclude);
    }

    /**
     * Returns true if the service is part of the package and the status of the service is set to "on". Returns false if the service is not in the package or it is but the status
     * of the service is set to "off".
     *
     * @param svcName
     *            The service name to lookup.
     * @param pkg
     *            The package to lookup up service.
     * @return a boolean.
     */
    public synchronized boolean serviceInPackageAndEnabled(String svcName, org.opennms.netmgt.config.threshd.Package pkg) {
        boolean result = false;

        for (Service tsvc : pkg.getServices()) {
            if (tsvc.getName().equalsIgnoreCase(svcName)) {
                // Ok its in the package. Now check the
                // status of the service
                final ServiceStatus status = tsvc.getStatus().orElse(ServiceStatus.OFF);
                if (status == ServiceStatus.ON) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}
