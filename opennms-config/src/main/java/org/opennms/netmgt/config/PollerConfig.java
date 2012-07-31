/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

/**
 * <p>PollerConfig interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public interface PollerConfig {

    /**
     * This method returns the boolean flag xmlrpc to indicate if notification
     * to external xmlrpc server is needed.
     *
     * @return true if need to notify an external xmlrpc server
     */
    boolean shouldNotifyXmlrpc();

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
     * Returns true if the path outage feature is enabled. If enabled, the code
     * looks for a critical path specification when processing nodeDown events.
     * If a critical path exists for the node, it will be tested. If the
     * critical path fails to respond, the eventReason parameter on the
     * nodeDown event is set to "pathOutage". This parameter will be used by
     * notifd to suppress nodeDown notification.
     *
     * @return a boolean.
     */
    boolean isPathOutageEnabled();

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
     * Returns the first package that the ip belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @return the first package that the ip belongs to, null if none
     */
    Package getFirstPackageMatch(String ipaddr);

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
     */
    String getNextOutageIdSql();
    
    /**
     * <p>enumeratePackage</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    public Enumeration<Package> enumeratePackage();
    
    /**
     * <p>getPackage</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    public Package getPackage(String pkgName);
    
    /**
     * <p>getServiceSelectorForPackage</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @return a {@link org.opennms.netmgt.model.ServiceSelector} object.
     */
    public ServiceSelector getServiceSelectorForPackage(Package pkg);

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads();

    /**
     * <p>getServiceMonitors</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, ServiceMonitor> getServiceMonitors();
    
    /**
     * <p>releaseAllServiceMonitors</p>
     */
    public void releaseAllServiceMonitors();

    /**
     * <p>getServiceMonitor</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.ServiceMonitor} object.
     */
    public ServiceMonitor getServiceMonitor(String svcName);
    
    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void update() throws IOException, MarshalException, ValidationException;
    
    /**
     * <p>save</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void save() throws MarshalException, IOException, ValidationException;

    
    /**
     * <p>addPackage</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    void addPackage(Package pkg);
    
    /**
     * <p>addMonitor</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param className a {@link java.lang.String} object.
     */
    void addMonitor(String svcName, String className);

    /**
     * <p>getConfiguration</p>
     *
     * @return a {@link org.opennms.netmgt.config.poller.PollerConfiguration} object.
     */
    PollerConfiguration getConfiguration();

    /**
     * <p>getServiceMonitorLocators</p>
     *
     * @param context a {@link org.opennms.netmgt.poller.DistributionContext} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context);

    Lock getReadLock();

    Lock getWriteLock();

}
