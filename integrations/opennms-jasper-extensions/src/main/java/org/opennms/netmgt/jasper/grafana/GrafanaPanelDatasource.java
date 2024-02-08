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

import static org.opennms.netmgt.jasper.grafana.ExceptionToPngRenderer.renderExceptionToPng;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.endpoints.grafana.api.Dashboard;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoints.grafana.api.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

public class GrafanaPanelDatasource implements JRRewindableDataSource {
    private static final Logger LOG = LoggerFactory.getLogger(GrafanaPanelDatasource.class);

    public static final String IMAGE_FIELD_NAME = "png";
    public static final String WIDTH_FIELD_NAME = "width";
    public static final String HEIGHT_FIELD_NAME = "height";
    public static final String TITLE_FIELD_NAME = "title";
    public static final String DATASOURCE_FIELD_NAME = "datasource";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String ROW_TITLE_FIELD_NAME = "rowTitle";

    private final GrafanaClient client;
    private final Dashboard dashboard;
    private final GrafanaQuery query;

    // state
    private final List<Panel> panels;
    private Iterator<Panel> iterator;
    private Panel currentPanel;
    private Map<Panel, CompletableFuture<byte[]>> panelRenders;

    public GrafanaPanelDatasource(GrafanaClient client, Dashboard dashboard, GrafanaQuery query) {
        this.client = Objects.requireNonNull(client);
        this.dashboard = Objects.requireNonNull(dashboard);
        this.query = Objects.requireNonNull(query);
        panels = dashboard.getPanels().stream()
                .flatMap(p -> {
                    if ("row".equals(p.getType())) {
                        return p.getPanels().stream();
                    } else {
                        return Stream.of(p);
                    }
                })
                .collect(Collectors.toList());
        moveFirst();
    }

    @Override
    public void moveFirst() {
        iterator = panels.iterator();
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
    public Object getFieldValue(final JRField jrField) {
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
        } else if (Objects.equals(ROW_TITLE_FIELD_NAME, fieldName)) {
            return getRowTitle(currentPanel);
        } else if (Objects.equals(IMAGE_FIELD_NAME, fieldName)) {
            try {
                maybeRenderPanels();
                LOG.debug("Waiting for panel with id: {} on dashboard with uid: {} to finish rendering.",
                        currentPanel.getId(), dashboard.getUid());
                final byte[] pngBytes = panelRenders.get(currentPanel).get(5, TimeUnit.MINUTES);
                LOG.debug("PNG successfully rendered ({} bytes) for panel with id: {} on dashboard with uid: {}",
                        pngBytes.length, currentPanel.getId(), dashboard.getUid());
                return pngBytes;
            } catch (TimeoutException|InterruptedException|ExecutionException e) {
                LOG.warn("Exception while rendering panel with id: {} on dashboard with uid: {}",
                        currentPanel.getId(), dashboard.getUid(), e);
                return renderExceptionToPng(e);
            }
        }
        return "<unknown field name: " + fieldName + ">" ;
    }

    private void maybeRenderPanels() {
        if (panelRenders != null) {
            // We already rendered the panels
            return;
        }
        panelRenders = new HashMap<>();

        // Issue the requests for all of the panels simultaneously
        LOG.debug("Triggering asynchronous rendering of PNGs for {} panels for dashboard: {}", panels.size(), dashboard.getTitle());
        // final String utcOffset = getUtcOffset(query);
        for (Panel panel : panels) {
            final CompletableFuture<byte[]> panelImageBytes = client.renderPngForPanel(dashboard, panel, query.getWidth(), query.getHeight(),
                    query.getFrom().getTime(), query.getTo().getTime(), query.getTimezone(), query.getVariables());
            panelRenders.put(panel, panelImageBytes);
        }
    }

    /**
     * Gets the current panel the previous row panel title if it has one.
     * @param panel
     * @return String with the row panel title if exists or an empty string if not.
     */
    private String getRowTitle(final Panel panel){
        String currentRowPanel = "";
        for(final Panel p : this.dashboard.getPanels()){
            if(p.getType().equals("row")){
                currentRowPanel = p.getTitle().trim();
            } else if (p.equals(panel)){
                return currentRowPanel;
            } else {
                // clear the title right after passing the first panel after the row panel.
                currentRowPanel = "";
            }
        }
        return currentRowPanel;
    }

}
