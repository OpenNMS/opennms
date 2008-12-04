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
package org.opennms.netmgt.provision.server;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.opennms.netmgt.provision.server.exchange.LineConversation;

/**
 * @author thedesloge
 *
 */
public class AsyncSimpleServer {
    
    private LineConversation m_lineConversation;
    private IoAcceptor m_acceptor;
    private IoHandler m_ioHandler;
    private int m_port = 9123;
    private int m_bufferSize = 2048;
    private int m_idleTime = 10;
    
    public void init() throws Exception {
        m_lineConversation = new LineConversation();
        onInit();
    }

    public void onInit() {}
    
    public void startServer() throws Exception {
        
        m_acceptor = new NioSocketAcceptor();
        m_acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        m_acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName( "UTF-8" ))));
        
        m_acceptor.setHandler(getServerHandler());
        m_acceptor.getSessionConfig().setReadBufferSize(getBufferSize());
        m_acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, getIdleTime());
        ((NioSocketAcceptor) m_acceptor).setReuseAddress(true);
        m_acceptor.bind(new InetSocketAddress(getPort()));
        
    }
    
    public void stopServer() throws Exception{
        m_acceptor.unbind();
        m_acceptor.dispose();
    }
    
    public void addRequestHandler(String request, String response) {
        m_lineConversation.addRequestHandler(request, response);
    }
    
    public void setBanner(String banner) {
        m_lineConversation.setBanner(banner);
    }
    
    public void setExpectedClose(String closeRequest) {
        m_lineConversation.setExpectedClose(closeRequest);
    }
    
    public void setExpectedClose(String closeRequest, String closeResponse) {
        m_lineConversation.setExpectedClose(closeRequest, closeResponse);
    }
    
    /**
     * @return
     */
    public IoHandler getServerHandler() {
        return m_ioHandler != null ? m_ioHandler : new SimpleServerHandler(m_lineConversation);
    }
    
    public void setServerHandler(IoHandler handler) {
        m_ioHandler = handler;
    }
    
    public void setPort(int port) {
        m_port = port;
    }

    public int getPort() {
        return m_port;
    }

    public void setBufferSize(int bufferSize) {
        m_bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return m_bufferSize;
    }

    public void setIdleTime(int idleTime) {
        m_idleTime = idleTime;
    }

    public int getIdleTime() {
        return m_idleTime;
    }
}
