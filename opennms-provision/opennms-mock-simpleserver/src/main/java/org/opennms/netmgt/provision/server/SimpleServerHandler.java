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

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.opennms.netmgt.provision.server.exchange.LineConversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SimpleServerHandler class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class SimpleServerHandler extends IoHandlerAdapter {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleServerHandler.class);
    
    private LineConversation m_conversation;
    
    /**
     * <p>Constructor for SimpleServerHandler.</p>
     *
     * @param conversation a {@link org.opennms.netmgt.provision.server.exchange.LineConversation} object.
     */
    public SimpleServerHandler(LineConversation conversation) {
        m_conversation = conversation;
    }
    
    /** {@inheritDoc} */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOG.warn("An error was caught in session {}", session, cause);
    }
    
    /** {@inheritDoc} */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        LOG.info("Session opened");
        if(m_conversation != null && m_conversation.hasBanner()) {
            LOG.info("Sending Banner: {} \n", m_conversation.getBanner());
            session.write(m_conversation.getBanner());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void messageReceived (IoSession session, Object message) throws Exception {
        LOG.info("Server received: {}\n", message.toString().trim());
        String str = message.toString();
        if(str.trim().equalsIgnoreCase(m_conversation.getExpectedClose())) {
            if(m_conversation.getExpectedCloseResponse() != null) {
                session.write(m_conversation.getExpectedCloseResponse());
            }
            if (!session.close(false).await(500)) { 
                LOG.warn("Conversation did not complete promptly in 500ms");
            }
            return;
        }
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        LOG.info("IDLE {}", session.getIdleCount(status));
    }

}
