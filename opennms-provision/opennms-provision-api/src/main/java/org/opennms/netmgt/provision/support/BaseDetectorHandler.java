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
package org.opennms.netmgt.provision.support;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>BaseDetectorHandler class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class BaseDetectorHandler<Request, Response> extends IoHandlerAdapter {
    
    private static final Logger LOG = LoggerFactory.getLogger(BaseDetectorHandler.class);
    
    private DetectFutureMinaImpl m_future;
    private AsyncClientConversation<Request, Response> m_conversation;
    

    /**
     * <p>setFuture</p>
     *
     * @param future a {@link org.opennms.netmgt.provision.DetectFuture} object.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     */
    public void setFuture(DetectFutureMinaImpl future) {
        m_future = future;
    }

    /**
     * <p>getFuture</p>
     *
     * @return a {@link org.opennms.netmgt.provision.DetectFuture} object.
     */
    public DetectFutureMinaImpl getFuture() {
        return m_future;
    }
    
    /** {@inheritDoc} */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        
    }

    /** {@inheritDoc} */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        if(!getConversation().hasBanner() && getConversation().getRequest() != null) {
            Object request = getConversation().getRequest();
            session.write(request);
       }
    }

    /** {@inheritDoc} */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        if(!getFuture().isDone()) {
            LOG.info("Session closed and detection is not complete. Setting service detection to false.");
            getFuture().setServiceDetected(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        // @see http://issues.opennms.org/browse/NMS-5311
        if (getConversation().hasBanner() && status == IdleStatus.READER_IDLE) {
            LOG.info("Session went idle without receiving banner. Setting service detection to false.");
            getFuture().setServiceDetected(false);
            session.close(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOG.debug("Caught a Throwable in BaseDetectorHandler", cause);
        getFuture().setException(cause);
        session.close(true);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void messageReceived(IoSession session, Object message) throws Exception {
        try {
            LOG.debug("Client Receiving: {}", message.toString().trim());
            
            if(getConversation().hasExchanges() && getConversation().validate((Response)message)) {
               
               Object request = getConversation().getRequest();
               
                if(request != null) {
                   session.write(request);
               }else if(request == null && getConversation().isComplete()){
                   LOG.info("Conversation is complete and there are no more pending requests. Setting service detection to true.");
                   getFuture().setServiceDetected(true);
                   session.close(false);
               }else {
                   LOG.info("Conversation is incomplete. Setting service detection to false.");
                   getFuture().setServiceDetected(false);
                   session.close(false);
               }
            }else {
                LOG.info("Conversation response was invalid. Setting service detection to false.");
                getFuture().setServiceDetected(false);
                session.close(false);
            }
            
        }catch(Throwable e){
              if(!session.isClosing()){
                  session.close(true);
              }
        }
        
    }

    /**
     * <p>setConversation</p>
     *
     * @param conversation a {@link org.opennms.netmgt.provision.support.AsyncClientConversation} object.
     */
    public void setConversation(AsyncClientConversation<Request, Response> conversation) {
        m_conversation = conversation;        
    }
    
    /**
     * <p>getConversation</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation} object.
     */
    public AsyncClientConversation<Request, Response> getConversation() {
        return m_conversation;
    }
    
}
