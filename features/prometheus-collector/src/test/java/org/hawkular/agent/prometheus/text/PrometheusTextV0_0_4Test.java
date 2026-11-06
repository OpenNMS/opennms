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

import static org.hawkular.agent.prometheus.text.AssertUtils.assertMetricFamily;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.MetricType;
import org.junit.Test;

/**
 * Validates PrometheusText0.0.4 format
 *
 */
public class PrometheusTextV0_0_4Test {

    @Test
    public void testSingleCounter() throws IOException {
        MetricFamily counter1 = new MetricFamily.Builder().setName("http_requests_total")
                .setType(MetricType.COUNTER)
                .setHelp("Total number of HTTP requests made.")
                .addMetric(Counter.builder().setName("http_requests_total")
                        .addLabels(Map.of("code", "200", "handler", "prometheus", "method", "get"))
                        .setValue(162030)
                        .build())
                .addMetric(Counter.builder().setName("http_requests_total")
                        .addLabels(Map.of("code", "200", "handler", "query", "method", "get"))
                        .setValue(40)
                        .build())
                .addMetric(Counter.builder().setName("http_requests_total")
                        .addLabels(Map.of("code", "200", "handler", "series", "method", "get"))
                        .setValue(Double.NaN)
                        .build())
                .addMetric(Counter.builder().setName("http_requests_total")
                        .addLabels(Map.of("code", "400", "handler", "query", "method", "get"))
                        .setValue(Double.POSITIVE_INFINITY)
                        .build())
                .addMetric(Counter.builder().setName("http_requests_total")
                        .addLabels(Map.of("code", "400", "handler", "series", "method", "get"))
                        .setValue(Double.NEGATIVE_INFINITY)
                        .build())
                .build();

        try (InputStream input = PrometheusTextV0_0_4Test.class.getResourceAsStream("/prometheus-counter.txt")) {
            TextPrometheusMetricDataParser parser = new TextPrometheusMetricDataParser(input, PrometheusTextFormatStrategy.INSTANCE);
            MetricFamily metricFamily = parser.parse();
            assertMetricFamily(counter1, metricFamily);
        }
    }

    @Test
    public void testThreeCounters() throws IOException {
        MetricFamily counter1 = new MetricFamily.Builder().setName("one_counter_total")
                .setType(MetricType.COUNTER)
                .setHelp("This is the first")
                .addMetric(Counter.builder().setName("one_counter_total").setValue(111).build())
                .build();

        MetricFamily counter2 = new MetricFamily.Builder().setName("two_counter_total")
                .setType(MetricType.COUNTER)
                .setHelp("This is the second")
                .addMetric(Counter.builder().setName("two_counter_total").setValue(222).build())
                .build();

        MetricFamily counter3 = new MetricFamily.Builder().setName("three_counter_total")
                .setType(MetricType.COUNTER)
                .setHelp("This is the third with type specified first")
                .addMetric(Counter.builder().setName("three_counter_total").setValue(333).build())
                .build();

        try (InputStream input = PrometheusTextV0_0_4Test.class.getResourceAsStream("/prometheus-three-counters.txt")) {
            TextPrometheusMetricDataParser parser = new TextPrometheusMetricDataParser(input, PrometheusTextFormatStrategy.INSTANCE);

            MetricFamily metricFamily1 = parser.parse();
            assertMetricFamily(counter1, metricFamily1);

            MetricFamily metricFamily2 = parser.parse();
            assertMetricFamily(counter2, metricFamily2);

            MetricFamily metricFamily3 = parser.parse();
            assertMetricFamily(counter3, metricFamily3);
        }
    }
}
