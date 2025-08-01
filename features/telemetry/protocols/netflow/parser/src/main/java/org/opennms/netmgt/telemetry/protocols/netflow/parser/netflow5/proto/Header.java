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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;

public final class Header {

    public static final int SIZE = 24;

    public static final int VERSION = 0x0005;

    public final int versionNumber; // uint16 - must be 0x0005
    public final int count; // uint16
    public final long sysUptime; // uint32
    public final long unixSecs; // uint32
    public final long unixNSecs; // uint32
    public final long flowSequence; // uint32
    public final int engineType; // uint8
    public final int engineId; // uint8
    public final int samplingAlgorithm;
    public final int samplingInterval;

    public Header(final ByteBuf buffer) throws InvalidPacketException {
        this.versionNumber = uint16(buffer);
        if (this.versionNumber != VERSION) {
            throw new InvalidPacketException(buffer, "Invalid version number: 0x%04X", this.versionNumber);
        }

        this.count = uint16(buffer);
        if (this.count < 1 || this.count > 30) {
            throw new InvalidPacketException(buffer, "Invalid record count: %d", this.count);
        }

        this.sysUptime = uint32(buffer);
        this.unixSecs = uint32(buffer);
        this.unixNSecs = uint32(buffer);
        this.flowSequence = uint32(buffer);
        this.engineType = uint8(buffer);
        this.engineId = uint8(buffer);

        final int sampling = uint16(buffer);
        this.samplingAlgorithm = sampling >>> 14;
        this.samplingInterval = sampling & ((2 << 13) - 1);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("versionNumber", this.versionNumber)
                .add("count", this.count)
                .add("sysUptime", this.sysUptime)
                .add("unixSecs", this.unixSecs)
                .add("unixNSecs", this.unixNSecs)
                .add("flowSequence", this.flowSequence)
                .add("engineType", this.engineType)
                .add("engineId", this.engineId)
                .add("samplingAlgorithm", this.samplingAlgorithm)
                .add("samplingInterval", this.samplingInterval)
                .toString();
    }

    public Iterable<Value<?>> asValues() {
        return ImmutableList.<Value<?>>builder()
                .add(new UnsignedValue("@versionNumber", this.versionNumber))
                .add(new UnsignedValue("@count", this.count))
                .add(new UnsignedValue("@sysUptime", this.sysUptime))
                .add(new UnsignedValue("@unixSecs", this.unixSecs))
                .add(new UnsignedValue("@unixNSecs", this.unixNSecs))
                .add(new UnsignedValue("@flowSequence", this.flowSequence))
                .add(new UnsignedValue("@engineType", this.engineType))
                .add(new UnsignedValue("@engineId", this.engineId))
                .add(new UnsignedValue("@samplingAlgorithm", this.samplingAlgorithm))
                .add(new UnsignedValue("@samplingInterval", this.samplingInterval))
                .build();
    }
}
