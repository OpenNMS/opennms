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

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface PollerConfig {

    /**
     * This method returns the boolean flag xmlrpc to indicate if notification
     * to external xmlrpc server is needed.
     * 
     * @return true if need to notify an external xmlrpc server
     */
    public abstract boolean getXmlrpc();

    /**
     * This method returns the configured critical service name.
     * 
     * @return the name of the configured critical service, or null if none is
     *         present
     */
    public abstract String getCriticalService();

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
    public abstract boolean pollAllIfNoCriticalServiceDefined();

    /**
     * Returns true if node outage processing is enabled.
     */
    public abstract boolean nodeOutageProcessingEnabled();

    /**
     * Returns true if serviceUnresponsive behavior is enabled. If enabled a
     * serviceUnresponsive event is generated for TCP-based services if the
     * service monitor is able to connect to the designated port but times out
     * before receiving the expected response. If disabled, an outage will be
     * generated in this scenario.
     */
    public abstract boolean serviceUnresponsiveEnabled();

    /**
     * Returns true if the path outage feature is enabled. If enabled, the code
     * looks for a critical path specification when processing nodeDown events.
     * If a critical path exists for the node, it will be tested. If the
     * critical path fails to respond, the eventReason parameter on the
     * nodeDown event is set to "pathOutage". This parameter will be used by
     * notifd to suppress nodeDown notification.
     */
    public abstract boolean pathOutageEnabled();

    /**
     * This method is used to rebuild the package agaist iplist mapping when
     * needed. When a node gained service event occurs, poller has to determine
     * which package the ip/service combination is in, but if the interface is a
     * newly added one, the package iplist should be rebuilt so that poller
     * could know which package this ip/service pair is in.
     */
    public abstract void rebuildPackageIpListMap();

    /**
     * Determine the list of IPs the filter rule for this package allows
     * @param pkg
     * @return
     */
    public abstract List getIpList(Package pkg);
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
    public abstract boolean interfaceInPackage(String iface, org.opennms.netmgt.config.poller.Package pkg);

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
    public abstract boolean serviceInPackageAndEnabled(String svcName, org.opennms.netmgt.config.poller.Package pkg);

    /**
     * Return the Service object with the given name from the give Package.
     * 
     * @param svcName the service name to lookup
     * @param pkg the packe to lookup the the service in
     * @return the Service object from the package with the give name, null if its not in the pkg
     */
    public abstract Service getServiceInPackage(String svcName, Package pkg);

    /**
     * Returns true if the service has a monitor configured, false otherwise.
     * 
     * @param svcName
     *            The service name to lookup.
     */
    public abstract boolean serviceMonitored(String svcName);

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
    public abstract org.opennms.netmgt.config.poller.Package getFirstPackageMatch(String ipaddr);

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
    public abstract boolean isPolled(String ipaddr);

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
    public abstract boolean isPolled(String svcName, org.opennms.netmgt.config.poller.Package pkg);

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
    public abstract boolean isPolled(String ipaddr, String svcName);

    /**
     * Retrieves configured RRD step size.
     * 
     * @param pkg
     *            Name of the data collection
     * 
     * @return RRD step size for the specified collection
     */
    public abstract int getStep(org.opennms.netmgt.config.poller.Package pkg);

    /**
     * Retrieves configured list of RoundRobin Archive statements.
     * 
     * @param pkg
     *            Name of the data collection
     * 
     * @return list of RRA strings.
     */
    public abstract List getRRAList(Package pkg);
    
    public abstract List getAllPackageMatches(String ipAddr);
    
    public abstract String getNextOutageIdSql();
    
    public Enumeration enumeratePackage();
    
    public Package getPackage(String pkgName);
    
    public ServiceSelector getServiceSelectorForPackage(Package pkg);

    public int getThreads();

    public Map getServiceMonitors();
    
    public void releaseAllServiceMonitors();

    public ServiceMonitor getServiceMonitor(String svcName);
    
    public void update() throws IOException, MarshalException, ValidationException;
    
    public void save() throws MarshalException, IOException, ValidationException;

    
    public abstract void addPackage(Package pkg);
    
    public abstract void addMonitor(String svcName, String className);

    public abstract PollerConfiguration getConfiguration();

    public abstract void saveResponseTimeData(String locationMonitor, OnmsMonitoredService monSvc, double responseTime, Package pkg);

    public abstract Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context);

}
