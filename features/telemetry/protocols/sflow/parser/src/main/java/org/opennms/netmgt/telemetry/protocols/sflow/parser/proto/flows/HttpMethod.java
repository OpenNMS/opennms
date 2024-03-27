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

// enum http_method {
//   OTHER    = 0,
//   OPTIONS  = 1,
//   GET      = 2,
//   HEAD     = 3,
//   POST     = 4,
//   PUT      = 5,
//   DELETE   = 6,
//   TRACE    = 7,
//   CONNECT  = 8
// };

public enum HttpMethod {
    OTHER(0),
    OPTIONS(1),
    GET(2),
    HEAD(3),
    POST(4),
    PUT(5),
    DELETE(6),
    TRACE(7),
    CONNECT(8);

    public final int value;

    HttpMethod(final int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", this.value)
                .toString();
    }

    public static HttpMethod from(final ByteBuf buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return OTHER;
            case 1:
                return OPTIONS;
            case 2:
                return GET;
            case 3:
                return HEAD;
            case 4:
                return POST;
            case 5:
                return PUT;
            case 6:
                return DELETE;
            case 7:
                return TRACE;
            case 8:
                return CONNECT;
            default:
                throw new InvalidPacketException(buffer, "Unknown value: {}", value);
        }
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeInt32(this.value);
    }
}
