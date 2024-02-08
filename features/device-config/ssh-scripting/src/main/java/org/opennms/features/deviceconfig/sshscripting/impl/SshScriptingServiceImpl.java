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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.sshd.client.ClientBuilder;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.kex.BuiltinDHFactories;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.sshscripting.SshScriptingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class SshScriptingServiceImpl implements SshScriptingService {

    private static Logger LOG = LoggerFactory.getLogger(SshScriptingServiceImpl.class);

    public static final String SCRIPT_VAR_TFTP_SERVER_IP = "tftpServerIp";

    private InetAddress tftpServerIPv4Address;
    private InetAddress tftpServerIPv6Address;

    boolean disableIOCollection;

    private String scriptDebugOutput;

    public void setTftpServerIPv4Address(final String tftpServerIPv4Address) throws UnknownHostException {
        if (!Strings.isNullOrEmpty(tftpServerIPv4Address)) {
            this.tftpServerIPv4Address = InetAddress.getByName(tftpServerIPv4Address);
        } else {
            this.tftpServerIPv4Address = null;
        }
    }

    public void setTftpServerIPv6Address(final String tftpServerIPv6Address) throws UnknownHostException {
        if (!Strings.isNullOrEmpty(tftpServerIPv6Address)) {
            this.tftpServerIPv6Address = InetAddress.getByName(tftpServerIPv6Address);
        } else {
            this.tftpServerIPv6Address = null;
        }
    }

    public void setDisableIOCollection(final String value) {
        this.disableIOCollection = Boolean.parseBoolean(value);
    }

    @Override
    public Result execute(
            String script,
            String user,
            String password,
            final String authKey,
            final SocketAddress target,
            final String hostKeyFingerprint,
            final String shell,
            Map<String, String> vars,
            Duration timeout
    ) {
        return Statement.parseScript(script).fold(
                errorLines -> Result.failure(errorLines.stream().collect(Collectors.joining("\n", "unrecognized statements:\n", ""))),
                statements -> {
                    try {
                        try (var sshInteraction = new SshInteractionImpl(user, password, authKey, target, hostKeyFingerprint, shell, vars, timeout, tftpServerIPv4Address, tftpServerIPv6Address, disableIOCollection)) {
                            LOG.debug("ssh connection successful, executing script: {}", script);
                            Statement prevStatement = null;
                            for (var statement : statements) {
                                try {
                                    LOG.debug("ssh scripting service executing - {}", sshInteraction.replaceVars(statement.toString()));
                                    statement.execute(sshInteraction);
                                } catch (Exception e) {
                                    // Get useful debugging info
                                    String errorDescription = getErrorDescription(e, sshInteraction, prevStatement, statement);

                                    var stdout = sshInteraction.stdout.toString(StandardCharsets.UTF_8);
                                    var stderr = sshInteraction.stderr.toString(StandardCharsets.UTF_8);
                                    var debugOutput = sshInteraction.getDebugOutput();
                                    LOG.error("ssh scripting exception - {} \n### script ###\n {} \n### stdout ###\n {} \n### stderr ###\n {}", errorDescription, script, stdout, stderr, e);
                                    return Result.failure("ssh scripting exception - " + errorDescription, stdout, stderr, debugOutput);
                                }
                                scriptDebugOutput = sshInteraction.getDebugOutput();
                                prevStatement = statement;
                            }
                            return Result.success("Script execution succeeded",  sshInteraction.stdout.toString(StandardCharsets.UTF_8),  sshInteraction.stderr.toString(StandardCharsets.UTF_8), sshInteraction.getDebugOutput());
                        }
                    } catch (Exception e) {
                        LOG.error("error with ssh interactions", e);
                        return Result.failure(e.getMessage());
                    }
                }
        );
    }

    /**
     *  Get a verbose description of the error and current state of script execution
     */
    private String getErrorDescription(Throwable t, SshInteraction sshInteraction, Statement prevStatement, Statement currentStatement) {

        if (currentStatement == null) {
            return t.getMessage();
        }

        String statementWithVars = sshInteraction.replaceVars(currentStatement.toString());
        if (prevStatement == null) {
            return t.getMessage() + " - encountered during initial " + statementWithVars + "\"";
        }

        String prevStatementWithVars = sshInteraction.replaceVars(prevStatement.toString());
        if (currentStatement.statementType == Statement.StatementType.await) {
            return t.getMessage() + " - encountered while waiting for \"" + statementWithVars + "\" following execution of \"" + prevStatementWithVars + "\"";
        } else {
            return t.getMessage() + " - encountered when sending input \"" + statementWithVars + "\" following successful \"" + prevStatementWithVars + "\"";
        }
    }

    @Override
    public String getScriptOutput() {
        return scriptDebugOutput;
    }

    private static class SshInteractionImpl implements SshInteraction, AutoCloseable {

        private final SshClient sshClient;
        private final ClientSession session;
        private final ClientChannel channel;

        // stdout and stderr capture the complete output of the interaction
        private final ByteArrayOutputStream stdout, stderr;
        private final ByteArrayOutputStream debugStdout, debugStderr;
        // awaitStdout receives the same bytes as stdout but is used to process await statements
        // -> successfully awaited parts are dropped
        // -> these parts are no more considered by following awaits
        private final ByteArrayOutputStream awaitStdout;
        // pipeToStdIn is an output stream that is used to feed bytes into stdin of the interaction
        private final PipedOutputStream pipeToStdin;

        private final ByteArrayOutputStream debugOutput;
        private final Map<String, String> vars = new HashMap<>();

        private final Duration timeout;
        private final Instant timeoutInstant;

        private final boolean disableIOCollection;

        private SshInteractionImpl(
                String user,
                String password,
                final String authKey,
                final SocketAddress target,
                final String hostKeyFingerprint,
                final String shell,
                Map<String, String> vars,
                Duration timeout,
                InetAddress tftpServerIPv4Address,
                InetAddress tftpServerIPv6Address,
                boolean disableIOCollection
        ) throws Exception {
            this.disableIOCollection = disableIOCollection;
            timeoutInstant = Instant.now().plus(timeout);
            sshClient = SshClient.setUpDefaultClient();

            sshClient.setServerKeyVerifier((clientSession, socketAddress, publicKey) -> {
                if (Strings.isNullOrEmpty(hostKeyFingerprint)) {
                    // If there is no host key specified, we accept all host keys as a graceful default.
                    // Opt-in on security is not optimal but convenient.
                    return true;
                }

                if (!socketAddress.equals(target)) {
                    // Some kind of proxy is here
                    return false;
                }

                return  KeyUtils.checkFingerPrint(hostKeyFingerprint, publicKey).getKey();
            });

            // We allow also older algorithms here, because Cisco and Aruba devices seem to be pretty picky.
            sshClient.setKeyExchangeFactories(NamedFactory.setUpTransformedFactories(
                    false,
                    BuiltinDHFactories.VALUES,
                    ClientBuilder.DH2KEX
            ));

            sshClient.setSignatureFactories(new ArrayList<>(BuiltinSignatures.VALUES));

            sshClient.start();
            try {
                session = sshClient
                        .connect(user, target)
                        .verify(Duration.between(Instant.now(), timeoutInstant))
                        .getSession();

                // we use the remote address to check whether we have to use the IPv4 or IPv6 property
                final InetAddress remoteAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress();

                final InetAddress localAddress;

                if (remoteAddress instanceof Inet4Address) {
                    if (tftpServerIPv4Address != null) {
                        localAddress = tftpServerIPv4Address;
                    } else {
                        localAddress = ((InetSocketAddress) session.getLocalAddress()).getAddress();
                    }
                } else {
                    if (tftpServerIPv6Address != null) {
                        localAddress = tftpServerIPv6Address;
                    } else {
                        localAddress = ((InetSocketAddress) session.getLocalAddress()).getAddress();
                    }
                }

                try {
                    if (password != null) {
                        session.addPasswordIdentity(password);
                    }

                    if (!Strings.isNullOrEmpty(authKey)) {
                        try {
                            SecurityUtils.getKeyPairResourceParser()
                                         .loadKeyPairs(this.session,
                                                       NamedResource.ofName("auth-key"),
                                                       null,
                                                       authKey)
                                         .forEach(session::addPublicKeyIdentity);
                        } catch (final Exception e) {
                            LOG.error("Invalid ssh private key", e);
                        }
                    }

                    session.auth().verify(Duration.between(Instant.now(), timeoutInstant));

                    channel = Strings.isNullOrEmpty(shell)
                              ? session.createShellChannel()
                              : session.createExecChannel(shell);

                    try {
                        stdout = new ByteArrayOutputStream();
                        stderr = new ByteArrayOutputStream();
                        debugStdout = new ByteArrayOutputStream();
                        debugStderr = new ByteArrayOutputStream();
                        awaitStdout = new ByteArrayOutputStream();
                        debugOutput = new ByteArrayOutputStream();

                        var debugTee = new TeeOutputStream(stdout, debugStdout);
                        var teeStdout = new TeeOutputStream(debugTee, awaitStdout);
                        var teeStderr = new TeeOutputStream(stderr, debugStderr);
                        var stdin = new PipedInputStream();
                        pipeToStdin = new PipedOutputStream(stdin);
                        channel.setIn(stdin);
                        channel.setOut(teeStdout);
                        channel.setErr(teeStderr);
                        channel.open().verify(Duration.between(Instant.now(), timeoutInstant));
                        this.vars.putAll(vars);
                        this.vars.put(SCRIPT_VAR_TFTP_SERVER_IP, InetAddressUtils.str(localAddress));
                        this.vars.put("user", user);
                        this.vars.put("password", password);
                    } catch (Exception e) {
                        channel.close();
                        throw e;
                    }
                } catch (Exception e) {
                    session.close();
                    throw e;
                }
            } catch (Exception e) {
                sshClient.stop();
                throw e;
            }
            this.timeout = timeout;
        }

        @Override
        public void close() throws Exception {
            try {
                try {
                    channel.close();
                } finally {
                    session.close();
                }
            } finally {
                sshClient.stop();
            }
        }

        @Override
        public void sendLine(String string) throws IOException {
            pipeToStdin.write((string + "\n").getBytes(StandardCharsets.UTF_8));
            pipeToStdin.flush();
        }

        @Override
        public void await(String string) throws Exception {
            var search = string.getBytes(StandardCharsets.UTF_8);
            while (Instant.now().isBefore(timeoutInstant)) {
                synchronized (awaitStdout) {
                    if (matchAndConsume(awaitStdout, search)) {
                        if (!disableIOCollection) {
                            debugOutput.write(debugStderr.toString().getBytes(StandardCharsets.UTF_8));
                            debugOutput.write(debugStdout.toString().getBytes(StandardCharsets.UTF_8));
                        }
                        debugStdout.reset();
                        debugStderr.reset();
                        return;
                    }
                }
                Thread.sleep(1000);
            }
            if (!disableIOCollection) {
                debugOutput.write(debugStderr.toString().getBytes(StandardCharsets.UTF_8));
                debugOutput.write(debugStdout.toString().getBytes(StandardCharsets.UTF_8));
            }
            throw new Exception("awaited output missing - expected: " + string);
        }

        @Override
        public String replaceVars(String string) {
            return StrSubstitutor.replace(string, vars);
        }

        String getDebugOutput() {
            if (disableIOCollection) {
                return "Script IO collection is disabled";
            }
            else {
                return debugOutput.toString(StandardCharsets.UTF_8);
            }
        }
    }

    static boolean matchAndConsume(ByteArrayOutputStream awaitStdout, byte[] search) {
        var bytes = awaitStdout.toByteArray();
        outer: for (int i = 0; i < bytes.length - search.length + 1; i++) {
            for (int j = 0; j < search.length; j++) {
                if (bytes[i + j] != search[j]) {
                    continue outer;
                }
            }
            // clear the complete awaitStdout...
            awaitStdout.reset();
            // ... and then restore the bytes after the match
            awaitStdout.write(bytes, i + search.length, bytes.length - i - search.length);
            return true;
        }
        return false;
    }

}
