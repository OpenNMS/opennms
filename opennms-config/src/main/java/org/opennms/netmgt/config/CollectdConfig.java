/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectdConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CollectdConfig.class);

    private CollectdConfiguration m_config;

    private final String m_localServer;

    private final boolean m_verifyServer;

    /**
     * Convenience object for CollectdConfiguration.
     *
     * @param config collectd configuration object
     * @param localServer local server name from opennms-server.xml
     * @param verifyServer verify server option from opennms-server.xml
     */
    protected CollectdConfig(final CollectdConfiguration config, final String localServer, final boolean verifyServer) {
        m_config = config;
        m_localServer = localServer;
        m_verifyServer = verifyServer;
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.collectd.CollectdConfiguration} object.
     */
    public CollectdConfiguration getConfig() {
        return m_config;
    }

    /**
     * <p>getPackages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Package> getPackages() {
        return m_config.getPackages();
    }

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads() {
        return m_config.getThreads();
    }

    /**
     * <p>getPackage</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.CollectdPackage} object.
     */
    public Package getPackage(final String name) {
        for (Package pkg : getPackages()) {
            if (pkg.getName().equals(name)) {
                return pkg;
            }
        }
        return null;
    }

    /**
     * Returns true if collection domain exists
     *
     * @param name
     *            The domain name to check
     * @return True if the domain exists
     */
    public boolean domainExists(final String name) {
        for (Package pkg : getPackages()) {
            if ((pkg.getIfAliasDomain() != null)
                    && pkg.getIfAliasDomain().equals(name)) {
                return true;
            }
        }
        return false;
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
        for (Package wpkg : getPackages()) {

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
     * and service names. Use {@link CollectdConfig#isServiceCollectionEnabled(OnmsIpInterface, String)}
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
        for (Package wpkg : getPackages()) {

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

    private static String getFilterRule(String filter, String localServer, boolean verifyServer) {
        StringBuffer filterRules = new StringBuffer(filter);
    
        if (verifyServer) {
            filterRules.append(" & (serverName == ");
            filterRules.append('\"');
            filterRules.append(localServer);
            filterRules.append('\"');
            filterRules.append(")");
        }
        return filterRules.toString();
    }

    public boolean interfaceInFilter(String iface, Package pkg) {
        String filter = pkg.getFilter().getContent();
        if (iface == null) return false;
        final InetAddress ifaceAddress = addr(iface);

        boolean filterPassed = false;

        // get list of IPs in this package
        List<InetAddress> ipList = Collections.emptyList();

        //
        // Get a list of IP address per package against the filter rules from
        // database and populate the package, IP list map.
        //
        String filterRules = getFilterRule(filter, m_localServer, m_verifyServer);
        
        LOG.debug("interfaceInFilter: package is {}. filter rules are {}", pkg.getName(), filterRules);
        try {
            ipList = FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules);
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
     * addresses. Move this implementation into {@link #interfaceInPackage(OnmsIpInterface)}.
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
}
