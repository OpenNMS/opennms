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
package org.opennms.core.test;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This listener will inject the {@link TestContext} into a test if it
 * supports the {@link TestContextAware} interface.
 */
public class TestContextAwareExecutionListener extends AbstractTestExecutionListener {

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		// FIXME: Is there a better way to inject the instance into the test class?
		if (testContext.getTestInstance() instanceof TestContextAware) {
			System.err.println("injecting TestContext into TestContextAware test: "
					+ testContext.getTestInstance().getClass().getSimpleName() + "."
					+ testContext.getTestMethod().getName());
			((TestContextAware) testContext.getTestInstance()).setTestContext(testContext);
		}
	}
}
