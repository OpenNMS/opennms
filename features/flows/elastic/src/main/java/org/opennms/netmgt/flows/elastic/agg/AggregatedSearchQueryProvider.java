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
package org.opennms.netmgt.flows.elastic.agg;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.filter.api.DscpFilter;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.FilterVisitor;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Here we store the search queries in the class-path in the form
 * of Freemarker templates and use the templating to perform the parameter
 * substitution.
 *
 * We could consider removing this in favor of using the High-level Elasticsearch client.
 */
public class AggregatedSearchQueryProvider implements FilterVisitor<String> {

    private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    public AggregatedSearchQueryProvider() {
        // Setup Freemarker
        cfg.setClassForTemplateLoading(getClass(), "");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String getFlowCountQuery(List<Filter> filters) {
        return render("flow_count.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .build());
    }

    public String getSumQuery(GroupedBy groupedBy, List<Filter> filters) {
        return render("agg_sum.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("groupedBy", groupedBy)
                .build());
    }

    public String getTopNQuery(int N, GroupedBy groupedBy, String aggregationType, String key, List<Filter> filters) {
        return render("agg_top_n.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("N", N)
                .put("groupedBy", groupedBy)
                .put("aggregationType", aggregationType)
                .put("key", key)
                .build());
    }

    public String getSeriesFromTotalsQuery(GroupedBy groupedBy, long step, long start, long end, List<Filter> filters) {
        return render("series_totals.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("groupedBy", groupedBy)
                .put("step", step)
                .put("start", start)
                .put("end", end)
                .build());
    }

    public String getSeriesFromTopNQuery(int N, GroupedBy groupedBy, String aggregationType, String key, long step, long start, long end, List<Filter> filters) {
        return render("series_top_n.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("N", N)
                .put("groupedBy", groupedBy)
                .put("aggregationType", aggregationType)
                .put("key", key)
                .put("step", step)
                .put("start", start)
                .put("end", end)
                .build());
    }

    public String getAllTerms(GroupedBy groupedBy, String groupedByField, int fieldSize, List<Filter> filters) {
        return render("all_terms.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("groupedBy", groupedBy)
                .put("groupedByField", groupedByField)
                .put("fieldSize", fieldSize)
                .build());
    }

    private String render(String templateName, Map<Object, Object> context) {
        try {
            final StringWriter writer = new StringWriter();
            final Template template = cfg.getTemplate(templateName);
            template.process(context, writer);
            return writer.toString();
        } catch (IOException|TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getFilterQueries(List<Filter> filters) {
        return filters.stream()
                .map(f -> f.visit(this))
                .collect(Collectors.toList());
    }

    @Override
    public String visit(ExporterNodeFilter exporterNodeFilter) {
        return render("filter_exporter_node.ftl", ImmutableMap.builder()
                .put("nodeCriteria", exporterNodeFilter.getCriteria())
                .build());
    }

    @Override
    public String visit(TimeRangeFilter timeRangeFilter) {
        return render("filter_time_range.ftl", ImmutableMap.builder()
                .put("start", timeRangeFilter.getStart())
                .put("end", timeRangeFilter.getEnd())
                .build());
    }

    @Override
    public String visit(SnmpInterfaceIdFilter snmpInterfaceIdFilter) {
        return render("filter_snmp_interface.ftl", ImmutableMap.builder()
                .put("snmpInterfaceId", snmpInterfaceIdFilter.getSnmpInterfaceId())
                .build());
    }

    public String getHostname(final String host, List<Filter> filters) {
        return render("hostname.ftl", ImmutableMap.builder()
                                                  .put("filters", getFilterQueries(filters))
                                                  .put("host", host)
                                                  .build());
    }

    @Override
    public String visit(DscpFilter dscpFilter) {
        return render("filter_term.ftl", ImmutableMap.builder()
                .put("term", "dscp")
                .put("values", dscpFilter.getDscp())
                .build());
    }

}
