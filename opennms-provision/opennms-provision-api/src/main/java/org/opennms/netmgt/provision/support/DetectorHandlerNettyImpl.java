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

import java.nio.channels.Channels;

import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.ChannelInboundHandlerAdapter;
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
public class DetectorHandlerNettyImpl<Request, Response> extends ChannelInboundHandlerAdapter {
    
    private static final Logger LOG = LoggerFactory.getLogger(DetectorHandlerNettyImpl.class);
    
    private AsyncClientConversation<Request, Response> m_conversation;

    /** {@inheritDoc} */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("channelActive()");
        if(!getConversation().hasBanner() && getConversation().getRequest() != null) {
            Object request = getConversation().getRequest();
            ctx.channel().write(request);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("channelInactive()");
    }

    /** {@inheritDoc} */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.debug("Caught a Throwable in {}", this.getClass().getName(), cause);
        //getFuture().setException(event.getCause());
        // Make sure that the channel is closed
        ctx.channel().close();
        // P
        ctx.fireChannelRead(cause);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        try {
            final AsyncClientConversation<Request, Response> conversation = getConversation();
            LOG.debug("Client Receiving: {}", message.toString().trim());
            LOG.debug("Conversation: {}", conversation);

            if(conversation.hasExchanges() && conversation.validate((Response)message)) {

                Object request = conversation.getRequest();

                if (request != null) {
                    LOG.debug("Writing request: {}", request);
                    ctx.channel().write(request);
                } else if (request == null && conversation.isComplete()) {
                    LOG.debug("Closing channel: {}", conversation);
                    ctx.channel().close();
                } else {
                    LOG.debug("Closing channel, detection failed: {}", conversation);
                    ctx.channel().close();
                    ctx.fireExceptionCaught(new ServiceDetectionFailedException());
                }
            } else {
                LOG.debug("Invalid response: {}", message.toString().trim());
                ctx.channel().close();
                ctx.fireExceptionCaught(new ServiceDetectionFailedException());
            }
        } catch(Throwable e) {
            LOG.debug("Exception caught!", e);
            ctx.channel().close();
            ctx.fireExceptionCaught(e);
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
