/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.ServiceMonitor;

/**
 * The Class MonitorTester.
 * <p>Execute a poller test from the command line using current settings from poller-configuration.xml</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public abstract class MonitorTester {

    private static final String CMD_SYNTAX = "poller-test [options]";

    public static void main(String[] args) {

        Options options = new Options();
        Option oI = new Option("i", "ipaddress", true, "IP Address to test [required]");
        oI.setRequired(true);
        options.addOption(oI);
        Option oS = new Option("s", "service", true, "Service name [required]");
        oS.setRequired(true);
        options.addOption(oS);
        options.addOption("P", "package", true, "Poller Package");
        options.addOption("p", "param", true, "Service parameter ~ key=value");
        options.addOption("c", "class", true, "Monitor Class");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: %s%n", e.getMessage()), options, null);
            System.exit(1);
        }

        final String packageName = cmd.getOptionValue('P');
        final String monitorClass = cmd.getOptionValue('c');
        final String ipAddress = cmd.getOptionValue('i');
        final String serviceName = cmd.getOptionValue('s');

        Map<String,Object> parameters = new HashMap<String,Object>();
        if (cmd.hasOption('p')) {
            for (String parm : cmd.getOptionValues('p')) {
                String[] data = parm.split("=");
                if (data.length == 2 && data[0] != null && data[1] != null) {
                    parameters.put(data[0], data[1]);
                }
            }
        }

        MonitoredService monSvc = new MonitoredService() {
            public String getSvcUrl() {
                return null;
            }
            public String getSvcName() {
                return serviceName;
            }
            public String getIpAddr() {
                return ipAddress;
            }
            public int getNodeId() {
                return 0;
            }
            public String getNodeLabel() {
                return getIpAddr();
            }
            public NetworkInterface<InetAddress> getNetInterface() {
                return new InetNetworkInterface(getAddress());
            }
            public InetAddress getAddress() {
                final InetAddress addr = InetAddressUtils.addr(ipAddress);
                if (addr == null) {
                    throw new IllegalStateException("Error getting localhost address");
                }
                return addr;
            }
        };

        try {
            PollerConfigFactory.init();
        } catch (Exception e) {
            System.err.printf("Error: Can't initialize poller-configuration.xml. %s%n", e.getMessage());
            System.exit(1);
        }
        PollerConfig config = PollerConfigFactory.getInstance();

        System.out.printf("Checking service %s on IP %s%n", serviceName, ipAddress);

        org.opennms.netmgt.config.poller.Package pkg = packageName == null ? config.getFirstLocalPackageMatch(ipAddress) : config.getPackage(packageName);
        if (pkg == null) {
            System.err.printf("Error: Package %s doesn't exist%n", packageName);
            System.exit(1);
        }
        System.out.printf("Package: %s%n", pkg.getName());

        Service svc = config.getServiceInPackage(serviceName, pkg);
        if (svc == null) {
            System.err.printf("Error: Service %s not defined on package %s%n", serviceName, packageName);
            System.exit(1);
        }

        ServiceMonitor monitor = null;
        if (monitorClass == null) {
            monitor = config.getServiceMonitor(serviceName);
            if (monitor == null) {
                System.err.printf("Error: Service %s doesn't have a monitor class defined%n", serviceName);
                System.exit(1);
            }
        } else {
            try {
                final Class<? extends ServiceMonitor> mc = Class.forName(monitorClass).asSubclass(ServiceMonitor.class);
                monitor = mc.newInstance();
            } catch (Exception e) {
                System.err.printf("Error: Can't instantiate %s because %s%n", monitorClass, e.getMessage());
                System.exit(1);
            }
        }
        System.out.printf("Monitor: %s%n", monitor.getClass().getName());

        if (config.isPolledLocally(ipAddress, serviceName)) {
            for (Parameter p : svc.getParameters()) {
                if (!parameters.containsKey(p.getKey())) {
                    parameters.put(p.getKey(), p.getValue() == null ? p.getAnyObject() : p.getValue());
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
                System.exit(1);
            }
        } else {
            System.err.printf("Error: Polling is not enabled for service %s using IP %s%n", serviceName, ipAddress);
        }

        System.exit(0);
    }

}
