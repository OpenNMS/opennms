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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.opennms.netmgt.config.api.PathOutageConfig;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;

/**
 * <p>PollerConfig interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface PollerConfig extends PathOutageConfig {

    /**
     * This method returns the configured critical service name.
     *
     * @return the name of the configured critical service, or null if none is
     *         present
     */
    String getCriticalService();

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
    boolean shouldPollAllIfNoCriticalServiceDefined();

    /**
     * Returns true if node outage processing is enabled.
     *
     * @return a boolean.
     */
    boolean isNodeOutageProcessingEnabled();

    /**
     * Returns true if serviceUnresponsive behavior is enabled. If enabled a
     * serviceUnresponsive event is generated for TCP-based services if the
     * service monitor is able to connect to the designated port but times out
     * before receiving the expected response. If disabled, an outage will be
     * generated in this scenario.
     *
     * @return a boolean.
     */
    boolean isServiceUnresponsiveEnabled();


    /**
     * Returns true if we want to do things async
     *
     * @return a boolean.
     */
    boolean isAsyncEngineEnabled();

    /**
     *
     * @return
     */
    int getMaxConcurrentAsyncPolls();

    /**
     * This method is used to rebuild the package against ip list mapping when
     * needed. When a node gained service event occurs, poller has to determine
     * which package the ip/service combination is in, but if the interface is a
     * newly added one, the package ip list should be rebuilt so that poller
     * could know which package this ip/service pair is in.
     */
    void rebuildPackageIpListMap();

    Iterable<Parameter> parameters(final Service svc);

    /**
     * Determine the list of IPs the filter rule for this package allows
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @return a {@link java.util.List} object.
     */
    List<InetAddress> getIpList(Package pkg);
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
    boolean isInterfaceInPackage(String iface, Package pkg);

    /**
     * Returns true if the service is part of the package and the status of the
     * service is set to "on". Returns false if the service is not in the
     * package or it is but the status of the service is set to "off".
     *
     * @param svcName
     *            The service name to lookup.
     * @param pkg
     *            The package to lookup up service.
     * @return a boolean.
     */
    boolean isServiceInPackageAndEnabled(String svcName, Package pkg);

    /**
     * Return the Service object with the given name from the give Package.
     *
     * @param svcName the service name to lookup
     * @param pkg the packe to lookup the the service in
     * @return the Service object from the package with the give name, null if its not in the pkg
     */
    Service getServiceInPackage(String svcName, Package pkg);

    /**
     * Returns true if the service has a monitor configured, false otherwise.
     *
     * @param svcName
     *            The service name to lookup.
     * @return a boolean.
     */
    boolean isServiceMonitored(String svcName);

    /**
     * Returns the first package that the ip belongs to that is not marked as remote, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @return the first package that the ip belongs to, null if none
     */
    Package getFirstLocalPackageMatch(String ipaddr);

    /**
     * Returns true if the ip is part of at least one package.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @return true if the ip is part of at least one package, false otherwise
     */
    boolean isPolled(String ipaddr);

    /**
     * Returns true if the ip is part of at least one package that is NOT marked
     * as remote
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @return true if the ip is part of at least one package, false otherwise
     */
    boolean isPolledLocally(String ipaddr);

    /**
     * Returns true if this package has the service enabled and if there is a
     * monitor for this service.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param svcName
     *            the service to check
     * @param pkg
     *            the package to check
     * @return true if the ip is part of at least one package and the service is
     *         enabled in this package and monitored, false otherwise
     */
    boolean isPolled(String svcName, Package pkg);

    /**
     * Returns true if the ip is part of at least one package and if this package
     * has the service enabled and if there is a monitor for this service.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @param svcName
     *            the service to check
     * @return true if the ip is part of at least one package and the service is
     *         enabled in this package and monitored, false otherwise
     */
    boolean isPolled(String ipaddr, String svcName);

    
    /**
     * Returns true if the ip is part of at least one package and if this package
     * has the service enabled and if there is a monitor for this service and the
     * package is NOT marked as remote
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @param svcName
     *            the service to check
     * @return true if the ip is part of at least one package and the service is
     *         enabled in this package and monitored, false otherwise
     */
    boolean isPolledLocally(String ipaddr, String svcName);

    /**
     * Retrieves configured RRD step size.
     *
     * @param pkg
     *            Name of the data collection
     * @return RRD step size for the specified collection
     */
    int getStep(Package pkg);

    /**
     * Retrieves configured list of RoundRobin Archive statements.
     *
     * @param pkg
     *            Name of the data collection
     * @return list of RRA strings.
     */
    List<String> getRRAList(Package pkg);
    
    /**
     * <p>getAllPackageMatches</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<String> getAllPackageMatches(String ipAddr);
    
    /**
     * <p>getNextOutageIdSql</p>
     *
     * @return a {@link java.lang.String} object.
     * 
     * @deprecated We should be using DAOs that autoincrement.
     */
    String getNextOutageIdSql();
    
    /**
     * <p>enumeratePackage</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    public Enumeration<Package> enumeratePackage();

    public List<Package> getPackages();

    /**
     * Find the {@link Package} containing the service selected for the given IP.
     * @param ipAddr the address to select the package for
     * @param serviceName the name of the service
     * @return the found package or {@code null} if no package matches
     */
    default Package findPackageForService(final String ipAddr, final String serviceName) {
        Package lastPkg = null;
        for (final var pkg : this.getPackages()) {
            if (pkg.getPerspectiveOnly()) {
                continue;
            }

            if (!this.isServiceInPackageAndEnabled(serviceName, pkg)) {
                continue;
            }

            if (!this.isInterfaceInPackage(ipAddr, pkg)) {
                continue;
            }

            lastPkg = pkg;
        }
        return lastPkg;
    }

    /**
     * Find the service for the given IP by service name.
     * @param ipAddr the address to select the package for
     * @param serviceName the name of the service
     * @return the found matching info
     */
    default Optional<Package.ServiceMatch> findService(final String ipAddr, final String serviceName) {
        final var pkg = this.findPackageForService(ipAddr, serviceName);
        if (pkg == null) {
            return Optional.empty();
        }

        return pkg.findService(serviceName);
    }

    /**
     * <p>getPackage</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    public Package getPackage(String pkgName);

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads();

    public Set<String> getServiceMonitorNames();

    public Optional<ServiceMonitorLocator> getServiceMonitorLocator(String svcName);
    
    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    public void update() throws IOException;
    
    /**
     * <p>save</p>
     *
     * @throws java.io.IOException if any.
     */
    public void save() throws IOException;

    
    /**
     * <p>addPackage</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    void addPackage(Package pkg);

    /**
     * <p>getLocalConfiguration</p>
     *
     * @return a {@link org.opennms.netmgt.config.poller.PollerConfiguration} object.
     */
    PollerConfiguration getLocalConfiguration();

    /**
     * <p>getExtendedConfiguration</p>
     *
     * @return a {@link org.opennms.netmgt.config.poller.PollerConfiguration} object.
     */
    default PollerConfiguration getExtendedConfiguration() {
        return getLocalConfiguration();
    }

    /**
     * <p>getServiceMonitorLocators</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<ServiceMonitorLocator> getServiceMonitorLocators();

    ServiceMonitorRegistry getServiceMonitorRegistry();

    void setExternalData(List<Package> externalPackages, List<Monitor> externalMonitors);

    /**
     * <p>getReadLock</p>
     * 
     * @return a Lock
     */
    Lock getReadLock();

    /**
     * <p>getWriteLock</p>
     * 
     * @return a Lock
     */
    Lock getWriteLock();

}
