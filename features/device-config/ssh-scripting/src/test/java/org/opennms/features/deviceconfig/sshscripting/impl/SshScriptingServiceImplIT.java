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
package org.opennms.features.deviceconfig.sshscripting.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.auth.pubkey.AuthorizedKeyEntriesPublickeyAuthenticator;
import org.apache.sshd.util.test.EchoShellFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.deviceconfig.sshscripting.SshScriptingService;

public class SshScriptingServiceImplIT {

    private static final String USER = "username";

    private static final String PASSWORD = "password";

    private SshServer sshd;

    private KeyPair hostKey;

    private static final String AUTH_KEY_PRIV = String.join("\n",
                                                            "-----BEGIN OPENSSH PRIVATE KEY-----",
                                                            "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAaAAAABNlY2RzYS",
                                                            "1zaGEyLW5pc3RwMjU2AAAACG5pc3RwMjU2AAAAQQR5vNT3wu+vPGJEixlWNpvJtJP43PIF",
                                                            "eNQNkb7W3+0cltYLG5q2B+ITra5fh311/zbnrWVS9FeIVERyq5x42EH8AAAAqN++TUzfvk",
                                                            "1MAAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBHm81PfC7688YkSL",
                                                            "GVY2m8m0k/jc8gV41A2Rvtbf7RyW1gsbmrYH4hOtrl+HfXX/NuetZVL0V4hURHKrnHjYQf",
                                                            "wAAAAhAOSF7jZV6pNDA4hMbxgj24CDbpNcJDS3sZqKsI7NXl+rAAAADGZvb2tlckBpZy0x",
                                                            "MQECAw==",
                                                            "-----END OPENSSH PRIVATE KEY-----");

    private static final String AUTH_KEY_PUB = "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBHm81PfC7688YkSLGVY2m8m0k/jc8gV41A2Rvtbf7RyW1gsbmrYH4hOtrl+HfXX/NuetZVL0V4hURHKrnHjYQfw=";

    private static final String AUTH_KEY_PRIV_ED25519 = String.join("\n",
                                                                    "-----BEGIN OPENSSH PRIVATE KEY-----",
                                                                    "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW",
                                                                    "QyNTUxOQAAACAsQ8BJjNnDpcq+qYor3n6firUk66dgXik/SLfOk75awgAAAJjYQcVg2EHF",
                                                                    "YAAAAAtzc2gtZWQyNTUxOQAAACAsQ8BJjNnDpcq+qYor3n6firUk66dgXik/SLfOk75awg",
                                                                    "AAAECOQdPPKj8Viohef2S+IbB8W/ZW6MpdRtAQfZqJgcMy7SxDwEmM2cOlyr6piivefp+K",
                                                                    "tSTrp2BeKT9It86TvlrCAAAADmZkMTAwNUBvcGVubm1zAQIDBAUGBw==",
                                                                    "-----END OPENSSH PRIVATE KEY-----");
    

    private static final String AUTH_KEY_PUB_ED25519 = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAICxDwEmM2cOlyr6piivefp+KtSTrp2BeKT9It86TvlrC foo@bar";
    
