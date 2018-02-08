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

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

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

    public Lane(final ByteBuffer buffer) throws InvalidPacketException {
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
}
