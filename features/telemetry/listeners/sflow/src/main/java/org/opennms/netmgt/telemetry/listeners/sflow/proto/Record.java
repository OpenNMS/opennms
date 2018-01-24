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



package org.opennms.netmgt.telemetry.listeners.sflow.proto;

import java.nio.ByteBuffer;
import java.util.Map;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.base.Objects;

// The data_format uniquely identifies the format of an opaque structure in
// the sFlow specification. A data_format is contructed as follows:
//   - The most significant 20 bits correspond to the SMI Private Enterprise
//     Code of the entity responsible for the structure definition. A value
//     of zero is used to denote standard structures defined by sflow.org.
//   - The least significant 12 bits are a structure format number assigned
//     by the enterprise that should uniquely identify the the format of the
//     structure.

public class Record<T> {

    public static class DataFormat {
        private final int enterpriseNumber;
        private final int formatNumber;

        public DataFormat(final int enterpriseNumber, final int formatNumber) {
            this.enterpriseNumber = enterpriseNumber;
            this.formatNumber = formatNumber;
        }

        public DataFormat(final ByteBuffer buffer) throws InvalidPacketException {
            final long dataFormat = BufferUtils.uint32(buffer);
            this.enterpriseNumber = (int)(dataFormat >> 12 & (2^20-1));
            this.formatNumber = (int)(dataFormat & (2^12-1));
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
    }

    public final DataFormat dataFormat;
    public final OpaqueList<T> data;

    public Record(final ByteBuffer buffer, final Map<DataFormat, Opaque.Parser<T>> dataFormats) throws InvalidPacketException {
        this.dataFormat = new DataFormat(buffer);

        final Opaque.Parser<T> parser = dataFormats.get(this.dataFormat); // TODO: Handle unknown
        this.data = new OpaqueList<>(buffer, parser);
    }
}
