/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * OPENNMS: verbatim copy of LocalConsoleManager.java from Apache Karaf 4.3.10,
 * with the setContextClassLoader() block in start() backported from Karaf 4.4.9.
 *
 * jline 3.26 and newer discover their terminal providers by reading the resource
 * META-INF/jline/providers/<name> through the *thread context* classloader (see
 * org.jline.terminal.spi.TerminalProvider.load). Inside Karaf that classloader is
 * the launcher's AppClassLoader, which cannot see resources inside the org.jline
 * bundle, so every provider lookup fails and jline silently falls back to a
 * DumbTerminal: the local console ("bin/karaf" without "server"/"daemon", as used
 * by features/{minion,sentinel}/runInPlace.sh) then echoes nothing and spins
 * redrawing its prompt. Remote consoles are unaffected because they build their
 * Terminal from streams and never go through provider discovery.
 *
 * Karaf fixed this in 4.4.x by pinning the context classloader to jline's own
 * while the Terminal is built. Karaf 4.3.x never got the fix, so we apply it here.
 *
 * Delete this module once the container moves to Karaf 4.4.x or newer.
 */
package org.apache.karaf.shell.impl.console.osgi;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.apache.karaf.jaas.boot.principal.ClientPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.apache.karaf.shell.impl.console.JLineTerminal;
import org.apache.karaf.shell.support.ShellUtil;
import org.apache.karaf.util.jaas.JaasHelper;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class LocalConsoleManager {
    
    private static final String INPUT_ENCODING = "input.encoding";
    private static final String KARAF_DELAY_CONSOLE = "karaf.delay.console";
    private static final String KARAF_LOCAL_USER = "karaf.local.user";
    private static final String KARAF_LOCAL_ROLES = "karaf.local.roles";
    private static final String KARAF_LOCAL_ROLES_DEFAULT = "admin,manager,viewer,systembundles";
    
    private SessionFactory sessionFactory;
    private BundleContext bundleContext;
    private Session session;
    private ServiceRegistration<?> registration;
    private boolean closing;

    private DelayedStarted watcher;

    public LocalConsoleManager(BundleContext bundleContext,
                               SessionFactory sessionFactory) throws Exception {
        this.bundleContext = bundleContext;
        this.sessionFactory = sessionFactory;
    }

    public void start() throws Exception {
        final Terminal terminal;

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader cl = TerminalBuilder.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(cl);
            terminal = TerminalBuilder.builder()
                    .nativeSignals(true)
                    .signalHandler(Terminal.SignalHandler.SIG_IGN)
                    .build();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        final Subject subject = createLocalKarafSubject();    
        this.session = JaasHelper.doAs(subject, (PrivilegedAction<Session>) () -> {
            String encoding = getEncoding();
            PrintStream pout = new PrintStream(terminal.output()) {
                @Override
                public void close() {
                    // do nothing
                }
            };
            session = sessionFactory.create(
                                  terminal.input(),
                                  pout,
                                  pout,
                                  new JLineTerminal(terminal),
                                  encoding,
                                  LocalConsoleManager.this::close);
            session.put(Session.IS_LOCAL, true);
            registration = bundleContext.registerService(Session.class, session, null);
            String name = "Karaf local console user " + ShellUtil.getCurrentUserName();
            boolean delayconsole = Boolean.parseBoolean(System.getProperty(KARAF_DELAY_CONSOLE));
            if (delayconsole) {
                watcher = new DelayedStarted(session, name, bundleContext, System.in);
                new Thread(watcher, name).start();
            } else {
                new Thread(session, name).start();
            }
            return session;
        });
        // TODO: register the local session so that ssh can add the agent
//        registration = bundleContext.register(CommandSession.class, console.getSession(), null);

    }

    /**
     * Get the default encoding.  Will first look at the LC_CTYPE environment variable, then the input.encoding
     * system property, then the default charset according to the JVM.
     *
     * @return The default encoding to use when none is specified.
     */
    public static String getEncoding() {
        // LC_CTYPE is usually in the form en_US.UTF-8
        String envEncoding = extractEncodingFromCtype(System.getenv("LC_CTYPE"));
        if (envEncoding != null) {
            return envEncoding;
        }
        return System.getProperty(INPUT_ENCODING, Charset.defaultCharset().name());
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

    private Subject createLocalKarafSubject() {

        String userName = System.getProperty(KARAF_LOCAL_USER);
        if (userName == null) {
            userName = "karaf";
        }

        final Subject subject = new Subject();
        subject.getPrincipals().add(new UserPrincipal(userName));
        subject.getPrincipals().add(new ClientPrincipal("local", "localhost"));

        String roles = System.getProperty(KARAF_LOCAL_ROLES, KARAF_LOCAL_ROLES_DEFAULT);
        if (roles != null) {
            for (String role : roles.split("[,]")) {
                subject.getPrincipals().add(new RolePrincipal(role.trim()));
            }
        }
        return subject;
    }

    public void stop() throws Exception {
        closing = true;
        if (registration != null) {
            registration.unregister();
        }
        // The bundle is stopped
        // so close the console and remove the callback so that the
        // osgi framework isn't stopped
        if (session != null) {
            session.close();
        }
        if (watcher != null) {
            watcher.stopDelayed();
        }
    }

    protected void close() {
        try {
            if (!closing) {
                bundleContext.getBundle(0).stop();
            }
        } catch (Exception e) {
            // Ignore
        }
    }

}
