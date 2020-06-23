/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
