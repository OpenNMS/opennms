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

// enum os_name {
//    unknown   = 0,
//    other     = 1,
//    linux     = 2,
//    windows   = 3,
//    darwin    = 4,
//    hpux      = 5,
//    aix       = 6,
//    dragonfly = 7,
//    freebsd   = 8,
//    netbsd    = 9,
//    openbsd   = 10,
//    osf       = 11,
//    solaris   = 12
// };

public enum OsName {
    unknown(0),
    other(1),
    linux(2),
    windows(3),
    darwin(4),
    hpux(5),
    aix(6),
    dragonfly(7),
    freebsd(8),
    netbsd(9),
    openbsd(10),
    osf(11),
    solaris(12);

    public final int value;

    OsName(final int value) {
        this.value = value;
    }

    public static OsName from(final ByteBuf buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return unknown;
            case 1:
                return other;
            case 2:
                return linux;
            case 3:
                return windows;
            case 4:
                return darwin;
            case 5:
                return hpux;
            case 6:
                return aix;
            case 7:
                return dragonfly;
            case 8:
                return freebsd;
            case 9:
                return netbsd;
            case 10:
                return openbsd;
            case 11:
                return osf;
            case 12:
                return solaris;
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
