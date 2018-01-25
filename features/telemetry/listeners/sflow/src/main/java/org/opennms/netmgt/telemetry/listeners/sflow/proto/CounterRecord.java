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

import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.collect.ImmutableMap;


// struct counter_record {
//   data_format counter_format;     /* The format of counter_data */
//   opaque counter_data<>;          /* A block of counters uniquely defined
//                                      by the counter_format. */
// }

public class CounterRecord extends Record<CounterData> {
    private static Map<DataFormat, Opaque.Parser<CounterData>> counterDataFormats = ImmutableMap.<DataFormat, Opaque.Parser<CounterData>>builder()
            .build();

    public CounterRecord(ByteBuffer buffer) throws InvalidPacketException {
        super(buffer, counterDataFormats);
    }
}
