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

import io.netty.channel.ChannelHandlerContext;

/**
 * CAUTION: This class is unused. This implementation has never been in production.
 */
public class TcpDetectorHandlerNettyImpl<Request,Response> extends DetectorHandlerNettyImpl<Request,Response> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // If the conversation has no banner and no request, then we are just checking to make
        // sure that the port is open. Close the channel.
        if(!getConversation().hasBanner() && getConversation().getRequest() == null) {
           ctx.channel().close();
       } else {
           // Otherwise, default to the normal DetectorHandler conversation behavior
           super.channelActive(ctx);
       }
    }
}
