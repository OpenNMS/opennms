/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
