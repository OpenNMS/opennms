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

package org.opennms.netmgt.jasper.measurement.local;

import static org.opennms.netmgt.jasper.helper.MeasurementsHelper.unmarshal;

import java.util.Objects;

import org.opennms.netmgt.jasper.measurement.EmptyJRDataSource;
import org.opennms.netmgt.jasper.measurement.MeasurementDataSource;
import org.opennms.netmgt.jasper.measurement.MeasurementDataSourceWrapper;
import org.opennms.netmgt.measurements.api.DefaultMeasurementsService;
import org.opennms.netmgt.measurements.api.ExpressionEngine;
import org.opennms.netmgt.measurements.api.FilterEngine;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.measurements.api.exceptions.ResourceNotFoundException;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;

/**
 * This data source is used when the reports are running within a OpenNMS JVM. In detail they should be used
 * if there is an implementation of {@link MeasurementFetchStrategy} available by {@link org.springframework.beans.BeanUtils#instantiate(Class)}.
 */
public class LocalMeasurementDataSourceWrapper implements MeasurementDataSourceWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(LocalMeasurementDataSourceWrapper.class);

    private final MeasurementsService fetchService;

    public LocalMeasurementDataSourceWrapper(MeasurementFetchStrategy fetchStrategy, ExpressionEngine expressionEngine, FilterEngine filterEngine) {
        Objects.requireNonNull(fetchStrategy);
        Objects.requireNonNull(expressionEngine);
        Objects.requireNonNull(filterEngine);
        this.fetchService = new DefaultMeasurementsService(fetchStrategy, expressionEngine, filterEngine);
    }

    @Override
    public JRRewindableDataSource createDataSource(String query) throws JRException {
        Objects.requireNonNull(query);
        QueryRequest queryRequest = unmarshal(query);
        Objects.requireNonNull(queryRequest);
        queryRequest.setRelaxed(true); // Enforce relaxed mode

        try {
            QueryResponse response = fetchService.query(queryRequest);
            return new MeasurementDataSource(response);
        } catch (ResourceNotFoundException rnfe) {
            LOG.warn("A attribute or resource was not found", rnfe);
            return new EmptyJRDataSource();
        } catch (Exception e) {
           LOG.error("An error occurred while fetching the measurement results", e);
           throw new JRException(e);
        }
    }

    @Override
    public void close() {
        // nothing to do
    }

}
