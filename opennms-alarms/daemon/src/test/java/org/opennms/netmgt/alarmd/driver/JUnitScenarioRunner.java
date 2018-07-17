/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.driver;

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
