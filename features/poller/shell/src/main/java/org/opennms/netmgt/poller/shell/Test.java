/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.shell;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

/**
 * This class was originally a standalone tool available in $OPENNMS_HOME/bin/poller-test
 * and was migrated to the Karaf shell as part of NMS-9095.
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @author jwhite
 */
@Command(scope = "poller", name = "test", description = "Execute a poller test from the command line using current settings from poller-configuration.xml")
@org.apache.karaf.shell.api.action.lifecycle.Service
public class Test implements Action {

    @Option(name = "-i", aliases = "--ipaddress", description = "IP Address to test", required = true, multiValued = false)
    String ipAddress;

    @Option(name = "-s", aliases = "--service", description = "Service name", required = true, multiValued = false)
    String serviceName;

    @Option(name = "-P", aliases = "--package", description = "Poller Package", required = false, multiValued = false)
    String packageName;

    @Option(name = "-p", aliases = "--param", description = "Service parameter ~ key=value", required = false, multiValued = true)
    List<String> serviceParameters;

    @Option(name = "-c", aliases = "--class", description = "Monitor Class", required = false, multiValued = false)
    String monitorClass;

    @Reference
    public ServiceMonitorRegistry registry;

    @Reference
    public IpInterfaceDao ipInterfaceDao;

    @Reference
    public TransactionOperations transactionTemplate;

    @Override
    public Object execute() throws Exception {
        // Parse/validate the IP address
        final InetAddress addr = InetAddressUtils.addr(ipAddress);
        if (addr == null) {
            throw new IllegalStateException("Error getting InetAddress object for " + ipAddress);
        }

        final Map<String,Object> parameters = Poll.parse(serviceParameters);
        final MonitoredService monSvc = transactionTemplate.execute(new TransactionCallback<MonitoredService>() {
            @Override
            public MonitoredService doInTransaction(TransactionStatus status) {
                final List<OnmsIpInterface> ips = ipInterfaceDao.findByIpAddress(ipAddress);
                if (ips == null  || ips.size() == 0) {
                    System.err.printf("Error: Can't find the IP address %s on the database\n", ipAddress);
                    return null;
                }
                if (ips.size() > 1) {
                    System.out.printf("Warning: there are several IP interface objects associated with the IP address %s (picking the first one)\n", ipAddress);
                }
                OnmsNode n = ips.get(0).getNode();
                return new SimpleMonitoredService(addr, n.getId(), n.getLabel(), serviceName);
            }
        });

        if (monSvc == null) {
            // This can happen if no matching IP address was found in the database,
            // in which case we already printed an error message above
            return null;
        }

        // Read a fresh copy of poller-configuration.xml
        final PollerConfig pollerConfig = ReadOnlyPollerConfigManager.create();

        System.out.printf("Checking service %s on IP %s%n", serviceName, ipAddress);

        final org.opennms.netmgt.config.poller.Package pkg = packageName == null ? pollerConfig.getFirstLocalPackageMatch(ipAddress) : pollerConfig.getPackage(packageName);
        if (pkg == null) {
            System.err.printf("Error: Package %s doesn't exist%n", packageName);
            return null;
        }
        System.out.printf("Package: %s%n", pkg.getName());

        final Service svc = pollerConfig.getServiceInPackage(serviceName, pkg);
        if (svc == null) {
            System.err.printf("Error: Service %s not defined on package %s%n", serviceName, packageName);
            return null;
        }

        ServiceMonitor monitor = null;
        if (monitorClass == null) {
            monitor = pollerConfig.getServiceMonitor(serviceName);
            if (monitor == null) {
                System.err.printf("Error: Service %s doesn't have a monitor class defined%n", serviceName);
                return null;
            }
        } else {
            monitor = registry.getMonitorByClassName(monitorClass);
            System.err.printf("Error: No monitor found with class name %s\n", monitorClass);
            if (monitor == null) {
                return null;
            }
        }
        System.out.printf("Monitor: %s%n", monitor.getClass().getName());

        if (pollerConfig.isPolledLocally(ipAddress, serviceName)) {
            for (Parameter p : svc.getParameters()) {
                if (!parameters.containsKey(p.getKey())) {
                    String value = p.getValue();
                    if (value == null) {
                        try {
                            value = JaxbUtils.marshal(p.getAnyObject());
                        } catch (Exception e) {}
                    }
                    parameters.put(p.getKey(), value);
                }
            }
            for (Entry<String,Object> e : parameters.entrySet()) {
                System.out.printf("Parameter %s : %s%n", e.getKey(), e.getValue());
            }
            try {
                PollStatus status = monitor.poll(monSvc, parameters);
                System.out.printf("Available ? %s (status %s[%s])%n", status.isAvailable(), status.getStatusName(), status.getStatusCode());
                if (status.isAvailable()) {
                    System.out.printf("Response time: %s%n", status.getResponseTime());
                } else {
                    if (status.getReason() != null) {
                        System.out.printf("Reason: %s%n", status.getReason());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error: Can't execute the monitor. " + e.getMessage());
                return null;
            }
        } else {
            System.err.printf("Error: Polling is not enabled for service %s using IP %s%n", serviceName, ipAddress);
        }
        return null;
    }
}
