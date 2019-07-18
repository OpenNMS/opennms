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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.opennms.core.network.IpListFromUrl;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.ThreshdConfigModifiable;
import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ServiceStatus;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.dao.api.EffectiveConfigurationDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.EffectiveConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

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
public final class ThreshdConfigFactory implements ThreshdConfigModifiable {
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

    @Autowired
    private EffectiveConfigurationDao m_configDao;

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

    @Override
    public synchronized void reload() {
        m_singleton = null;
        m_loaded = false;
        try {
            init();
        } catch (IOException e) {
            // TODO Log ERROR and continue with existing config
            e.printStackTrace();
        }
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized ThreshdConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }
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

    @Override
    public synchronized void rebuildPackageIpListMap() {
        createPackageIpListMap();
    }

    @Override
    public synchronized void saveCurrent() throws IOException {
        // marshall to a string first, then write the string to the file. This way the original config
        final String xmlString = JaxbUtils.marshal(m_config);
        if (xmlString != null) {
            saveXML(xmlString);
            reload();
        }
        saveEffective();
    }

    @Override
    public synchronized ThreshdConfiguration getConfiguration() {
        return m_config;
    }

    @Override
    public synchronized boolean interfaceInPackage(String iface, Package pkg) {

        final InetAddress ifaceAddr = addr(iface);
        boolean filterPassed = false;

        // get list of IPs in this package
        List<InetAddress> ipList = m_pkgIpMap.get(pkg);
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

    @Override
    public synchronized boolean serviceInPackageAndEnabled(String svcName, Package pkg) {
        boolean result = false;
        // TODO - this loop is inefficient for thresholding - should initialize a Map<Name, Serivce>.
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

    // injection of EffectiveConfigurationDao
    public void setEffectiveConfigurationDao(EffectiveConfigurationDao effectiveConfigurationDao) {
        m_configDao = effectiveConfigurationDao;
        saveEffective();
    }

    // Go through the configuration and build a mapping of each configured URL to a list of IPs configured in that URL - done at init() time so that repeated file reads can be
    // avoided
    private void createUrlIpMap() {
        m_urlIPMap = new HashMap<String, List<String>>();

        for (Package pkg : m_config.getPackages()) {
            for (String urlname : pkg.getIncludeUrls()) {
                List<String> iplist = IpListFromUrl.fetch(urlname);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(urlname, iplist);
                }
            }
        }
    }

    // This method is used to establish package against an iplist iplist mapping, with which, the iplist is selected per package via the configured filter rules from the database.
    private void createPackageIpListMap() {
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

    // This method is used to determine if the named interface is included in the passed package's url includes.
    private boolean interfaceInUrl(String addr, String url) {
        List<String> iplist = m_urlIPMap.get(url);
        if (iplist != null && iplist.size() > 0) {
            return iplist.contains(addr);
        }
        return false;
    }

    private void saveXML(String xmlString) throws IOException {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME);
        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
        fileWriter.write(xmlString);
        fileWriter.flush();
        fileWriter.close();
    }

    private synchronized void saveEffective() {
        EffectiveConfiguration effective = new EffectiveConfiguration();
        effective.setKey(ConfigFileConstants.getFileName(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME));
        effective.setConfiguration(getJsonConfig());
        effective.setLastUpdated(new Date());
        m_configDao.save(effective);
    }

    private String getJsonConfig() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(objectMapper.getTypeFactory()));
            return objectMapper.writeValueAsString(m_config);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }
}
