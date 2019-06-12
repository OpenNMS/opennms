/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.endpoints.grafana.api.Dashboard;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoints.grafana.api.Panel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

public class GrafanaPanelDatasource implements JRRewindableDataSource {
    public static final String IMAGE_FIELD_NAME = "png";
    public static final String WIDTH_FIELD_NAME = "width";
    public static final String HEIGHT_FIELD_NAME = "height";
    public static final String TITLE_FIELD_NAME = "title";
    public static final String DATASOURCE_FIELD_NAME = "datasource";
    public static final String DESCRIPTION_FIELD_NAME = "description";

    private final GrafanaClient client;
    private final Dashboard dashboard;
    private final GrafanaQuery query;

    // state
    private Iterator<Panel> iterator;
    private Panel currentPanel;

    public GrafanaPanelDatasource(GrafanaClient client, Dashboard dashboard, GrafanaQuery query) {
        this.client = Objects.requireNonNull(client);
        this.dashboard = Objects.requireNonNull(dashboard);
        this.query = Objects.requireNonNull(query);
        moveFirst();
    }

    @Override
    public void moveFirst() {
        iterator = dashboard.getPanels().stream()
                .flatMap(p -> {
                    if ("row".equals(p.getType())) {
                        return p.getPanels().stream();
                    } else {
                        return Stream.of(p);
                    }
                })
                .collect(Collectors.toList())
                .iterator();
    }

    @Override
    public boolean next() {
        if (!iterator.hasNext()) {
            return false;
        }
        currentPanel = iterator.next();
        return true;
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        final String fieldName = jrField.getName();
        if (Objects.equals(WIDTH_FIELD_NAME, fieldName)) {
            return query.getWidth();
        } else if (Objects.equals(HEIGHT_FIELD_NAME, fieldName)) {
            return query.getHeight();
        } else if (Objects.equals(TITLE_FIELD_NAME, fieldName)) {
            return currentPanel.getTitle();
        } else if (Objects.equals(DATASOURCE_FIELD_NAME, fieldName)) {
            return currentPanel.getDatasource();
        } else if (Objects.equals(DESCRIPTION_FIELD_NAME, fieldName)) {
            return currentPanel.getDescription();
        } else if (Objects.equals(IMAGE_FIELD_NAME, fieldName)) {
            try {
                return client.renderPngForPanel(dashboard, currentPanel, query.getWidth(), query.getHeight(),
                        query.getFrom().getTime(), query.getTo().getTime(), query.getVariables());
            } catch (IOException e) {
                throw new JRException("Exception while rendering panel: " + currentPanel.getTitle(), e);
            }
        }
        return "<unknown field name: " + fieldName + ">" ;
    }

}
