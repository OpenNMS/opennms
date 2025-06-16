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
    private final Timer bulkDeleteTimer;
    private final Counter tasksFailedCounter;

    public ElasticAlarmMetrics(MetricRegistry metrics, BlockingQueue<Task> taskQueue) {
        bulkIndexSizeHistogram = metrics.histogram("bulk-index-size");
        bulkIndexTimer = metrics.timer("bulk-index-timer");
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

    public Timer getBulkDeleteTimer() {
        return bulkDeleteTimer;
    }

    public Counter getTasksFailedCounter() {
        return tasksFailedCounter;
    }
}
