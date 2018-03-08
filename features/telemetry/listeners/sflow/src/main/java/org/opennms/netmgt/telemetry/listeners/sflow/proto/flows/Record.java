/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/


package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Opaque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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

        public DataFormat(final ByteBuffer buffer) throws InvalidPacketException {
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

    public Record(final ByteBuffer buffer, final Map<DataFormat, Opaque.Parser<T>> dataFormats) throws InvalidPacketException {
        this.dataFormat = new DataFormat(buffer);

        final Opaque.Parser<T> parser = dataFormats.get(this.dataFormat);
        if (parser != null) {
            this.data = new Opaque(buffer, Optional.empty(), parser);

        } else {
            LOG.debug("Unknown record type: {}:{}", dataFormat.enterpriseNumber, dataFormat.formatNumber);
            System.out.println("Unknown record type: " + dataFormat.enterpriseNumber + " / " + dataFormat.formatNumber);
            this.data = new Opaque(buffer, Optional.empty(), Opaque::parseUnknown);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dataFormat", this.dataFormat)
                .add("data", this.data)
                .toString();
    }

    public abstract void writeBson(final BsonWriter bsonWriter);
}
