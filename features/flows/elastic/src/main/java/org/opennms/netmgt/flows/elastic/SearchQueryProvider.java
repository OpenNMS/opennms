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

package org.opennms.netmgt.flows.elastic;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * This makes it cleaner than storing the search queries as multiline strings
 * in Java code, and is much less verbose than storing these as POJOs.
 *
 */
public class SearchQueryProvider implements FilterVisitor<String> {

    private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    public SearchQueryProvider() {
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

    public String getUniqueNodeExporters(long size, List<Filter> filters) {
        return render("unique_node_exporters.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("size", size)
                .build());
    }

    public String getUniqueSnmpInterfaces(long size, List<Filter> filters) {
        return render("unique_snmp_interfaces.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("size", size)
                .build());
    }

    public String getTopNQuery(int N, String groupByTerm, String keyForMissingTerm, List<Filter> filters) {
        return render("top_n_terms.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("N", N)
                .put("groupByTerm", groupByTerm)
                .put("keyForMissingTerm", keyForMissingTerm != null ? keyForMissingTerm : "")
                .build());
    }

    public String getSeriesFromTopNQuery(List<String> topN, long step, long start, long end,
                                         String groupByTerm, List<Filter> filters) {
        return render("series_for_terms.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("topN", topN)
                .put("groupByTerm", groupByTerm)
                .put("step", step)
                .put("start", start)
                .put("end", end)
                .build());
    }

    public String getSeriesFromMissingQuery(long step, long start, long end, String groupByTerm,
                                            String keyForMissingTerm, List<Filter> filters) {
        return render("series_for_missing.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("groupByTerm", groupByTerm)
                .put("keyForMissingTerm", keyForMissingTerm)
                .put("step", step)
                .put("start", start)
                .put("end", end)
                .build());
    }

    public String getSeriesFromOthersQuery(List<String> topN, long step, long start, long end,
                                           String groupByTerm, boolean excludeMissing,
                                           List<Filter> filters) {
        return render("series_for_others.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("topN", topN)
                .put("groupByTerm", groupByTerm)
                .put("excludeMissing", excludeMissing)
                .put("step", step)
                .put("start", start)
                .put("end", end)
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
}