    @Before
    public void prepare() throws Exception {
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

    private SshScriptingService.Result execute(String password, Map<String, String> vars, String... statements) {
        return execute("localhost", password, vars, statements);
    }

    private SshScriptingService.Result execute(String host, String password, Map<String, String> vars, String... statements) {
        String script = List.of(statements).stream().collect(Collectors.joining("\n"));
        var ss = new SshScriptingServiceImpl();
        return ss.execute(script, USER, password, null, new InetSocketAddress(host, sshd.getPort()), null, null, vars, Duration.ofMillis(10000));
    }

    private SshScriptingService.Result execute(String... statements) {
        return execute(PASSWORD, Collections.emptyMap(), statements);
    }

    @Test
    public void login() {
        // 'user' and 'password' are made available as variables by default
        var result = execute(
                PASSWORD,
                Collections.emptyMap()
        );
        assertThat(result.isSuccess(), is(true));
    }

    @Test
    public void login_failure() {
        // 'user' and 'password' are made available as variables by default
        var result = execute(
                PASSWORD + "x",
                Collections.emptyMap()
        );
        assertThat(result.isFailed(), is(true));
    }

    @Test
    public void echo() {
        var result = execute(
                "send: abc",
                "await: abc",
                "send: uvw",
                "await: uvw"
        );
        assertThat(result.isSuccess(), is(true));
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
        assertThat(result.isSuccess(), is(true));
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

        var result = ss.execute(script, USER, PASSWORD, null, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(4000));
        assertThat(result.isFailed(), is(true));
        assertThat(result.stdout.isPresent(), is(true));
        assertThat(result.stdout.get(), is("abc\nuvw\n"));
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

        var result = ss.execute(script, USER, PASSWORD, null, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(4000));
        assertThat(result.isSuccess(), is(true));
    }

    private void setupSSHServer() throws Exception {
        this.hostKey = SecurityUtils.getKeyPairGenerator(KeyUtils.RSA_ALGORITHM).generateKeyPair();

        sshd = SshServer.setUpDefaultServer();
        sshd.setShellFactory(new EchoShellFactory());
        sshd.setKeyPairProvider(KeyPairProvider.wrap(this.hostKey));
        sshd.setPasswordAuthenticator(
                (user, password, session) -> StringUtils.equals(user, USER) && StringUtils.equals(password, PASSWORD)
        );
        sshd.setPublickeyAuthenticator(new AuthorizedKeyEntriesPublickeyAuthenticator(null,
                                                                                      null,
                                                                                      List.of(AuthorizedKeyEntry.parseAuthorizedKeyEntry(AUTH_KEY_PUB), AuthorizedKeyEntry.parseAuthorizedKeyEntry(AUTH_KEY_PUB_ED25519)),
                                                                                      null));
        sshd.start();
    }

    public void testIpAddresses() throws Exception {
        checkIpAddress("::1", "0000:0000:0000:0000:0000:0000:0000:0001");
        checkIpAddress("localhost", "127.0.0.1");

        final String anIPv4Address = "192.168.31.1";
        checkIpAddress("localhost", anIPv4Address, anIPv4Address, null);

        final String anIPv6Address = "2001:0638:0301:11a0::1";
        checkIpAddress("::1", "2001:0638:0301:11a0:0000:0000:0000:0001", null, anIPv6Address);
    }

    public void checkIpAddress(final String hostname, final String expectedIp) throws Exception {
        checkIpAddress(hostname, expectedIp, null, null);
    }

    public void checkIpAddress(final String hostname, final String expectedIp, final String ipv4Address, final String ipv6Address) throws Exception {
        final SshScriptingServiceImpl ss = new SshScriptingServiceImpl();

        ss.setTftpServerIPv4Address(ipv4Address);
        ss.setTftpServerIPv6Address(ipv6Address);

        final String script = List.of(
                            "send: ${tftpServerIp}",
                            "await: "+expectedIp
                        ).stream().collect(Collectors.joining("\n"));

        final SshScriptingService.Result result = ss.execute(script, USER, PASSWORD, null, new InetSocketAddress(hostname, sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));

        assertThat(result.isSuccess(), is(true));
    }

    public void checkDevice(final String filename, final String username, final String password, final String hostname, final String filenameSuffix, final String tftpServer) throws IOException {
        final SshScriptingServiceImpl ss = new SshScriptingServiceImpl();
        ss.setTftpServerIPv4Address(tftpServer);

        byte[] encoded = Files.readAllBytes(Paths.get("../../../opennms-base-assembly/src/main/filtered/etc/examples/device-config/" + filename));
        final String script = new String(encoded, StandardCharsets.UTF_8).replace("${filenameSuffix}", filenameSuffix);
        final SshScriptingService.Result result = ss.execute(script, username, password, null, new InetSocketAddress(hostname, 22), null, null, Collections.emptyMap(), Duration.ofMillis(20000));

        if (result.stdout.isPresent()) {
            System.out.println("StdOut: "+result.stdout.get());
        }
        if (result.stderr.isPresent()) {
            System.out.println("StdErr: "+result.stderr.get());
        }
        System.out.println("Message: "+result.message);

        assertThat(result.isSuccess(), is(true));
    }

    /**
     * Method for local testing dcb example scripts using real hardware. Of course, ignored by default.
     */
    @Test
    @Ignore("enable to test DCB")
    public void testDevices() throws Exception {
        final String tftpServer = "10.174.24.55";

        // tested with Aruba 6100 switch
        checkDevice("aruba-cx-cli.dcb", "dcb", "DCBpass!", "10.174.24.41", "001", tftpServer);
        checkDevice("aruba-cx-json.dcb", "dcb", "DCBpass!", "10.174.24.41", "002", tftpServer);

        // tested with Aruba 2450 switch
        checkDevice("aruba-os-config.dcb", "dcb", "DCBpass!", "10.174.24.42", "003", tftpServer);

        // tested with Cisco 2960 switch
        checkDevice("cisco-ios-running.dcb", "dcb", "DCBpass!", "10.174.24.43", "004", tftpServer);
        checkDevice("cisco-ios-startup.dcb", "dcb", "DCBpass!", "10.174.24.43", "005", tftpServer);

        // tested with Juniper SRX-1500 firewall
        checkDevice("juniper-junos-config-gz.dcb", "dcb", "DCBpass!", "10.174.24.44", "006", tftpServer);
        checkDevice("juniper-junos-config-txt.dcb", "dcb", "DCBpass!", "10.174.24.44", "007", tftpServer);
        checkDevice("juniper-junos-config-set.dcb", "dcb", "DCBpass!", "10.174.24.44", "008", tftpServer);

        // tested with Palo Alto virtual firewall (PA-VM-ESX-10.0.4)
        checkDevice("paloalto-panos-config.dcb", "dcb", "DCBpass!", "10.174.24.45", "009", tftpServer);
    }

    @Test
    public void testHostKey() throws Exception {
        final SshScriptingServiceImpl ss = new SshScriptingServiceImpl();

        var result1 = ss.execute("send:foo\nawait:foo", USER, PASSWORD, null, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result1.isSuccess(), is(true));

        var result2 = ss.execute("send:foo\nawait:foo", USER, PASSWORD, null, new InetSocketAddress("localhost", sshd.getPort()), KeyUtils.getFingerPrint(this.hostKey.getPublic()), null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result2.isSuccess(), is(true));

        var result3 = ss.execute("send:foo\nawait:foo", USER, PASSWORD, null, new InetSocketAddress("localhost", sshd.getPort()), "invalid", null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result3.isSuccess(), is(false));
    }

    @Test
    public void testAuthKey() throws Exception {
        final SshScriptingServiceImpl ss = new SshScriptingServiceImpl();

        var result1 = ss.execute("send:foo\nawait:foo", USER, PASSWORD, AUTH_KEY_PRIV, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result1.isSuccess(), is(true));

        var result2 = ss.execute("send:foo\nawait:foo", USER, null, AUTH_KEY_PRIV, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result2.isSuccess(), is(true));

        var result3 = ss.execute("send:foo\nawait:foo", USER, "wrong", AUTH_KEY_PRIV, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result3.isSuccess(), is(true));

        var result4 = ss.execute("send:foo\nawait:foo", USER, PASSWORD, "invalid", new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result4.isSuccess(), is(true));

        var result5 = ss.execute("send:foo\nawait:foo", USER, "wrong", "invalid", new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result5.isSuccess(), is(false));
    }

    @Test
    public void testAuthKey_ed25519() throws Exception {
        final SshScriptingServiceImpl ss = new SshScriptingServiceImpl();

        var result1 = ss.execute("send:foo\nawait:foo", USER, PASSWORD, AUTH_KEY_PRIV_ED25519, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result1.isSuccess(), is(true));

        var result2 = ss.execute("send:foo\nawait:foo", USER, null, AUTH_KEY_PRIV_ED25519, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result2.isSuccess(), is(true));

        var result3 = ss.execute("send:foo\nawait:foo", USER, "wrong", AUTH_KEY_PRIV_ED25519, new InetSocketAddress("localhost", sshd.getPort()), null, null, Collections.emptyMap(), Duration.ofMillis(10000));
        assertThat(result3.isSuccess(), is(true));
    }
}
