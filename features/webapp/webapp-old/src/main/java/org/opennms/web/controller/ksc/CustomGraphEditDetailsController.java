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
// Modifications:
//
// 2008 Feb 03: Use Assert.state in afterPropertiesSet().  Use KscReportEditor
//              for tracking editing state in the user's session. - dj@opennms.org
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

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.KscReportEditor;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.MissingParameterException;
import org.opennms.web.graph.KscResultSet;
import org.opennms.web.svclayer.KscReportService;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class CustomGraphEditDetailsController extends AbstractController implements InitializingBean {
    
    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;
    private ResourceService m_resourceService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String resourceId = request.getParameter("resourceId");
        if (resourceId == null) {
            throw new MissingParameterException("resourceId");
        }
        
        //optional parameter graphtype
        String prefabReportName = request.getParameter("graphtype");
        
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
    
    public PrefabGraph getPrefabGraphFromList(PrefabGraph[] graphs, String name) {
        for (PrefabGraph graph : graphs) {
            if (graph.getName().equals(name)) {
                return graph;
            }
        }
        return null;
    }

    public ResourceService getResourceService() {
        return m_resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }

    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_resourceService != null, "property resourceService must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
    }

}
