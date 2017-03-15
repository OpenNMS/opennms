/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
