/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.controller.admin.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReport;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
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
    private SystemReport m_systemReport;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        String operation = request.getParameter("operation");
        
        if (!StringUtils.hasText(operation)) {
            operation = "list";
        }
        
        LogUtils.debugf(this, "Calling operation %s in SystemReportController", operation);
        
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

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_systemReport);
    }
}
