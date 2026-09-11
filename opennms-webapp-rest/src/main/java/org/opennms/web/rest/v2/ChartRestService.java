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
package org.opennms.web.rest.v2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.ChartConfigFactory;
import org.opennms.netmgt.config.charts.BarChart;
import org.opennms.netmgt.config.charts.Rgb;
import org.opennms.netmgt.config.charts.SeriesDef;
import org.opennms.netmgt.config.charts.SubTitle;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.v2.api.ChartRestApi;
import org.opennms.web.rest.v2.model.ChartDataDto;
import org.opennms.web.rest.v2.model.ChartSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Serves the bar charts of chart-configuration.xml as data, so the dashboard
 * can draw them client-side. The queries are the ones the legacy charts page
 * rendered through JFreeChart; nothing here is writable.
 */
@Service
public class ChartRestService implements ChartRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(ChartRestService.class);

    // the only sub-label class the legacy config ships; it maps numeric severities to names
    private static final String SEVERITY_SUB_LABELS = "org.opennms.web.charts.SeveritySubLabels";

    @Override
    public Response getCharts() {
        try {
            final List<ChartSummaryDto> summaries = new ArrayList<>();
            for (final BarChart chart : configuredCharts()) {
                summaries.add(fill(new ChartSummaryDto(), chart));
            }
            return Response.ok(summaries).build();
        } catch (final IOException e) {
            LOG.error("Unable to read the chart configuration", e);
            return Response.serverError().build();
        }
    }

    @Override
    public Response getChart(final String name) {
        try {
            final Optional<BarChart> chart = configuredCharts().stream()
                    .filter(c -> c.getName() != null && c.getName().equals(name))
                    .findFirst();
            if (chart.isEmpty()) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(evaluate(chart.get())).build();
        } catch (final IOException e) {
            LOG.error("Unable to read the chart configuration", e);
            return Response.serverError().build();
        } catch (final SQLException e) {
            LOG.error("Unable to run the queries of chart '{}'", name, e);
            return Response.serverError().build();
        }
    }

    private static Collection<BarChart> configuredCharts() throws IOException {
        synchronized (ChartConfigFactory.class) {
            ChartConfigFactory.init();
            ChartConfigFactory.getInstance().update();
            return ChartConfigFactory.getInstance().getConfiguration().getBarChartCollection();
        }
    }

    private static <T extends ChartSummaryDto> T fill(final T dto, final BarChart chart) {
        dto.setName(chart.getName());
        dto.setTitle(chart.getTitle() == null ? chart.getName() : chart.getTitle().getValue());
        dto.setSubTitle(chart.getSubTitleCollection().stream()
                .map(SubTitle::getTitle)
                .filter(t -> t != null && t.getValue() != null)
                .map(t -> t.getValue())
                .findFirst()
                .orElse(null));
        dto.setDomainAxisLabel(chart.getDomainAxisLabel());
        dto.setRangeAxisLabel(chart.getRangeAxisLabel());
        return dto;
    }

    private static ChartDataDto evaluate(final BarChart chart) throws SQLException {
        final ChartDataDto dto = fill(new ChartDataDto(), chart);
        // category order is first appearance across the series, as JFreeChart's dataset did
        final Map<String, ChartDataDto.Category> categories = new LinkedHashMap<>();
        final List<Map<String, Number>> valuesBySeries = new ArrayList<>();
        final boolean severityLabels = chart.getSubLabelClass().map(SEVERITY_SUB_LABELS::equals).orElse(false);

        try (Connection conn = DataSourceFactory.getInstance().getConnection()) {
            for (final SeriesDef def : chart.getSeriesDefCollection()) {
                final Map<String, Number> values = new LinkedHashMap<>();
                try (Statement statement = conn.createStatement();
                     ResultSet rs = statement.executeQuery(def.getJdbcDataSet().getSql())) {
                    final int columns = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        final String key = String.valueOf(rs.getObject(1));
                        final Object value = columns > 1 ? rs.getObject(2) : null;
                        values.put(key, value instanceof Number ? (Number) value : null);
                        categories.computeIfAbsent(key, k -> new ChartDataDto.Category(k, severityLabels ? severityLabel(k) : k));
                    }
                }
                valuesBySeries.add(values);
                final ChartDataDto.Series series = new ChartDataDto.Series();
                series.setName(def.getSeriesName());
                series.setColor(def.getRgb().map(ChartRestService::hex).orElse(null));
                dto.getSeries().add(series);
            }
        }

        dto.getCategories().addAll(categories.values());
        for (int i = 0; i < dto.getSeries().size(); i++) {
            final Map<String, Number> values = valuesBySeries.get(i);
            final List<Number> aligned = new ArrayList<>();
            for (final String key : categories.keySet()) {
                aligned.add(values.get(key));
            }
            dto.getSeries().get(i).setValues(aligned);
        }
        return dto;
    }

    private static String severityLabel(final String key) {
        try {
            return OnmsSeverity.get(Integer.parseInt(key)).getLabel();
        } catch (final RuntimeException e) {
            return key;
        }
    }

    private static String hex(final Rgb rgb) {
        return String.format("#%02x%02x%02x",
                rgb.getRed().getRgbColor(), rgb.getGreen().getRgbColor(), rgb.getBlue().getRgbColor());
    }
}
