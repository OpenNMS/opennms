// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.report.availability.AvailabilityReportViewerService;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>SimpleXMLReportController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class SimpleXMLReportController extends AbstractController {

    private int m_reportId;

    private AvailabilityReportViewerService m_viewerService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse res) throws Exception {
        String[] requiredParameters = new String[] { "format", "reportid" };

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
        String format = WebSecurityUtils.sanitizeString(request.getParameter("format"));
        if (format.equalsIgnoreCase("pdf")) {
            mav.setViewName("/reports/pdfreportviewer");
        } else if (format.equalsIgnoreCase("svg")) {
            mav.setViewName("/reports/svgreportviewer");
        } else {
            mav.setViewName("/reports/htmlreportviewer");
        }
        m_viewerService.setReportId(m_reportId);
        mav.addObject("source", m_viewerService.createSource());
        return mav;
    }


    /**
     * <p>getViewerService</p>
     *
     * @return a {@link org.opennms.report.availability.AvailabilityReportViewerService} object.
     */
    public AvailabilityReportViewerService getViewerService() {
        return m_viewerService;
    }

    /**
     * <p>setViewerService</p>
     *
     * @param service a {@link org.opennms.report.availability.AvailabilityReportViewerService} object.
     */
    public void setViewerService(AvailabilityReportViewerService service) {
        m_viewerService = service;
    }

}
