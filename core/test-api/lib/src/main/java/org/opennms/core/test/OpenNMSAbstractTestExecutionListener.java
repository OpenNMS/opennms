/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * <p>OpenNMSAbstractTestExecutionListener class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OpenNMSAbstractTestExecutionListener extends AbstractTestExecutionListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(OpenNMSAbstractTestExecutionListener.class);
	
    /** {@inheritDoc} */
    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        LOG.debug("starting test method {}", testContext.getTestMethod());
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);
        LOG.debug("finishing test method {}", testContext.getTestMethod());
    }

    /**
     * <p>findTestAnnotation</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param testContext a {@link org.springframework.test.context.TestContext} object.
     * @param <T> a T object.
     * @return a T object.
     */
    protected <T extends Annotation> T findTestAnnotation(final Class<T> clazz, final TestContext testContext) {
        final Method testMethod = testContext.getTestMethod();
        T config = testMethod.getAnnotation(clazz);
        if (config != null) {
            return config;
        }
        
        LOG.trace("unable to find method annotation for context {}", testContext.getApplicationContext());

        config = ((Class<?>) testContext.getTestClass()).getAnnotation(clazz);
        if (config != null) {
            return config;
        }
        
        LOG.trace("unable to find class annotation for context {}", testContext.getApplicationContext());
        
        return null;
    }
}
