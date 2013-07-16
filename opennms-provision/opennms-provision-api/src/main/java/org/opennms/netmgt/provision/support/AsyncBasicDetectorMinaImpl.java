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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.support.trustmanager.RelaxedX509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AsyncBasicDetectorMinaImpl class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class AsyncBasicDetectorMinaImpl<Request, Response> extends AsyncBasicDetector<Request, Response> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AsyncBasicDetectorMinaImpl.class);
    
    private BaseDetectorHandler<Request, Response> m_detectorHandler = new BaseDetectorHandler<Request, Response>();
    private IoFilterAdapter m_filterLogging = null;
    private ProtocolCodecFilter m_protocolCodecFilter = new ProtocolCodecFilter(new TextLineCodecFactory(CHARSET_UTF8));
    
    private final ConnectionFactory m_connectionFactory;

    private static class SlightlyMoreVerboseLoggingFilter extends LoggingFilter {
        protected Logger m_logger;

        public SlightlyMoreVerboseLoggingFilter() {
            super();
            m_logger = LoggerFactory.getLogger(LoggingFilter.class.getName());
        }

        /**
         * Log the specific flavor of IDLE that is encountered.
         */
        @Override
        public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
            if (IdleStatus.BOTH_IDLE.equals(status)) {
                m_logger.info("BOTH_IDLE");
            } else if (IdleStatus.READER_IDLE.equals(status)) {
                m_logger.info("READER_IDLE");
            } else if (IdleStatus.WRITER_IDLE.equals(status)) {
                m_logger.info("WRITER_IDLE");
            }
            nextFilter.sessionIdle(session, status);
        }
    }

    /**
     * <p>Constructor for AsyncBasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     */
    public AsyncBasicDetectorMinaImpl(final String serviceName, final int port) {
        super(serviceName, port);
        m_connectionFactory = ConnectionFactory.getFactory(getTimeout());
    }
    
    /**
     * <p>Constructor for AsyncBasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    public AsyncBasicDetectorMinaImpl(final String serviceName, final int port, final int timeout, final int retries){
        super(serviceName, port, timeout, retries);
        m_connectionFactory = ConnectionFactory.getFactory(getTimeout());
    }
    
    /**
     * <p>dispose</p>
     */
    @Override
    public void dispose(){
        LOG.debug("calling dispose on detector {}", getServiceName());
        ConnectionFactory.dispose(m_connectionFactory);
    }
    
    /** {@inheritDoc} */
    @Override
    public final DetectFuture isServiceDetected(final InetAddress address) {

        final DetectFutureMinaImpl detectFuture = new DetectFutureMinaImpl(this);

        try {
            // Set this up here because it can throw an Exception, which we want
            // to throw now, not in initializeSession
            final SSLContext c = createClientSSLContext();

            // Create an IoSessionInitializer that will configure this individual
            // session. Previously, all this was done on a new Connector each time
            // but that was leaking file handles all over the place. This way gives
            // us per-connection settings without the overhead of creating new
            // Connectors each time
            IoSessionInitializer<ConnectFuture> init = new IoSessionInitializer<ConnectFuture>() {

                @Override
                public void initializeSession(IoSession session, ConnectFuture future) {
                    // Add filters to the session
                    if(isUseSSLFilter()) {
                        final SslFilter filter = new SslFilter(c);
                        filter.setUseClientMode(true);
                        session.getFilterChain().addFirst("SSL", filter);
                    }
                    session.getFilterChain().addLast( "logger", getLoggingFilter() != null ? getLoggingFilter() : new SlightlyMoreVerboseLoggingFilter() );
                    session.getFilterChain().addLast( "codec", getProtocolCodecFilter());

                    // Make the minimum idle timeout 1 second
                    int idleTimeInSeconds = Math.max(1, Math.round(getIdleTime() / 1000.0f));
                    // Set all of the idle time limits. Make sure to specify values in
                    // seconds!!!
                    session.getConfig().setReaderIdleTime(idleTimeInSeconds);
                    session.getConfig().setWriterIdleTime(idleTimeInSeconds);
                    session.getConfig().setBothIdleTime(idleTimeInSeconds);
                }
            };

            // Start communication
            final InetSocketAddress socketAddress = new InetSocketAddress(address, getPort());
            final ConnectFuture cf = m_connectionFactory.connect(socketAddress, init, createDetectorHandler(detectFuture));
            cf.addListener(retryAttemptListener(detectFuture, socketAddress, init, getRetries()));
        } catch (KeyManagementException e) {
            detectFuture.setException(e);
        } catch (NoSuchAlgorithmException e) {
            detectFuture.setException(e);
        } catch (Throwable e) {
            detectFuture.setException(e);
        }

        return detectFuture;
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    private static final SSLContext createClientSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] tm = { new RelaxedX509TrustManager() };
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        return sslContext;
    }

    /**
     * Handles the retry attempts. Listens to see when the ConnectFuture is finished and checks if there was 
     * an exception thrown. If so, it then attempts a retry if there are more retries.
     * 
     * @param connector
     * @param detectFuture
     * @param address
     * @param retryAttempt
     * @return IoFutureListener<ConnectFuture>
     */
    private final IoFutureListener<ConnectFuture> retryAttemptListener(final DetectFutureMinaImpl detectFuture, final InetSocketAddress address, final IoSessionInitializer<ConnectFuture> init, final int retryAttempt) {
        return new IoFutureListener<ConnectFuture>() {

            @Override
            public void operationComplete(ConnectFuture future) {
                final Throwable cause = future.getException();
               
                if (cause instanceof IOException) {
                    if(retryAttempt == 0) {
                        LOG.info("Service {} detected false: {}: {}",getServiceName(), cause.getClass().getName(), cause.getMessage());
                        detectFuture.setServiceDetected(false);
                    }else {
                        LOG.info("Connection exception occurred: {} for service {}, retrying attempt {}", cause, getServiceName(), retryAttempt);
                        future = m_connectionFactory.reConnect(address, init, createDetectorHandler(detectFuture));
                        future.addListener(retryAttemptListener(detectFuture, address, init, retryAttempt - 1));
                    }
                }else if(cause instanceof Throwable) {
                    LOG.info("Threw a Throwable and detection is false for service {}", getServiceName(), cause);
                    detectFuture.setServiceDetected(false);
                }
            }
            
        };
    }

    /**
     * <p>setDetectorHandler</p>
     *
     * @param detectorHandler a {@link org.opennms.netmgt.provision.support.BaseDetectorHandler} object.
     */
    protected final void setDetectorHandler(final BaseDetectorHandler<Request, Response> detectorHandler) {
        m_detectorHandler = detectorHandler;
    }
    
    /**
     * <p>createDetectorHandler</p>
     *
     * @param future a {@link org.opennms.netmgt.provision.DetectFuture} object.
     * @return a {@link org.apache.mina.core.service.IoHandler} object.
     */
    protected final IoHandler createDetectorHandler(final DetectFutureMinaImpl future) {
        m_detectorHandler.setConversation(getConversation());
        m_detectorHandler.setFuture(future);
        return m_detectorHandler;
    }

    /**
     * <p>setLoggingFilter</p>
     *
     * @param filterLogging a {@link org.apache.mina.core.filterchain.IoFilterAdapter} object.
     */
    protected final void setLoggingFilter(final IoFilterAdapter filterLogging) {
        m_filterLogging = filterLogging;
    }

    /**
     * <p>getLoggingFilter</p>
     *
     * @return a {@link org.apache.mina.core.filterchain.IoFilterAdapter} object.
     */
    protected final IoFilterAdapter getLoggingFilter() {
        return m_filterLogging;
    }

    /**
     * <p>setProtocolCodecFilter</p>
     *
     * @param protocolCodecFilter a {@link org.apache.mina.filter.codec.ProtocolCodecFilter} object.
     */
    protected final void setProtocolCodecFilter(final ProtocolCodecFilter protocolCodecFilter) {
        m_protocolCodecFilter = protocolCodecFilter;
    }

    /**
     * <p>getProtocolCodecFilter</p>
     *
     * @return a {@link org.apache.mina.filter.codec.ProtocolCodecFilter} object.
     */
    protected final ProtocolCodecFilter getProtocolCodecFilter() {
        return m_protocolCodecFilter;
    }

    /**
     * <p>getDetectorHandler</p>
     *
     * @return a {@link org.apache.mina.core.service.IoHandler} object.
     */
    protected final IoHandler getDetectorHandler() {
        return m_detectorHandler;
    }
}
