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
public class SearchQueryProvider {

    private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    public SearchQueryProvider() {
        // Setup Freemarker
        cfg.setClassForTemplateLoading(getClass(), "");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String getFlowCountQuery(long start, long end) {
        return render("flow_count.ftl", ImmutableMap.builder()
                .put("start", start)
                .put("end", end)
                .build());
    }

    public String getTopNQuery(int N, long start, long end, String groupByTerm) {
        return render("top_n.ftl", ImmutableMap.builder()
                .put("start", start)
                .put("end", end)
                .put("N", N)
                .put("groupByTerm", groupByTerm)
                .build());
    }

    public String getSeriesFromTopNQuery(List<String> topN, long start, long end, long step, String groupByTerm) {
        return render("top_n_series.ftl", ImmutableMap.builder()
                .put("start", start)
                .put("end", end)
                .put("topN", topN)
                .put("step", step)
                .put("groupByTerm", groupByTerm)
                .build());
    }

    public String getTotalBytesFromTopNQuery(List<String> topN, long start, long end, String groupByTerm) {
        return render("top_n_totals.ftl", ImmutableMap.builder()
                .put("start", start)
                .put("end", end)
                .put("topN", topN)
                .put("groupByTerm", groupByTerm)
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
}
