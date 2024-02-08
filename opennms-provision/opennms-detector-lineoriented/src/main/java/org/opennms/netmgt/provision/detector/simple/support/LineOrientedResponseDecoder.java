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
package org.opennms.netmgt.provision.detector.simple.support;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;

/**
 * <p>LineOrientedResponseDecoder class.</p>
 *
 * @author Seth
 */
public class LineOrientedResponseDecoder extends OneToOneDecoder {

    /**
     * This method decodes {@link String} objects into {@link LineOrientedResponse} instances
     * that contain the byte representation of the response.
     */
    @Override
    public Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
        return new LineOrientedResponse((String)msg);
    }
}
