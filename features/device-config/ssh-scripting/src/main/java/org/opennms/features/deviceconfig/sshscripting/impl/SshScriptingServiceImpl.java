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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.session.ClientSession;
import org.opennms.features.deviceconfig.sshscripting.SshScriptingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshScriptingServiceImpl implements SshScriptingService {

    private static Logger LOG = LoggerFactory.getLogger(SshScriptingServiceImpl.class);

    @Override
    public Optional<Failure> execute(
            String script,
            String user,
            String password,
            String host,
            int port,
            Map<String, String> vars,
            Duration timeout
    ) {
        return Statement.parseScript(script).fold(
                errorLines -> Optional.of(
                        new Failure(errorLines.stream().collect(Collectors.joining("\n", "unrecognized statements:\n", "")), null, null)
                ),
                statements -> {
                    try {
                        try (var sshInteraction = new SshInteractionImpl(user, password, host, port, vars, timeout)) {
                            for (var statement : statements) {
                                try {
                                    statement.execute(sshInteraction);
                                } catch (Exception e) {
                                    var stdout = sshInteraction.stdout.toString(StandardCharsets.UTF_8);
                                    var stderr = sshInteraction.stderr.toString(StandardCharsets.UTF_8);
                                    LOG.error("ssh scripting exception - statement: " + statement +
                                              "\n### script ###\n" + script +
                                              "\n### stdout ###\n" + stdout +
                                              "\n### stderr ###\n" + stderr, e);
                                    return Optional.of(
                                            new Failure(
                                                    "ssh scripting exception - msg: " + e.getMessage() + "; statement: \"" + statement + "\"; script:\n" + script,
                                                    Optional.of(stdout),
                                                    Optional.of(stderr)
                                            )
                                    );
                                }
                            }
                            return Optional.empty();
                        }
                    } catch (Exception e) {
                        return Optional.of(new Failure(e.getMessage(), Optional.empty(), Optional.empty()));
                    }
                }
        );
    }

    private static class SshInteractionImpl implements SshInteraction, AutoCloseable {

        private final SshClient sshClient;
        private final ClientSession session;
        private final ClientChannel channel;

        // stdout and stderr capture the complete output of the interaction
        private final ByteArrayOutputStream stdout, stderr;
        // awaitStdout receives the same bytes as stdout but is used to process await statements
        // -> successfully awaited parts are dropped
        // -> these parts are no more considered by following awaits
        private final ByteArrayOutputStream awaitStdout;
        // pipeToStdIn is an output stream that is used to feed bytes into stdin of the interaction
        private final PipedOutputStream pipeToStdin;
        private final Map<String, String> vars = new HashMap<>();

        private final Duration timeout;

        private SshInteractionImpl(
                String user,
                String password,
                String host,
                int port,
                Map<String, String> vars,
                Duration timeout
        ) throws Exception {
            sshClient = SshClient.setUpDefaultClient();
            sshClient.start();
            try {
                session = sshClient
                        .connect(user, host, port)
                        .verify(timeout)
                        .getSession();
                try {

                    session.addPasswordIdentity(password);
                    session.auth().verify(timeout);

                    channel = session.createShellChannel();

                    try {
                        stdout = new ByteArrayOutputStream();
                        stderr = new ByteArrayOutputStream();
                        awaitStdout = new ByteArrayOutputStream();

                        var teeStdout = new TeeOutputStream(stdout, awaitStdout);
                        var stdin = new PipedInputStream();
                        pipeToStdin = new PipedOutputStream(stdin);
                        channel.setIn(stdin);
                        channel.setOut(teeStdout);
                        channel.setErr(stderr);
                        channel.open().verify(timeout);
                        this.vars.putAll(vars);
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
            var awaitUntil = Instant.now().plus(timeout);
            while (Instant.now().isBefore(awaitUntil)) {
                synchronized (awaitStdout) {
                    if (matchAndConsume(awaitStdout, search)) {
                        return;
                    }
                }
                Thread.sleep(1000);
            }
            throw new Exception("awaited output missing - expected: " + string);
        }

        @Override
        public String replaceVars(String string) {
            return StrSubstitutor.replace(string, vars);
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
