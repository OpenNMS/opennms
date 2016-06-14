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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.minion.provisiond.shell;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;

@Command(scope = "minion", name = "detect", description = "Manually invoke and test provisiond detectors.",
    detailedDescription="Example usage:\n"
            + "\t minion:detect LOOP 127.0.0.1 ipMatch=127.0.*.*\n"
            + "\t minion:detect HTTP 127.0.0.1 port=8000\n")
@Service
public class MinionDetector implements Action {

    @Argument(index = 0, name = "service", description = "Service to detect", required = true, multiValued = false, valueToShowInHelp = "icmp")
    @Completion(ServiceNameCompleter.class)
    String serviceName = null;

    @Argument(index = 1, name = "address", description = "The IP address against which the detector will run", required = true, multiValued = false)
    String address = null;

    @Argument(index = 2, name = "attributes", description = "Detector attributes in key=value form", multiValued = true)
    List<String> attributes;

    @Reference
    ServiceDetectorRegistry serviceDetectorRegistry;

    @Override
    public Object execute() throws Exception {
        InetAddress ipAddress = InetAddress.getByName(address);
        boolean isServiceDetected = false;
        Map<String, String> properties = parse(attributes);

        ServiceDetector detector = serviceDetectorRegistry.getDetectorByServiceName(serviceName, properties);
        if (detector == null) {
            System.out.println("No detector found with service name: " + serviceName);
            return null;
        }

        System.out.printf("Trying to detect the '%s' service on %s ...\n", serviceName, address);
        detector.init();
        if (detector instanceof SyncServiceDetector) {
            SyncServiceDetector syncDetector = (SyncServiceDetector)detector;
            isServiceDetected = syncDetector.isServiceDetected(ipAddress);
        } else if (detector instanceof AsyncServiceDetector) {
            AsyncServiceDetector asyncDetector = (AsyncServiceDetector)detector;
            DetectFuture future = asyncDetector.isServiceDetected(ipAddress);
            future.awaitFor();
            isServiceDetected = future.isServiceDetected();
        }
        detector.dispose();

        System.out.printf("The '%s' service %s detected on %s\n", serviceName,
                isServiceDetected ? "WAS" : "WAS NOT", address);
        return null;
    }

    private Map<String, String> parse(List<String> attributeList) {
        Map<String, String> properties = new HashMap<>();
        if (attributeList != null) {
            for (String keyValue : attributeList) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid property "
                            + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1,
                                                      keyValue.length());
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }
}
