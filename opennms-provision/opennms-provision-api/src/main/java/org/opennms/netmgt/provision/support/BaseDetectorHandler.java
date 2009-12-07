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
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.DetectFuture;

/**
 * @author Donald Desloge
 *
 */
public class BaseDetectorHandler<Request, Response> extends IoHandlerAdapter {
    
    private DetectFuture m_future;
    private AsyncClientConversation<Request, Response> m_conversation;
    

    public void setFuture(DetectFuture future) {
        m_future = future;
    }

    public DetectFuture getFuture() {
        return m_future;
    }
    
    public void sessionCreated(IoSession session) throws Exception {
        
    }

    public void sessionOpened(IoSession session) throws Exception {
        if(!m_conversation.hasBanner()) {
            Object request = m_conversation.getRequest();
            session.write(request);
       }
    }

    public void sessionClosed(IoSession session) throws Exception {
        if(!getFuture().isDone()) {
            getFuture().setServiceDetected(false);
        }
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        getFuture().setServiceDetected(false);
        session.close(false);
    }

    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LogUtils.debugf(this, cause, "Caught a Throwable in BaseDetectorHandler");
        getFuture().setException(cause);
        session.close(true);
    }

    @SuppressWarnings("unchecked")
    public void messageReceived(IoSession session, Object message) throws Exception {
        try {    
        System.err.printf("Client Receiving: %s\n", message.toString().trim());
            
            if(m_conversation.hasExchanges() && m_conversation.validate((Response)message)) {
               
               Object request = m_conversation.getRequest();
               
                if(request != null) {
                   session.write(request);
               }else if(request == null && m_conversation.isComplete()){
                   getFuture().setServiceDetected(true);
                   session.close(false);
               }else {
                   
                   getFuture().setServiceDetected(false);
                   session.close(false);
               }
            }else {
                getFuture().setServiceDetected(false);
                session.close(false);
            }
            
        }catch(Exception e){
              if(!session.isClosing()){
                  session.close(true);
              }
        }
        
    }

    public void messageSent(IoSession session, Object message) throws Exception {}

    /**
     * @param conversation
     */
    public void setConversation(AsyncClientConversation<Request, Response> conversation) {
        m_conversation = conversation;        
    }
    
    public AsyncClientConversation<Request, Response> getConversation() {
        return m_conversation;
    }
    
}
