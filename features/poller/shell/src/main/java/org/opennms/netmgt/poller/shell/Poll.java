/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;

@Command(scope = "opennms-poller", name = "poll", description = "Used to invoke a monitor against a host at a specific location, or to test a service monitor definition from a given poller package.")
@Service
public class Poll implements Action {

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String location = "Default";

    @Option(name = "-S", aliases = "--service", description = "Service name", required = false, multiValued = false)
    String serviceName;

    @Option(name = "-P", aliases = "--package", description = "Poller Package", required = false, multiValued = false)
    String packageName;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String systemId;

    @Option(name = "-t", aliases = "--ttl", description = "Time to live", required = false, multiValued = false)
    Long ttlInMs;

    @Option(name = "-n", aliases = "--node-id", description = "Node Id for Service", required = false, multiValued = false)
    int nodeId;

    @Option(name = "-c", aliases = "--class", description = "Monitor Class", required = false, multiValued = false)
    @Completion(MonitorClassNameCompleter.class)
    String className;

    @Argument(index = 0, name = "host", description = "Hostname or IP Address of the system to poll", required = true, multiValued = false)
    String host;

    @Argument(index = 1, name = "attributes", description = "Monitor specific attributes in key=value form", multiValued = true)
    List<String> attributes;

    @Reference
    public LocationAwarePollerClient locationAwarePollerClient;

    @Reference
    public NodeDao nodeDao;

    @Reference
    public IpInterfaceDao ipInterfaceDao;

    @Reference
    public SessionUtils sessionUtils;

