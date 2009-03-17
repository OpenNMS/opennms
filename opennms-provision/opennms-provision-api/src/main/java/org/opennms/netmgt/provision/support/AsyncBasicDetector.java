/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.support;

import java.net.ConnectException;
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
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.support.AsyncClientConversation.AsyncExchangeImpl;
import org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.trustmanager.RelaxedX509TrustManager;

/**
 * @author Donald Desloge
 *
 */
public abstract class AsyncBasicDetector<Request, Response> extends AsyncAbstractDetector {
    
    private BaseDetectorHandler<Request, Response> m_detectorHandler = new BaseDetectorHandler<Request, Response>();
    private IoFilterAdapter m_filterLogging;
    private ProtocolCodecFilter m_protocolCodecFilter = new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" )));
    private int m_idleTime = 1;
    private AsyncClientConversation<Request, Response> m_conversation = new AsyncClientConversation<Request, Response>();
    private boolean useSSLFilter = false;
    
    private static ConnectorFactory s_connectorFactory = new ConnectorFactory();
    private SocketConnector m_connector;
    
    public AsyncBasicDetector(String serviceName, int port) {
        super(serviceName, port);
    }
    
    /**
     * @param serviceName
     * @param port
     * @param timeout
     * @param retries
     */
    public AsyncBasicDetector(String serviceName, int port, int timeout, int retries){
        super(serviceName, port, timeout, retries);
    }
    
    abstract protected void onInit();
    
    @Override
    public DetectFuture isServiceDetected(InetAddress address, DetectorMonitor monitor) throws Exception {
        DetectFuture future = null;
        
        m_connector = s_connectorFactory.getConnector();
        
        future = new DefaultDetectFuture(this);
        
        // Set connect timeout.
        m_connector.setConnectTimeoutMillis( getTimeout() );
        m_connector.setHandler( createDetectorHandler(future) );
        
        if(isUseSSLFilter()) {
            SslFilter filter = new SslFilter(createClientSSLContext());
            filter.setUseClientMode(true);
            m_connector.getFilterChain().addFirst("SSL", filter);
        }
        
        m_connector.getFilterChain().addLast( "logger", getLoggingFilter() != null ? getLoggingFilter() : new LoggingFilter() );
        m_connector.getFilterChain().addLast( "codec", getProtocolCodecFilter());
        m_connector.getSessionConfig().setIdleTime( IdleStatus.READER_IDLE, getIdleTime() );

        // Start communication
        InetSocketAddress socketAddress = new InetSocketAddress(address, getPort());
        ConnectFuture cf = m_connector.connect( socketAddress );
        cf.addListener(retryAttemptListener( m_connector, future, socketAddress, getRetries() ));
        
        return future;
    }
    
    public void dispose(){
        s_connectorFactory.dispose(m_connector);
        m_connector = null;
    }
    
    /**
     * @return
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    private SSLContext createClientSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] tm = { new RelaxedX509TrustManager() };
        SSLContext sslContext = SSLContext.getInstance("SSL");
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
    private IoFutureListener<ConnectFuture> retryAttemptListener(final SocketConnector connector,final DetectFuture detectFuture, final InetSocketAddress address, final int retryAttempt) {
        return new IoFutureListener<ConnectFuture>() {

            public void operationComplete(ConnectFuture future) {
                Throwable cause = future.getException();
               
                if(cause instanceof ConnectException) {
                    if(retryAttempt == 0) {
                        System.out.println("service " + getServiceName() + " detected false");
                        detectFuture.setServiceDetected(false);
                    }else {
                        System.out.println("Connection exception occurred " + cause + " for service " + getServiceName() + " retrying attempt: "  + retryAttempt);
                        future = connector.connect(address);
                        future.addListener(retryAttemptListener(connector, detectFuture, address, retryAttempt -1));
                    }
                }else if(cause instanceof Throwable) {
                    System.out.println("Threw a Throwable and detection is false for service " + getServiceName() + " Throwable: " + cause);
                    detectFuture.setServiceDetected(false);
                } 
            }
            
        };
    }
    
    /**
     * 
     * @param bannerValidator
     */
    protected void expectBanner(ResponseValidator<Response> bannerValidator) {
        getConversation().setHasBanner(true);
        getConversation().addExchange(new AsyncExchangeImpl<Request, Response>(null, bannerValidator));
    }
    
    /**
     * 
     * @param request
     * @param responseValidator
     */
    protected void send(Request request, ResponseValidator<Response> responseValidator) {
        getConversation().addExchange(new AsyncExchangeImpl<Request, Response>(request, responseValidator));
    }
    
    
    /**
     * 
     * @param detectorHandler
     */
    protected void setDetectorHandler(BaseDetectorHandler<Request, Response> detectorHandler) {
        m_detectorHandler = detectorHandler;
    }
    
    /**
     * 
     * @param future
     * @return
     */
    protected IoHandler createDetectorHandler(DetectFuture future) {
        ((BaseDetectorHandler<Request, Response>) m_detectorHandler).setConversation(getConversation());
        m_detectorHandler.setFuture(future);
        return m_detectorHandler;
    }

    protected void setLoggingFilter(IoFilterAdapter filterLogging) {
        m_filterLogging = filterLogging;
    }

    protected IoFilterAdapter getLoggingFilter() {
        return m_filterLogging;
    }

    protected void setProtocolCodecFilter(ProtocolCodecFilter protocolCodecFilter) {
        m_protocolCodecFilter = protocolCodecFilter;
    }

    protected ProtocolCodecFilter getProtocolCodecFilter() {
        return m_protocolCodecFilter;
    }

    public void setIdleTime(int idleTime) {
        m_idleTime = idleTime;
    }

    public int getIdleTime() {
        return m_idleTime;
    }

    protected IoHandler getDetectorHandler() {
        return m_detectorHandler;
    }

    protected void setConversation(AsyncClientConversation<Request, Response> conversation) {
        m_conversation = conversation;
    }

    protected AsyncClientConversation<Request, Response> getConversation() {
        return m_conversation;
    }
    
    protected Request request(Request request) {
        return request;
    }
    
    protected ResponseValidator<Response> startsWith(final String prefix) {
        return new ResponseValidator<Response>() {

            public boolean validate(Object message) {
                String str = message.toString().trim();
                return str.startsWith(prefix);
            }
            
        };
    }
    
    public ResponseValidator<Response> find(final String regex){
        return new ResponseValidator<Response>() {

            public boolean validate(Object message) {
                String str = message.toString().trim();
                return Pattern.compile(regex).matcher(str).find();
            }
          
            
        };
    }

    public void setUseSSLFilter(boolean useSSLFilter) {
        this.useSSLFilter = useSSLFilter;
    }

    public boolean isUseSSLFilter() {
        return useSSLFilter;
    }
       

}
