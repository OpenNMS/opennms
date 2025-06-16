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

import java.util.Map;
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import io.netty.buffer.ByteBuf;

// The data_format uniquely identifies the format of an opaque structure in
// the sFlow specification. A data_format is contructed as follows:
//   - The most significant 20 bits correspond to the SMI Private Enterprise
//     Code of the entity responsible for the structure definition. A value
//     of zero is used to denote standard structures defined by sflow.org.
//   - The least significant 12 bits are a structure format number assigned
//     by the enterprise that should uniquely identify the the format of the
//     structure.

public abstract class Record<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Record.class);

    public static class DataFormat {
        private final int enterpriseNumber;
        private final int formatNumber;

        private DataFormat(final int enterpriseNumber, final int formatNumber) {
            this.enterpriseNumber = enterpriseNumber;
            this.formatNumber = formatNumber;
        }

        public DataFormat(final ByteBuf buffer) throws InvalidPacketException {
            final int dataFormat = (int) BufferUtils.uint32(buffer);
            this.enterpriseNumber = (dataFormat >> 12 & (2 << 20) - 1);
            this.formatNumber = (dataFormat & (2 << 12) - 1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataFormat dataFormat = (DataFormat) o;
            return enterpriseNumber == dataFormat.enterpriseNumber &&
                    formatNumber == dataFormat.formatNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(enterpriseNumber, formatNumber);
        }

        public static DataFormat from(final int formatNumber) {
            return new DataFormat(0, formatNumber);
        }

        public static DataFormat from(final int enterpriseNumber, final int formatNumber) {
            return new DataFormat(enterpriseNumber, formatNumber);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("enterpriseNumber", this.enterpriseNumber)
                    .add("formatNumber", this.formatNumber)
                    .toString();
        }

        public String toId() {
            return String.format("%s:%s", this.enterpriseNumber, this.formatNumber);
        }
    }

    public final DataFormat dataFormat;
    public final Opaque<T> data;

    public Record(final ByteBuf buffer, final Map<DataFormat, Opaque.Parser<T>> dataFormats) throws InvalidPacketException {
        this.dataFormat = new DataFormat(buffer);

        final Opaque.Parser<T> parser = dataFormats.get(this.dataFormat);
        if (parser != null) {
            this.data = new Opaque(buffer, Optional.empty(), parser);

        } else {
            LOG.debug("Unknown record type: {}:{}", dataFormat.enterpriseNumber, dataFormat.formatNumber);
            this.data = new Opaque(buffer, Optional.empty(), Opaque::parseUnknown);
        }
    }

    public Record(final DataFormat dataFormat, final Opaque<T> data) {
        this.dataFormat = dataFormat;
        this.data = data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dataFormat", this.dataFormat)
                .add("data", this.data)
                .toString();
    }

    public abstract void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr);
    public abstract void visit(SampleDatagramVisitor visitor);

}
