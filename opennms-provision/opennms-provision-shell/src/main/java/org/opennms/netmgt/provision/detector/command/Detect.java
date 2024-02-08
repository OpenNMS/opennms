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

@Command(scope = "opennms", name = "detect", description = "Detect the service on a host at specified location")
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
