//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// Modifications:
//
// 2008 Sep 28: Handle XSS scripting issues. - ranger@opennms.org
// 2008 Feb 03: Use Assert.state in afterPropertiesSet().  Use KscReportEditor
//              for tracking editing state in the user's session. - dj@opennms.org
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.KscReportEditor;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.KscReportService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * <p>FormProcGraphController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class FormProcGraphController extends AbstractController implements InitializingBean {
    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        KscReportEditor editor = KscReportEditor.getFromSession(request.getSession(), true);
        
        // Get The Customizable (Working) Graph 
        Graph graph = editor.getWorkingGraph();

        // Get Form Variables
        String action = WebSecurityUtils.sanitizeString(request.getParameter("action"));
        String timespan = WebSecurityUtils.sanitizeString(request.getParameter("timespan"));
        String graphtype = WebSecurityUtils.sanitizeString(request.getParameter("graphtype"));
        String title = WebSecurityUtils.sanitizeString(request.getParameter("title"));
        String g_index = WebSecurityUtils.sanitizeString(request.getParameter("graphindex"));
        int graph_index = WebSecurityUtils.safeParseInt(g_index);
        graph_index--; 
     
        // Save the modified variables into the working graph 
        graph.setTitle(title);
        graph.setTimespan(timespan);
        graph.setGraphtype(graphtype);
        
        OnmsResource resource = getKscReportService().getResourceFromGraph(graph);

        if (action.equals("Save")) {
            // The working graph is complete now... lets save working graph to working report 
            editor.unloadWorkingGraph(graph_index);
        }
        
        if (action.equals("Save") || action.equals("Cancel")) {
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (action.equals("Update")) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customGraphEditDetails.htm");
            modelAndView.addObject("resourceId", resource.getId());
            modelAndView.addObject("graphtype", graph.getGraphtype());
            return modelAndView;
        } else if (action.equals("ChooseResource")) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customGraphChooseResource.htm");
            modelAndView.addObject("resourceId", resource.getId());
            modelAndView.addObject("selectedResourceId", resource.getId());
            return modelAndView;
        } else {
            throw new IllegalArgumentException("parameter action of '" + action + "' is not supported.  Must be one of: Save, Cancel, Update, or ChooseResource");
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

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
    }
}
