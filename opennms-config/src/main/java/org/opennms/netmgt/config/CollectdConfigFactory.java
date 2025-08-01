/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Collection Daemon from the collectd-configuration.xml file.
 *
 * A mapping of the configured URLs to the IP list they contain is built at
 * init() time so as to avoid numerous file reads.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:jamesz@opennms.com">James Zuo</a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 */
public class CollectdConfigFactory implements org.opennms.netmgt.config.api.CollectdConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CollectdConfigFactory.class);
    public static final String SELECT_METHOD_MIN = "min";

    private CollectdConfiguration localCollectdConfig;
    private final Object m_collectdConfigMutex = new Object();

    private final String m_fileName;

    private List<Package> extendedPackages = Collections.emptyList();
    private List<Package> mergedPackages = Collections.emptyList();
    private List<Collector> extendedCollectors = Collections.emptyList();
    private List<Collector> mergedCollectors = Collections.emptyList();

    public CollectdConfigFactory() throws IOException {
        m_fileName = ConfigFileConstants.getFile(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME).getPath();

        init(new FileInputStream(m_fileName));
    }

    /**
     * For testing purposes only.
     * 
     * @param stream
     * @throws IOException
     */
    public CollectdConfigFactory(InputStream stream) throws IOException {
        m_fileName = null;

        init(stream);
    }

    /**
     * <p>Constructor for CollectdConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws IOException
     */
    private void init(final InputStream stream) throws IOException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(stream);
            CollectdConfiguration config = JaxbUtils.unmarshal(CollectdConfiguration.class, isr);
            synchronized (m_collectdConfigMutex) {
                this.localCollectdConfig = config;
            }
        } finally {
            IOUtils.closeQuietly(isr);
        }

        this.setUpInternalData();
    }

    private void setUpInternalData() {
        synchronized (this.m_collectdConfigMutex) {
            this.mergedCollectors = ListUtils.union(localCollectdConfig.getCollectors(), this.extendedCollectors);
            this.mergedPackages = ListUtils.union(localCollectdConfig.getPackages(), this.extendedPackages);
        }
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public void reload() throws IOException {
        init(new FileInputStream(m_fileName));
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws java.io.IOException if any.
     */
    public void saveCurrent() throws IOException {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME);

        CollectdConfiguration config = null;
        synchronized (m_collectdConfigMutex) {
            config = localCollectdConfig;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(cfgFile);
            JaxbUtils.marshal(config, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }

        reload();
    }

    /**
     * <p>getCollectdConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.collectd.CollectdConfiguration} object.
     */
    public CollectdConfiguration getLocalCollectdConfig() {
        synchronized (m_collectdConfigMutex) {
            return localCollectdConfig;
        }
    }

    @Override
    public Integer getThreads() {
        return this.localCollectdConfig.getThreads();
    }

    /**
     * Returns true if collection package exists
     *
     * @param name
     *            The package name to check
     * @return True if the package exists
     */
    public boolean packageExists(String name) {
        synchronized (m_collectdConfigMutex) {
            for (final Package pkg : this.mergedPackages) {
                if (pkg.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * <p>getPackage</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.collectd.Package} object.
     */
    public Package getPackage(final String name) {
        synchronized (m_collectdConfigMutex) {
            for (Package pkg : this.mergedPackages) {
                if (pkg.getName().equals(name)) {
                    return pkg;
                }
            }
            return null;
        }
    }

    @Override
    public List<Package> getPackages() {
        return this.mergedPackages;
    }

    @Override
    public List<Collector> getCollectors() {
        return this.mergedCollectors;
    }

    /**
     * Returns true if collection domain exists
     *
     * @param name
     *            The domain name to check
     * @return True if the domain exists
     */
    public boolean domainExists(final String name) {
        synchronized (m_collectdConfigMutex) {
            for (Package pkg : this.mergedPackages) {
                if ((pkg.getIfAliasDomain() != null)
                        && pkg.getIfAliasDomain().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Returns true if the specified service's interface is included by at least one
     * package which has the specified service and that service is enabled (set
     * to "on").
     *
     * @param service
     *            {@link OnmsMonitoredService} to check
     * @return true if Collectd config contains a package which includes the
     *         specified interface and has the specified service enabled.
     */
    public boolean isServiceCollectionEnabled(final OnmsMonitoredService service) {
        return isServiceCollectionEnabled(service.getIpInterface(), service.getServiceName());
    }

    /**
     * Returns true if the specified interface is included by at least one
     * package which has the specified service and that service is enabled (set
     * to "on").
     *
     * @param iface
     *            {@link OnmsIpInterface} to lookup
     * @param svcName
     *            The service name to lookup
     * @return true if Collectd config contains a package which includes the
     *         specified interface and has the specified service enabled.
     */
    public boolean isServiceCollectionEnabled(final OnmsIpInterface iface, final String svcName) {
        for (Package wpkg : this.mergedPackages) {

            // Does the package include the interface?
            if (interfaceInPackage(iface, wpkg)) {
                // Yes, now see if package includes
                // the service and service is enabled
                //
                if (wpkg.serviceInPackageAndEnabled(svcName)) {
                    // Thats all we need to know...
                	return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if the specified interface is included by at least one
     * package which has the specified service and that service is enabled (set
     * to "on").
     *
     * @deprecated This function should take normal model objects instead of bare IP addresses
     * and service names. Use {@link #isServiceCollectionEnabled(OnmsIpInterface, String)}
     * instead.
     *
     * @param ipAddr
     *            IP address of the interface to lookup
     * @param svcName
     *            The service name to lookup
     * @return true if Collectd config contains a package which includes the
     *         specified interface and has the specified service enabled.
     */
    public boolean isServiceCollectionEnabled(final String ipAddr, final String svcName) {
        synchronized (m_collectdConfigMutex) {
            for (Package wpkg : this.mergedPackages) {

                // Does the package include the interface?
                //
                if (interfaceInPackage(ipAddr, wpkg)) {
                    // Yes, now see if package includes
                    // the service and service is enabled
                    //
                    if (wpkg.serviceInPackageAndEnabled(svcName)) {
                        // Thats all we need to know...
                    	return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean interfaceInFilter(String iface, Package pkg) {
        String filter = pkg.getFilter().getContent();
        if (iface == null) return false;
        final InetAddress ifaceAddress = addr(iface);

        boolean filterPassed = false;

        //
        // Get a list of IP address per package against the filter rules from
        // database and populate the package, IP list map.
        //

        LOG.debug("interfaceInFilter: package is {}. filter rules are {}", pkg.getName(), filter);
        try {
            final List<InetAddress> ipList = FilterDaoFactory.getInstance().getActiveIPAddressList(filter);
            filterPassed = ipList.contains(ifaceAddress);
            if (!filterPassed) {
                LOG.debug("interfaceInFilter: Interface {} passed filter for package {}?: false", iface, pkg.getName());
            }
        } catch (Throwable t) {
            LOG.error("interfaceInFilter: Failed to map package: {} to an IP List with filter \"{}\"", pkg.getName(), pkg.getFilter().getContent(), t);
        }

        return filterPassed;
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
     * @deprecated This function should take normal model objects instead of bare IP 
     * addresses. Move this implementation into {@link #interfaceInPackage(OnmsIpInterface, Package)}.
     *
     * @param iface
     *            The interface to test against the package.
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    public boolean interfaceInPackage(final String iface, Package pkg) {
        boolean filterPassed = interfaceInFilter(iface, pkg);

        if (!filterPassed) {
            return false;
        }

        //
        // Ensure that the interface is in the specific list or
        // that it is in the include range and is not excluded
        //

        byte[] addr = toIpAddrBytes(iface);

        boolean has_range_include = pkg.hasIncludeRange(iface);
        boolean has_specific = pkg.hasSpecific(addr);

        has_specific = pkg.hasSpecificUrl(iface, has_specific);
        boolean has_range_exclude = pkg.hasExcludeRange(iface);

        boolean packagePassed = has_specific || (has_range_include && !has_range_exclude);
        if(packagePassed) {
            LOG.info("interfaceInPackage: Interface {} passed filter and specific/range for package {}?: {}", iface, pkg.getName(), packagePassed);
        } else {
            LOG.debug("interfaceInPackage: Interface {} passed filter and specific/range for package {}?: {}", iface, pkg.getName(), packagePassed);
        }
        return packagePassed;
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
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    public boolean interfaceInPackage(final OnmsIpInterface iface, Package pkg) {
        return interfaceInPackage(iface.getIpAddressAsString(), pkg);
    }

    @Override
    public void setExternalData(final List<Package> packages, final List<Collector> collectors) {
        synchronized (this.m_collectdConfigMutex) {
            this.extendedPackages = packages;
            this.extendedCollectors = collectors;

            this.setUpInternalData();
        }
    }
}
