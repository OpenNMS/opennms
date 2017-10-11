/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.netflow.v5;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.adapters.netflow.v5.fields.IntField;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.fields.LongField;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.fields.ShortField;

public class NetflowHeader {

    private static final int HEADER_SIZE = 24;

    private static final int BODY_SIZE = 48;

    private final ByteBuffer data;

    public NetflowHeader(ByteBuffer data) {
        this.data = data;
    }

    public int getVersion() {
        return new IntField(0, 1, data, getOffset()).getValue();
    }

    public int getCount() {
        return new IntField(2, 3, data, getOffset()).getValue();
    }

    public long getSysUptime() {
        return new LongField(4, 7, data, getOffset()).getValue();
    }

    public long getUnixSecs() {
        return new LongField(8, 11, data, getOffset()).getValue();
    }

    public long getUnixNsecs() {
        return new LongField(12, 15, data, getOffset()).getValue();
    }

    public long getFlowSequence() {
        return new LongField(16, 19, data, getOffset()).getValue();
    }

    public int getEngineType() {
        return new ShortField(20, 20, data, getOffset()).getValue();
    }

    public int getEngineId() {
        return new ShortField(21, 21, data, getOffset()).getValue();
    }

    public int getSamplingInterval() {
        return new IntField(22, 23, data, getOffset()).getValue();
    }

    private int getOffset() {
        return 0;
    }

    public int getSize() {
        return HEADER_SIZE;
    }

    public int getBodySize() {
        return BODY_SIZE;
    }
}
