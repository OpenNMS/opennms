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

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.graph.KscResultSet;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.api.KscReportService;
import org.opennms.web.svclayer.api.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>CustomGraphEditDetailsController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class CustomGraphEditDetailsController extends AbstractController implements InitializingBean {
    
    public enum Parameters {
        resourceId,
        graphtype
    }
    
    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;
    private ResourceService m_resourceService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String resourceId = request.getParameter(Parameters.resourceId.toString());
        if (resourceId == null) {
            throw new MissingParameterException(Parameters.resourceId.toString());
        }
        
        //optional parameter graphtype
        String prefabReportName = request.getParameter(Parameters.graphtype.toString());
        
        KscReportEditor editor = KscReportEditor.getFromSession(request.getSession(), true);
        
        Report report = editor.getWorkingReport(); 
        org.opennms.netmgt.config.kscReports.Graph sample_graph = editor.getWorkingGraph(); 
        if (sample_graph == null) {
            throw new IllegalArgumentException("Invalid working graph argument -- null pointer. Possibly missing prefab report in snmp-graph.properties?");
        }

        // Set the resourceId in the working graph in case it changed
        sample_graph.setResourceId(resourceId);
        
        OnmsResource resource = getKscReportService().getResourceFromGraph(sample_graph);
        PrefabGraph[] graph_options = getResourceService().findPrefabGraphsForResource(resource);

        PrefabGraph display_graph = null;
        if (graph_options.length > 0) {
            if (prefabReportName == null) {
                display_graph = graph_options[0];
            } else {
                display_graph = getPrefabGraphFromList(graph_options, sample_graph.getGraphtype());
            }
        }
        
        Calendar begin_time = Calendar.getInstance();
        Calendar end_time = Calendar.getInstance();
        KSC_PerformanceReportFactory.getBeginEndTime(sample_graph.getTimespan(), begin_time, end_time);
        
        KscResultSet resultSet = new KscResultSet(sample_graph.getTitle(), begin_time.getTime(), end_time.getTime(), resource, display_graph);
        
        ModelAndView modelAndView = new ModelAndView("KSC/customGraphEditDetails");
        
        modelAndView.addObject("resultSet", resultSet);
        
        modelAndView.addObject("prefabGraphs", graph_options);
        
        modelAndView.addObject("timeSpans", getKscReportService().getTimeSpans(false));
        modelAndView.addObject("timeSpan", sample_graph.getTimespan());
        
        int graph_index = editor.getWorkingGraphIndex(); 
        int max_graphs = report.getGraphCount();
        if (graph_index == -1) {
            graph_index = max_graphs++;
        }
        modelAndView.addObject("graphIndex", graph_index);
        modelAndView.addObject("maxGraphIndex", max_graphs);
        
        return modelAndView;
    }
    
    /**
     * <p>getPrefabGraphFromList</p>
     *
     * @param graphs an array of {@link org.opennms.netmgt.model.PrefabGraph} objects.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PrefabGraph} object.
     */
    public PrefabGraph getPrefabGraphFromList(PrefabGraph[] graphs, String name) {
        for (PrefabGraph graph : graphs) {
            if (graph.getName().equals(name)) {
                return graph;
            }
        }
        return null;
    }

    /**
     * <p>getResourceService</p>
     *
     * @return a {@link org.opennms.web.svclayer.api.ResourceService} object.
     */
    public ResourceService getResourceService() {
        return m_resourceService;
    }

    /**
     * <p>setResourceService</p>
     *
     * @param resourceService a {@link org.opennms.web.svclayer.api.ResourceService} object.
     */
    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
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
        Assert.state(m_resourceService != null, "property resourceService must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
    }

}
