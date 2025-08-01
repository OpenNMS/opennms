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
package org.opennms.web.controller.admin.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.systemreport.SystemReport;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * SystemReportController
 *
 * @author ranger
 * @since 1.8.6
 */
public class SystemReportController extends AbstractController implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(SystemReportController.class);

    private SystemReport m_systemReport;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        String operation = request.getParameter("operation");
        
        if (!StringUtils.hasText(operation)) {
            operation = "list";
        }
        
        LOG.debug("Calling operation {} in SystemReportController", operation);
        
        if ("run".equalsIgnoreCase(operation)){
            SystemReportFormatter formatter = null;
            final String formatterName = request.getParameter("formatter");
            
            for (final SystemReportFormatter f : m_systemReport.getFormatters()) {
                if (f.getName().equals(formatterName)) {
                    formatter = f;
                    break;
                }
            }
            
            if (formatter == null) {
                throw new FormatterNotFoundException("Unable to locate formatter plugin for format type '" + formatterName + "'");
            }
            
            final List<String> selectedPlugins = Arrays.asList(request.getParameterValues("plugins"));
            final List<SystemReportPlugin> plugins = new ArrayList<>();
            for (final SystemReportPlugin plugin : m_systemReport.getPlugins()) {
                if (selectedPlugins.contains(plugin.getName())) {
                    plugins.add(plugin);
                }
            }
            
            return new ModelAndView(new FormatterView(formatter), "report", new SystemReportInfo(Arrays.asList(formatter), plugins));
        } else {
            return new ModelAndView("redirect:/admin/support/systemReportList.htm");
        }
    }

    public void setSystemReport(final SystemReport report) {
        m_systemReport = report;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_systemReport);
    }
}
