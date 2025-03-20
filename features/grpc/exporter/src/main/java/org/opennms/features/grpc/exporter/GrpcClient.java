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

package org.opennms.features.grpc.exporter;

import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class GrpcClient {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcClient.class);

    private final String host;
    private final String tlsCertPath;
    private final boolean tlsEnabled;
    private ManagedChannel channel;
    private final ClientInterceptor clientInterceptor;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public AtomicBoolean isItTest = new AtomicBoolean(false);
    public final int PORT = 50051;

    public GrpcClient(final String host,
                      final String tlsCertPath,
                      final boolean tlsEnabled,
                      ClientInterceptor clientInterceptor) {
        this.host = Objects.requireNonNull(host);
        this.tlsCertPath = tlsCertPath;
        this.tlsEnabled = tlsEnabled;
        this.clientInterceptor = clientInterceptor;
    }

    public ConnectivityState getChannelState() {
        return this.channel != null ? (this.channel.getState(true)) : ConnectivityState.SHUTDOWN;
    }

    public ManagedChannel getChannel () {
        return this.channel;
    }

    public String getHost () {
        return this.host;
    }

    public boolean getStopped () {
        return this.stopped.get();
    }

    public AtomicBoolean getIsItTest() {
        return isItTest;
    }

    public void setIsItTest(AtomicBoolean isItTest) {
        this.isItTest = isItTest;
    }

    public synchronized void startGrpcConnection() throws SSLException {

        if(getChannelState().equals(ConnectivityState.READY))
        {
            LOG.info("Grpc Channel already connected and in ready state!");
            return;
        }
        if(isItTest.get()) {

            // Example target with localhost and dynamic port
            this.channel = ManagedChannelBuilder.forTarget("localhost:" + PORT)
                    .usePlaintext()  // non-SSL connection
                    .build();

        }else {

        final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forTarget(this.host)
                .intercept(clientInterceptor)
                .keepAliveWithoutCalls(true);

        if (tlsEnabled && tlsCertPath != null && !tlsCertPath.isBlank()) {
            this.channel = channelBuilder.useTransportSecurity()
                    .sslContext(GrpcSslContexts.forClient()
                            .trustManager(new File(tlsCertPath)).build())
                    .build();
            LOG.info("TLS enabled with cert at {}", tlsCertPath);
        } else if (tlsEnabled) {
            // Use system store specified in javax.net.ssl.trustStore
            this.channel = channelBuilder.useTransportSecurity()
                    .build();
            LOG.info("TLS enabled with certs from system store");
        } else {
            this.channel = channelBuilder.usePlaintext()
                    .build();
            LOG.info("TLS disabled, using plain text");
        }
        LOG.info("Grpc client started connection to {}", this.host);
    }
    }

    public synchronized void stopGrpcConnection() {
        if (this.channel != null) {
            this.channel.shutdownNow();
        }
        stopped.set(true);
        LOG.info("Grpc client stopped for host {} ", this.host);
    }
}
