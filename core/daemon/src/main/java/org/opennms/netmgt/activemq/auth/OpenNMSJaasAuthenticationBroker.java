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
package org.opennms.netmgt.activemq.auth;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.jaas.JassCredentialCallbackHandler;
import org.apache.activemq.security.AbstractAuthenticationBroker;
import org.apache.activemq.security.JaasAuthenticationBroker;
import org.apache.activemq.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenNMSJaasAuthenticationBroker extends AbstractAuthenticationBroker {

    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSJaasAuthenticationBroker.class);

    private static final String JAAS_CONTEXT_NAME = "opennms";

    private static final Pattern EXTRACT_IP_ADDRESS_PATTERN = Pattern.compile("tcp://([0-9\\.:]+):([0-9]+)");

    private boolean isJaasContextAvailable = false;

    private final Set<InetAddress> trustedHosts;
    private final String usernameForTrustedHosts;
    private final Set<Principal> principalsForTrustedHosts;

    public OpenNMSJaasAuthenticationBroker(Broker next, Set<InetAddress> trustedHosts,
            String usernameForTrustedHosts, Set<Principal> principalsForTrustedHosts) {
        super(next);
        this.trustedHosts = Objects.requireNonNull(trustedHosts);
        this.usernameForTrustedHosts = Objects.requireNonNull(usernameForTrustedHosts);
        this.principalsForTrustedHosts = Objects.requireNonNull(principalsForTrustedHosts);
    }

    @Override
    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
        if (context.getSecurityContext() == null) {
            // The connection is not yet authenticated, is the remote address trusted?
            authenticateBasedOnRemoteAddress(context, info);
            if (context.getSecurityContext() == null) {
                // We don't trust the remote address, authenticate using JAAS
                synchronized(this) {
                    // The JAAS context may not be currently available, so staal
                    // until it does become available
                    if (!isJaasContextAvailable) {
                        isJaasContextAvailable = waitForJaasContext();
                    }
                }
                authenticateUsingJaas(context, info);
            }
        }
        // Continue calling the chain of filters
        super.addConnection(context, info);
    }

    protected static InetAddress getAddressFromConnectionString(String remoteAddress) {
        final Matcher m = EXTRACT_IP_ADDRESS_PATTERN.matcher(remoteAddress);
        if (m.matches()) {
            try {
                return InetAddress.getByName(m.group(1));
            } catch (UnknownHostException e) {
                // pass
            }
        }
        return null;
    }

    private void authenticateBasedOnRemoteAddress(ConnectionContext context, ConnectionInfo info) {
        boolean grant = false;

        final String connectionString = context.getConnection().getRemoteAddress();
        if (connectionString.startsWith("vm://")) {
            // Always grant VM connections
            grant = true;
        } else {
            final InetAddress remoteAddress = getAddressFromConnectionString(connectionString);
            if (remoteAddress == null) {
                LOG.warn("Unable to determine remote address from connection string: {}", connectionString);
            } else if (trustedHosts.contains(remoteAddress)) {
                grant = true;
            }
        }

        if (!grant) {
            LOG.info("Connection from '{}' is NOT trusted.", connectionString);
            return;
        } else {
            LOG.info("Connection from '{}' is trusted.", connectionString);
            // Always create a new security context, even if it contains the same attributes
            // as the last context
            final SecurityContext securityContext = new SecurityContext(usernameForTrustedHosts) {
                @Override
                public Set<Principal> getPrincipals() {
                    return principalsForTrustedHosts;
                }
            };
            context.setSecurityContext(securityContext);
            securityContexts.add(securityContext);
        }
    }

    private boolean waitForJaasContext() {
        final long sleepMs = TimeUnit.SECONDS.toMillis(2);
        final long maxSleepMs = TimeUnit.MINUTES.toMillis(3);
        for (long k = 0; k < maxSleepMs; k += sleepMs) {
            try {
                new LoginContext(JAAS_CONTEXT_NAME);
                return true;
            } catch (LoginException e) {
                // pass
            }

            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                break;
            }
        }
        return false;
    }

    private void authenticateUsingJaas(ConnectionContext context, ConnectionInfo info) {
        // Set the TCCL since it seems JAAS needs it to find the login
        // module classes.
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(JaasAuthenticationBroker.class.getClassLoader());
        try {
            SecurityContext s = authenticate(info.getUserName(), info.getPassword(), null);
            context.setSecurityContext(s);
            securityContexts.add(s);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Override
    public SecurityContext authenticate(String username, String password, X509Certificate[] certificates) throws SecurityException {
        SecurityContext result = null;
        JassCredentialCallbackHandler callback = new JassCredentialCallbackHandler(username, password);
        try {
            LoginContext lc = new LoginContext(JAAS_CONTEXT_NAME, callback);
            lc.login();
            Subject subject = lc.getSubject();

            result = new JaasSecurityContext(username, subject);
        } catch (Exception ex) {
            throw new SecurityException("User name [" + username + "] or password is invalid.", ex);
        }

        return result;
    }
}
