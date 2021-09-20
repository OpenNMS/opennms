/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

/**
 * CAUTION: This class is unused. This implementation has never been in production.
 */
public class TcpDetectorHandlerNettyImpl<Request,Response> extends DetectorHandlerNettyImpl<Request,Response> {
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
        // If the conversation has no banner and no request, then we are just checking to make
        // sure that the port is open. Close the channel.
        if(!getConversation().hasBanner() && getConversation().getRequest() == null) {
           ctx.getChannel().close();
       } else {
           // Otherwise, default to the normal DetectorHandler conversation behavior
           super.channelOpen(ctx, event);
       }
    }
}
