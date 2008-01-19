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
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class FormProcMainController extends AbstractController implements InitializingBean {
    
    private KSC_PerformanceReportFactory m_kscReportFactory;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get Form Variables
        int report_index = 0; 
        String report_action = request.getParameter("report_action");
        
        if (report_action == null) {
            throw new MissingParameterException("report_action");
        }
          
        if ((report_action.equals("Customize")) || (report_action.equals("View")) || (report_action.equals("CreateFrom")) || (report_action.equals("Delete"))) {
            String r_index = request.getParameter("report");
            if (r_index == null) {
                throw new MissingParameterException("report");
            } 
            report_index = WebSecurityUtils.safeParseInt(r_index);
            if ((report_action.equals("Customize")) || (report_action.equals("CreateFrom"))) {  
                // Go ahead and tell report factory to put the report config into the working report area
                getKscReportFactory().loadWorkingReport(report_index);
                if (report_action.equals("CreateFrom")) {  // Need to set index to -1 for this case to have Customizer create new report index 
                   getKscReportFactory().setWorkingReportIndex(-1);
                }
            }
            if (report_action.equals("Delete")) {  // Take care of this case right now
                getKscReportFactory().deleteReportAndSave(report_index); 
            }
        } else { 
            if (report_action.equals("Create")) {
                report_index = -1;
               // Go ahead and tell report factory to put the report config (a blank config) into the working report area
               getKscReportFactory().loadWorkingReport(report_index);
            }
            else {
                throw new ServletException ("Invalid Parameter contents for report_action");
            }
        }
        
        if (report_action.equals("View")) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customView.htm");
            modelAndView.addObject("report", report_index);
            modelAndView.addObject("type", "custom");
            return modelAndView;
        } else { 
            if ((report_action.equals("Customize")) || (report_action.equals("Create")) || (report_action.equals("CreateFrom"))) {
                return new ModelAndView("redirect:/KSC/customReport.htm");
            } else {
                return new ModelAndView("redirect:/KSC/index.htm");
            } 
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
    }

   

}
