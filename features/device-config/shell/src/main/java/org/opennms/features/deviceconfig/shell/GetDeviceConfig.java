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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.features.deviceconfig.service.DeviceConfigUtil;
import org.opennms.netmgt.poller.DeviceConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

@Command(scope = "opennms", name = "device-config-get", description = "Get device config from a specific Interface")
@Service
public class GetDeviceConfig implements Action {

    @Reference
    private DeviceConfigService deviceConfigService;

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String location = "Default";

    @Argument(index = 0, name = "host", description = "Hostname or IP Address of the system to poll", required = true, multiValued = false)
    String host;

    @Option(name = "-n", aliases = "--node-id", description = "Node Id for Service", required = false, multiValued = false)
    int nodeId;

    @Option(name = "-c", aliases = "--config-type", description = "Device Config Type", required = false, multiValued = false)
    String configType = "default";

    @Option(name = "-t", aliases = "--timeout", description = "Timeout for device config retrieval in msec", required = false, multiValued = false)
    int timeout = 60000;

    @Option(name = "-e", aliases = "--encoding", description = "Encoding format", required = false, multiValued = false)
    String encoding = Charset.defaultCharset().name();


    @Override
    public Object execute() throws Exception {
        try {
            InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.printf("Not a valid host %s \n", host);
            return null;
        }
        CompletableFuture<DeviceConfig> future = deviceConfigService.getDeviceConfig(host, location, configType, timeout);
        while (true) {
            try {
                try {
                    DeviceConfig deviceConfig = future.get(1, TimeUnit.SECONDS);
                    if (deviceConfig != null) {
                        System.out.printf("Received file %s with content .. \n\n", deviceConfig.getFilename());
                        if (deviceConfig.getFilename().contains(".gz")) {
                            // Decompress if this is compressed file
                            byte[] dcBytes = DeviceConfigUtil.decompressGzipToBytes(deviceConfig.getContent());
                            String config =  new String(dcBytes, Charset.forName(encoding));
                            System.out.println(config);
                        }

                    } else {
                        System.out.println("Failed to fetch device config");
                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted.");
                } catch (ExecutionException e) {
                    System.out.println("Failed to fetch device config.");
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

}
