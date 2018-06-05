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

package org.opennms.netmgt.provision.detector.command;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;

@Command(scope = "provision", name = "detect", description = "Detect the service on a host at specified location")
@Service
public class Detect implements Action {

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String m_location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String m_systemId;

    @Argument(index = 0, name = "detectorType", description = "Service to detect", required = true, multiValued = false)
    @Completion(ServiceNameCompleter.class)
    String serviceName;

    @Argument(index = 1, name = "host", description = "Hostname or IP Address of the system to detect", required = true, multiValued = false)
    String m_host;

    @Argument(index = 2, name = "attributes", description = "Detector attributes in key=value form", multiValued = true)
    List<String> attributes;

    @Reference
    public LocationAwareDetectorClient locationAwareDetectorClient;

    @Override
    public Object execute() throws UnknownHostException {
        System.out.printf("Trying to detect '%s' on '%s' ", serviceName, m_host);
        final CompletableFuture<Boolean> future = locationAwareDetectorClient.detect()
                .withLocation(m_location)
                .withSystemId(m_systemId)
                .withServiceName(serviceName)
                .withAddress(InetAddress.getByName(m_host))
                .withAttributes(parse(attributes))
                .execute();

        while (true) {
            try {
                try {
                    boolean isDetected = future.get(1, TimeUnit.SECONDS);
                    System.out.printf("\n'%s' %s detected on %s\n",
                            serviceName,
                            isDetected ? "WAS" : "WAS NOT",
                            m_host);
                } catch (InterruptedException e) {
                    System.out.println("\nInterrupted.");
                } catch (ExecutionException e) {
                    System.out.printf("\nDetection failed with: %s\n", e);
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

    private static Map<String, String> parse(List<String> attributeList) {
        Map<String, String> properties = new HashMap<>();
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
