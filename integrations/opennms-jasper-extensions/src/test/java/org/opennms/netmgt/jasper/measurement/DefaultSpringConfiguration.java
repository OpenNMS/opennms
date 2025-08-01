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
package org.opennms.netmgt.jasper.measurement;

import org.opennms.netmgt.jasper.helper.SpringHelper;
import org.opennms.netmgt.measurements.api.ExpressionEngine;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.FilterEngine;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.api.exceptions.ExpressionException;
import org.opennms.netmgt.measurements.impl.NullFetchStrategy;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spring configuration to define all beans required by MeasurementsService
@Configuration
public class DefaultSpringConfiguration {

    @Autowired
    private ApplicationContext context;

    @Bean(name="expressionEngine")
    public ExpressionEngine createExpressionEngine() {
        return new ExpressionEngine() {
            @Override
            public void applyExpressions(QueryRequest request, FetchResults results) throws ExpressionException {
                // do nothing
            }
        };
    }

    @Bean(name="filterEngine")
    public FilterEngine createFilterEngine() {
        return new FilterEngine();
    }

    @Bean(name="measurementFetchStrategy")
    public MeasurementFetchStrategy createFetchStrategy() {
        // Dummy implementation
        return new NullFetchStrategy();
    }

    @Bean(name="springHelper")
    public SpringHelper createSpringHelper() {
        SpringHelper helper = new SpringHelper();
        helper.setApplicationContext(context);
        return helper;
    }
}
