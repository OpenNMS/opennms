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
package org.opennms.features.eifadapter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private final Charset charset = StandardCharsets.UTF_8;

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
