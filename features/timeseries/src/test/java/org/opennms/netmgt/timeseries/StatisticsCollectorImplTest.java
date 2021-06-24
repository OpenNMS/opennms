/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
