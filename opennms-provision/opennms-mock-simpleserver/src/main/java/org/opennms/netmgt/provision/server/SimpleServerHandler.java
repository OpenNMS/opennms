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

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.server.exchange.LineConversation;

/**
 * @author thedesloge
 *
 */
public class SimpleServerHandler extends IoHandlerAdapter {
    
    private LineConversation m_conversation;
    
    public SimpleServerHandler(LineConversation conversation) {
        m_conversation = conversation;
    }
    
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
    
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        LogUtils.infof(this, "Session opened");
        if(m_conversation != null && m_conversation.hasBanner()) {
            LogUtils.infof(this, "Sending Banner: %s \n", m_conversation.getBanner());
            session.write(m_conversation.getBanner());
        }
    }
    
    @Override
    public void messageReceived (IoSession session, Object message) throws Exception {
        LogUtils.infof(this, "Server received: %s\n", message.toString().trim());
        String str = message.toString();
        if(str.trim().equalsIgnoreCase(m_conversation.getExpectedClose())) {
            if(m_conversation.getExpectedCloseResponse() != null) {
                session.write(m_conversation.getExpectedCloseResponse());
            }
            session.close(false);
            return;
        }
        
    }
    
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        LogUtils.infof(this, "IDLE " + session.getIdleCount(status));
    }

}
