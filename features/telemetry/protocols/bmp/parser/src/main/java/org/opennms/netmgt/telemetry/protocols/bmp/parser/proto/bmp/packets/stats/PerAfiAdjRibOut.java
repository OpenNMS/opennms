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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint64;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;

public class PerAfiAdjRibOut implements Metric {
    public final int afi;            // uint16
    public final int safi;           // uint8
    public final UnsignedLong gauge; // uint64

    public PerAfiAdjRibOut(final ByteBuf buffer) {
        this.afi = uint16(buffer);
        this.safi = uint8(buffer);
        this.gauge = uint64(buffer);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("afi", this.afi)
                .add("safi", this.safi)
                .add("gauge", this.gauge)
                .toString();
    }
}
