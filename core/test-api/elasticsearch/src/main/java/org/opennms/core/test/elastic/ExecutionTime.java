/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.test.elastic;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionTime implements TestRule {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTime.class);

    private long expectedExecutionTime;
    private TimeUnit timeUnit;
    private long delta;
    private long startTime;

    // when delta is provided, it uses the same unit as timeUnit
    public ExecutionTime(long expectedExecutionTime, TimeUnit timeUnit, long delta) {
        if (expectedExecutionTime < 0) throw new IllegalArgumentException("expectedExecutionTime must be >= 0");
        if (delta < 0) throw new IllegalArgumentException("delta must be >= 0");
        Objects.requireNonNull(timeUnit);

        this.expectedExecutionTime = expectedExecutionTime;
        this.timeUnit = timeUnit;
        this.delta = delta;
    }

    public ExecutionTime(long expectedExecutionTime, TimeUnit timeUnit) {
        this(expectedExecutionTime, timeUnit, 0);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                startTime = System.currentTimeMillis();

                base.evaluate();

                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;

                long expectedExecutionTimeInMs = TimeUnit.MILLISECONDS.convert(expectedExecutionTime, timeUnit);
                long deltaInMs = TimeUnit.MILLISECONDS.convert(delta, timeUnit);

                LOG.info("Verifying execution time. Expected: {}, Actual: {}", expectedExecutionTimeInMs + deltaInMs, executionTime);

                // Verify that it is in range
                Assert.assertThat(expectedExecutionTimeInMs, CoreMatchers.anyOf(
                        Matchers.greaterThanOrEqualTo(executionTime),
                        Matchers.lessThanOrEqualTo(executionTime + deltaInMs)
                ));
            }
        };
    }

    public void resetStartTime() {
        this.startTime = System.currentTimeMillis();
    }
}
