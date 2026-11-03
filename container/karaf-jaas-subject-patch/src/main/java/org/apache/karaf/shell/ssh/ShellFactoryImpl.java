/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.karaf.shell.ssh;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.karaf.jaas.boot.principal.ClientPrincipal;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.apache.karaf.shell.support.ShellUtil;
import org.apache.karaf.util.jaas.JaasHelper;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ShellFactory;

/**
 * SSHD {@link org.apache.sshd.server.command.Command} factory which provides access to
 * Shell.
 */
public class ShellFactoryImpl implements ShellFactory {

    private SessionFactory sessionFactory;

    public ShellFactoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Command createShell(ChannelSession channelSession) {
        return new ShellImpl();
    }

    public class ShellImpl implements Command {
        private InputStream in;

        private OutputStream out;

        private OutputStream err;

        private ExitCallback callback;

        private ServerSession session;

        private Session shell;

        private SshTerminal terminal;

        private boolean closed;

        @Override
        public void setInputStream(final InputStream in) {
            this.in = in;
        }

        @Override
        public void setOutputStream(final OutputStream out) {
            this.out = out;
        }

        @Override
        public void setErrorStream(final OutputStream err) {
            this.err = err;
        }

        @Override
        public void setExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        @Override
        public void start(ChannelSession channelSession, Environment environment) throws IOException {
            this.session = channelSession.getServerSession();
            try {
                final Subject subject = session.getAttribute(KarafJaasAuthenticator.SUBJECT_ATTRIBUTE_KEY);
                String encoding = getEncoding(environment);
                terminal = new SshTerminal(environment, in, out, encoding);
                final PrintStream pout = new PrintStream(terminal.output(), true, encoding);
                final PrintStream perr = err instanceof PrintStream ?
                        (PrintStream) err : out == err ? pout : new PrintStream(err, true, encoding);
                shell = sessionFactory.create(in, pout,
                        perr, terminal, encoding, this::destroy);
                for (Map.Entry<String, String> e : environment.getEnv().entrySet()) {
                    shell.put(e.getKey(), e.getValue());
                }
                shell.put(Subject.class.getName(), subject);
                // OPENNMS: the console thread inherits the context class loader of the sshd NIO2
                // thread starting it, which JDK 24+ always creates as an InnocuousThread with the
                // system class loader. On OpenNMS that loader sees neither Spring nor OpenNMS, so
                // code relying on the context class loader (e.g. Spring proxies) fails in
                // commands, and in the pool threads they start. Use the loader of jaas.boot:
                // opennms_bootstrap's loader on OpenNMS, the system class loader elsewhere.
                JaasHelper.runAs(subject, () -> {
                    final Thread thread = new Thread(shell, "Karaf ssh console user " + ShellUtil.getCurrentUserName());
                    thread.setContextClassLoader(ClientPrincipal.class.getClassLoader());
                    thread.start();
                });
            } catch (Exception e) {
                throw new IOException("Unable to start shell", e);
            }
        }

        public void destroy() {
            if (!closed) {
                closed = true;
                callback.onExit(0);
                session.close(true);
            }
        }

        @Override
        public void destroy(ChannelSession channelSession) {
            destroy();
        }
    }

    /**
     * Get the default encoding.  Will first look at the LC_CTYPE environment variable, then the input.encoding
     * system property, then the default charset according to the JVM.
     *
     * @return The default encoding to use when none is specified.
     */
    public static String getEncoding(Environment env) {
        // LC_CTYPE is usually in the form en_US.UTF-8
        String ctype = env.getEnv().getOrDefault("LC_TYPE", System.getenv("LC_CTYPE"));
        String envEncoding = extractEncodingFromCtype(ctype);
        if (envEncoding != null) {
            return envEncoding;
        }
        return System.getProperty("input.encoding", Charset.defaultCharset().name());
    }

    /**
     * Parses the LC_CTYPE value to extract the encoding according to the POSIX standard, which says that the LC_CTYPE
     * environment variable may be of the format <code>[language[_territory][.codeset][@modifier]]</code>
     *
     * @param ctype The ctype to parse, may be null
     * @return The encoding, if one was present, otherwise null
     */
    static String extractEncodingFromCtype(String ctype) {
        if (ctype != null && ctype.indexOf('.') > 0) {
            String encodingAndModifier = ctype.substring(ctype.indexOf('.') + 1);
            if (encodingAndModifier.indexOf('@') > 0) {
                return encodingAndModifier.substring(0, encodingAndModifier.indexOf('@'));
            } else {
                return encodingAndModifier;
            }
        }
        return null;
    }

}
