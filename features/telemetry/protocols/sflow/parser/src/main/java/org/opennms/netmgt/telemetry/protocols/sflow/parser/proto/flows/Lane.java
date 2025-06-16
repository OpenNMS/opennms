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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct lane {
//   unsigned int index; /* 1-based index of lane within module, 0=unknown */
//   unsigned int tx_bias_current; /* microamps */
//   unsigned int tx_power;        /* microwatts */
//   unsigned int tx_power_min;    /* microwatts */
//   unsigned int tx_power_max;    /* microwatts */
//   unsigned int tx_wavelength;   /* nanometers */
//   unsigned int rx_power;        /* microwatts */
//   unsigned int rx_power_min;    /* microwatts */
//   unsigned int rx_power_max;    /* microwatts */
//   unsigned int rx_wavelength;   /* nanometers */
// };

public class Lane {
    public final long index;
    public final long tx_bias_current;
    public final long tx_power;
    public final long tx_power_min;
    public final long tx_power_max;
    public final long tx_wavelength;
    public final long rx_power;
    public final long rx_power_min;
    public final long rx_power_max;
    public final long rx_wavelength;

    public Lane(final ByteBuf buffer) throws InvalidPacketException {
        this.index = BufferUtils.uint32(buffer);
        this.tx_bias_current = BufferUtils.uint32(buffer);
        this.tx_power = BufferUtils.uint32(buffer);
        this.tx_power_min = BufferUtils.uint32(buffer);
        this.tx_power_max = BufferUtils.uint32(buffer);
        this.tx_wavelength = BufferUtils.uint32(buffer);
        this.rx_power = BufferUtils.uint32(buffer);
        this.rx_power_min = BufferUtils.uint32(buffer);
        this.rx_power_max = BufferUtils.uint32(buffer);
        this.rx_wavelength = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", this.index)
                .add("tx_bias_current", this.tx_bias_current)
                .add("tx_power", this.tx_power)
                .add("tx_power_min", this.tx_power_min)
                .add("tx_power_max", this.tx_power_max)
                .add("tx_wavelength", this.tx_wavelength)
                .add("rx_power", this.rx_power)
                .add("rx_power_min", this.rx_power_min)
                .add("rx_power_max", this.rx_power_max)
                .add("rx_wavelength", this.rx_wavelength)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("index", this.index);
        bsonWriter.writeInt64("tx_bias_current", this.tx_bias_current);
        bsonWriter.writeInt64("tx_power", this.tx_power);
        bsonWriter.writeInt64("tx_power_min", this.tx_power_min);
        bsonWriter.writeInt64("tx_power_max", this.tx_power_max);
        bsonWriter.writeInt64("tx_wavelength", this.tx_wavelength);
        bsonWriter.writeInt64("rx_power", this.rx_power);
        bsonWriter.writeInt64("rx_power_min", this.rx_power_min);
        bsonWriter.writeInt64("rx_power_max", this.rx_power_max);
        bsonWriter.writeInt64("rx_wavelength", this.rx_wavelength);
        bsonWriter.writeEndDocument();
    }
}
