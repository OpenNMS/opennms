/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.timeseries.samplewrite;

public class TimeseriesWriterConfig {
    public enum BufferType {RINGBUFFER, OFFHEAP}

    /**
     * Special comment about bufferSize and ringBufferSize.
     * If bufferSize defined return bufferSize, otherwise return ringBufferSize
     */
    private int bufferSize;

    private int numWriterThreads;

    private BufferType bufferType = BufferType.RINGBUFFER;

    // ringbuffer backward compatibility, use bufferSize instead
    @Deprecated
    private int ringBufferSize;

    // for offheap
    private int batchSize;

    private String path;

    private long maxFileSize = -1;

    public int getBufferSize() {
        return bufferSize == -1 ? ringBufferSize : bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getNumWriterThreads() {
        return numWriterThreads;
    }

    public void setNumWriterThreads(int numWriterThreads) {
        this.numWriterThreads = numWriterThreads;
    }

    public BufferType getBufferType() {
        return bufferType;
    }

    public void setBufferType(BufferType bufferType) {
        this.bufferType = bufferType;
    }

    public void setRingBufferSize(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getMaxFileSize() {
        return maxFileSize == -1 ? Long.MAX_VALUE : maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}