    @Override
    public Object execute() throws Exception {
        final InetAddress ipAddress = InetAddress.getByName(host);
        final MonitoredService service;

        if (className == null) {
            if (serviceName == null) {
                System.err.println("Please specify at least the monitor class (--class) or the package/service (--package/--service)\n");
                return null;
            }
            className = retrieveClassName(ipAddress, packageName, serviceName);

            if (className == null) {
                // still null, we already output an error message
                return null;
            }
        }

        service = sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsNode node;

            if (nodeId > 0) {
                node = nodeDao.get(nodeId);

                if (node == null) {
                    System.err.printf("Error: Can't find node with Id %d the database%n", nodeId);
                    return null;
                }
            } else {
                final List<OnmsIpInterface> ips = ipInterfaceDao.findByIpAddress(InetAddressUtils.str(ipAddress)).stream()
                        .filter(i -> location.equals(i.getNode().getLocation().getLocationName()))
                        .collect(Collectors.toList());

                if (ips.size() == 0) {
                    System.err.printf("Error: Can't find the IP address %s on the database%n", InetAddressUtils.str(ipAddress));
                    return null;
                }
                if (ips.size() > 1) {
                    System.out.printf("Warning: there are several IP interface objects associated with the IP address %s (picking the first one)%n", InetAddressUtils.str(ipAddress));
                }
                node = ips.get(0).getNode();
            }

            return new SimpleMonitoredService(ipAddress, node.getId(), node.getLabel(), serviceName == null ? "SVC" : serviceName, location);
        });

        final Map<String, Object> parameters = retrieveParameters(ipAddress, packageName, serviceName);
        parameters.putAll(parse(attributes));

        final CompletableFuture<PollerResponse> future = locationAwarePollerClient.poll()
                .withService(service)
                .withSystemId(systemId)
                .withMonitorClassName(className)
                .withTimeToLive(ttlInMs)
                .withAttributes(parameters)
                .execute();

        if (packageName != null) {
            System.out.printf("Package: %s%n", packageName);
        }
        if (serviceName != null) {
            System.out.printf("Service: %s%n", serviceName);
        }
        if (className != null) {
            System.out.printf("Monitor: %s%n", className);
        }
        for (Map.Entry<String,Object> e : parameters.entrySet()) {
            System.out.printf("Parameter %s: %s%n", e.getKey(), e.getValue());
        }

        while (true) {
            try {
                try {
                    PollStatus pollStatus = future.get(1, TimeUnit.SECONDS).getPollStatus();

                    if (pollStatus.getStatusCode() == PollStatus.SERVICE_AVAILABLE) {
                        System.out.printf("\nService is %s on %s using %s:\n", pollStatus.getStatusName(), host, className);
                        final Map<String, Number> properties = pollStatus.getProperties();
                        if (properties.size() > 0) {
                            properties.entrySet().stream().forEach(e -> {
                                System.out.printf("\t%s: %.4f\n", e.getKey(),
                                        e.getValue() != null ? e.getValue().doubleValue() : null);
                            });
                        } else {
                            System.out.printf("(No properties were returned by the monitor.\n");
                        }
                    } else {
                        System.out.printf("\nService is %s on %s using %s\n", pollStatus.getStatusName(), host, className);
                        System.out.printf("\tReason: %s\n", pollStatus.getReason());
                    }
                } catch (InterruptedException e) {
                    System.out.println("\nInterrupted.");
                } catch (ExecutionException e) {
                    System.out.printf("\nPoll failed with: %s\n", e);
                }
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
            System.out.flush();
        }
        return null;
    }

    private String retrieveClassName(final InetAddress ipAddress, final String packageName, final String serviceName) throws Exception {
        final PollerConfig pollerConfig = ReadOnlyPollerConfigManager.create();

        final org.opennms.netmgt.config.poller.Package pkg = packageName == null ? pollerConfig.getFirstLocalPackageMatch(InetAddressUtils.str(ipAddress)) : pollerConfig.getPackage(packageName);
        if (pkg == null) {
            System.err.printf("Error: Package %s doesn't exist%n", packageName);
            return null;
        }

        final org.opennms.netmgt.config.poller.Service svc = pollerConfig.getServiceInPackage(serviceName, pkg);
        if (svc == null) {
            System.err.printf("Error: Service %s not defined on package %s%n", serviceName, packageName);
            return null;
        }

        final Optional<Package.ServiceMatch> service = pkg.findService(serviceName);
        if (!service.isPresent()) {
            System.err.printf("Error: Service %s not defined%n", serviceName);
            return null;
        }

        final ServiceMonitor monitor = pollerConfig.getServiceMonitor(service.get().service.getName());
        if (monitor == null) {
            System.err.printf("Error: Service %s doesn't have a monitor class defined%n", serviceName);
            return null;
        }

        return monitor.getClass().getName();
    }

    private Map<String, Object> retrieveParameters(final InetAddress ipAddress, final String packageName, final String serviceName) throws Exception {
        final Map<String, Object> parameters = new TreeMap<>();

        if (serviceName == null) {
            return parameters;
        }

        final PollerConfig pollerConfig = ReadOnlyPollerConfigManager.create();

        final org.opennms.netmgt.config.poller.Package pkg = packageName == null ? pollerConfig.getFirstLocalPackageMatch(InetAddressUtils.str(ipAddress)) : pollerConfig.getPackage(packageName);
        if (pkg == null) {
            System.err.printf("Error: Package %s doesn't exist%n", packageName);
            return parameters;
        }

        final org.opennms.netmgt.config.poller.Service svc = pollerConfig.getServiceInPackage(serviceName, pkg);
        if (svc == null) {
            System.err.printf("Error: Service %s not defined on package %s%n", serviceName, packageName);
            return parameters;
        }

        final Optional<Package.ServiceMatch> service = pkg.findService(serviceName);
        if (!service.isPresent()) {
            System.err.printf("Error: Service %s not defined%n", serviceName);
            return parameters;
        }

        for(final Parameter parameter : svc.getParameters()) {
            String value = parameter.getValue();

            if (value == null) {
                try {
                    value = JaxbUtils.marshal(parameter.getAnyObject());
                } catch (Exception e) {}
            }

            parameters.put(parameter.getKey(), value);
        }

        return parameters;
    }

    protected static Map<String, Object> parse(List<String> attributeList) {
        final Map<String, Object> properties = new HashMap<>();
        if (attributeList != null) {
            for (String keyValue : attributeList) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid property " + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1, keyValue.length());
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }

}
