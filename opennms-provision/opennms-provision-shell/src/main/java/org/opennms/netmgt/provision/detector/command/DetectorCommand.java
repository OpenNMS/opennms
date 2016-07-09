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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.provision.detector.common.DetectorResponseDTO;
import org.opennms.netmgt.provision.detector.common.LocationAwareDetectorClient;

@Command(scope = "provision", name = "detect", description = "Detect the service on a host at specified location")
public class DetectorCommand extends OsgiCommandSupport {

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String m_location;

    @Argument(index = 0, name = "detectorType", description = "Service to detect", required = true, multiValued = false)
    String serviceName;

    @Argument(index = 1, name = "host", description = "Hostname or IP Address of the system to detect", required = true, multiValued = false)
    String m_host;

    @Argument(index = 2, name = "attributes", description = "Detector attributes in key=value form", multiValued = true)
    List<String> attributes;

    private LocationAwareDetectorClient locationAwareDetectorClient;

    @Override
    protected Object doExecute() throws Exception {
        System.out.printf("Trying to detect '%s' service  on  '%s' ", serviceName, m_host);
        Map<String, String> properties = parse(attributes);
        final CompletableFuture<DetectorResponseDTO> future = locationAwareDetectorClient.detect().atLocation(m_location).atAddress(m_host).withAttributes(properties).byService(serviceName).execute();
        while (true) {
            try {
                DetectorResponseDTO detectorResponse = future.get(1,
                                                                  TimeUnit.SECONDS);
                System.out.printf("\nThe '%s' service %s detected on %s\n",
                                  serviceName,
                                  detectorResponse.isDetected() ? "WAS"
                                                                : "WAS NOT",
                                  m_host);
                if (StringUtils.isNotBlank(detectorResponse.getFailureMesage())) {
                    System.out.printf("with error response %s",
                                      detectorResponse.getFailureMesage());
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

    public void setLocationAwareDetectorClient(LocationAwareDetectorClient locationAwareDetectorClient) {
        this.locationAwareDetectorClient = locationAwareDetectorClient;
    }

    private Map<String, String> parse(List<String> attributeList) {
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
