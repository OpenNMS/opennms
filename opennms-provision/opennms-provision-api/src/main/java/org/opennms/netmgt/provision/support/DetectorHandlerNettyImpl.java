/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        LOG.debug("channelOpen()");
        if(!getConversation().hasBanner() && getConversation().getRequest() != null) {
            Object request = getConversation().getRequest();
            ctx.channel().write(request);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("channelClosed()");
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
