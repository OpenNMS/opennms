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
package org.opennms.features.deviceconfig.shell;

import com.google.common.base.Strings;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.features.deviceconfig.service.DeviceConfigService.DeviceConfigBackupResponse;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Command(scope = "opennms", name = "dcb-trigger", description = "Trigger device config backup from a specific Interface")
@Service
public class DcbTriggerCommand implements Action {

    @Reference
    private DeviceConfigService deviceConfigService;

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String location = "Default";

    @Argument(index = 0, name = "host", description = "Hostname or IP Address of the system to poll", required = true, multiValued = false)
    String host;

    @Option(name = "-s", aliases = "--service", description = "Device Config Service", required = false, multiValued = false)
    String service = "DeviceConfig";

    @Option(name = "-p", aliases = "--persist", description = "Whether to persist config or not")
    boolean persist = false;

    @Option(name = "-v", aliases = "--verbose", description = "See script output line-by-line", required = false, multiValued = false)
    boolean verbose = false;

    @Override
    public Object execute() throws Exception {
        try {
            InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.printf("Not a valid host %s \n", host);
            return null;
        }
        CompletableFuture<DeviceConfigBackupResponse> future = deviceConfigService.triggerConfigBackup(host, location, service, persist);
        while (true) {
            try {
                try {
                    var response = future.get(1, TimeUnit.SECONDS);
                    if (Strings.isNullOrEmpty(response.getErrorMessage())) {
                        System.out.printf("\nTriggered config backup for %s at location %s", host, location);
                        if (persist) {
                            System.out.println(" and persisted");
                        }
                        else {
                            System.out.println();
                        }
                    } else {
                        System.err.println("Failed to trigger device config backup: " + response.getErrorMessage());
                    }
                    if (verbose && !Strings.isNullOrEmpty(response.getScriptOutput())) {
                        System.out.printf("---SSH script output---\n%s\n----------end----------\n", response.getScriptOutput());
                    }
                    break;
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                    break;
                }
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
            System.out.flush();
        }

        return null;
    }

}
