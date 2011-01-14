//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// Created: February 18th, 2010 jonathan@opennms.org
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.web.svclayer.CategoryConfigService;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * <p>OnlineReportController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class OnlineReportController extends SimpleFormController {
    
    private ReportWrapperService m_reportWrapperService;
    private CategoryConfigService m_catConfigService;
    private CategoryDao m_categoryDao;
    
    /**
     * <p>Constructor for OnlineReportController.</p>
     */
    public OnlineReportController() {
        setFormView("report/database/onlineReport");
    }
    
    /** {@inheritDoc} */
    @Override
    protected Object formBackingObject(HttpServletRequest req) throws Exception {
        return m_reportWrapperService.getParameters(req.getParameter("reportId"));
    }

    /** {@inheritDoc} */
    @Override
    protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
        String reportId = req.getParameter("reportId");
        Map<String, Object> data = new HashMap<String, Object>();
        List<ReportFormat> formats = m_reportWrapperService.getFormats(reportId);
        data.put("formats", formats);
        List<String> onmsCategories = m_categoryDao.getAllCategoryNames();
        data.put("onmsCategories", onmsCategories);
        List<String> categories = m_catConfigService.getCategoriesList();
        data.put("categories", categories);
        return data;

    }
    
    /** {@inheritDoc} */
    @Override
    protected void initBinder(HttpServletRequest req,
            ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(
                                    Date.class,
                                    new CustomDateEditor(
                                                         new SimpleDateFormat(
                                                                              "yyyy-MM-dd"),
                                                         true));
    }
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        ReportParameters parameters = (ReportParameters) command;
        if ((parameters.getFormat() == ReportFormat.PDF)
                || (parameters.getFormat() == ReportFormat.SVG) ) {
            response.setContentType("application/pdf;charset=UTF-8");
            response.setHeader("Content-disposition", "inline; filename=report.pdf");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "cache");
            response.setHeader("Cache-Control", "must-revalidate");
        }
        m_reportWrapperService.runAndRender(parameters, ReportMode.IMMEDIATE, response.getOutputStream());        
        return null;
    }
    
    /**
     * <p>setReportWrapperService</p>
     *
     * @param reportWrapperService a {@link org.opennms.reporting.core.svclayer.ReportWrapperService} object.
     */
    public void setReportWrapperService(ReportWrapperService reportWrapperService) {
        m_reportWrapperService = reportWrapperService;
    }
    
    /**
     * <p>setCategoryConfigService</p>
     *
     * @param catConfigService a {@link org.opennms.web.svclayer.CategoryConfigService} object.
     */
    public void setCategoryConfigService(CategoryConfigService catConfigService) {
        m_catConfigService = catConfigService;
    }
    
    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
    	m_categoryDao = categoryDao;
    }
}
