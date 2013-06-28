/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
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
