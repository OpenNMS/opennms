/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.InsufficientParametersException;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.UserAuthFactory;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.apache.sshd.client.config.hosts.HostConfigEntryResolver;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.common.AttributeRepository;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.SshException;
import org.apache.sshd.core.CoreModuleProperties;
import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier;
import org.apache.sshd.common.config.keys.KeyUtils;

import java.nio.file.Paths;

/**
 * This class is designed to be used by the service poller framework to test
 * the availability of SSH remote login. The class implements the
 * ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public final class MinaSshMonitor extends AbstractServiceMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(MinaSshMonitor.class);

    private static final int DEFAULT_RETRY = 0;

    /**
     * Constant <code>DEFAULT_TIMEOUT=3000</code>
     */
    public static final int DEFAULT_TIMEOUT = 3000;

    /**
     * Constant <code>DEFAULT_PORT=22</code>
     */
    public static final int DEFAULT_PORT = 22;

    public static final List<UserAuthFactory> NONINTERACTIVE_USER_AUTH_FACTORIES = Collections.unmodifiableList(
            Arrays.asList(
                    UserAuthPublicKeyFactory.INSTANCE,
                    UserAuthPasswordFactory.INSTANCE));

    /**
     * {@inheritDoc}
     *
     * Poll the specified address for service availability.
     * <p>
     * @param svc
     * @param parameters
     * @return
     *         <p>
     * @see #poll(InetAddress, Map)
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = PollStatus.unknown("polling never attempted");

        if (parameters == null) {
            throw new NullPointerException("parameter cannot be null");
        }

        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);
        String user = ParameterMap.getKeyedString(parameters, "user", null);
        final String password = ParameterMap.getKeyedString(parameters, "password", null);
        final String identifyFileName = ParameterMap.getKeyedString(parameters, "identity-file", null);
        final String remoteCommand = ParameterMap.getKeyedString(parameters, "remote-command", null);
        final String knownHostsFileName = ParameterMap.getKeyedString(parameters, "known-hosts-file", null);

        final String host = svc.getNodeLabel();
        final String hostAddress = InetAddressUtils.str(svc.getAddress());

        Collection<KeyPair> keys = null;
        List<UserAuthFactory> userFactories;

        if (Strings.isNullOrEmpty(hostAddress)) {
            throw new IllegalArgumentException("node is missing an ip address");
        }

        if (Strings.isNullOrEmpty(host)) {
            throw new IllegalArgumentException("node is missing a nodelabel");
        }

        try (SshClient client = SshClient.setUpDefaultClient()) {
            // Our client should only present non-interactive authentication methods
            client.setUserAuthFactories(NONINTERACTIVE_USER_AUTH_FACTORIES);
            if (!Strings.isNullOrEmpty(knownHostsFileName)) {
                ServerKeyVerifier current = client.getServerKeyVerifier();
                current = new DefaultKnownHostsServerKeyVerifier(current, false, Paths.get(knownHostsFileName));
                client.setServerKeyVerifier(current);
            }
            client.start();
            LOG.info("mina-ssh client started: client={}", client);

            if (!Strings.isNullOrEmpty(identifyFileName)) {
                try {
                    KeyPairResourceLoader loader = SecurityUtils.getKeyPairResourceParser();
                    keys = loader.loadKeyPairs(null, Paths.get(identifyFileName), client.getFilePasswordProvider());
                    for(KeyPair keyPair : keys) {
                        final String fp = KeyUtils.getFingerPrint(keyPair.getPublic());
                        LOG.info("loaded key with fingerprint {} from file {}", fp, identifyFileName);
                    }
                } catch (GeneralSecurityException e) {
                    LOG.warn("GeneralSecurityException loading ssh identity keypair file", e);
                } catch (IOException e) {
                    LOG.warn("IOException loading ssh identity keypair file", e);
                }
            }
            if (keys == null) {
                keys = Collections.emptySet();
            }
            if (Strings.isNullOrEmpty(password) && keys.isEmpty()) {
                throw new IllegalArgumentException("Both password and identity-files are not defined");
            }

            /* Create a host config entry since we want to connect to the ip address
             * and use our node-label as the "real hostname".
             */
            HostConfigEntry hostConfig = new HostConfigEntry();
            hostConfig.setHost(hostAddress);
            hostConfig.setHostName(hostAddress);
            hostConfig.setUsername(user);
            hostConfig.setPort(port);

            TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

            for (tracker.reset(); tracker.shouldRetry() && !status.isAvailable(); tracker.nextAttempt()) {
                tracker.startAttempt();
                status = PollStatus.unknown("polling never attempted");
                try (ClientSession session = client.connect(hostConfig, null, null)
                        .verify(tracker.getConnectionTimeout())
                        .getSession()) {
                    LOG.info("mina-ssh client session started: session={}", session);

                    if (!Strings.isNullOrEmpty(password)) {
                        session.addPasswordIdentity(password);
                    }
                    for(KeyPair keyPair : keys) {
                        session.addPublicKeyIdentity(keyPair);
                    }
                    session.auth().verify(tracker.getConnectionTimeout());

                    if (!Strings.isNullOrEmpty(remoteCommand)) {
                        try {
                            final String cmdOutput = session.executeRemoteCommand(remoteCommand);
                            LOG.info("mina-ssh client remote command output: '{}'", cmdOutput);
                        } catch (RemoteException e) {
                            status = PollStatus.unavailable(e.getMessage());
                            continue;
                        }
                    }

                    status = PollStatus.available(tracker.elapsedTimeInMillis());
                    LOG.info("mina-ssh client session ended: session={}", session);
                    break;
                } catch (UnresolvedAddressException e) {
                    status = PollStatus.unavailable("invalid hostname '" + host + "'");
                    break;
                } catch (IllegalArgumentException e) {
                    LOG.warn("service configuration issue", e);
                } catch (SshException e) {
                    LOG.info("ssh exception while connecting to host '{}'", host, e);
                    status = PollStatus.unavailable(e.getMessage());
                } catch (IOException e) {
                    LOG.warn("ioexception while connecting to host '{}'", host, e);
                    status = PollStatus.unavailable(e.getMessage());
                }
            }

            client.stop();
            LOG.info("mina-ssh client ended: client={}", client);
        } catch (IOException e) {
            LOG.error("ioexception from client", e);
        }
        return status;
    }
}
