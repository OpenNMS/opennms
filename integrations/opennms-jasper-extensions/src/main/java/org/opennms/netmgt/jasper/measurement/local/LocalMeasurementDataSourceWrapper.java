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
