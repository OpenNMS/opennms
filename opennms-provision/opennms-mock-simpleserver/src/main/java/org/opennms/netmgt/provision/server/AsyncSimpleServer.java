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
package org.opennms.netmgt.provision.server;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.opennms.netmgt.provision.server.exchange.LineConversation;

/**
 * <p>AsyncSimpleServer class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class AsyncSimpleServer {

    private LineConversation m_lineConversation;
    private IoAcceptor m_acceptor;
    private IoHandler m_ioHandler;
    private int m_port = 9123;
    private int m_bufferSize = 2048;
    private int m_idleTime = 10;
    
    /**
     * <p>init</p>
     *
     * @throws java.lang.Exception if any.
     */
    public final void init() throws Exception {
        m_lineConversation = new LineConversation();
        onInit();
    }

    /**
     * <p>onInit</p>
     */
    protected void onInit() {
        // Do nothing by default
    }
    
    /**
     * <p>startServer</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void startServer() throws Exception {
        
        m_acceptor = new NioSocketAcceptor();
        m_acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        m_acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(StandardCharsets.UTF_8)));
        
        m_acceptor.setHandler(getServerHandler());
        m_acceptor.getSessionConfig().setReadBufferSize(getBufferSize());
        m_acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, getIdleTime());
        ((NioSocketAcceptor) m_acceptor).setReuseAddress(true);
        m_acceptor.bind(new InetSocketAddress(getPort()));
        
    }
    
    /**
     * <p>stopServer</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void stopServer() throws Exception{
        m_acceptor.unbind();
        m_acceptor.dispose();
    }
    
    /**
     * <p>addRequestHandler</p>
     *
     * @param request a {@link java.lang.String} object.
     * @param response a {@link java.lang.String} object.
     */
    public void addRequestHandler(String request, String response) {
        m_lineConversation.addRequestHandler(request, response);
    }
    
    /**
     * <p>setBanner</p>
     *
     * @param banner a {@link java.lang.String} object.
     */
    public void setBanner(String banner) {
        m_lineConversation.setBanner(banner);
    }
    
    /**
     * <p>setExpectedClose</p>
     *
     * @param closeRequest a {@link java.lang.String} object.
     */
    public void setExpectedClose(String closeRequest) {
        m_lineConversation.setExpectedClose(closeRequest);
    }
    
    /**
     * <p>setExpectedClose</p>
     *
     * @param closeRequest a {@link java.lang.String} object.
     * @param closeResponse a {@link java.lang.String} object.
     */
    public void setExpectedClose(String closeRequest, String closeResponse) {
        m_lineConversation.setExpectedClose(closeRequest, closeResponse);
    }
    
    /**
     * <p>getServerHandler</p>
     *
     * @return a {@link org.apache.mina.core.service.IoHandler} object.
     */
    public IoHandler getServerHandler() {
        return m_ioHandler != null ? m_ioHandler : new SimpleServerHandler(m_lineConversation);
    }
    
    /**
     * <p>setServerHandler</p>
     *
     * @param handler a {@link org.apache.mina.core.service.IoHandler} object.
     */
    public void setServerHandler(IoHandler handler) {
        m_ioHandler = handler;
    }
    
    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>setBufferSize</p>
     *
     * @param bufferSize a int.
     */
    public void setBufferSize(int bufferSize) {
        m_bufferSize = bufferSize;
    }

    /**
     * <p>getBufferSize</p>
     *
     * @return a int.
     */
    public int getBufferSize() {
        return m_bufferSize;
    }

    /**
     * <p>setIdleTime</p>
     *
     * @param idleTime a int.
     */
    public void setIdleTime(int idleTime) {
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
}
