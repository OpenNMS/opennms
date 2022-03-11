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

package org.opennms.features.deviceconfig.sshscripting.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.After;
import org.junit.Before;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.util.test.EchoShellFactory;
import org.junit.Test;
import org.opennms.features.deviceconfig.sshscripting.SshScriptingService;

public class SshScriptingServiceImplIT {

    private static final String USER = "username";

    private static final String PASSWORD = "password";

    private SshServer sshd;

    @Before
    public void prepare() throws IOException {
        setupSSHServer();
    }

    @After
    public void cleanup() throws InterruptedException {
        try {
            sshd.stop(true);
        } catch (Exception e) {
            // do nothing
        }
    }

    private Optional<SshScriptingService.Failure> execute(String password, Map<String, String> vars, String... statements) {
        return execute("localhost", password, vars, statements);
    }

    private Optional<SshScriptingService.Failure> execute(String host, String password, Map<String, String> vars, String... statements) {
        String script = List.of(statements).stream().collect(Collectors.joining("\n"));
        var ss = new SshScriptingServiceImpl();
        return ss.execute(script, USER, password, host, sshd.getPort(), vars, Duration.ofMillis(10000));
    }

    private Optional<SshScriptingService.Failure> execute(String... statements) {
        return execute(PASSWORD, Collections.emptyMap(), statements);
    }

    @Test
    public void login() {
        // 'user' and 'password' are made available as variables by default
        var result = execute(
                PASSWORD,
                Collections.emptyMap()
        );
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void login_failure() {
        // 'user' and 'password' are made available as variables by default
        var result = execute(
                PASSWORD + "x",
                Collections.emptyMap()
        );
        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void echo() {
        var result = execute(
                "send: abc",
                "await: abc",
                "send: uvw",
                "await: uvw"
        );
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void variable_substitution() {
        var vars = new HashMap<String, String>() {{
           put("x", "var");
        }};
        // 'user' and 'password' are made available as variables by default
        var result = execute(
                PASSWORD,
                vars,
                "send: ${user} ${password} ${x}",
                "await: " + USER + " " + PASSWORD + " ${x}"
        );
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void await_nonexistent() {
        var ss = new SshScriptingServiceImpl();
        String script =
                List.of(
                        "send: abc",
                        "send: uvw",
                        "await: 123"
                ).stream().collect(Collectors.joining("\n"));

        var result = ss.execute(script, USER, PASSWORD, "localhost", sshd.getPort(), Collections.emptyMap(), Duration.ofMillis(4000));
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().stdout.isPresent(), is(true));
        assertThat(result.get().stdout.get(), is("abc\nuvw\n"));
    }

    @Test
    public void await_something_in_between() {
        var ss = new SshScriptingServiceImpl();
        String script =
                List.of(
                        "send: abc",
                        "send: uvw",
                        "send: 123",
                        "await: uvw"
                ).stream().collect(Collectors.joining("\n"));

        var result = ss.execute(script, USER, PASSWORD, "localhost", sshd.getPort(), Collections.emptyMap(), Duration.ofMillis(4000));
        assertThat(result.isEmpty(), is(true));
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

    @Test
    public void testIpAddresses() throws Exception {
        testIpAddress("::1", "0000:0000:0000:0000:0000:0000:0000:0001");
        testIpAddress("localhost", "127.0.0.1");

        final String anIPv4Address = "192.168.31.1";
        testIpAddress("localhost", anIPv4Address, anIPv4Address, null);

        final String anIPv6Address = "2001:0638:0301:11a0::1";
        testIpAddress("::1", "2001:0638:0301:11a0:0000:0000:0000:0001", null, anIPv6Address);
    }

    public void testIpAddress(final String hostname, final String expectedIp) throws Exception {
        testIpAddress(hostname, expectedIp, null, null);
    }

    public void testIpAddress(final String hostname, final String expectedIp, final String ipv4Address, final String ipv6Address) throws Exception {
        final SshScriptingServiceImpl ss = new SshScriptingServiceImpl();

        ss.setTftpServerIPv4Address(ipv4Address);
        ss.setTftpServerIPv6Address(ipv6Address);

        final String script = List.of(
                            "send: ${tftpServerIp}",
                            "await: "+expectedIp
                        ).stream().collect(Collectors.joining("\n"));

        final Optional<SshScriptingService.Failure> result = ss.execute(script, USER, PASSWORD, hostname, sshd.getPort(), Collections.emptyMap(), Duration.ofMillis(10000));

        assertThat(result.isPresent(), is(false));
    }
}
