/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
                    } else {
                        System.err.println("Failed to trigger device config backup: " + response.getErrorMessage());
                    }
                    if (verbose && !Strings.isNullOrEmpty(response.getScriptOutput())) {
                        System.out.println(String.format("---SSH script output---\n%s\n----------end----------", response.getScriptOutput()));
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
