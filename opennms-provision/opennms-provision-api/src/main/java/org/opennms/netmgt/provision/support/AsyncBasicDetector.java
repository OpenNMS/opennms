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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.opennms.netmgt.provision.DetectorFuture;
import org.opennms.netmgt.provision.DetectorMonitor;

/**
 * @author thedesloge
 *
 */
public abstract class AsyncBasicDetector extends AsyncAbstractDetector {
    
    private IoHandlerAdapter m_detectorHandler;
    private IoFilterAdapter m_filterLogging;
    private ProtocolCodecFilter m_protocolCodecFilter = new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" )));
    private int m_idleTime = 10;
    
    abstract protected void onInit();
    
    @SuppressWarnings("deprecation")
    @Override
    public DetectorFuture isServiceDetected(InetAddress address, DetectorMonitor monitor) {
        SocketConnector connector = new NioSocketConnector();

        // Set connect timeout.
         //connector.getDefaultConfig().setConnectTimeout(30);
        connector.setConnectTimeoutMillis( 3000 );
        connector.setHandler( getDetectorHandler() );
        connector.getFilterChain().addLast( "logger", getLoggingFilter() != null ? getLoggingFilter() : new LoggingFilter() );
        connector.getFilterChain().addLast( "codec", getProtocolCodecFilter());
        connector.getSessionConfig().setIdleTime( IdleStatus.READER_IDLE, getIdleTime() );

        // Start communication.
        //ConnectFuture cf = connector.connect( new InetSocketAddress( address, 9123 ));
        DetectorFuture df = (DetectorFuture) connector.connect( new InetSocketAddress( address, 9123 ));
        
        // Wait for the connection attempt to be finished.
        df.join();
        df.getSession();
        
        return df;
    }

    public void setDetectorHandler(IoHandlerAdapter detectorHandler) {
        m_detectorHandler = detectorHandler;
    }

    public IoHandlerAdapter getDetectorHandler() {
        return m_detectorHandler;
    }

    public void setLoggingFilter(IoFilterAdapter filterLogging) {
        m_filterLogging = filterLogging;
    }

    public IoFilterAdapter getLoggingFilter() {
        return m_filterLogging;
    }

    public void setProtocolCodecFilter(ProtocolCodecFilter protocolCodecFilter) {
        m_protocolCodecFilter = protocolCodecFilter;
    }

    public ProtocolCodecFilter getProtocolCodecFilter() {
        return m_protocolCodecFilter;
    }

    public void setIdleTime(int idleTime) {
        m_idleTime = idleTime;
    }

    public int getIdleTime() {
        return m_idleTime;
    }

}
