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
package org.opennms.netmgt.flows.elastic;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        cfg.setAutoImports(ImmutableMap.builder()
                                       .put("onms", "common.ftl")
                                       .build());
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

    public String getSeriesFromQuery(Collection<String> from, long step, long start, long end,
                                     String groupByTerm, List<Filter> filters) {
        final var builder = ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("from", from)
                .put("groupByTerm", groupByTerm)
                .put("step", step)
                .put("start", start)
                .put("end", end);
        getSnmpInterfaceId(filters).ifPresent(iif -> builder.put("snmpInterfaceId", iif));
        return render("series_for_terms.ftl", builder.build());
    }

    public String getSeriesFromQuery(int size, long step, long start, long end,
                                     String groupByTerm, List<Filter> filters) {
        final var builder = ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("size", size)
                .put("groupByTerm", groupByTerm)
                .put("step", step)
                .put("start", start)
                .put("end", end);
        getSnmpInterfaceId(filters).ifPresent(iif -> builder.put("snmpInterfaceId", iif));
        return render("series_for_terms.ftl", builder.build());
    }

    public String getSeriesFromMissingQuery(long step, long start, long end, String groupByTerm,
                                            String keyForMissingTerm, List<Filter> filters) {
        final var builder = ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("groupByTerm", groupByTerm)
                .put("keyForMissingTerm", keyForMissingTerm)
                .put("step", step)
                .put("start", start)
                .put("end", end);
        getSnmpInterfaceId(filters).ifPresent(iif -> builder.put("snmpInterfaceId", iif));
        return render("series_for_missing.ftl", builder.build());
    }

    public String getSeriesFromOthersQuery(Collection<String> from, long step, long start, long end,
                                           String groupByTerm, boolean excludeMissing,
                                           List<Filter> filters) {
        final var builder = ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("from", from)
                .put("groupByTerm", groupByTerm)
                .put("excludeMissing", excludeMissing)
                .put("step", step)
                .put("start", start)
                .put("end", end);
        getSnmpInterfaceId(filters).ifPresent(iif -> builder.put("snmpInterfaceId", iif));
        return render("series_for_others.ftl", builder.build());
    }

    public String getApplicationsQuery(String prefix, long limit, List<Filter> filters) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(filters);
        return render("aggregate_by_fuzzed_field.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("N", limit)
                .put("field", "netflow.application")
                .put("prefix", prefix)
                .build());
    }

    public String getHostsQuery(String regex, long limit, List<Filter> filters) {
        Objects.requireNonNull(filters);

        return render("aggregate_by_regex.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("regex", regex)
                .put("limit", limit)
                .put("field", "hosts")
                .build());
    }

    public String getConversationsRegexQuery(String regex, long limit, List<Filter> filters) {
        Objects.requireNonNull(filters);

        return render("aggregate_by_regex.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("regex", regex)
                .put("limit", limit)
                .put("field", "netflow.convo_key")
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

    private Optional<Integer> getSnmpInterfaceId(List<Filter> filters) {
        return filters.stream()
                .filter(f -> f instanceof SnmpInterfaceIdFilter)
                .map(f -> (SnmpInterfaceIdFilter)f)
                .map(SnmpInterfaceIdFilter::getSnmpInterfaceId)
                .findFirst();
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

    @Override
    public String visit(final DscpFilter dscpFilter) {
        return render("filter_term.ftl", ImmutableMap.builder()
                .put("term", "netflow.dscp")
                .put("values", dscpFilter.getDscp())
                .build());
    }

    public String getHostnameByConversationQuery(final String convoKey, final List<Filter> filters) {
        return render("hostname_by_convo.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("convoKey", convoKey)
                .build());
    }

    public String getHostnameByHostQuery(final String host, final List<Filter> filters) {
        return render("hostname_by_host.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("host", host)
                .build());
    }

    public String getAllValues(String field, int fieldSize, List<Filter> filters) {
        return render("field_values_for_all.ftl", ImmutableMap.builder()
                .put("filters", getFilterQueries(filters))
                .put("field", field)
                .put("fieldSize", fieldSize)
                .build());
    }
}
