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
 * Modifications:
 * 
 * Created: December 21st, 2009 jonathan@opennms.org
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.controller;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.report.availability.svclayer.OnmsReportService;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class DownloadReportController extends AbstractController {

    private ReportStoreService m_reportStoreService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String[] requiredParameters = new String[] { "locatorId", "format" };

        for (String requiredParameter : requiredParameters) {
            if (request.getParameter(requiredParameter) == null) {
                throw new MissingParameterException(requiredParameter,
                                                    requiredParameters);
            }
        }

        try {
            Integer reportCatalogEntryId = new Integer(WebSecurityUtils.safeParseInt(request.getParameter("locatorId")));
            if (request.getParameter("format").equalsIgnoreCase("pdf")
                    || request.getParameter("format").equalsIgnoreCase("svg")) {
                response.setContentType("application/pdf;charset=UTF-8");
                response.setHeader("Content-disposition", "inline; filename="
                                   + reportCatalogEntryId.toString()
                                   + ".pdf");
                response.setHeader("Pragma", "public");
                response.setHeader("Cache-Control", "cache");
                response.setHeader("Cache-Control", "must-revalidate");
            }
            m_reportStoreService.render(
                                        reportCatalogEntryId,
                                        request.getParameter("format"),
                                        (OutputStream) response.getOutputStream());
        } catch (NumberFormatException e) {
            // TODO something usefule here.
        }

        return null;
    }
    
    public void setReportStoreService(ReportStoreService reportStoreService) {
        m_reportStoreService = reportStoreService;
    }

}
