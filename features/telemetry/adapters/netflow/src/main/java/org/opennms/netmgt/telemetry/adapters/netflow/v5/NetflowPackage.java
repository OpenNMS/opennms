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
import java.util.ArrayList;
import java.util.List;

public class NetflowPackage {

    public static final int VERSION = 5;

    public static final int MIN_COUNT = 1;

    public static final int MAX_COUNT = 30;

    private final ByteBuffer data;

    public NetflowPackage(ByteBuffer data) {
        this.data = data;
    }

    public NetflowHeader getHeader() {
        return new NetflowHeader(this.data);
    }

    public NetflowRecord getRecord(int recordIndex) {
        if (recordIndex < 0 || recordIndex >= getHeader().getCount()) {
            throw new IndexOutOfBoundsException("Cannot access record, recordIndex must be >= 0 and < getHeader().getCount()");
        }

        final int headerSize = getHeader().getSize();
        final int bodySize = getHeader().getBodySize();
        final int offset = recordIndex * bodySize + headerSize;
        final NetflowRecord record = new NetflowRecord(data, offset);
        return record;
    }

    public List<NetflowRecord> getRecords() {
        final List<NetflowRecord> records = new ArrayList<>();
        final NetflowHeader header = getHeader();

        for (int i=0; i<header.getCount(); i++) {
            final NetflowRecord record = getRecord(i);
            records.add(record);
        }

        return records;
    }

    public int getVersion() {
        return getHeader().getVersion();
    }

    public boolean isValid() {
        // ensure fields can be set
        boolean valid = getVersion() == VERSION
                && getHeader().getCount() >= MIN_COUNT && getHeader().getCount() <= MAX_COUNT
                && getRecords().size() == getHeader().getCount();
            //TODO MVR I noticed some packages are longer than they should be. May worth investigating
//                && data.array().length == getHeader().getSize() + getHeader().getBodySize() * getHeader().getCount();
        return valid;
    }
}
