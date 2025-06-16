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

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public final class FlowSetHeader {

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |          Set ID               |          Length               |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public static final int TEMPLATE_SET_ID = 2;
    public static final int OPTIONS_TEMPLATE_SET_ID = 3;

    public enum Type {
        TEMPLATE_SET,
        OPTIONS_TEMPLATE_SET,
        DATA_SET
    }

    public static final int SIZE = 4;

    public final int setId; // uint16
    public final int length; // uint16

    public FlowSetHeader(final ByteBuf buffer) throws InvalidPacketException {
        this.setId = uint16(buffer);
        if (this.setId < 256 && this.setId != TEMPLATE_SET_ID && this.setId != OPTIONS_TEMPLATE_SET_ID) {
            // The Set ID values of 0 and 1 are not used, for historical reasons [RFC3954], values from 4 to 255 are
            // reserved for future use.
            throw new InvalidPacketException(buffer, "Invalid set ID: %d", this.setId);
        }

        this.length = uint16(buffer);
    }

    public Type getType() {
        if (this.setId == TEMPLATE_SET_ID) return Type.TEMPLATE_SET;
        if (this.setId == OPTIONS_TEMPLATE_SET_ID) return Type.OPTIONS_TEMPLATE_SET;
        if (this.setId >= 256) return Type.DATA_SET;

        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("setId", setId)
                .add("length", length)
                .toString();
    }
}
