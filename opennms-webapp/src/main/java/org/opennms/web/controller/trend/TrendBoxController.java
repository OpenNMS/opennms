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
