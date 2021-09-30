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
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.trend.TrendConfiguration;
import org.opennms.netmgt.config.trend.TrendDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class TrendBoxController extends AbstractController implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(TrendBoxController.class);
    private final File CONFIG_FILE = new File("etc/trend-configuration.xml");

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final ModelAndView modelAndView = new ModelAndView("trend/trend-box");
        final List<TrendDefinition> filteredTrendDefinitions = getConfiguration().getTrendDefinitions().stream().filter(TrendDefinition::isVisible).collect(Collectors.toList());
        modelAndView.addObject("trendDefinitions", filteredTrendDefinitions);
        return modelAndView;
    }

    public TrendConfiguration getConfiguration() {
        return JaxbUtils.unmarshal(TrendConfiguration.class, CONFIG_FILE);
    }

    @Override
    public void afterPropertiesSet() {
    }
}
