/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.prometheus;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.MetricType;
import org.hawkular.agent.prometheus.types.Summary;
import org.junit.Assert;
import org.junit.Test;

public class MetricFamilyTest {
    @Test
    public void testBuild() {
        Counter counter = new Counter.Builder().setName("foo").setValue(0.1).build();
        Gauge gauge = new Gauge.Builder().setName("foo").setValue(0.1).build();
        new Summary.Builder().setName("foo").setSampleCount(123).setSampleSum(0.5)
                .addQuantile(0.25, 100.1).addQuantile(0.75, 200.2).build();
        new Histogram.Builder().setName("foo").setSampleCount(123).setSampleSum(0.5)
                .addBucket(0.25, 100).addBucket(1.0, 200).build();

        try {
            new MetricFamily.Builder().build();
            Assert.fail("Should have thrown exception because name is not set");
        } catch (IllegalArgumentException expected) {
        }

        MetricFamily family = new MetricFamily.Builder().setName("foo").build();
        Assert.assertEquals(MetricType.GAUGE, family.getType());
	// untyped metrics should be treated as gauge

        try {
            new MetricFamily.Builder().setName("foo").setType(MetricType.COUNTER).addMetric(gauge).build();
            Assert.fail("Should have thrown exception because type did not match the metrics");
        } catch (IllegalArgumentException expected) {
        }

        try {
            new MetricFamily.Builder().setName("foo").setType(MetricType.GAUGE).addMetric(counter).build();
            Assert.fail("Should have thrown exception because type did not match the metrics");
        } catch (IllegalArgumentException expected) {
        }

        try {
            new MetricFamily.Builder().setName("foo").setType(MetricType.SUMMARY).addMetric(counter).build();
            Assert.fail("Should have thrown exception because type did not match the metrics");
        } catch (IllegalArgumentException expected) {
        }

        try {
            new MetricFamily.Builder().setName("foo").setType(MetricType.HISTOGRAM).addMetric(counter)
                    .build();
            Assert.fail("Should have thrown exception because type did not match the metrics");
        } catch (IllegalArgumentException expected) {
        }

        try {
            new MetricFamily.Builder().setName("foo").setType(MetricType.COUNTER)
                    .addMetric(counter).addMetric(counter).addMetric(gauge).build(); // last one isn't the right type
            Assert.fail("Should have thrown exception because type did not match the metrics");
        } catch (IllegalArgumentException expected) {
        }

        family = new MetricFamily.Builder().setName("foo").setType(MetricType.SUMMARY).build();
        Assert.assertEquals("foo", family.getName());
        Assert.assertNull(family.getHelp());
        Assert.assertEquals(MetricType.SUMMARY, family.getType());
        Assert.assertNotNull(family.getMetrics());
        Assert.assertTrue(family.getMetrics().isEmpty());

        family = new MetricFamily.Builder().setName("foo").setHelp("foo help").setType(MetricType.COUNTER)
                .addMetric(counter).addMetric(counter).build();
        Assert.assertEquals("foo", family.getName());
        Assert.assertEquals("foo help", family.getHelp());
        Assert.assertEquals(MetricType.COUNTER, family.getType());
        Assert.assertEquals(2, family.getMetrics().size());
    }
}
