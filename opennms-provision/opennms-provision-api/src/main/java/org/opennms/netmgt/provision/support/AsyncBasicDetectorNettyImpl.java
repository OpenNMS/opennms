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
package org.opennms.netmgt.provision.support;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.RelaxedX509ExtendedTrustManager;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.support.DetectFutureNettyImpl.ServiceDetectionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * <p>AsyncBasicDetectorNettyImpl class.</p>
 * 
 * CAUTION: This class is unused. This implementation has never been in production.
 *
 * @author Seth
 */
public abstract class AsyncBasicDetectorNettyImpl<Request, Response> extends AsyncBasicDetector<Request, Response> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AsyncBasicDetectorNettyImpl.class);

    public AsyncBasicDetectorNettyImpl(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>Constructor for AsyncBasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    public AsyncBasicDetectorNettyImpl(final String serviceName, final int port, final int timeout, final int retries){
        super(serviceName, port, timeout, retries);
    }
    
    /**
     * <p>dispose</p>
     */
    @Override
    public void dispose(){
        LOG.debug("calling dispose on detector {}", getServiceName());
    }
    
    /** {@inheritDoc} */
    @Override
    public final DetectFuture isServiceDetected(final InetAddress address) {

        DetectFuture detectFuture = new DetectFutureFailedImpl(this, new IllegalStateException());

        try {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(eventLoopGroup)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            final ChannelPipeline retval = nioSocketChannel.pipeline();
                            appendToPipeline(retval);
                            retval.addLast("detectorHandler", getDetectorHandler(getConversation()));
                            if (isUseSSLFilter()) {
                                // Use a relaxed SSL context
                                retval.addLast("sslHandler", new SslHandler(createClientSSLContext().createSSLEngine()));
                            }
                        }
                    });

            final SocketAddress remoteAddress = new InetSocketAddress(address, getPort());
            final ChannelFuture future = bootstrap.connect(remoteAddress);
            future.addListener(new RetryChannelFutureListener(remoteAddress, this.getRetries()));
            detectFuture = new DetectFutureNettyImpl(this, (ChannelPromise)future);
        } catch (Throwable e) {
            detectFuture = new DetectFutureFailedImpl(this, e);
        }

        return detectFuture;
    }

    protected void appendToPipeline(ChannelPipeline retval) {
        // Do nothing by default.
    }

    protected DetectorHandlerNettyImpl<Request, Response> getDetectorHandler(AsyncClientConversation<Request,Response> conversation) {
        DetectorHandlerNettyImpl<Request, Response> handler = new DetectorHandlerNettyImpl<Request, Response>();
        handler.setConversation(conversation);
        return handler;
    }

    /**
     * Upstream handler that will reattempt connections if an exception is generated on the
     * channel.
     * 
     * TODO: This doesn't work yet... need to figure out how to do retries with Netty
     */
    private class RetryChannelFutureListener implements GenericFutureListener<ChannelPromise> {
        private final SocketAddress m_remoteAddress;
        private int m_retries;

        public RetryChannelFutureListener(SocketAddress remoteAddress, int retries) {
            m_remoteAddress = remoteAddress;
            m_retries = retries;
        }

        @Override
        public void operationComplete(ChannelPromise future) {
            final Throwable cause = future.cause();

            if (cause != null) {
                if(cause instanceof IOException) {
                    if (m_retries == 0) {
                        LOG.info("Service {} detected false",getServiceName());
                        future.setFailure(new ServiceDetectionFailedException());
                    } else {
                        LOG.info("Connection exception occurred {} for service {}, retrying attempt {}", cause, getServiceName(), m_retries);
                        // Get an ephemeral port on the localhost interface
                        final InetSocketAddress localAddress = new InetSocketAddress(InetAddressUtils.getLocalHostAddress(), 0);

                        // Remove the current RetryChannelHandler
                        future.removeListener(this);
                        // Add a new listener with 1 fewer retry
                        LOG.error("RETRIES {}", m_retries);
                        future.addListener(new RetryChannelFutureListener(m_remoteAddress, m_retries - 1));
                        // Reconnect the channel
                        future.channel().bind(localAddress);
                        future.channel().connect(m_remoteAddress);
                    }
                } else {
                    LOG.info("Threw a Throwable and detection is false for service {}", getServiceName(), cause);
                    future.setFailure(new ServiceDetectionFailedException());
                }
            }
        }
    }

    private static SSLContext createClientSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] tm = { new RelaxedX509ExtendedTrustManager() };
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        return sslContext;
    }
}
