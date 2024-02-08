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

import org.opennms.core.spring.BeanUtils;
import org.springframework.context.access.DefaultLocatorFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This listener will inject the {@link ApplicationContext} from 
 * the {@link TestContext} into {@link BeanUtils} so that it can locate
 * beans inside the test context instead of using {@link DefaultLocatorFactory}.
 * If {@link BeanUtils} uses {@link DefaultLocatorFactory}, it will start
 * up another Spring context hierarchy inside the tests causing duplicate
 * beans.
 */
public class BeanUtilsTestContextInjectionExecutionListener extends AbstractTestExecutionListener {

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		BeanUtils.setStaticApplicationContext(testContext.getApplicationContext());
	}
}
