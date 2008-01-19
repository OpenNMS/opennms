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
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.KscReportService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class FormProcViewController extends AbstractController implements InitializingBean {
    
    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get Form Variables
        int report_index = 0; 
        String override_timespan = null;
        String override_graphtype = null;
        String report_action = request.getParameter("action");
        String domain = request.getParameter("domain");
        if (report_action == null) {
            throw new MissingParameterException ("action", new String[] {"action","report","type"});
        }
        String report_type = request.getParameter("type");
        if (report_type == null) {
            throw new MissingParameterException ("type", new String[] {"action","report","type"});
        }

        if ((report_action.equals("Customize")) || (report_action.equals("Update"))) {
            String r_index = request.getParameter("report");
            if (r_index != null && !r_index.equals("null")) {
               report_index = WebSecurityUtils.safeParseInt(r_index); 
            } else if (domain == null) {
                throw new MissingParameterException("report or domain", new String[] {"report or domain","type"});
            }
            override_timespan = request.getParameter("timespan");
            if ((override_timespan == null) || (override_timespan.equals("null"))) {
                override_timespan = "none";
            }
            override_graphtype = request.getParameter("graphtype");
            if ((override_graphtype == null) || (override_graphtype.equals("null"))) {
                override_graphtype = "none";
            }
            if (report_action.equals("Customize")) {
                if (report_type.equals("node")) {
                    Report report = m_kscReportService.buildNodeReport(report_index);
                    getKscReportFactory().loadWorkingReport(report); 
                    getKscReportFactory().setWorkingReportIndex(-1); // Must set index to -1 to make customizer create a new report, not replace
                } else if (report_type.equals("domain")) {
                    Report report = m_kscReportService.buildDomainReport(domain);
                    getKscReportFactory().loadWorkingReport(report); 
                    getKscReportFactory().setWorkingReportIndex(-1); // Must set index to -1 to make customizer create a new report, not replace
                } else { 
                    // Go ahead and tell report factory to put the indexed report config into the working report area
                    getKscReportFactory().loadWorkingReport(report_index);
                }
                // Now inject any override characteristics into the working report model
                Report working_report = getKscReportFactory().getWorkingReport();
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
            modelAndView.addObject("report", report_index);
            modelAndView.addObject("domain", domain);
            modelAndView.addObject("type", report_type);
            
            if (override_timespan != null) { 
                modelAndView.addObject("timespan", override_timespan);
            }
            if (override_graphtype != null) { 
                modelAndView.addObject("graphtype", override_graphtype);
            }

            return modelAndView;
        } else if (report_action.equals("Customize")) { 
            return new ModelAndView("redirect:/KSC/customReport.htm", "report", report_index);
        } else if (report_action.equals("Exit")) {
            return new ModelAndView("redirect:/KSC/index.htm");
        } else {
            throw new IllegalArgumentException("parameter action of '" + report_action + "' is not supported.  Must be one of: Update, Customize, or Exit");
        }
    }

    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_kscReportFactory == null) {
            throw new IllegalStateException("property kscReportFactory must be set");
        }
        
        if (m_kscReportService == null) {
            throw new IllegalStateException("property kscReportService must be set");
        }
    }

    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

   

}
