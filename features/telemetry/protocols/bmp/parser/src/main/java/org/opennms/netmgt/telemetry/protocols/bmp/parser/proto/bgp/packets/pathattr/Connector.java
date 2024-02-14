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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

/**
 *            0                   1
 *            0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
 *           +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *           |0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1|
 *           +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *           |                               |
 *           |  IPv4 Address of PE           |
 *           |                               |
 *           +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class Connector implements Attribute {
    public final int connector; // uint16

    public Connector(final ByteBuf buffer, final PeerFlags flags) {
        this.connector = uint16(buffer);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("connector", this.connector)
                .toString();
    }
}
