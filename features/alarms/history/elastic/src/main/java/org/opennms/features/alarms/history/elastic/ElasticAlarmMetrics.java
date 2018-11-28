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

package org.opennms.features.alarms.history.elastic;

import java.util.concurrent.BlockingQueue;

import org.opennms.features.alarms.history.elastic.tasks.Task;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class ElasticAlarmMetrics {

    private final Histogram bulkIndexSizeHistogram;
    private final Timer bulkIndexTimer;
    private final Timer deleteTimer;
    private final Timer bulkDeleteTimer;
    private final Counter tasksFailedCounter;

    public ElasticAlarmMetrics(MetricRegistry metrics, BlockingQueue<Task> taskQueue) {
        bulkIndexSizeHistogram = metrics.histogram("bulk-index-size");
        bulkIndexTimer = metrics.timer("bulk-index-timer");
        deleteTimer = metrics.timer("delete-timer");
        bulkDeleteTimer = metrics.timer("bulk-delete-timer");
        tasksFailedCounter = metrics.counter("tasks-failed");
        metrics.register("task-queue-size", (Gauge<Integer>) taskQueue::size);
    }

    public Histogram getBulkIndexSizeHistogram() {
        return bulkIndexSizeHistogram;
    }

    public Timer getBulkIndexTimer() {
        return bulkIndexTimer;
    }

    public Timer getDeleteTimer() {
        return deleteTimer;
    }

    public Timer getBulkDeleteTimer() {
        return bulkDeleteTimer;
    }

    public Counter getTasksFailedCounter() {
        return tasksFailedCounter;
    }
}
