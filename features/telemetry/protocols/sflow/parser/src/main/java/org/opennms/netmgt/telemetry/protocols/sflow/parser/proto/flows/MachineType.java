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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// enum machine_type {
//    unknown = 0,
//    other   = 1,
//    x86     = 2,
//    x86_64  = 3,
//    ia64    = 4,
//    sparc   = 5,
//    alpha   = 6,
//    powerpc = 7,
//    m68k    = 8,
//    mips    = 9,
//    arm     = 10,
//    hppa    = 11,
//    s390    = 12
// };

public enum MachineType {
    unknown(0),
    other(1),
    x86(2),
    x86_64(3),
    ia64(4),
    sparc(5),
    alpha(6),
    powerpc(7),
    m68k(8),
    mips(9),
    arm(10),
    hppa(11),
    s390(12);

    public final int value;

    MachineType(final int value) {
        this.value = value;
    }

    public static MachineType from(final ByteBuf buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 0:
                return unknown;
            case 1:
                return other;
            case 2:
                return x86;
            case 3:
                return x86_64;
            case 4:
                return ia64;
            case 5:
                return sparc;
            case 6:
                return alpha;
            case 7:
                return powerpc;
            case 8:
                return m68k;
            case 9:
                return mips;
            case 10:
                return arm;
            case 11:
                return hppa;
            case 12:
                return s390;
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

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeInt32(this.value);
    }
}
