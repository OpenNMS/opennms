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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public final class Header {

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |       Version Number          |            Length             |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                           Export Time                         |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                       Sequence Number                         |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                    Observation Domain ID                      |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public static final int SIZE = 16;

    public static final int VERSION = 0x000a;

    public final int versionNumber; // uint16 - must be 0x000a
    public final int length; // uint16
    public final long exportTime; // uint32
    public final long sequenceNumber; // uint32
    public final long observationDomainId; // uint32

    public Header(final ByteBuf buffer) throws InvalidPacketException {
        this.versionNumber = uint16(buffer);
        if (this.versionNumber != VERSION) {
            throw new InvalidPacketException(buffer, "Invalid version number: 0x%04X", this.versionNumber);
        }

        this.length = uint16(buffer);
        if (this.length <= 0) {
            throw new InvalidPacketException(buffer, "Empty packet");
        }

        this.exportTime = uint32(buffer);
        this.sequenceNumber = uint32(buffer);
        this.observationDomainId = uint32(buffer);
    }

    public int payloadLength() {
        return this.length - Header.SIZE;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("versionNumber", versionNumber)
                .add("length", length)
                .add("exportTime", exportTime)
                .add("sequenceNumber", sequenceNumber)
                .add("observationDomainId", observationDomainId)
                .toString();
    }
}
