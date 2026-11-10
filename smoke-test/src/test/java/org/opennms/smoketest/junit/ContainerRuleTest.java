/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;

public class ContainerRuleTest {

    private static final Description DESCRIPTION = Description.createSuiteDescription(ContainerRuleTest.class);

    private final List<String> events = new ArrayList<>();

    private class FakeContainer implements Startable, TestLifecycleAware {
        private final String name;
        private final RuntimeException startFailure;

        FakeContainer(String name, RuntimeException startFailure) {
            this.name = name;
            this.startFailure = startFailure;
        }

        @Override
        public void start() {
            events.add(name + ":start");
            if (startFailure != null) {
                throw startFailure;
            }
        }

        @Override
        public void stop() {
            events.add(name + ":stop");
        }

        @Override
        public void beforeTest(TestDescription description) {
            events.add(name + ":beforeTest:" + description.getFilesystemFriendlyName());
        }

        @Override
        public void afterTest(TestDescription description, Optional<Throwable> throwable) {
            events.add(name + ":afterTest:" + throwable.map(t -> t.getMessage()).orElse("ok"));
        }
    }

    private Statement test(final RuntimeException failure) {
        return new Statement() {
            @Override
            public void evaluate() {
                events.add("test");
                if (failure != null) {
                    throw failure;
                }
            }
        };
    }

    @Test
    public void chainedContainersFollowTestcontainersLifecycle() throws Throwable {
        RuleChain.outerRule(ContainerRule.of(new FakeContainer("a", null)))
                .around(ContainerRule.of(new FakeContainer("b", null)))
                .apply(test(null), DESCRIPTION)
                .evaluate();

        final String name = ContainerRuleTest.class.getName() + "-null";
        assertEquals(Arrays.asList(
                "a:beforeTest:" + name, "a:start",
                "b:beforeTest:" + name, "b:start",
                "test",
                "b:afterTest:ok", "b:stop",
                "a:afterTest:ok", "a:stop"), events);
    }

    @Test
    public void testFailureIsPassedToAfterTestAndRethrown() throws Throwable {
        final RuntimeException failure = new RuntimeException("boom");
        try {
            ContainerRule.of(new FakeContainer("a", null)).apply(test(failure), DESCRIPTION).evaluate();
            fail("expected the test failure to propagate");
        } catch (RuntimeException e) {
            assertSame(failure, e);
        }
        assertEquals(Arrays.asList("a:beforeTest:" + ContainerRuleTest.class.getName() + "-null",
                "a:start", "test", "a:afterTest:boom", "a:stop"), events);
    }

    @Test
    public void startFailureStillCollectsAndStops() throws Throwable {
        try {
            ContainerRule.of(new FakeContainer("a", new RuntimeException("no start"))).apply(test(null), DESCRIPTION).evaluate();
            fail("expected the start failure to propagate");
        } catch (RuntimeException e) {
            assertEquals("no start", e.getMessage());
        }
        assertEquals(Arrays.asList("a:beforeTest:" + ContainerRuleTest.class.getName() + "-null",
                "a:start", "a:afterTest:no start", "a:stop"), events);
    }

    @Test
    public void stopFailureIsReportedWithTestFailure() throws Throwable {
        final FakeContainer container = new FakeContainer("a", null) {
            @Override
            public void stop() {
                super.stop();
                throw new IllegalStateException("no stop");
            }
        };
        try {
            ContainerRule.of(container).apply(test(new RuntimeException("boom")), DESCRIPTION).evaluate();
            fail("expected failures to propagate");
        } catch (MultipleFailureException e) {
            assertEquals(2, e.getFailures().size());
        }
    }
}
