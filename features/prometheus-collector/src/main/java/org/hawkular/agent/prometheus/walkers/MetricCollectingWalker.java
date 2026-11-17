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
package org.hawkular.agent.prometheus.walkers;

import java.util.LinkedList;
import java.util.List;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.Metric;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.Summary;

/**
 * Gathers the complete list of metrics, ignoring the metric families.
 */
public class MetricCollectingWalker implements PrometheusMetricsWalker {
    private List<Metric> metrics;

    @Override
    public void walkStart() {
        metrics = new LinkedList<>();
    }

    @Override
    public void walkFinish(int familiesProcessed, int metricsProcessed) {
        // pass
    }

    @Override
    public void walkMetricFamily(MetricFamily family, int index) {
        metrics.addAll(family.getMetrics());
    }

    @Override
    public void walkCounterMetric(MetricFamily family, Counter counter, int index) {
        // pass
    }

    @Override
    public void walkGaugeMetric(MetricFamily family, Gauge gauge, int index) {
        // pass
    }

    @Override
    public void walkSummaryMetric(MetricFamily family, Summary summary, int index) {
        // pass
    }

    @Override
    public void walkHistogramMetric(MetricFamily family, Histogram histogram, int index) {
        // pass
    }
    
    public List<Metric> getMetrics() {
        return metrics;
    }
}
