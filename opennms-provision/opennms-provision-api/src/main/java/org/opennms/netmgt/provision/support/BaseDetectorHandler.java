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

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.opennms.netmgt.provision.DetectFuture;

/**
 * @author Donald Desloge
 *
 */
public class BaseDetectorHandler<Request> extends IoHandlerAdapter {
    
    private DetectFuture m_future;
    private AsyncClientConversation<Request> m_conversation;
    

    public void setFuture(DetectFuture future) {
        m_future = future;
    }

    public DetectFuture getFuture() {
        return m_future;
    }
    
    public void sessionCreated(IoSession session) throws Exception {
    }

    public void sessionOpened(IoSession session) throws Exception {
       if(!m_conversation.hasBannerValidator()) {
           session.write(m_conversation.getRequest());
       }
    }

    public void sessionClosed(IoSession session) throws Exception {
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
    }

    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        
        if(m_conversation.hasExchanges() && !m_conversation.isComplete()) {
           Object request = m_conversation.validate(message);
            if(request != null) {
               session.write(request);
           }else {
               getFuture().setServiceDetected(false);
               session.close();
           }
        }else {
            getFuture().setServiceDetected(true);
            session.close();
        }
        
    }

    public void messageSent(IoSession session, Object message) throws Exception {}

    /**
     * @param conversation
     */
    public void setConversation(AsyncClientConversation<Request> conversation) {
        m_conversation = conversation;        
    }
    
}
