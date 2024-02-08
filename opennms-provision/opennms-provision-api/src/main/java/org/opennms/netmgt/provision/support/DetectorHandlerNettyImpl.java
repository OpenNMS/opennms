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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
//import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.support.DetectFutureNettyImpl.ServiceDetectionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DetectorHandlerNettyImpl class.</p>
 *
 * CAUTION: This class is unused. This implementation has never been in production.
 *
 * @author Seth
 */
public class DetectorHandlerNettyImpl<Request, Response> extends SimpleChannelHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(DetectorHandlerNettyImpl.class);
    
    //private DetectFuture m_future;
    private AsyncClientConversation<Request, Response> m_conversation;

    /**
     * <p>setFuture</p>
     *
     * @param future a {@link org.opennms.netmgt.provision.DetectFuture} object.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     */
    /*
    public void setFuture(DetectFuture future) {
        m_future = future;
    }

    /**
     * <p>getFuture</p>
     *
     * @return a {@link org.opennms.netmgt.provision.DetectFuture} object.
     * /
    public DetectFuture getFuture() {
        return m_future;
    }
    */

    /** {@inheritDoc} */
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
        LOG.debug("channelOpen()");
        if(!getConversation().hasBanner() && getConversation().getRequest() != null) {
            Object request = getConversation().getRequest();
            ctx.getChannel().write(request);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
        LOG.debug("channelClosed()");
        /*
        if(!getFuture().isDone()) {
            getFuture().setServiceDetected(false);
        }
        */
    }

    /*
    I'm not sure how to create a Netty equivalent to this...

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        if(getConversation().hasBanner() && status == IdleStatus.READER_IDLE) {
            getFuture().setServiceDetected(false);
            session.close(true);
        }
    }
     */

    /** {@inheritDoc} */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) throws Exception {
        LOG.debug("Caught a Throwable in {}", this.getClass().getName(), event.getCause());
        //getFuture().setException(event.getCause());
        // Make sure that the channel is closed
        ctx.getChannel().close();
        // P
        ctx.sendUpstream(event);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent message) {
        try {
            final AsyncClientConversation<Request, Response> conversation = getConversation();
            LOG.debug("Client Receiving: {}", message.getMessage().toString().trim());
            LOG.debug("Conversation: {}", conversation);

            if(conversation.hasExchanges() && conversation.validate((Response)message.getMessage())) {

                Object request = conversation.getRequest();

                if (request != null) {
                    LOG.debug("Writing request: {}", request);
                    ctx.getChannel().write(request);
                } else if (request == null && conversation.isComplete()) {
                    LOG.debug("Closing channel: {}", conversation);
                    //getFuture().setServiceDetected(true);
                    ctx.getChannel().close();
                } else {
                    //getFuture().setServiceDetected(false);
                    LOG.debug("Closing channel, detection failed: {}", conversation);
                    ctx.getChannel().close();
                    Channels.fireExceptionCaught(ctx, new ServiceDetectionFailedException());
                }
            } else {
                LOG.debug("Invalid response: {}", message.getMessage().toString().trim());
                //getFuture().setServiceDetected(false);
                ctx.getChannel().close();
                Channels.fireExceptionCaught(ctx, new ServiceDetectionFailedException());
            }
        } catch(Throwable e) {
            LOG.debug("Exception caught!", e);
            ctx.getChannel().close();
            Channels.fireExceptionCaught(ctx, e);
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
