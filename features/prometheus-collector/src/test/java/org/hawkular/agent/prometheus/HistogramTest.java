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

import org.hawkular.agent.prometheus.types.Histogram;
import org.junit.Assert;
import org.junit.Test;

public class HistogramTest {
    @Test
    public void testBuild() {
        Histogram histogram;

        try {
            histogram = new Histogram.Builder().build();
            Assert.fail("Should have thrown exception because name is not set");
        } catch (IllegalArgumentException expected) {
        }

        histogram = new Histogram.Builder().setName("foo").setSampleCount(123).setSampleSum(0.5)
                .addBucket(0.25, 100).addBucket(1.0, 200)
                .addLabel("one", "111").build();
        Assert.assertEquals("foo", histogram.getName());
        Assert.assertEquals(123, histogram.getSampleCount());
        Assert.assertEquals(0.5, histogram.getSampleSum(), 0.001);
        Assert.assertEquals(2, histogram.getBuckets().size());
        Assert.assertEquals(0.25, histogram.getBuckets().get(0).getUpperBound(), 0.01);
        Assert.assertEquals(100, histogram.getBuckets().get(0).getCumulativeCount());
        Assert.assertEquals(1.0, histogram.getBuckets().get(1).getUpperBound(), 0.01);
        Assert.assertEquals(200, histogram.getBuckets().get(1).getCumulativeCount());
        Assert.assertEquals(1, histogram.getLabels().size());
        Assert.assertEquals("111", histogram.getLabels().get("one"));
    }
}
