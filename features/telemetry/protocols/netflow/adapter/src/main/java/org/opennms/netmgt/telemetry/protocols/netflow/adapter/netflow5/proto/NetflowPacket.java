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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.proto;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NetflowPacket {

    public static final int VERSION = 5;
    public static final int MIN_COUNT = 1;
    public static final int MAX_COUNT = 30;

    private static final int HEADER_SIZE = 24;

    private static final int BODY_SIZE = 48;
    private final int version;
    private final int count;
    private final long sysUptime;
    private final long unixSecs;
    private final long unixNSecs;
    private final long flowSequence;
    private final short engineType;
    private final short engineId;
    private final int samplingAlgorithm;
    private final int samplingInterval;

    private final List<NetflowRecord> records = new ArrayList<>();

    public NetflowPacket(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    public NetflowPacket(ByteBuffer data) {
        // Check that at least the header can be read
        if (data.array().length < HEADER_SIZE) {
            throw new IllegalArgumentException("A netflow packet must contain at least " + HEADER_SIZE + " bytes, but only " + data.remaining() + " have been provided.");
        }

        // Parse header
        this.version =  Utils.getInt(0, 1, data, 0);
        this.count = Utils.getInt(2, 3, data, 0);
        this.sysUptime = Utils.getLong(4, 7, data, 0);
        this.unixSecs = Utils.getLong(8, 11, data, 0);
        this.unixNSecs = Utils.getLong(12, 15, data, 0);
        this.flowSequence = Utils.getLong(16, 19, data, 0);
        this.engineType =  Utils.getShort(20, 20, data, 0); 
        this.engineId =  Utils.getShort(21, 21, data, 0);
        this.samplingAlgorithm = (Utils.getInt(22, 23, data, 0) & 0b11000000_00000000) >> 14;
        this.samplingInterval = Utils.getInt(22, 23, data, 0) & 0b00111111_11111111;

        // Parse body
        // determine how many records are there, as this.count could be wrong
        int theoreticallyRecordCount = (data.array().length - HEADER_SIZE) / BODY_SIZE;
        int readRecordCount = Math.min(this.count, theoreticallyRecordCount);
        for (int i = 0; i < readRecordCount; i++) {
            final int offset = i * BODY_SIZE + HEADER_SIZE;
            final NetflowRecord record = new NetflowRecord(data, offset);
            this.records.add(record);
        }
    }

    public boolean isValid() {
        // ensure fields can be set
        boolean valid = this.version == VERSION
                && this.count >= MIN_COUNT && this.count <= MAX_COUNT
                && getRecords().size() == this.count;
        return valid;
    }

    public NetflowRecord getRecord(int recordIndex) {
        if (recordIndex < 0 || recordIndex >= this.count) {
            throw new IndexOutOfBoundsException("Cannot access record, recordIndex must be >= 0 and < getHeader().getCount()");
        }
        return this.records.get(recordIndex);
    }

    public List<NetflowRecord> getRecords() {
       return this.records;
    }

    public int getSize() {
        return HEADER_SIZE;
    }

    public int getVersion() {
        return version;
    }

    public int getCount() {
        return count;
    }

    public long getSysUptime() {
        return sysUptime;
    }

    public long getUnixSecs() {
        return unixSecs;
    }

    public long getUnixNSecs() {
        return unixNSecs;
    }

    public long getFlowSequence() {
        return flowSequence;
    }

    public int getEngineType() {
        return engineType;
    }

    public int getEngineId() {
        return engineId;
    }

    public int getSamplingAlgorithm() {
        return samplingAlgorithm;
    }

    public int getSamplingInterval() {
        return samplingInterval;
    }
}
