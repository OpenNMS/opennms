/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
            final List<SystemReportPlugin> plugins = new ArrayList<SystemReportPlugin>();
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
