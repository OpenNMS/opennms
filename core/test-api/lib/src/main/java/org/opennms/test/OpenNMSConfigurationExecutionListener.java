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
package org.opennms.test;

import java.lang.reflect.Method;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This {@link TestExecutionListener} initializes the {@link DaoTestConfigBean}.
 *
 * @author brozow
 */
public class OpenNMSConfigurationExecutionListener extends AbstractTestExecutionListener {

    private JUnitConfigurationEnvironment findAnnotation(final TestContext testContext) {
        JUnitConfigurationEnvironment anno = null;
        final Method testMethod = testContext.getTestMethod();
        if (testMethod != null) {
            anno = testMethod.getAnnotation(JUnitConfigurationEnvironment.class);
        }
        if (anno == null) {
            final Class<?> testClass = testContext.getTestClass();
            anno = testClass.getAnnotation(JUnitConfigurationEnvironment.class);
        }
        return anno;
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        final JUnitConfigurationEnvironment anno = findAnnotation(testContext);
        if (anno != null) {
            DaoTestConfigBean bean = new DaoTestConfigBean();
            bean.afterPropertiesSet();
            // Set any additional system properties that are specified in the unit test annotation
            for (String prop : anno.systemProperties()) {
                int equals = prop.indexOf("=");
                if (equals > 0) {
                    String key = prop.substring(0, equals);
                    String value = prop.substring(equals + 1);
                    System.setProperty(key, value);
                } else {
                    throw new IllegalArgumentException("Invalid system property value: " + prop);
                }
            }
        }
    }
}
