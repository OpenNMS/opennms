//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Sep 28: Handle XSS scripting issues. - ranger@opennms.org
// 2008 Feb 03: Use Assert.state in afterPropertiesSet().  Use KscReportEditor
//              for tracking editing state in the user's session.
//              Refactor handleRequestInternal(). - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.web.controller.ksc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.KscReportEditor;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.XssRequestWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class FormProcMainController extends AbstractController implements InitializingBean {
    
    private KSC_PerformanceReportFactory m_kscReportFactory;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String action = WebSecurityUtils.sanitizeString(request.getParameter("report_action"));

        if (action == null) {
            throw new MissingParameterException("report_action");
        }

        KscReportEditor editor = KscReportEditor.getFromSession(req.getSession(), false);
        
        if (action.equals("Customize")) {
            editor.loadWorkingReport(getKscReportFactory(), getReportIndex(req));
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (action.equals("CreateFrom")) {
            editor.loadWorkingReportDuplicate(getKscReportFactory(), getReportIndex(req));
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (action.equals("Delete")) {
            getKscReportFactory().deleteReportAndSave(getReportIndex(req)); 
            return new ModelAndView("redirect:/KSC/index.htm");
        } else if (action.equals("Create")) {
            editor.loadNewWorkingReport();
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (action.equals("View")) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customView.htm");
            modelAndView.addObject("report", getReportIndex(req));
            modelAndView.addObject("type", "custom");
            return modelAndView;
        } else {
            throw new ServletException ("Invalid Parameter contents for report_action: " + action);
        }
    }

    private int getReportIndex(HttpServletRequest request) {
        String report = WebSecurityUtils.sanitizeString(request.getParameter("report"));
        if (report == null) {
            throw new MissingParameterException("report");
        } 
        return WebSecurityUtils.safeParseInt(report);
    }

    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
    }
}
