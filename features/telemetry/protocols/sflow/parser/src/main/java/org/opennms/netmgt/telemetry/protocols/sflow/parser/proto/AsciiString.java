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
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto;

import java.nio.charset.StandardCharsets;

import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class AsciiString {
    public final int length;
    public final String value;

    public AsciiString(final ByteBuf buffer) throws InvalidPacketException {
        this.length = (int) BufferUtils.uint32(buffer);
        this.value = new String(BufferUtils.bytes(buffer, this.length), StandardCharsets.US_ASCII);

        // Skip over optional padding
        BufferUtils.skip(buffer, (4 - (this.length % 4)) % 4);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("length", this.length)
                .add("value", this.value)
                .toString();
    }
}
