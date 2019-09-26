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

package org.opennms.netmgt.telemetry.distributed.common;

import java.util.Optional;

import org.opennms.netmgt.telemetry.config.api.QueueDefinition;

public class MapBasedQueueDef implements QueueDefinition {
    private final String name;
    private final Optional<Integer> threads;
    private final Optional<Integer> queueSize;
    private final Optional<Integer> batchSize;
    private final Optional<Integer> batchInterval;

    public MapBasedQueueDef(final PropertyTree definition) {
        this.name = definition.getRequiredString("name");
        this.threads = definition.getOptionalInteger("queue", "threads");
        this.queueSize = definition.getOptionalInteger("queue", "size");
        this.batchSize = definition.getOptionalInteger("batch", "size");
        this.batchInterval = definition.getOptionalInteger("batch", "interval");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Integer> getNumThreads() {
        return threads;
    }

    @Override
    public Optional<Integer> getBatchSize() {
        return batchSize;
    }

    @Override
    public Optional<Integer> getBatchIntervalMs() {
        return batchInterval;
    }

    @Override
    public Optional<Integer> getQueueSize() {
        return queueSize;
    }
}
