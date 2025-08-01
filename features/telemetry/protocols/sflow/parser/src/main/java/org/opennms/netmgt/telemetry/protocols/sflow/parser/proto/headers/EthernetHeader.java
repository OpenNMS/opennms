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
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import io.netty.buffer.ByteBuf;

public class EthernetHeader {

    public final Integer vlan;

    public final Inet4Header inet4Header;
    public final Inet6Header inet6Header;

    public final byte[] rawHeader;

    public EthernetHeader(final ByteBuf buffer) throws InvalidPacketException {
        BufferUtils.skip(buffer, 6); // dstMAC
        BufferUtils.skip(buffer, 6); // srcMAC

        int type = BufferUtils.uint16(buffer);
        if (type == 0x8100) {
            // 802.1Q (VLAN-Tagging)
            this.vlan = BufferUtils.uint16(buffer) & 0x0fff;
            type = BufferUtils.uint16(buffer);
        } else {
            this.vlan = null;
        }

        switch (type) {
            case 0x0800: // IPv4
                this.inet4Header = new Inet4Header(buffer);
                this.inet6Header = null;
                this.rawHeader = null;
                break;

            case 0x86DD: // IPv6
                this.inet4Header = null;
                this.inet6Header = new Inet6Header(buffer);
                this.rawHeader = null;
                break;

            default:
                this.inet4Header = null;
                this.inet6Header = null;
                this.rawHeader = BufferUtils.bytes(buffer, buffer.readableBytes());
        }
    }

    public EthernetHeader(final Integer vlan, final Inet4Header inet4Header, final Inet6Header inet6Header, final byte[] rawHeader) {
        this.vlan = vlan;
        this.inet4Header = inet4Header;
        this.inet6Header = inet6Header;
        this.rawHeader = rawHeader;
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        if (this.vlan != null) {
            bsonWriter.writeInt64("vlan", this.vlan);
        }

        bsonWriter.writeEndDocument();
    }
}
