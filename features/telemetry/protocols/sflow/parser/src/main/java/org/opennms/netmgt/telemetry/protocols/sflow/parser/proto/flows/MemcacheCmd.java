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
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// enum memcache_cmd {
//   OTHER    = 0,
//   SET      = 1,
//   ADD      = 2,
//   REPLACE  = 3,
//   APPEND   = 4,
//   PREPEND  = 5,
//   CAS      = 6,
//   GET      = 7,
//   GETS     = 8,
//   INCR     = 9,
//   DECR     = 10,
//   DELETE   = 11,
//   STATS    = 12,
//   FLUSH    = 13,
//   VERSION  = 14,
//   QUIT     = 15,
//   TOUCH    = 16
// };

public enum MemcacheCmd {
    OTHER(0),
    SET(1),
    ADD(2),
    REPLACE(3),
    APPEND(4),
    PREPEND(5),
    CAS(6),
    GET(7),
    GETS(8),
    INCR(9),
    DECR(10),
    DELETE(11),
    STATS(12),
    FLUSH(13),
    VERSION(14),
    QUIT(15),
    TOUCH(16);

    public final int value;

    MemcacheCmd(final int value) {
        this.value = value;
    }

    public static MemcacheCmd from(final ByteBuf buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return OTHER;
            case 1:
                return SET;
            case 2:
                return ADD;
            case 3:
                return REPLACE;
            case 4:
                return APPEND;
            case 5:
                return PREPEND;
            case 6:
                return CAS;
            case 7:
                return GET;
            case 8:
                return GETS;
            case 9:
                return INCR;
            case 10:
                return DECR;
            case 11:
                return DELETE;
            case 12:
                return STATS;
            case 13:
                return FLUSH;
            case 14:
                return VERSION;
            case 15:
                return QUIT;
            case 16:
                return TOUCH;
            default:
                throw new InvalidPacketException(buffer, "Unknown value: {}", value);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", this.value)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeInt32(this.value);
    }
}
