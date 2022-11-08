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
