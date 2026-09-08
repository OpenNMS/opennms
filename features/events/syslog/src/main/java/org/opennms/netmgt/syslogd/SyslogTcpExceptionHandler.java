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
package org.opennms.netmgt.syslogd;

import java.net.InetSocketAddress;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Closes a syslog TCP connection whose stream can no longer be decoded.
 *
 * A framing error leaves the position of every later message unknown, so skipping bytes
 * cannot recover it; dropping the connection lets the sender resynchronise. Logs at most
 * once per connection, since a sender that reconnects in a loop would fill the log.
 */
public class SyslogTcpExceptionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogTcpExceptionHandler.class);

    private final InetSocketAddress source;

    private boolean logged;

    public SyslogTcpExceptionHandler(final InetSocketAddress source) {
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (!logged) {
            logged = true;
            LOG.warn("Closing syslog TCP connection from {}: {}", source, cause.getMessage());
            LOG.debug("Syslog TCP connection from {} failed", source, cause);
        }
        ctx.close();
    }
}
