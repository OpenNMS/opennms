/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.dao.db;

import java.lang.reflect.Method;

import org.opennms.test.DaoTestConfigBean;
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
