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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPClient;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.util.test.EchoShellFactory;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.deviceconfig.sshscripting.SshScriptingService;
import org.opennms.features.deviceconfig.sshscripting.impl.SshScriptingServiceImpl;
import org.opennms.features.deviceconfig.tftp.impl.TftpServerImpl;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

public class DeviceConfigMonitorIT {

    private static final String USER = "username";

    private static final String PASSWORD = "password";
    private SshServer sshd;
    private final TftpServerImpl tftpServer = new TftpServerImpl();

    @Before
    public void setup() throws IOException {
        setupSSHServer();
    }

    @After
    public void destroy() throws IOException {
        sshd.stop(true);
        tftpServer.close();
    }

    @Test
    public void testDeviceConfigMonitor() throws IOException, InterruptedException {
        MonitoredService svc = Mockito.mock(MonitoredService.class);
        Mockito.when(svc.getIpAddr()).thenReturn("localhost");
        Mockito.when(svc.getAddress()).thenReturn(InetAddress.getLocalHost());
        SshScriptingService sshScriptingService = new SshScriptingServiceImpl();
        int port = 6903;
        tftpServer.setPort(port);
        DeviceConfigMonitor deviceConfigMonitor = new DeviceConfigMonitor();
        deviceConfigMonitor.setSshScriptingService(sshScriptingService);
        deviceConfigMonitor.setTftpServer(tftpServer);
        Map<String, Object> params = new HashMap<>();
        params.put("username", USER);
        params.put("password", PASSWORD);
        params.put("script", "send:config");
        params.put("port", sshd.getPort());
        params.put("timeout", 15000L);
        params.put("ttl", 20000L);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                uploadConfig(port);
            } catch (IOException | InterruptedException e) {
                Assert.fail();
            }
        });
        PollStatus pollStatus = deviceConfigMonitor.poll(svc, params);
        Assert.assertThat(pollStatus.getStatusName(), Matchers.is("Up"));

    }

    private void uploadConfig(int port) throws IOException, InterruptedException {
            sshd.addSessionListener(new SessionListener() {
                @Override
                public void sessionCreated(Session session) {
                    SessionListener.super.sessionCreated(session);
                    TFTPClient client = new TFTPClient();
                    byte[] config = UUID.randomUUID().toString().getBytes(StandardCharsets.US_ASCII);
                    var fileName = "test";
                    try {
                        client.open();
                        client.sendFile(fileName, TFTP.BINARY_MODE, new ByteArrayInputStream(config), InetAddress.getLocalHost(), port);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        client.close();
                    }
                }
            });
    }

    private void setupSSHServer() throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setShellFactory(new EchoShellFactory());
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setPasswordAuthenticator(
                (user, password, session) -> StringUtils.equals(user, USER) && StringUtils.equals(password, PASSWORD)
        );
        sshd.start();
    }
}
