/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.ksc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.svclayer.api.KscReportService;
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
    public enum Parameters {
        action,
        timespan,
        graphtype,
        title,
        graphindex
    }

    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        KscReportEditor editor = KscReportEditor.getFromSession(request.getSession(), true);
        
        // Get The Customizable (Working) Graph 
        Graph graph = editor.getWorkingGraph();

        // Get Form Variables
        String action = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.action.toString()));
        String timespan = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.timespan.toString()));
        String graphtype = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.graphtype.toString()));
        String title = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.title.toString()));
        String g_index = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.graphindex.toString()));
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
     * @return a {@link org.opennms.web.svclayer.api.KscReportService} object.
     */
    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    /**
     * <p>setKscReportService</p>
     *
     * @param kscReportService a {@link org.opennms.web.svclayer.api.KscReportService} object.
     */
    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
    }
}
