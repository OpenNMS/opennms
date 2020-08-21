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
import org.junit.Assert;
import org.junit.Test;

public class CounterTest {
    @Test
    public void testBuild() {
        Counter counter;

        try {
            counter = new Counter.Builder().build();
            Assert.fail("Should have thrown exception because name is not set");
        } catch (IllegalArgumentException expected) {
        }

        counter = new Counter.Builder().setName("foo").setValue(0.1).build();
        Assert.assertEquals("foo", counter.getName());
        Assert.assertEquals(0.1, counter.getValue(), 0.001);
        Assert.assertTrue(counter.getLabels().isEmpty());

        counter = new Counter.Builder().setName("foo").setValue(0.1).addLabel("one", "111").build();
        Assert.assertEquals("foo", counter.getName());
        Assert.assertEquals(0.1, counter.getValue(), 0.001);
        Assert.assertEquals(1, counter.getLabels().size());
        Assert.assertEquals("111", counter.getLabels().get("one"));

        counter = new Counter.Builder().setName("foo").setValue(0.1).addLabel("one", "1").addLabel("two", "2").build();
        Assert.assertEquals("foo", counter.getName());
        Assert.assertEquals(0.1, counter.getValue(), 0.001);
        Assert.assertEquals(2, counter.getLabels().size());
        Assert.assertEquals("1", counter.getLabels().get("one"));
        Assert.assertEquals("2", counter.getLabels().get("two"));

    }
}
