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
package org.hawkular.agent.prometheus.text;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Metric;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.MetricType;
import org.hawkular.agent.prometheus.types.Summary;

public class AssertUtils {
    
    public static void assertMetricFamily(MetricFamily expectedFamily, MetricFamily metricFamily) {
        assertEquals(expectedFamily.getName(), metricFamily.getName());
        assertEquals(expectedFamily.getHelp(), metricFamily.getHelp());
        assertEquals(expectedFamily.getType(), metricFamily.getType());
        List<Metric> expectedMetrics = expectedFamily.getMetrics();
        List<Metric> metrics = metricFamily.getMetrics();
        assertEquals(expectedMetrics.size(), metrics.size());

        for (int i = 0; i < expectedMetrics.size(); i++) {
            Metric expectedMetric = expectedMetrics.get(i);
            Metric metric = metrics.get(i);
            assertBaseMetric(expectedMetric, metric, expectedFamily.getName());
            MetricType type = expectedFamily.getType();
            switch (type) {
                case COUNTER:
                    assertCounter((Counter) expectedMetric, metric);
                    break;
                case GAUGE:
                case UNKNOWN:
                    assertGauge((Gauge) expectedMetric, metric);
                    break;
                case SUMMARY:
                    assertSummary((Summary) expectedMetric, metric);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported metric type: " + type);
            }
        }
    }
    
    public static void assertBaseMetric(Metric expectedMetric, Metric metric, String name) {
        assertEquals(name, metric.getName());
        if(expectedMetric.getLabels() != null) {
            assertEquals(expectedMetric.getLabels().size(), metric.getLabels().size());
            for (Map.Entry<String, String> expectedLabel : expectedMetric.getLabels().entrySet()) {
                assertEquals(expectedLabel.getValue(), metric.getLabels().get(expectedLabel.getKey()));
            }
        }
        optionalAssertEquals(expectedMetric.getTimestamp(), metric.getTimestamp());
        optionalAssertEquals(expectedMetric.getCreated(), metric.getCreated());
    }
    
    public static void optionalAssertEquals(Object expected, Object actual) {
        if (expected != null) {
            assertEquals(expected, actual);
        }
    }
    
    public static void assertCounter(Counter expectedMetric, Metric metric) {
        assertEquals(Counter.class, metric.getClass());
        Counter counter = (Counter) metric;
        assertEquals(expectedMetric.getValue(), counter.getValue(), 0.001);
    }
    
    public static void assertGauge(Gauge expectedMetric, Metric metric) {
        assertEquals(Gauge.class, metric.getClass());
        Gauge gauge = (Gauge) metric;
        assertEquals(expectedMetric.getValue(), gauge.getValue(), 0.001);
    }
    
    public static void assertSummary(Summary expectedMetric, Metric metric) {
        assertEquals(Summary.class, metric.getClass());  
        Summary summary = (Summary) metric;
        long sampleCount = summary.getSampleCount();
        double sampleSum = summary.getSampleSum();
        assertEquals(sampleCount, expectedMetric.getSampleCount());
        assertEquals(sampleSum, expectedMetric.getSampleSum(), 0.001);
    }
}
