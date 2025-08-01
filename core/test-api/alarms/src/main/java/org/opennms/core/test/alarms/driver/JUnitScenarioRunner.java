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
package org.opennms.core.test.alarms.driver;

import java.util.Objects;

import org.junit.runners.model.InitializationError;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class JUnitScenarioRunner extends OpenNMSJUnit4ClassRunner implements TestExecutionListener {

    private final Scenario scenario;
    private ScenarioResults results;

    public JUnitScenarioRunner(Class<? extends ScenarioHandler> clazz, Scenario scenario) throws InitializationError {
        super(clazz);
        this.scenario = Objects.requireNonNull(scenario);
        getTestContextManager().registerTestExecutionListeners(this);
    }

    @Override
    public void beforeTestMethod(TestContext testContext)  {
        ((ScenarioHandler)testContext.getTestInstance()).setScenario(scenario);
    }

    @Override
    public void afterTestMethod(TestContext testContext)  {
        results = ((ScenarioHandler)testContext.getTestInstance()).getResults();
        scenario.awaitUntilComplete();
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        // pass
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        // pass
    }

    @Override
    public void prepareTestInstance(TestContext testContext) {
        // pass
    }

    public ScenarioResults getResults() {
        return results;
    }
}
