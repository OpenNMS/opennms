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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;

/**
 * Runs a Testcontainers {@link Startable} as a JUnit 4 rule.
 *
 * Testcontainers 2.x containers no longer implement {@link TestRule}. This
 * reproduces the 1.x lifecycle: beforeTest, start, test, afterTest (with the
 * failure, including a failed start), stop.
 */
public final class ContainerRule implements TestRule {

    private final Startable container;

    private ContainerRule(final Startable container) {
        this.container = Objects.requireNonNull(container);
    }

    public static ContainerRule of(final Startable container) {
        return new ContainerRule(container);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final TestDescription testDescription = toTestDescription(description);
                final List<Throwable> errors = new ArrayList<>();
                try {
                    if (container instanceof TestLifecycleAware) {
                        ((TestLifecycleAware) container).beforeTest(testDescription);
                    }
                    container.start();
                    base.evaluate();
                    afterTest(testDescription, Optional.empty(), errors);
                } catch (Throwable t) {
                    errors.add(t);
                    afterTest(testDescription, Optional.of(t), errors);
                } finally {
                    try {
                        container.stop();
                    } catch (Throwable t) {
                        errors.add(t);
                    }
                }
                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    private void afterTest(final TestDescription description, final Optional<Throwable> failure, final List<Throwable> errors) {
        if (container instanceof TestLifecycleAware) {
            try {
                ((TestLifecycleAware) container).afterTest(description, failure);
            } catch (Throwable t) {
                errors.add(t);
            }
        }
    }

    private static TestDescription toTestDescription(final Description description) {
        // Same names as Testcontainers 1.x, so log and recording paths don't change.
        final String testId = description.getDisplayName();
        final String filesystemFriendlyName = description.getClassName() + "-" + description.getMethodName();
        return new TestDescription() {
            @Override
            public String getTestId() {
                return testId;
            }

            @Override
            public String getFilesystemFriendlyName() {
                return filesystemFriendlyName;
            }
        };
    }
}
