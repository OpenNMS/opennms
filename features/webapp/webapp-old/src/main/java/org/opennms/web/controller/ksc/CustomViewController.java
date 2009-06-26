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
// 2009 Jan 26: Modified handleRequestInternal - part of ksc performance improvement. - ayres@opennms.org
// 2008 Oct 22: Lots of cleanup.  - dj@opennms.org
// 2008 Sep 28: Handle XSS security issues. - ranger@opennms.org
// 2008 Feb 03: Use Asserts in afterPropertiesSet() and setDefaultGraphsPerLine().
//              Use new getReportByIndex method on the KSC factory. - dj@opennms.org
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.graph.KscResultSet;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.svclayer.KscReportService;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class CustomViewController extends AbstractController implements InitializingBean {
    
    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;
    private ResourceService m_resourceService;
    private int m_defaultGraphsPerLine = 0;
    private Executor m_executor;
    
    private Set<String> m_resourcesPendingPromotion = Collections.synchronizedSet(new HashSet<String>());

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] { "report or domain", "type" };
      
        // Get Form Variable
        String reportType = WebSecurityUtils.sanitizeString(request.getParameter("type"));
        if (reportType == null) {
            throw new MissingParameterException("type", requiredParameters);
        }
      
        String reportIdString = WebSecurityUtils.sanitizeString(request.getParameter("report"));
        String domain = WebSecurityUtils.sanitizeString(request.getParameter("domain"));
        int reportId = 0;
        if (reportIdString != null) {
            reportId = WebSecurityUtils.safeParseInt(reportIdString);
        } else if (domain == null) {
            throw new MissingParameterException("report or domain", requiredParameters);
        }
      
        String overrideTimespan = WebSecurityUtils.sanitizeString(request.getParameter("timespan"));
        if ("null".equals(overrideTimespan) || "none".equals(overrideTimespan)) {
            overrideTimespan = null;
        }

        String overrideGraphType = WebSecurityUtils.sanitizeString(request.getParameter("graphtype"));
        if ("null".equals(overrideGraphType) || "none".equals(overrideGraphType)) {
            overrideGraphType = null;
        }
      
        // Load report to view 
        Report report = null;
        if ("node".equals(reportType)) {
            report = getKscReportService().buildNodeReport(reportId);
        } else if ("domain".equals(reportType)) {
            report = getKscReportService().buildDomainReport(domain);
        } else if ("custom".equals(reportType)) {
            report = m_kscReportFactory.getReportByIndex(reportId);
        } else {
            throw new IllegalArgumentException("value to 'type' parameter of '" + reportType + "' is not supported.  Must be one of: node, domain, or custom");
        }
      
        if (report == null) {
            throw new ServletException("Report does not exist");
        }
      
        // Get the list of available prefabricated graph options 
        HashMap<String, OnmsResource> resourceMap = new HashMap<String, OnmsResource>();
        Set<PrefabGraph> prefabGraphs = new TreeSet<PrefabGraph>();
        List<Graph> graphCollection = report.getGraphCollection();
        if (!graphCollection.isEmpty()) {
            List<OnmsResource> resources = getKscReportService().getResourcesFromGraphs(graphCollection);
            for (int i = 0; i < graphCollection.size(); i++) {
                Graph graph = graphCollection.get(i);
                OnmsResource resource = resources.get(i);
                resourceMap.put(graph.toString(), resource);
                if (resource == null) {
                    log().debug("Could not get resource for graph " + graph + " in report " + report.getTitle());
                } else {
                    prefabGraphs.addAll(Arrays.asList(getResourceService().findPrefabGraphsForResource(resource)));
                }
            }
      
            // Get default graph type from first element of graph_options
            // XXX Do we care about the tests on reportType?
            if (("node".equals(reportType) || "domain".equals(reportType))
                    && overrideGraphType == null
                    && !prefabGraphs.isEmpty()) {
                // Get the name of the first item.  prefabGraphs is sorted.
                overrideGraphType = prefabGraphs.iterator().next().getName();
                if (log().isDebugEnabled()) {
                    log().debug("custom_view: setting default graph type to " + overrideGraphType);
                }
            }
        }
        
        List<KscResultSet> resultSets = new ArrayList<KscResultSet>(report.getGraphCount());
        for (Graph graph : graphCollection) {
            OnmsResource resource = resourceMap.get(graph.toString());
            if (resource != null) {
                promoteResourceAttributesIfNecessary(resource);
            }

            String displayGraphType;
            if (overrideGraphType == null) {
                displayGraphType = graph.getGraphtype();
            } else {
                displayGraphType = overrideGraphType;
            }
            
            PrefabGraph displayGraph;
            try {
                displayGraph = getResourceService().getPrefabGraph(displayGraphType);
            } catch (ObjectRetrievalFailureException e) {
                if (log().isDebugEnabled()) {
                    log().debug("The prefabricated graph '" + displayGraphType + "' does not exist: " + e, e);
                }
                displayGraph = null;
            }
            
            boolean foundGraph = false;
            if (resource != null) {
                for (PrefabGraph availableGraph : getResourceService().findPrefabGraphsForResource(resource)) {
                    if (availableGraph.equals(displayGraph)) {
                        foundGraph = true;
                        break;
                    }
                }
            }
            
            if (!foundGraph) {
                displayGraph = null;
            }
            
            // gather start/stop time information
            String displayTimespan = null;
            if (overrideTimespan == null) {
                displayTimespan = graph.getTimespan();
            } else {
                displayTimespan = overrideTimespan;
            }
            Calendar beginTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            KSC_PerformanceReportFactory.getBeginEndTime(displayTimespan, beginTime, endTime);
            
            KscResultSet resultSet = new KscResultSet(graph.getTitle(), beginTime.getTime(), endTime.getTime(), resource, displayGraph);
            resultSets.add(resultSet);
        }
        
        ModelAndView modelAndView = new ModelAndView("KSC/customView");

        modelAndView.addObject("loggedIn", request.getRemoteUser() != null);
        modelAndView.addObject("reportType", reportType);
        if (report != null) {
            modelAndView.addObject("report", reportIdString);
        }
        if (domain != null) {
            modelAndView.addObject("domain", domain);
        }
        
        modelAndView.addObject("title", report.getTitle());
        modelAndView.addObject("resultSets", resultSets);
        
        if (report.getShow_timespan_button()) {
            if (overrideTimespan == null || !getKscReportService().getTimeSpans(true).containsKey(overrideTimespan)) {
                modelAndView.addObject("timeSpan", "none");
            } else {
                modelAndView.addObject("timeSpan", overrideTimespan);
            }
            modelAndView.addObject("timeSpans", getKscReportService().getTimeSpans(true));
        } else {
            // Make sure it's null so the pulldown list isn't shown
            modelAndView.addObject("timeSpan", null);
        }

        if (report.getShow_graphtype_button()) {
            LinkedHashMap<String, String> graphTypes = new LinkedHashMap<String, String>();
            graphTypes.put("none", "none");
            for (PrefabGraph graphOption : prefabGraphs) {
                graphTypes.put(graphOption.getName(), graphOption.getName());
            }
            
            if (overrideGraphType == null || !graphTypes.containsKey(overrideGraphType)) {
                modelAndView.addObject("graphType", "none");
            } else {
                modelAndView.addObject("graphType", overrideGraphType);
            }
            modelAndView.addObject("graphTypes", graphTypes);
        } else {
            // Make sure it's null so the pulldown list isn't shown
            modelAndView.addObject("graphType", null);
        }
        
        modelAndView.addObject("showCustomizeButton", !request.isUserInRole(Authentication.READONLY_ROLE) && (request.getRemoteUser() != null));

        if (report.getGraphs_per_line() > 0) {
            modelAndView.addObject("graphsPerLine", report.getGraphs_per_line());
        } else {
            modelAndView.addObject("graphsPerLine", getDefaultGraphsPerLine());
        }
        
        return modelAndView;
    }

    private void promoteResourceAttributesIfNecessary(final OnmsResource resource) {
        boolean needToSchedule = m_resourcesPendingPromotion.add(resource.getId());
        if (needToSchedule) {
            m_executor.execute(new Runnable() {

                public void run() {
                        getResourceService().promoteGraphAttributesForResource(resource);
                        m_resourcesPendingPromotion.remove(resource.getId());
                }
                
            });
        }
        
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    public int getDefaultGraphsPerLine() {
        return m_defaultGraphsPerLine;
    }

    public void setDefaultGraphsPerLine(int defaultGraphsPerLine) {
        Assert.isTrue(defaultGraphsPerLine > 0, "property defaultGraphsPerLine must be greater than zero");

        m_defaultGraphsPerLine = defaultGraphsPerLine;
    }

    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

    public ResourceService getResourceService() {
        return m_resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
        Assert.state(m_resourceService != null, "property resourceService must be set");
        Assert.state(m_defaultGraphsPerLine != 0, "property defaultGraphsPerLine must be set");
        
        m_executor = Executors.newSingleThreadExecutor();
    }

}
