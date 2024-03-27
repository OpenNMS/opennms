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
package org.opennms.features.deviceconfig.sink.module;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;

public class DeviceConfigSinkModuleImpl extends AbstractXmlSinkModule<DeviceConfigSinkDTO, DeviceConfigSinkDTO> implements DeviceConfigSinkModule, AsyncPolicy {

    public static final String MODULE_ID = "DeviceConfig";

    private int numConsumerThreads = 3;
    private int queueSize = 1000;
    private int numThreads = 3;
    private boolean blockWhenFull = true;

    public DeviceConfigSinkModuleImpl() {
        super(DeviceConfigSinkDTO.class);
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public AggregationPolicy<DeviceConfigSinkDTO, DeviceConfigSinkDTO, ?> getAggregationPolicy() {
        // no aggregation
        return null;
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return this;
    }

    @Override
    public int getNumConsumerThreads() {
        return numConsumerThreads;
    }

    public void setNumConsumerThreads(int numConsumerThreads) {
        this.numConsumerThreads = numConsumerThreads;
    }

    @Override
    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @Override
    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public boolean isBlockWhenFull() {
        return blockWhenFull;
    }

    public void setBlockWhenFull(boolean blockWhenFull) {
        this.blockWhenFull = blockWhenFull;
    }

}
