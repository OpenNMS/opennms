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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.svclayer.api.KscReportService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>FormProcReportController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class FormProcReportController extends AbstractController implements InitializingBean {

    public enum Parameters {
        action,
        report_title,
        show_timespan,
        show_graphtype,
        graph_index,
        graphs_per_line
    }

    public enum Actions {
        Save,
        AddGraph,
        DelGraph,
        ModGraph
    }

    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        KscReportEditor editor = KscReportEditor.getFromSession(request.getSession(), true);
        
        // Get The Customizable Report 
        Report report = editor.getWorkingReport();

        // Get Form Variables
        String action = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.action.toString()));
        String report_title = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.report_title.toString()));
        String show_timespan = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.show_timespan.toString()));
        String show_graphtype = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.show_graphtype.toString()));
        String g_index = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.graph_index.toString()));
        int graph_index = WebSecurityUtils.safeParseInt(g_index);
        int graphs_per_line = WebSecurityUtils.safeParseInt(request.getParameter(Parameters.graphs_per_line.toString()));
     
        // Save the global variables into the working report
        report.setTitle(report_title);
        if (show_graphtype == null) {
            report.setShow_graphtype_button(false);
        } else {
            report.setShow_graphtype_button(true);
        }
        
        if (show_timespan == null) {
            report.setShow_timespan_button(false);
        } else {
            report.setShow_timespan_button(true);
        } 
        
        if (graphs_per_line > 0) {
            report.setGraphs_per_line(graphs_per_line);
        } else {
            report.setGraphs_per_line(0);
        } 

        if (Actions.Save.toString().equals(action)) {
            // The working model is complete now... lets save working model to configuration file 
            try {
                // First copy working report into report arrays
                editor.unloadWorkingReport(getKscReportFactory());
                // Save the changes to the config file
                getKscReportFactory().saveCurrent();
                // Go ahead and unload the editor from the session since we're done using it
                KscReportEditor.unloadFromSession(request.getSession());
            } catch (Throwable e) {
                throw new ServletException("Couldn't save report: " + e.getMessage(), e);
            }
        } else {
            if (Actions.AddGraph.toString().equals(action) || Actions.ModGraph.toString().equals(action)) {
                // Making a graph change... load it into the working area (the graph_index of -1 indicates a new graph)
                editor.loadWorkingGraph(graph_index);
            } else {
                if (Actions.DelGraph.toString().equals(action)) { 
                    report.removeGraph(report.getGraph(graph_index));
                } else {
                    throw new ServletException("Invalid Argument for Customize Form Action.");
                }
            }
        }
        
        if (Actions.Save.toString().equals(action)) {
            return new ModelAndView("redirect:/KSC/index.htm");
        } else if (Actions.DelGraph.toString().equals(action)) {
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (Actions.AddGraph.toString().equals(action)) {
            return new ModelAndView("redirect:/KSC/customGraphChooseParentResource.htm");
        } else if (Actions.ModGraph.toString().equals(action)) {
            Graph graph = editor.getWorkingGraph();
            OnmsResource resource = getKscReportService().getResourceFromGraph(graph);
            String graphType = graph.getGraphtype();
            
            Map<String,String> modelData = new HashMap<String,String>();
            modelData.put(CustomGraphEditDetailsController.Parameters.resourceId.toString(), resource.getId());
            modelData.put(CustomGraphEditDetailsController.Parameters.graphtype.toString(), graphType);
            return new ModelAndView("redirect:/KSC/customGraphEditDetails.htm", modelData);
        } else {
            throw new IllegalArgumentException("Parameter action of '" + action + "' is not supported.  Must be one of: Save, Cancel, Update, AddGraph, or DelGraph");
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
    public void afterPropertiesSet() {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
    }

}
