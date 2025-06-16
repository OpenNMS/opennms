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
