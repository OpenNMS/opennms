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
//              for tracking editing state in the user's session.  Format code. - dj@opennms.org
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
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.KscReportService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>FormProcViewController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class FormProcViewController extends AbstractController implements InitializingBean {

    public enum Parameters {
        action,
        domain,
        timespan,
        type,
        report,
        graphtype
    }

    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get Form Variables
        int report_index = -1; 
        String override_timespan = null;
        String override_graphtype = null;
        String report_action = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.action.toString()));
        String domain = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.domain.toString()));
        if (report_action == null) {
            throw new MissingParameterException ("action", new String[] {"action", "report", "type"});
        }
        String report_type = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.type.toString()));
        if (report_type == null) {
            throw new MissingParameterException ("type", new String[] {"action", "report", "type"});
        }

        if (report_action.equals("Customize") || report_action.equals("Update")) {
            String r_index = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.report.toString()));
            if (r_index != null && !r_index.equals("null")) {
               report_index = WebSecurityUtils.safeParseInt(r_index); 
            } else if (domain == null) {
                throw new MissingParameterException("report or domain", new String[] {"report or domain" , "type"});
            }
            override_timespan = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.timespan.toString()));
            if ((override_timespan == null) || override_timespan.equals("null")) {
                override_timespan = "none";
            }
            override_graphtype = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.graphtype.toString()));
            if (override_graphtype == null || override_graphtype.equals("null")) {
                override_graphtype = "none";
            }
            if (report_action.equals("Customize")) {
             // Fetch the KscReportEditor or create one if there isn't one already
                KscReportEditor editor = KscReportEditor.getFromSession(request.getSession(), false);
                
                if (report_type.equals("node")) {
                    editor.loadWorkingReport(m_kscReportService.buildNodeReport(report_index)); 
                } else if (report_type.equals("domain")) {
                    editor.loadWorkingReport(m_kscReportService.buildDomainReport(domain));
                } else { 
                    editor.loadWorkingReport(getKscReportFactory(), report_index);
                }
                
                // Now inject any override characteristics into the working report model
                Report working_report = editor.getWorkingReport();
                for (int i=0; i<working_report.getGraphCount(); i++) {
                    Graph working_graph = working_report.getGraph(i);
                    if (!override_timespan.equals("none")) { 
                        working_graph.setTimespan(override_timespan); 
                    }
                    if (!override_graphtype.equals("none")) { 
                        working_graph.setGraphtype(override_graphtype); 
                    }
                }
            }
        } else { 
            if (!report_action.equals("Exit")) {
                throw new ServletException ("Invalid Parameter contents for report_action");
            }
        }
        
        if (report_action.equals("Update")) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customView.htm");
            modelAndView.addObject("type", report_type);

            if (report_index >= 0) {
                modelAndView.addObject("report", report_index);
            }
            if (domain != null) {
                modelAndView.addObject("domain", domain);
            }
            if (override_timespan != null) { 
                modelAndView.addObject("timespan", override_timespan);
            }
            if (override_graphtype != null) { 
                modelAndView.addObject("graphtype", override_graphtype);
            }

            return modelAndView;
        } else if (report_action.equals("Customize")) { 
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (report_action.equals("Exit")) {
            return new ModelAndView("redirect:/KSC/index.htm");
        } else {
            throw new IllegalArgumentException("parameter action of '" + report_action + "' is not supported.  Must be one of: Update, Customize, or Exit");
        }
    }

    /**
     * <p>getKscReportFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    /**
     * <p>setKscReportFactory</p>
     *
     * @param kscReportFactory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
    }

    /**
     * <p>getKscReportService</p>
     *
     * @return a {@link org.opennms.web.svclayer.KscReportService} object.
     */
    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    /**
     * <p>setKscReportService</p>
     *
     * @param kscReportService a {@link org.opennms.web.svclayer.KscReportService} object.
     */
    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

   

}
