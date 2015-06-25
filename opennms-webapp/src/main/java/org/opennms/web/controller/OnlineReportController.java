/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.web.svclayer.CategoryConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>OnlineReportController class.</p>
 */
@Controller
@RequestMapping("/report/database/onlineReport.htm")
public class OnlineReportController {

    public static final String COMMAND_NAME = "parameters";
    
    @Autowired
    private ReportWrapperService m_reportWrapperService;

    @Autowired
    private CategoryConfigService m_catConfigService;

    @Autowired
    private CategoryDao m_categoryDao;

    @RequestMapping(method=RequestMethod.GET)
    public void referenceData(ModelMap data, @RequestParam("reportId") String reportId) {

        List<ReportFormat> formats = m_reportWrapperService.getFormats(reportId);
        data.put("formats", formats);
        List<String> onmsCategories = m_categoryDao.getAllCategoryNames();
        data.put("onmsCategories", onmsCategories);
        List<String> categories = m_catConfigService.getCategoriesList();
        data.put("categories", categories);
    }

    @InitBinder
    public void initBinder(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(
                Date.class,
                new CustomDateEditor(
                        new SimpleDateFormat("yyyy-MM-dd"),
                        true
                )
        );
    }

    @ModelAttribute(COMMAND_NAME)
    public ReportParameters formBackingObject(HttpServletRequest req) throws Exception {
        return m_reportWrapperService.getParameters(req.getParameter("reportId"));
    }

    @RequestMapping(method=RequestMethod.POST)
    public void onSubmit(HttpServletResponse response, @ModelAttribute(COMMAND_NAME) ReportParameters parameters, BindingResult errors) throws IOException {

        if ((parameters.getFormat() == ReportFormat.PDF) || (parameters.getFormat() == ReportFormat.SVG) ) {
            response.setContentType("application/pdf;charset=UTF-8");
            response.setHeader("Content-disposition", "inline; filename=report.pdf");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "cache");
            response.setHeader("Cache-Control", "must-revalidate");
        }

        if(parameters.getFormat() == ReportFormat.CSV) {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-disposition", "inline; filename=report.csv");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "cache");
            response.setHeader("Cache-Control", "must-revalidate");
        }

        m_reportWrapperService.runAndRender(parameters, ReportMode.IMMEDIATE, response.getOutputStream());
    }
}
