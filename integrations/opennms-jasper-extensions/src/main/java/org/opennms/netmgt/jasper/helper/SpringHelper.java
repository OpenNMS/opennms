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
package org.opennms.netmgt.jasper.helper;

import org.opennms.netmgt.measurements.api.ExpressionEngine;
import org.opennms.netmgt.measurements.api.FilterEngine;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringHelper implements ApplicationContextAware {

    private static Logger LOG = LoggerFactory.getLogger(SpringHelper.class);

    private ApplicationContext applicationContext;

    public MeasurementFetchStrategy getMeasurementFetchStrategy() {
        return getBean("measurementFetchStrategy", MeasurementFetchStrategy.class);
    }

    public ExpressionEngine getExpressionEngine() {
        return getBean("expressionEngine", ExpressionEngine.class);
    }

    public FilterEngine getFilterEngine() {
        return getBean("filterEngine", FilterEngine.class);
    }

    public <T> T getBean(String name, Class<T> clazz) {
        if (applicationContext == null) {
            LOG.error("Could not instantiate bean with name '{}' and type '{}'. ApplicationContext is '{}'", name, clazz, applicationContext);
            return null;
        }
        try {
            return applicationContext.getBean(name, clazz);
        } catch (Exception ex) {
            LOG.error("Could not instantiate bean with name '{}' and type '{}'", name, clazz, ex);
            return null;
        }
    }

    public ApplicationContext getSpringContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
