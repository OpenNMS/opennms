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
package org.opennms.netmgt.timeseries;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.netmgt.timeseries.stats.StatisticsCollectorImpl;

public class StatisticsCollectorImplTest {

    @Test
    public void test() {
        StatisticsCollectorImpl stats = new StatisticsCollectorImpl(4);
        stats.record(createSample(3));
        stats.record(createSample(4));
        stats.record(createSample(4));
        stats.record(createSample(5));
        stats.record(createSample(5));
        stats.record(createSample(6));
        stats.record(createSample(7));
        stats.record(createSample(8));
        stats.record(createSample(9));
        stats.record(createSample(10));
        stats.record(createSample(11));
        stats.record(createSample(12));
        List<Metric> topN = stats.getTopNMetricsWithMostTags();
        assertEquals(10, topN.size());
        assertEquals(12, count(topN.get(0)));
        assertEquals(4, count(topN.get(9)));

        List<String> topNTags = stats.getTopNTags();
        assertEquals(3, topNTags.size());
    }

    private List<Sample> createSample(int noTags) {
        ImmutableMetric.MetricBuilder b = ImmutableMetric.builder()
                .intrinsicTag(IntrinsicTagNames.resourceId, "aa")
                .intrinsicTag(IntrinsicTagNames.name, UUID.randomUUID().toString());
        for(int i=2 ; i<noTags; i++) {
            b.metaTag("tagKey", UUID.randomUUID().toString());
        }
        return Collections.singletonList(ImmutableSample.builder().metric(b.build()).time(Instant.now()).value(42.0).build());
    }

    private int count(Metric metric) {
        return metric.getIntrinsicTags().size() + metric.getMetaTags().size() + metric.getExternalTags().size(); // intrinsic tag count is always 2
    }
}
