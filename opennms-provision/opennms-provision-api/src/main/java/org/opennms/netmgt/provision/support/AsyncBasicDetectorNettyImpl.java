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
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.RelaxedX509ExtendedTrustManager;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.support.DetectFutureNettyImpl.ServiceDetectionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AsyncBasicDetectorNettyImpl class.</p>
 * 
 * CAUTION: This class is unused. This implementation has never been in production.
 *
 * @author Seth
 */
public abstract class AsyncBasicDetectorNettyImpl<Request, Response> extends AsyncBasicDetector<Request, Response> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AsyncBasicDetectorNettyImpl.class);
    
    private static final ChannelFactory m_factory = new NioClientSocketChannelFactory(
        Executors.newFixedThreadPool(
          Runtime.getRuntime().availableProcessors(),
          new LogPreservingThreadFactory("AsyncBasicDetectorNettyImpl.boss", Integer.MAX_VALUE)
        ),
        Executors.newFixedThreadPool(
          Runtime.getRuntime().availableProcessors(),
          new LogPreservingThreadFactory("AsyncBasicDetectorNettyImpl.worker", Integer.MAX_VALUE)
        )
    ); 

    /**
     * <p>Constructor for AsyncBasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     */
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
        m_factory.releaseExternalResources();
    }
    
    /** {@inheritDoc} */
    @Override
    public final DetectFuture isServiceDetected(final InetAddress address) {

        DetectFuture detectFuture = new DetectFutureFailedImpl(this, new IllegalStateException());

        try {
            ClientBootstrap bootstrap = new ClientBootstrap(m_factory);

            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                @Override
                public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline retval = Channels.pipeline();

                    // Upstream handlers
                    //retval.addLast("retryHandler", new RetryChannelHandler());
                    appendToPipeline(retval);

                    // Downstream handlers
                    retval.addLast("detectorHandler", getDetectorHandler(getConversation()));
                    if (isUseSSLFilter()) {
                        // Use a relaxed SSL context
                        retval.addLast("sslHandler", new SslHandler(createClientSSLContext().createSSLEngine()));
                    }

                    return retval;
                }
            });

            bootstrap.setOption("tcpNoDelay", true);
            bootstrap.setOption("keepAlive", true);

            SocketAddress remoteAddress = new InetSocketAddress(address, getPort());
            ChannelFuture future = bootstrap.connect(remoteAddress);
            future.addListener(new RetryChannelFutureListener(remoteAddress, this.getRetries()));
            detectFuture = new DetectFutureNettyImpl(this, future);

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
        //handler.setFuture(detectFuture);
        return handler;
    }

    /**
     * Upstream handler that will reattempt connections if an exception is generated on the
     * channel.
     * 
     * TODO: This doesn't work yet... need to figure out how to do retries with Netty
     */
    private class RetryChannelFutureListener implements ChannelFutureListener {
        private final SocketAddress m_remoteAddress;
        private int m_retries;

        public RetryChannelFutureListener(SocketAddress remoteAddress, int retries) {
            m_remoteAddress = remoteAddress;
            m_retries = retries;
        }

        @Override
        public void operationComplete(ChannelFuture future) {
            final Throwable cause = future.getCause();

            if (cause != null) {
                if(cause instanceof IOException) {
                    if (m_retries == 0) {
                        LOG.info("Service {} detected false",getServiceName());
                        future.setFailure(new ServiceDetectionFailedException());
                    } else {
                        LOG.info("Connection exception occurred {} for service {}, retrying attempt {}", cause, getServiceName(), m_retries);
                        // Get an ephemeral port on the localhost interface
                        final InetSocketAddress localAddress = new InetSocketAddress(InetAddressUtils.getLocalHostAddress(), 0);

                        // Disconnect the channel
                        //future.getChannel().disconnect().awaitUninterruptibly();
                        //future.getChannel().unbind().awaitUninterruptibly();

                        // Remove the current RetryChannelHandler
                        future.removeListener(this);
                        // Add a new listener with 1 fewer retry
                        LOG.error("RETRIES {}", m_retries);
                        future.addListener(new RetryChannelFutureListener(m_remoteAddress, m_retries - 1));
                        // Reconnect the channel
                        future.getChannel().bind(localAddress);
                        future.getChannel().connect(m_remoteAddress);
                    }
                } else {
                    LOG.info("Threw a Throwable and detection is false for service {}", getServiceName(), cause);
                    future.setFailure(new ServiceDetectionFailedException());
                }
            }
        }
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    private static SSLContext createClientSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] tm = { new RelaxedX509ExtendedTrustManager() };
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        return sslContext;
    }
}
