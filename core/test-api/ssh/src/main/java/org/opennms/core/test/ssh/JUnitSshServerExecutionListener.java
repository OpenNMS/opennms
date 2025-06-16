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
package org.opennms.core.test.ssh;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.opennms.core.test.ssh.annotations.JUnitSshServer;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.SocketUtils;

public class JUnitSshServerExecutionListener extends AbstractTestExecutionListener {
    private static final Logger LOG = LoggerFactory.getLogger(JUnitSshServerExecutionListener.class);

    private SshServer server;

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);

        if (server == null) {
            startServer(testContext);
        }
    }

    private void startServer(final TestContext testContext) throws IOException {
        final var testMethod = testContext.getTestMethod();
        final var config = testMethod.getAnnotation(JUnitSshServer.class);

        InetAddress host = null;
        if (config != null && config.listenHostname() != null && !config.listenHostname().isBlank()) {
            try {
                host = InetAddress.getByName(config.listenHostname());
            } catch (final UnknownHostException e) {
                LOG.error("unable to determine InetAddress from hostname {}" , config.listenHostname(), e);
                throw e;
            }
        }
        if (host == null) {
            host = InetAddressUtils.getLocalHostAddress();
        }

        final var canonicalHostName = host.getCanonicalHostName();
        final var port = (config != null && config.port() > 0)? config.port() : SocketUtils.findAvailableTcpPort();

        server = SshServer.setUpDefaultServer();
        server.setHost(canonicalHostName);
        server.setPort(port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setPublickeyAuthenticator((s, publicKey, session) -> true);
        server.setPasswordAuthenticator((username, password, session) -> {
            for (final var login : config.logins()) {
                if (username.equals(login.username()) && password.equals(login.password())) {
                    return true;
                }
            }
            return false;
        });

        CoreModuleProperties.WELCOME_BANNER.set(server, "JUnitSshServer SSH 0.0");

        try {
            server.start();
        } catch (final BindException e) {
            LOG.warn("Failed to start SSH server on {}:{}", canonicalHostName, port, e);
        }

        if (testContext.getTestInstance() instanceof SshServerDataProviderAware) {
            final var provider = new JUnitSshServerDataProvider(server, host, port);
            LOG.debug("injecting data provider into SshServerDataProviderAware test: {}", testContext.getTestInstance());
            ((SshServerDataProviderAware)testContext.getTestInstance()).setSshServerDataProvider(provider);
        }
    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        this.server.stop(true);
        server = null;
    }

    public static class JUnitSshServerDataProvider implements SshServerDataProvider {
        private SshServer server;
        private InetAddress host;
        private int port;

        public JUnitSshServerDataProvider(final SshServer server, final InetAddress host, final int port) {
            this.server = server;
            this.host = host;
            this.port = port;
        }

        @Override
        public InetAddress getHost() {
            return host;
        }

        @Override
        public int getPort() {
            return port;
        }

        @Override
        public void setIdentification(final String banner) {
            CoreModuleProperties.SERVER_IDENTIFICATION.set(server, banner);
        }
    }
}
