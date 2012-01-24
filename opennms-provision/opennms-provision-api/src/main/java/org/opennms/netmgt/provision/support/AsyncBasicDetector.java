/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

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
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.support.AsyncClientConversation.AsyncExchangeImpl;
import org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.trustmanager.RelaxedX509TrustManager;

/**
 * <p>Abstract AsyncBasicDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class AsyncBasicDetector<Request, Response> extends AsyncAbstractDetector {
    
    protected static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    private BaseDetectorHandler<Request, Response> m_detectorHandler = new BaseDetectorHandler<Request, Response>();
    private IoFilterAdapter m_filterLogging = null;
    private ProtocolCodecFilter m_protocolCodecFilter = new ProtocolCodecFilter(new TextLineCodecFactory(CHARSET_UTF8));
    private int m_idleTime = 1;
    private AsyncClientConversation<Request, Response> m_conversation = new AsyncClientConversation<Request, Response>();
    private boolean useSSLFilter = false;
    
    private final ConnectionFactory m_connectionFactory;
    
    /**
     * <p>Constructor for AsyncBasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     */
    public AsyncBasicDetector(final String serviceName, final int port) {
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
    public AsyncBasicDetector(final String serviceName, final int port, final int timeout, final int retries){
        super(serviceName, port, timeout, retries);
        m_connectionFactory = ConnectionFactory.getFactory(getTimeout());
    }
    
    /**
     * <p>onInit</p>
     */
    abstract protected void onInit();
    
    /** {@inheritDoc} */
    @Override
    public DetectFuture isServiceDetected(final InetAddress address, final DetectorMonitor monitor) {

        final DetectFuture detectFuture = new DefaultDetectFuture(this);

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

                public void initializeSession(IoSession session, ConnectFuture future) {
                    // Add filters to the session
                    if(isUseSSLFilter()) {
                        final SslFilter filter = new SslFilter(c);
                        filter.setUseClientMode(true);
                        session.getFilterChain().addFirst("SSL", filter);
                    }
                    session.getFilterChain().addLast( "logger", getLoggingFilter() != null ? getLoggingFilter() : new LoggingFilter() );
                    session.getFilterChain().addLast( "codec", getProtocolCodecFilter());
                    session.getConfig().setIdleTime(IdleStatus.READER_IDLE, getIdleTime());
                    // Give the session an IoHandler that will get everything delegated to it
                    // by the SessionDelegateIoHandler
                    session.setAttribute( IoHandler.class, createDetectorHandler(detectFuture) );
                }
            };

            // Start communication
            final InetSocketAddress socketAddress = new InetSocketAddress(address, getPort());
            final ConnectFuture cf = m_connectionFactory.connect(socketAddress, init);
            cf.addListener(retryAttemptListener(m_connectionFactory, detectFuture, socketAddress, init, getRetries() ));
        } catch (KeyManagementException e) {
            detectFuture.setException(e);
            //detectFuture.setServiceDetected(false);
        } catch (NoSuchAlgorithmException e) {
            detectFuture.setException(e);
            //detectFuture.setServiceDetected(false);
        } catch (IOException e) {
            detectFuture.setException(e);
            //detectFuture.setServiceDetected(false);
        }

        return detectFuture;
    }

    /**
     * <p>dispose</p>
     */
    public void dispose(){
        LogUtils.debugf(this, "calling dispose on detector %s", getServiceName());
        ConnectionFactory.dispose(m_connectionFactory);
    }
    
    /**
     * @return
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    private SSLContext createClientSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
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
    private IoFutureListener<ConnectFuture> retryAttemptListener(final ConnectionFactory connector, final DetectFuture detectFuture, final InetSocketAddress address, final IoSessionInitializer<ConnectFuture> init, final int retryAttempt) {
        return new IoFutureListener<ConnectFuture>() {

            public void operationComplete(ConnectFuture future) {
                final Throwable cause = future.getException();
               
                if(cause instanceof IOException) {
                    if(retryAttempt == 0) {
                        LogUtils.infof(this, "Service %s detected false",getServiceName());
                        detectFuture.setServiceDetected(false);
                    }else {
                        LogUtils.infof(this, "Connection exception occurred %s for service %s, retrying attempt %d", cause, getServiceName(), retryAttempt);
                        // Connect without using a semaphore
                        /*
                        m_connectionFactory = ConnectionFactory.getFactory(getTimeout());
                        final ConnectFuture cf = m_connectionFactory.connect(address, init);
                        cf.addListener(retryAttemptListener( m_connectionFactory, detectFuture, address, init, retryAttempt - 1));
                        */
                        future = connector.reConnect(address, init);
                        future.addListener(retryAttemptListener(connector, detectFuture, address, init, retryAttempt - 1));
                    }
                }else if(cause instanceof Throwable) {
                    LogUtils.infof(this, cause, "Threw a Throwable and detection is false for service %s", getServiceName());
                    detectFuture.setServiceDetected(false);
                } 
            }
            
        };
    }
    
    /**
     * <p>expectBanner</p>
     *
     * @param bannerValidator a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected void expectBanner(final ResponseValidator<Response> bannerValidator) {
        getConversation().setHasBanner(true);
        getConversation().addExchange(new AsyncExchangeImpl<Request, Response>(null, bannerValidator));
    }
    
    /**
     * <p>send</p>
     *
     * @param request a Request object.
     * @param responseValidator a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected void send(final Request request, final ResponseValidator<Response> responseValidator) {
        getConversation().addExchange(new AsyncExchangeImpl<Request, Response>(request, responseValidator));
    }
    
    
    /**
     * <p>setDetectorHandler</p>
     *
     * @param detectorHandler a {@link org.opennms.netmgt.provision.support.BaseDetectorHandler} object.
     */
    protected void setDetectorHandler(final BaseDetectorHandler<Request, Response> detectorHandler) {
        m_detectorHandler = detectorHandler;
    }
    
    /**
     * <p>createDetectorHandler</p>
     *
     * @param future a {@link org.opennms.netmgt.provision.DetectFuture} object.
     * @return a {@link org.apache.mina.core.service.IoHandler} object.
     */
    protected IoHandler createDetectorHandler(final DetectFuture future) {
        m_detectorHandler.setConversation(getConversation());
        m_detectorHandler.setFuture(future);
        return m_detectorHandler;
    }

    /**
     * <p>setLoggingFilter</p>
     *
     * @param filterLogging a {@link org.apache.mina.core.filterchain.IoFilterAdapter} object.
     */
    protected void setLoggingFilter(final IoFilterAdapter filterLogging) {
        m_filterLogging = filterLogging;
    }

    /**
     * <p>getLoggingFilter</p>
     *
     * @return a {@link org.apache.mina.core.filterchain.IoFilterAdapter} object.
     */
    protected IoFilterAdapter getLoggingFilter() {
        return m_filterLogging;
    }

    /**
     * <p>setProtocolCodecFilter</p>
     *
     * @param protocolCodecFilter a {@link org.apache.mina.filter.codec.ProtocolCodecFilter} object.
     */
    protected void setProtocolCodecFilter(final ProtocolCodecFilter protocolCodecFilter) {
        m_protocolCodecFilter = protocolCodecFilter;
    }

    /**
     * <p>getProtocolCodecFilter</p>
     *
     * @return a {@link org.apache.mina.filter.codec.ProtocolCodecFilter} object.
     */
    protected ProtocolCodecFilter getProtocolCodecFilter() {
        return m_protocolCodecFilter;
    }

    /**
     * <p>setIdleTime</p>
     *
     * @param idleTime a int.
     */
    public void setIdleTime(final int idleTime) {
        m_idleTime = idleTime;
    }

    /**
     * <p>getIdleTime</p>
     *
     * @return a int.
     */
    public int getIdleTime() {
        return m_idleTime;
    }

    /**
     * <p>getDetectorHandler</p>
     *
     * @return a {@link org.apache.mina.core.service.IoHandler} object.
     */
    protected IoHandler getDetectorHandler() {
        return m_detectorHandler;
    }

    /**
     * <p>setConversation</p>
     *
     * @param conversation a {@link org.opennms.netmgt.provision.support.AsyncClientConversation} object.
     */
    protected void setConversation(final AsyncClientConversation<Request, Response> conversation) {
        m_conversation = conversation;
    }

    /**
     * <p>getConversation</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation} object.
     */
    protected AsyncClientConversation<Request, Response> getConversation() {
        return m_conversation;
    }
    
    /**
     * <p>request</p>
     *
     * @param request a Request object.
     * @return a Request object.
     */
    protected Request request(final Request request) {
        return request;
    }
    
    /**
     * <p>startsWith</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected ResponseValidator<Response> startsWith(final String prefix) {
        return new ResponseValidator<Response>() {

            public boolean validate(final Object message) {
                final String str = message.toString().trim();
                return str.startsWith(prefix);
            }
            
        };
    }
    
    /**
     * <p>find</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    public ResponseValidator<Response> find(final String regex){
        return new ResponseValidator<Response>() {

            public boolean validate(final Object message) {
                final String str = message.toString().trim();
                return Pattern.compile(regex).matcher(str).find();
            }
          
            
        };
    }

    /**
     * <p>Setter for the field <code>useSSLFilter</code>.</p>
     *
     * @param useSSLFilter a boolean.
     */
    public void setUseSSLFilter(final boolean useSSLFilter) {
        this.useSSLFilter = useSSLFilter;
    }

    /**
     * <p>isUseSSLFilter</p>
     *
     * @return a boolean.
     */
    public boolean isUseSSLFilter() {
        return useSSLFilter;
    }
}
