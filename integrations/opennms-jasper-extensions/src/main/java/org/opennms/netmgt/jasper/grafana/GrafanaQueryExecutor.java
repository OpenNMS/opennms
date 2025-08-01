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
package org.opennms.netmgt.jasper.grafana;

import java.io.IOException;
import java.util.Map;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.endpoints.grafana.api.Dashboard;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClientFactory;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpointService;
import org.opennms.netmgt.endpoints.grafana.client.GrafanaClientImpl;
import org.opennms.netmgt.endpoints.grafana.client.GrafanaServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;

public class GrafanaQueryExecutor extends JRAbstractQueryExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(GrafanaQueryExecutor.class);

    private static final String GRAFANA_ENDPOINT_UID_PARM = "GRAFANA_ENDPOINT_UID";

    @SuppressWarnings("unchecked")
    private static final ServiceLookup<Class<?>, String> SERVICE_LOOKUP = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
            .blocking()
            .build();

    public GrafanaQueryExecutor(JasperReportsContext context, JRDataset dataset, Map<String,? extends JRValueParameter> parameters) {
        super(context, dataset, parameters);
        if (dataset != null) {
            parseQuery();
        }
    }

    @Override
    protected String getParameterReplacement(String parameterName) {
        return String.valueOf(getParameterValue(parameterName));
    }

    @Override
    public GrafanaPanelDatasource createDatasource() throws JRException {
        final String queryString = getQueryString();
        LOG.debug("Create datasource for query '{}'", queryString);
        final GrafanaQuery grafanaQuery = new GrafanaQuery(queryString);
        LOG.debug("Parsed query: {}", grafanaQuery);

        final GrafanaClient client;
        final String grafanaEndpointUid = getStringParameterOrProperty(GRAFANA_ENDPOINT_UID_PARM);
        if (Strings.isNullOrEmpty(grafanaEndpointUid)) {
            LOG.debug("No Grafana endpoint UID was set, using server configuration from the user's environment.");
            final GrafanaServerConfiguration config = GrafanaServerConfiguration.fromEnv();
            client = new GrafanaClientImpl(config);
        } else {
            final GrafanaEndpointService grafanaEndpointService = SERVICE_LOOKUP.lookup(GrafanaEndpointService.class, null);
            final GrafanaEndpoint grafanaEndpointDefinition = grafanaEndpointService.getEndpointByUid(grafanaEndpointUid);
            if (grafanaEndpointDefinition == null) {
                throw new IllegalArgumentException("No endpoint definition found for UID: " + grafanaEndpointUid);
            }
            final GrafanaClientFactory grafanaClientFactory = SERVICE_LOOKUP.lookup(GrafanaClientFactory.class, null);
            client = grafanaClientFactory.createClient(grafanaEndpointDefinition);
        }

        try {
            final Dashboard dashboard = client.getDashboardByUid(grafanaQuery.getDashboardUid());
            return new GrafanaPanelDatasource(client, dashboard, grafanaQuery);
        } catch (IOException e) {
            throw new JRException(e);
        }
    }

    @Override
    public void close() {
        // pass
    }

    @Override
    public boolean cancelQuery() {
        return false;
    }
}
