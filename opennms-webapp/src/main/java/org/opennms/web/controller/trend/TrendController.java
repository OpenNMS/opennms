/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.trend;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.trend.TrendConfiguration;
import org.opennms.netmgt.config.trend.TrendDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class TrendController extends AbstractController implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(TrendController.class);
    private final File CONFIG_FILE = new File("etc/trend-configuration.xml");

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final ModelAndView modelAndView = new ModelAndView("trend/trend");

        final TrendDefinition trendDefinition = getConfiguration().getTrendDefintionForName(request.getParameter("name"));

        if (trendDefinition != null) {
            final List<Double> valuesList = lookupData(trendDefinition.getQuery());
            final String valuesString = StringUtils.join(valuesList, ',');

            final Map<String, String> replacements = variableReplacements(valuesList);

            trendDefinition.setTitle(
                    replace(trendDefinition.getTitle(),
                            replacements)
            );

            trendDefinition.setSubtitle(
                    replace(trendDefinition.getSubtitle(),
                            replacements)
            );

            trendDefinition.setDescription(
                    replace(trendDefinition.getDescription(),
                            replacements)
            );

            modelAndView.addObject("trendDefinition", trendDefinition);
            modelAndView.addObject("trendValues", valuesList);
            modelAndView.addObject("trendValuesString", valuesString);
        } else {
            LOG.warn("trend definition is null for name '{}'", request.getParameter("name"));
        }

        return modelAndView;
    }

    private Map<String, String> variableReplacements(final List<Double> values) {
        final Map<String, String> replacements = new HashMap<>();

        final DoubleSummaryStatistics doubleSummaryStatistics = values.stream().mapToDouble(Double::doubleValue).summaryStatistics();

        replacements.put("${doubleMax}", String.format("%.2f", doubleSummaryStatistics.getMax()));
        replacements.put("${intMax}", String.format("%d", (int) doubleSummaryStatistics.getMax()));

        replacements.put("${doubleMin}", String.format("%.2f", doubleSummaryStatistics.getMin()));
        replacements.put("${intMin}", String.format("%d", (int) doubleSummaryStatistics.getMin()));

        replacements.put("${doubleAvg}", String.format("%.2f", doubleSummaryStatistics.getAverage()));
        replacements.put("${intAvg}", String.format("%d", (int) doubleSummaryStatistics.getAverage()));

        replacements.put("${doubleSum}", String.format("%.2f", doubleSummaryStatistics.getSum()));
        replacements.put("${intSum}", String.format("%d", (int) doubleSummaryStatistics.getSum()));

        for (int i = 0; i < values.size(); i++) {
            double current = values.get(i);

            replacements.put("${doubleValue[" + i + "]}", String.format("%.2f", current));
            replacements.put("${intValue[" + i + "]}", String.format("%d", (int) current));

            if (i > 0) {
                double previous = values.get(i - 1);
                double change = current - previous;

                replacements.put("${doubleValueChange[" + i + "]}", String.format("%+.2f", change));
                replacements.put("${intValueChange[" + i + "]}", String.format("%+d", (int) change));
            } else {
                replacements.put("${doubleValueChange[" + i + "]}", "NaN");
                replacements.put("${intValueChange[" + i + "]}", "NaN");
            }
        }

        if (values.size() > 0) {
            replacements.put("${doubleLastValueChange}", replacements.get("${doubleValueChange[" + (values.size() - 1) + "]}"));
            replacements.put("${intLastValueChange}", replacements.get("${intValueChange[" + (values.size() - 1) + "]}"));

            replacements.put("${doubleLastValue}", replacements.get("${doubleValue[" + (values.size() - 1) + "]}"));
            replacements.put("${intLastValue}", replacements.get("${intValue[" + (values.size() - 1) + "]}"));
        } else {
            replacements.put("${doubleLastValueChange}", "NaN");
            replacements.put("${intLastValueChange}", "NaN");

            replacements.put("${doubleLastValue}", "NaN");
            replacements.put("${intLastValue}", "NaN");
        }

        return replacements;
    }

    private String replace(String string, final Map<String, String> replacements) {
        for (final Map.Entry<String, String> replacement : replacements.entrySet()) {
            string = string.replace(replacement.getKey(), replacement.getValue());
        }

        return string;
    }

    public TrendConfiguration getConfiguration() {
        return JaxbUtils.unmarshal(TrendConfiguration.class, CONFIG_FILE);
    }

    public List<Double> lookupData(final String query) throws SQLException {
        final List<Double> dataSet = new ArrayList<>();

        Connection connection = null;

        try {
            connection = DataSourceFactory.getInstance().getConnection();

            final Statement statement = connection.createStatement();

            final ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                dataSet.add(resultSet.getDouble(1));
            }

            resultSet.close();
            statement.close();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return dataSet;
    }

    @Override
    public void afterPropertiesSet() {
    }
}
