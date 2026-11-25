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
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.MetricType;
import org.hawkular.agent.prometheus.types.Summary;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <class description>
 *
 */
public class OpenMetricsTextV1Test {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenMetricsTextV1Test.class);
    
    @Test
    public void testOpenMetrics() throws IOException {
        
        MetricFamily gauge = new MetricFamily.Builder().setName("go_goroutines")
                .setType(MetricType.GAUGE)
                .setHelp("Number of goroutines that currently exist.")
                .addMetric(Gauge.builder().setName("go_goroutines").setValue(69).build())
                .build();
        
        
        MetricFamily counter1 = new MetricFamily.Builder().setName("process_cpu_seconds")
                .setType(MetricType.COUNTER)
                .setHelp("Total user and system CPU time spent in seconds.")
                .addMetric(Counter.builder().setName("process_cpu_seconds").setValue(4.20072246e+06).build())
                .build();
        
        
        Instant sampleTs1 = Instant.ofEpochSecond(1488452300L);
        
        MetricFamily counter2 = new MetricFamily.Builder().setName("http_requests")
                .setType(MetricType.COUNTER)
                .setHelp("Total number of HTTP requests made.")
                .addMetric(Counter.builder().setName("http_requests")
                        .addLabels(Map.of("method", "get", "code", "200"))
                        .setValue(1027)
                        .setTimestamp(sampleTs1).build())
                .addMetric(Counter.builder().setName("http_requests")
                        .addLabels(Map.of("method", "post", "code", "200"))
                        .setValue(32)
                        .setTimestamp(sampleTs1).build())
                .build();
       
        
        Instant sampleTs2 = Instant.ofEpochSecond(1520879607L);
        Instant createdTs = Instant.ofEpochSecond(1605281325L);
        
        MetricFamily counter3 = new MetricFamily.Builder().setName("foo")
                .setType(MetricType.COUNTER)
                .setHelp("")
                .addMetric(Counter.builder().setName("foo")
                        .addLabel("color", "red")
                        .setValue(17)
                        .setTimestamp(sampleTs2)
                        .setCreated(createdTs).build())
                .addMetric(Counter.builder().setName("foo")
                        .addLabel("color", "green")
                        .setValue(10)
                        .setTimestamp(sampleTs2)
                        .setCreated(createdTs).build())
                .build();
        

        MetricFamily summary = new MetricFamily.Builder().setName("acme_http_router_request_seconds")
                .setType(MetricType.SUMMARY)
                .setHelp("Latency though all of ACME's HTTP request router.")
                .addMetric(Summary.builder()
                        .setName("acme_http_router_request_seconds")
                        .addLabels(Map.of("path", "/api/v1", "method", "GET"))
                        .setSampleSum(9036.32)
                        .setSampleCount(807283)
                        .setCreated(Instant.ofEpochSecond(1605281325L))
                        .build())
                .addMetric(Summary.builder()
                        .setName("acme_http_router_request_seconds")
                        .addLabels(Map.of("path", "/api/v2", "method", "POST"))
                        .setSampleSum(479.3)
                        .setSampleCount(34)
                        .setCreated(Instant.ofEpochSecond(1605281325L))
                        .build()
                        )
                .build();
        
        MetricFamily unknownMetric = new MetricFamily.Builder().setName("unknown_metric_1")
                .setType(MetricType.UNKNOWN)
                .setHelp("A metric with an unknown type")
                .addMetric(Gauge.builder().setName("unknown_metric_1").setValue(42).build())
                .build();
        
        MetricFamily untyped1 = new MetricFamily.Builder().setName("unknown_metric_2")
                .setType(MetricType.UNKNOWN)
                .setHelp("Missing type, defaults to unknown")
                .addMetric(Gauge.builder().setName("unknown_metric_2").setValue(55).build())
                .build();
        
        MetricFamily untyped2 = new MetricFamily.Builder().setName("unknown_metric_3")
                .setType(MetricType.UNKNOWN)
                .setHelp("")
                .addMetric(Gauge.builder().setName("unknown_metric_3").setValue(66).build())
                .build();
        
        MetricFamily anotherGauge = new MetricFamily.Builder().setName("another_gauge")
                .setType(MetricType.GAUGE)
                .setHelp("Another gauge metric.")
                .addMetric(Gauge.builder().setName("another_gauge").setValue(128).build())
                .build();
                
        List<MetricFamily> expectedMetricFamilies = List.of(gauge, counter1, counter2, counter3, summary, unknownMetric, untyped1, untyped2, anotherGauge);
        
        try(InputStream input = PrometheusTextV0_0_4Test.class.getResourceAsStream("/openmetrics.txt")){
            TextPrometheusMetricDataParser parser = new TextPrometheusMetricDataParser(input, OpenMetricsFormatStrategy.INSTANCE);
            for (MetricFamily expected : expectedMetricFamilies) {
                LOGGER.info("Validating metric {}", expected.getName());
                MetricFamily metricFamily = parser.parse();
                assertMetricFamily(expected, metricFamily);
            }
            assertNull("Expected no more metric families", parser.parse());
        }
    }


}
