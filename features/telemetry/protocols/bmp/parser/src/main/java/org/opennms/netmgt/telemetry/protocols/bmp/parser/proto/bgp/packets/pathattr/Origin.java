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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.util.function.Function;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class Origin implements Attribute {
    public final Value value;

    public Origin(final ByteBuf buffer, final PeerFlags flags) {
        this.value = Value.from(uint8(buffer));
    }

    public enum Value {
        IGP,
        EGP,
        INCOMPLETE,
        UNKNOWN;

        private static Value from(final int code) {
            switch (code) {
                case 0: return IGP;
                case 1: return EGP;
                case 2: return INCOMPLETE;
                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Originator Code: {}", code);
                    return UNKNOWN;
            }
        }

        public <R> R map(final Function<Value, R> mapper) {
            return mapper.apply(this);
        }
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", this.value)
                .toString();
    }
}
