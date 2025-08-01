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

import java.util.Map;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;
import org.opennms.netmgt.jasper.helper.MeasurementsHelper;
import org.opennms.netmgt.jasper.measurement.local.LocalMeasurementDataSourceWrapper;
import org.opennms.netmgt.jasper.measurement.remote.RemoteMeasurementDataSourceWrapper;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MeasurementQueryExecutor extends JRAbstractQueryExecuter {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementQueryExecutor.class);

    private static final String SSL_PROPERTY_KEY = "org.opennms.netmgt.jasper.measurement.ssl.enable";

    private MeasurementDataSourceWrapper datasourceWrapper;

    protected MeasurementQueryExecutor(JasperReportsContext jasperReportsContext, JRDataset dataset, Map<String, ? extends JRValueParameter> parametersMap) {
        super(jasperReportsContext, dataset, parametersMap);
        if (dataset != null) {
            parseQuery();
        }
    }

    @Override
    protected String getParameterReplacement(String parameterName) {
        return String.valueOf(getParameterValue(parameterName));
    }

    @Override
    public boolean cancelQuery() throws JRException {
        return false;
    }

    @Override
    public JRRewindableDataSource createDatasource() throws JRException {
        LOG.debug("Create datasource for query '{}'", getQueryString());
        if (datasourceWrapper == null) {
            datasourceWrapper = createDatasourceWrapper();
        }
        return datasourceWrapper.createDataSource(getQueryString());
    }

    @Override
    public void close() {
        try {
            if (datasourceWrapper != null) {
                datasourceWrapper.close();
            }
        } finally {
            datasourceWrapper = null;
        }
    }

    private MeasurementDataSourceWrapper createDatasourceWrapper() {
        if (MeasurementsHelper.isRunInOpennmsJvm()) {
            return new LocalMeasurementDataSourceWrapper(
                    MeasurementsHelper.getSpringHelper().getMeasurementFetchStrategy(),
                    MeasurementsHelper.getSpringHelper().getExpressionEngine(),
                    MeasurementsHelper.getSpringHelper().getFilterEngine());
        }

        LOG.warn("No {} implementation found. Falling back to HTTP mode.", MeasurementFetchStrategy.class);
        boolean useSsl = Boolean.valueOf(System.getProperty(SSL_PROPERTY_KEY, "false")).booleanValue();
        return new RemoteMeasurementDataSourceWrapper(
                useSsl,
                (String) getParameterValue(Parameters.URL),  // required parameter
                (String) getParameterValue(Parameters.USERNAME, true), // optional parameter
                (String) getParameterValue(Parameters.PASSWORD, true) // optional parameter
        );
    }
}
