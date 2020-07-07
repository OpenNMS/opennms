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
