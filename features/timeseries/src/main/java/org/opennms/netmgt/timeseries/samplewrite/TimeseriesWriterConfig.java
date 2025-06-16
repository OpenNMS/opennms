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
