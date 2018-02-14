/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.eifadapter;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.camel.component.netty4.ChannelHandlerFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.dao.api.NodeDao;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class EifMessageDecoder extends MessageToMessageDecoder<ByteBuf> implements ChannelHandlerFactory {

    private final StringBuilder buffer = new StringBuilder();
    private final Charset charset = Charset.defaultCharset();

    private NodeDao nodeDao;

    @Override
    public ChannelHandler newChannelHandler() {
        return new EifMessageDecoder();
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        buffer.append(msg.toString(charset));

        if ( buffer.toString().contains("<START>>") && buffer.toString().contains(";END") ) {
            int eifStart = buffer.indexOf("<START>>");
            int eifEnd = buffer.lastIndexOf(";END");
            final StringBuilder eif = new StringBuilder(buffer.substring(eifStart,eifEnd+4));
            buffer.delete(eifStart,eifEnd+4);
            List<Event> e = EifParser.translateEifToOpenNMS(nodeDao, eif);
            if (e != null) {
                Log eifEvents = new Log();
                e.forEach(event -> eifEvents.addEvent(event));
                out.add(eifEvents);
            }
        }
    }

}
