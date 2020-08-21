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

import org.hawkular.agent.prometheus.types.Gauge;
import org.junit.Assert;
import org.junit.Test;

public class GaugeTest {
    @Test
    public void testBuild() {
        Gauge gauge;

        try {
            gauge = new Gauge.Builder().build();
            Assert.fail("Should have thrown exception because name is not set");
        } catch (IllegalArgumentException expected) {
        }

        gauge = new Gauge.Builder().setName("foo").setValue(0.1).build();
        Assert.assertEquals("foo", gauge.getName());
        Assert.assertEquals(0.1, gauge.getValue(), 0.001);
        Assert.assertTrue(gauge.getLabels().isEmpty());

        gauge = new Gauge.Builder().setName("foo").setValue(0.1).addLabel("one", "111").build();
        Assert.assertEquals("foo", gauge.getName());
        Assert.assertEquals(0.1, gauge.getValue(), 0.001);
        Assert.assertEquals(1, gauge.getLabels().size());
        Assert.assertEquals("111", gauge.getLabels().get("one"));

        gauge = new Gauge.Builder().setName("foo").setValue(0.1).addLabel("one", "1").addLabel("two", "2").build();
        Assert.assertEquals("foo", gauge.getName());
        Assert.assertEquals(0.1, gauge.getValue(), 0.001);
        Assert.assertEquals(2, gauge.getLabels().size());
        Assert.assertEquals("1", gauge.getLabels().get("one"));
        Assert.assertEquals("2", gauge.getLabels().get("two"));

    }
}
