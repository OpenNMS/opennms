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
package org.opennms.netmgt.telemetry.shell;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.telemetry.api.receiver.Connector;

@Command(scope = "opennms", name = "telemetry-connector", description = "Lists configured telemetry connectors")
@Service
public class ConnectorCommands implements Action {

    @Reference
    private Connector connectorService;

    @Argument(
            index = 0,
            name = "nodeId",
            description = "Numeric node ID",
            required = true,
            multiValued = false
    )
    int nodeId;

    @Argument(
            index = 1,
            name = "host",
            description = "Hostname or IP address",
            required = true,
            multiValued = false
    )
    String host;

    @Argument(
            index = 2,
            name = "port",
            description = "Port number (1–65535)",
            required = true,
            multiValued = false
    )
    String portStr;

    @Argument(
            index = 3,
            name = "mode",
            description = "Mode of operation",
            required = true,
            multiValued = false
    )
    String mode;

    @Argument(
            index = 4,
            name = "paths",
            description = "Comma-separated list of paths",
            required = true,
            multiValued = false
    )

    String paths;

    @Override
    public Object execute() throws Exception {

        InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.err.printf("Invalid host: %s%n", host);
            return null;
        }


        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.printf("Invalid port: %s (must be 1–65535)%n", portStr);
            return null;
        }

        List<String> pathList = Arrays.stream(paths.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (pathList.isEmpty()) {
            System.err.println("At least one path must be provided.");
            return null;
        }


        List<Map<String, String>> paramList = new ArrayList<>();
        for (String p : pathList) {
            Map<String, String> entry = new HashMap<>();
            entry.put("hostname", host);
            entry.put("port", String.valueOf(port));
            entry.put("mode", mode);
            entry.put("path", p);
            paramList.add(entry);
        }


        try {
            connectorService.stream(nodeId, ipAddress, paramList);
            System.out.printf(
                    "Streaming started: nodeId=%d, host=%s, port=%d, mode=%s, paths=%s%n",
                    nodeId, host, port, mode, pathList
            );
        } catch (Exception e) {
            System.err.println("Failed to start stream: " + e.getMessage());
        }

        return null;
    }
}