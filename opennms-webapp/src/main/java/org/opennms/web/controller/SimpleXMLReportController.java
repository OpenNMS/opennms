/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.report.availability.AvailabilityReportViewerService;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class SimpleXMLReportController extends AbstractController {

    private int m_reportId;

    private String m_format;
    
    private AvailabilityReportViewerService m_viewerService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse res) throws Exception {
        String[] requiredParameters = new String[] { "reportid" };

        for (String requiredParameter : requiredParameters) {
            if (request.getParameter(requiredParameter) == null) {
                throw new MissingParameterException(requiredParameter,
                                                    requiredParameters);
            }
        }
        try {
            m_reportId = WebSecurityUtils.safeParseInt(request.getParameter("reportid"));
        } catch (NumberFormatException e) {
        }
        ModelAndView mav = new ModelAndView();
        if (m_format.equalsIgnoreCase("pdf")) {
            mav.setViewName("/report/availability/pdfreportviewer");
        } else if (m_format.equalsIgnoreCase("svg")) {
            mav.setViewName("/report/availability/svgreportviewer");
        } else {
            mav.setViewName("/report/availability/htmlreportviewer");
        }
        m_viewerService.setReportId(m_reportId);
        mav.addObject("source", m_viewerService.createSource());
        return mav;
    }


    public AvailabilityReportViewerService getViewerService() {
        return m_viewerService;
    }

    public void setViewerService(AvailabilityReportViewerService service) {
        m_viewerService = service;
    }
    
    public void setFormat(String format) {
        m_format = format;
    }

}
