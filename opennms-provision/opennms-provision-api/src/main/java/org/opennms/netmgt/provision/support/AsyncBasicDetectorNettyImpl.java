/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.support.DetectFutureNettyImpl.ServiceDetectionFailedException;
import org.opennms.netmgt.provision.support.trustmanager.RelaxedX509TrustManager;
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
          Runtime.getRuntime().availableProcessors()
          // TODO: Should be uncommented when merging to master
          //new LogPreservingThreadFactory(getClass().getSimpleName() + ".boss", Integer.MAX_VALUE, false)
        ),
        Executors.newFixedThreadPool(
          Runtime.getRuntime().availableProcessors()
          // TODO: Should be uncommented when merging to master
          //new LogPreservingThreadFactory(getClass().getSimpleName() + ".worker", Integer.MAX_VALUE, false)
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
            } else if(cause instanceof Throwable) {
                LOG.info("Threw a Throwable and detection is false for service {}", getServiceName(), cause);
                future.setFailure(new ServiceDetectionFailedException());
            } 
        }
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    private static SSLContext createClientSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] tm = { new RelaxedX509TrustManager() };
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        return sslContext;
    }
}
