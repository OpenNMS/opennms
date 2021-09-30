/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
