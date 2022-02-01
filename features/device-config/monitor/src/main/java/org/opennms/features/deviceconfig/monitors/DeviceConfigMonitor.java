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

package org.opennms.features.deviceconfig.monitors;

import org.opennms.core.spring.BeanUtils;
import org.opennms.features.deviceconfig.sshscripting.SshScriptingService;
import org.opennms.features.deviceconfig.tftp.TftpFileReceiver;
import org.opennms.features.deviceconfig.tftp.TftpServer;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeviceConfigMonitor extends AbstractServiceMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigMonitor.class);
    private TftpServer tftpServer;
    private SshScriptingService sshScriptingService;
    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(1); // 60sec
    private static final long DEFAULT_TTL = 180000; //180sec
    private static final int DEFAULT_SSH_PORT = 22;

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        if (tftpServer == null) {
            tftpServer = BeanUtils.getBean("daoContext", "tftpServer", TftpServer.class);
        }
        if (sshScriptingService == null) {
            sshScriptingService = BeanUtils.getBean("daoContext", "sshScriptingService", SshScriptingService.class);
        }
        TftpReceiver tftpReceiver = new TftpReceiver(svc.getAddress());
        try {
            tftpServer.register(tftpReceiver);
            try {
                String script = getObjectAsStringFromParams(parameters, "script");
                String user = getObjectAsStringFromParams(parameters, "username");
                String password = getObjectAsStringFromParams(parameters, "password");
                Integer port = getKeyedInteger(parameters, "port", DEFAULT_SSH_PORT);
                Long timeout = getKeyedLong(parameters, "timeout", DEFAULT_DURATION.toMillis());
                Duration duration = Duration.ofMillis(timeout);
                Long ttl = getKeyedLong(parameters, "ttl", DEFAULT_TTL);
                Optional<SshScriptingService.Failure> sshResult =
                        sshScriptingService.execute(script, user, password, svc.getIpAddr(), port, new HashMap<>(), duration);
                return sshResult.map(failure -> {
                    var reason = "Config retrieval could not be triggered - message: " + failure.message
                            + "\nstdout: " + failure.stdout
                            + "\nstderr: " + failure.stderr;
                    LOG.error(reason);
                    return PollStatus.unavailable(reason);
                }).orElseGet(() -> {
                    CompletableFuture<byte[]> configObj = tftpReceiver.getConfigFuture();
                    try {
                        byte[] config = configObj.get(ttl, TimeUnit.MILLISECONDS);
                        PollStatus pollStatus = PollStatus.up();
                        pollStatus.setDeviceConfig(config);
                        return pollStatus;
                    } catch (TimeoutException | InterruptedException | ExecutionException e) {
                        LOG.error("Config retrieval failed", e);
                        return PollStatus.unresponsive("Config retrieval failed : " + e.getMessage());
                    }
                });
            } finally {
                tftpServer.deregister(tftpReceiver);
            }
        } catch (IOException e) {
            LOG.error("Exception while connecting to TFTP Server ", e);
            return PollStatus.down("Couldn't connect to Tftp Server" + e.getMessage());
        }
    }

    static private class TftpReceiver implements TftpFileReceiver {

        private final CompletableFuture<byte[]> configFuture = new CompletableFuture<>();
        private final InetAddress serviceAddress;

        public TftpReceiver(InetAddress serviceAddress) {
            this.serviceAddress = serviceAddress;
        }

        @Override
        public void onFileReceived(InetAddress address, String fileName, byte[] content) {
            if (serviceAddress.equals(address)) {
                configFuture.complete(content);
            }
        }

        public CompletableFuture<byte[]> getConfigFuture() {
            return configFuture;
        }
    }

    public void setTftpServer(TftpServer tftpServer) {
        this.tftpServer = tftpServer;
    }

    public void setSshScriptingService(SshScriptingService sshScriptingService) {
        this.sshScriptingService = sshScriptingService;
    }

    private String getObjectAsStringFromParams(Map<String, Object> params, String key) {
        Object obj = params.get(key);
        if (obj instanceof String) {
            return (String) obj;
        }
        throw new IllegalArgumentException(key + " is not an instance of String");
    }

}
