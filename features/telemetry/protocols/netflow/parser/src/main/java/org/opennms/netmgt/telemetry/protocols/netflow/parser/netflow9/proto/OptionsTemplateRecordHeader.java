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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public final class OptionsTemplateRecordHeader {

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |         Template ID           |      Option Scope Length      |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |        Option Length          |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public static final int SIZE = 6;

    public final int templateId; // uint16
    public final int optionScopeLength; // uint16
    public final int optionLength; // uint16

    public OptionsTemplateRecordHeader(final ByteBuf buffer) throws InvalidPacketException {
        this.templateId = uint16(buffer);
        if (this.templateId <= 255) {
            // Since Template IDs are used as Set IDs in the Sets they describe
            throw new InvalidPacketException(buffer, "Invalid template ID: %d", this.templateId);
        }

        this.optionScopeLength = uint16(buffer);
        this.optionLength = uint16(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("templateId", templateId)
                .add("optionScopeLength", optionScopeLength)
                .add("optionLength", optionLength)
                .toString();
    }
}
