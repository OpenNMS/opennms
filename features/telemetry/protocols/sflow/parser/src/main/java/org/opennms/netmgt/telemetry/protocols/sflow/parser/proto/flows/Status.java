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

// enum status {
//   SUCCESS         = 0,
//   OTHER           = 1,
//   TIMEOUT         = 2,
//   INTERNAL_ERROR  = 3,
//   BAD_REQUEST     = 4,
//   FORBIDDEN       = 5,
//   TOO_LARGE       = 6,
//   NOT_IMPLEMENTED = 7,
//   NOT_FOUND       = 8,
//   UNAVAILABLE     = 9,
//   UNAUTHORIZED    = 10
// };

public enum Status {
    SUCCESS(0),
    OTHER(1),
    TIMEOUT(2),
    INTERNAL_ERROR(3),
    BAD_REQUEST(4),
    FORBIDDEN(5),
    TOO_LARGE(6),
    NOT_IMPLEMENTED(7),
    NOT_FOUND(8),
    UNAVAILABLE(9),
    UNAUTHORIZED(10);

    public final int value;

    Status(final int value) {
        this.value = value;
    }

    public static Status from(final ByteBuf buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return SUCCESS;
            case 1:
                return OTHER;
            case 2:
                return TIMEOUT;
            case 3:
                return INTERNAL_ERROR;
            case 4:
                return BAD_REQUEST;
            case 5:
                return FORBIDDEN;
            case 6:
                return TOO_LARGE;
            case 7:
                return NOT_IMPLEMENTED;
            case 8:
                return NOT_FOUND;
            case 9:
                return UNAVAILABLE;
            case 10:
                return UNAUTHORIZED;
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
