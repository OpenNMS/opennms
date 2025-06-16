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
