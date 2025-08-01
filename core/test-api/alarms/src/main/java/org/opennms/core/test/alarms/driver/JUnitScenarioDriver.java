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

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.model.InitializationError;

public class JUnitScenarioDriver {

    public ScenarioResults run(Scenario scenario) {
        // Run the test
        final JUnitCore jUnitCore = new JUnitCore();
        final JUnitScenarioRunner runner;
        try {
            runner = new JUnitScenarioRunner(AlarmdDriver.class, scenario);
        } catch (InitializationError initializationError) {
            throw new IllegalStateException(initializationError);
        }

        final Result junitResult = jUnitCore.run(runner);
        if(!junitResult.wasSuccessful()) {
            throw new RuntimeException("Playback failed:" + junitResult.getFailures());
        }

        final ScenarioResults results = runner.getResults();
        if (results == null) {
            throw new IllegalStateException("Results not set");
        }

        return results;
    }
}
