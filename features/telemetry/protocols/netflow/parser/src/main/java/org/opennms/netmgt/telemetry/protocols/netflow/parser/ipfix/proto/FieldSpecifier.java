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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;

import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.MissingTemplateException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElement;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Field;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Scope;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public final class FieldSpecifier implements Field, Scope {
    private static final Logger LOG = LoggerFactory.getLogger(FieldSpecifier.class);

    /*
     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |E|  Information Element ident. |        Field Length           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                      Enterprise Number                        |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public final int informationElementId; // uint16 { enterprise_bit:1, element_id: 15 }
    public final int fieldLength; // uint16

    public final Optional<Long> enterpriseNumber; // uint32

    public final InformationElement informationElement;

    final InformationElementDatabase informationElementDatabase;

    public FieldSpecifier(final InformationElementDatabase informationElementDatabase, final ByteBuf buffer) throws InvalidPacketException {
        final int elementId = uint16(buffer);

        this.informationElementDatabase = informationElementDatabase;
        this.informationElementId = elementId & 0x7FFF;
        this.fieldLength = uint16(buffer);

        if ((elementId & 0x8000) == 0) {
            this.enterpriseNumber = Optional.empty();
        } else {
            long enterpriseNumber = uint32(buffer);
            this.enterpriseNumber = Optional.of(enterpriseNumber);
        }

        this.informationElement = informationElementDatabase
                .lookup(Protocol.IPFIX, this.enterpriseNumber, this.informationElementId).orElseGet(() -> {
                    LOG.warn("Undeclared information element: {}", UndeclaredValue.nameFor(this.enterpriseNumber, this.informationElementId));
                    return UndeclaredValue.parser(this.enterpriseNumber, this.informationElementId);
                });

        if (this.fieldLength > this.informationElement.getMaximumFieldLength() || this.fieldLength < this.informationElement.getMinimumFieldLength()) {
            throw new InvalidPacketException(buffer, "Template field '%s' has illegal size: %d (min=%d, max=%d)",
                    this.informationElement.getName(),
                    this.fieldLength,
                    this.informationElement.getMinimumFieldLength(),
                    this.informationElement.getMaximumFieldLength());
        }
    }

    @Override
    public Value<?> parse(final Session.Resolver resolver, final ByteBuf buffer) throws InvalidPacketException, MissingTemplateException {
        try {
            return this.informationElement.parse(informationElementDatabase, resolver, buffer);
        } catch (final InvalidPacketException e) {
            throw new InvalidPacketException(e, "Failed to parse IPFix information element: enterpriseNumber=%s informationElementId=%d", this.enterpriseNumber, this.informationElementId);
        }
    }

    @Override
    public int length() {
        return this.fieldLength;
    }

    @Override
    public String getName() {
        return this.informationElement.getName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("informationElementId", informationElementId)
                .add("enterpriseNumber", enterpriseNumber)
                .add("fieldLength", fieldLength)
                .toString();
    }
}
