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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.opennms.api.reporting.ReportException;
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
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/report/database/onlineReport.htm")
public class OnlineReportController {

    @Autowired
    private ReportWrapperService m_reportWrapperService;

    @Autowired
    private CategoryConfigService m_catConfigService;

    @Autowired
    private CategoryDao m_categoryDao;

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

    @RequestMapping(method=RequestMethod.GET)
    public void handleGet(ModelMap modelMap, @RequestParam("reportId") String reportId) {
        modelMap.addAttribute("formats", m_reportWrapperService.getFormats(reportId));
        modelMap.addAttribute("onmsCategories", m_categoryDao.getAllCategoryNames());
        modelMap.addAttribute("categories", m_catConfigService.getCategoriesList());
    }

    @ModelAttribute("parameters")
    public ReportParameters getReportParameters(@RequestParam("reportId") String reportId) {
       return m_reportWrapperService.getParameters(reportId);
    }

    @RequestMapping(method=RequestMethod.POST, params="cancel")
    public ModelAndView onCancel() {
        return new ModelAndView(new RedirectView("/report/database/reportList.htm", true));
    }

    @RequestMapping(method=RequestMethod.POST, params="run")
    public String handleSubmit(ModelMap modelMap, HttpServletResponse response, @ModelAttribute("parameters") ReportParameters parameters) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            m_reportWrapperService.runAndRender(parameters, ReportMode.IMMEDIATE, outputStream);
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
            response.getOutputStream().write(outputStream.toByteArray());
            return null;
        } catch (ReportException ex) {
            // forward to same page, but now show errors
            modelMap.addAttribute("errorMessage", ex.getMessage());
            modelMap.addAttribute("errorCause", ex.getCause());
            handleGet(modelMap, parameters.getReportId()); // add default view parameters
            return "report/database/onlineReport";
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }
}
