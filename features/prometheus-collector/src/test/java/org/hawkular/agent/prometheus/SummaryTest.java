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

import org.hawkular.agent.prometheus.types.Summary;
import org.junit.Assert;
import org.junit.Test;

public class SummaryTest {
    @Test
    public void testBuild() {
        Summary summary;

        try {
            summary = new Summary.Builder().build();
            Assert.fail("Should have thrown exception because name is not set");
        } catch (IllegalArgumentException expected) {
        }

        summary = new Summary.Builder().setName("foo").setSampleCount(123).setSampleSum(0.5)
                .addQuantile(0.25, 100.1).addQuantile(0.75, 200.2)
                .addLabel("one", "111").build();
        Assert.assertEquals("foo", summary.getName());
        Assert.assertEquals(123, summary.getSampleCount());
        Assert.assertEquals(0.5, summary.getSampleSum(), 0.001);
        Assert.assertEquals(2, summary.getQuantiles().size());
        Assert.assertEquals(0.25, summary.getQuantiles().get(0).getQuantile(), 0.01);
        Assert.assertEquals(100.1, summary.getQuantiles().get(0).getValue(), 0.01);
        Assert.assertEquals(0.75, summary.getQuantiles().get(1).getQuantile(), 0.01);
        Assert.assertEquals(200.2, summary.getQuantiles().get(1).getValue(), 0.01);
        Assert.assertEquals(1, summary.getLabels().size());
        Assert.assertEquals("111", summary.getLabels().get("one"));
    }
}
