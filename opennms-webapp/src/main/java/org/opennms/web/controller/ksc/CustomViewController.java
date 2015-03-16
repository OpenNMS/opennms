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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.api.Authentication;
import org.opennms.web.graph.KscResultSet;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.api.KscReportService;
import org.opennms.web.svclayer.api.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>CustomViewController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class CustomViewController extends AbstractController implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(CustomViewController.class);


    public enum Parameters {
        report,
        type,
        timespan,
        graphtype
    }

    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;
    private ResourceService m_resourceService;
    private int m_defaultGraphsPerLine = 0;
    private Executor m_executor;
    
    private Set<String> m_resourcesPendingPromotion = Collections.synchronizedSet(new HashSet<String>());

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] { "report", "type" };
      
        // Get Form Variable
        int reportId = -1;
        String reportType = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.type.toString()));
        String reportIdString = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.report.toString()));
        if (reportType == null) {
            throw new MissingParameterException(Parameters.type.toString(), requiredParameters);
        }
        if (reportIdString == null) {
            throw new MissingParameterException(Parameters.report.toString(), requiredParameters);
        }
      
        if (reportType.equals("node") || reportType.equals("custom")) {
            reportId = WebSecurityUtils.safeParseInt(reportIdString);
        }
      
        String overrideTimespan = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.timespan.toString()));
        if ("null".equals(overrideTimespan) || "none".equals(overrideTimespan)) {
            overrideTimespan = null;
        }

        String overrideGraphType = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.graphtype.toString()));
        if ("null".equals(overrideGraphType) || "none".equals(overrideGraphType)) {
            overrideGraphType = null;
        }
      
        // Load report to view 
        Report report = null;
        if ("node".equals(reportType)) {
            LOG.debug("handleRequestInternal: buildNodeReport(reportId) {}", reportId);
            report = getKscReportService().buildNodeReport(reportId);
        } else if ("nodeSource".equals(reportType)) {
            LOG.debug("handleRequestInternal: buildNodeSourceReport(nodeSource) {}", reportIdString);
            report = getKscReportService().buildNodeSourceReport(reportIdString);
        } else if ("domain".equals(reportType)) {
            LOG.debug("handleRequestInternal: buildDomainReport(reportIdString) {}", reportIdString);
            report = getKscReportService().buildDomainReport(reportIdString);
        } else if ("custom".equals(reportType)) {
            LOG.debug("handleRequestInternal: getReportByIndex(reportId) {}", reportId);
            report = m_kscReportFactory.getReportByIndex(reportId);
            if (report == null) {
                throw new ServletException("Report could not be found in config file for index '" + reportId + "'");
            }
        } else {
            throw new IllegalArgumentException("value to 'type' parameter of '" + reportType + "' is not supported.  Must be one of: node, nodeSource, domain, or custom");
        }
      
        // Get the list of available prefabricated graph options 
        Map<String, OnmsResource> resourceMap = new HashMap<String, OnmsResource>();
        Set<PrefabGraph> prefabGraphs = new TreeSet<PrefabGraph>();
        removeBrokenGraphsFromReport(report);
        List<Graph> graphCollection = report.getGraphCollection();
        if (!graphCollection.isEmpty()) {
            List<OnmsResource> resources = getKscReportService().getResourcesFromGraphs(graphCollection);
            for (int i = 0; i < graphCollection.size(); i++) {
                Graph graph = graphCollection.get(i);
                OnmsResource resource = null;
                try {
                    resource = resources.get(i);
                }catch(IndexOutOfBoundsException e) {
                    LOG.debug("Resource List Index Out Of Bounds Caught ", e);
                }
                
                resourceMap.put(graph.toString(), resource);
                if (resource == null) {
                    LOG.debug("Could not get resource for graph {} in report {}", graph, report.getTitle());
                } else {
                    prefabGraphs.addAll(Arrays.asList(getResourceService().findPrefabGraphsForResource(resource)));
                }
                
                
            }
      
            // Get default graph type from first element of graph_options
            // XXX Do we care about the tests on reportType?
            if (("node".equals(reportType) || "nodeSource".equals(reportType) || "domain".equals(reportType))
                    && overrideGraphType == null
                    && !prefabGraphs.isEmpty()) {
                // Get the name of the first item.  prefabGraphs is sorted.
                overrideGraphType = prefabGraphs.iterator().next().getName();
                    LOG.debug("custom_view: setting default graph type to {}", overrideGraphType);
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
                    LOG.debug("The prefabricated graph '{}' does not exist: {}", displayGraphType, e, e);
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
        
        modelAndView.addObject("showCustomizeButton", ( request.isUserInRole( Authentication.ROLE_ADMIN ) || !request.isUserInRole(Authentication.ROLE_READONLY) ) && (request.getRemoteUser() != null));

        if (report.getGraphs_per_line() > 0) {
            modelAndView.addObject("graphsPerLine", report.getGraphs_per_line());
        } else {
            modelAndView.addObject("graphsPerLine", getDefaultGraphsPerLine());
        }
        
        return modelAndView;
    }
    
    private void removeBrokenGraphsFromReport(Report report) {
        for (Iterator<Graph> itr = report.getGraphCollection().iterator(); itr.hasNext();) {
            Graph graph = itr.next();
            try {
                getKscReportService().getResourceFromGraph(graph);
            } catch (ObjectRetrievalFailureException orfe) {
                LOG.error("Removing graph '{}' in KSC report '{}' because the resource it refers to could not be found. Perhaps resource '{}' (or its ancestor) referenced by this graph no longer exists?", graph.getTitle(), report.getTitle(), graph.getResourceId());
                itr.remove();
            } catch (Throwable e) {
                LOG.error("Unexpected error while scanning through graphs in report: {}", e.getMessage(), e);
                itr.remove();
            }
        }
    }

    private void promoteResourceAttributesIfNecessary(final OnmsResource resource) {
        boolean needToSchedule = false;
        if(resource != null && resource.getId() != null) {
            needToSchedule = m_resourcesPendingPromotion.add(resource.getId());
        }
        if (needToSchedule) {
            m_executor.execute(new Runnable() {

                @Override
                public void run() {
                        getResourceService().promoteGraphAttributesForResource(resource);
                        m_resourcesPendingPromotion.remove(resource.getId());
                }
                
            });
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
     * <p>getDefaultGraphsPerLine</p>
     *
     * @return a int.
     */
    public int getDefaultGraphsPerLine() {
        return m_defaultGraphsPerLine;
    }

    /**
     * <p>setDefaultGraphsPerLine</p>
     *
     * @param defaultGraphsPerLine a int.
     */
    public void setDefaultGraphsPerLine(int defaultGraphsPerLine) {
        Assert.isTrue(defaultGraphsPerLine > 0, "property defaultGraphsPerLine must be greater than zero");

        m_defaultGraphsPerLine = defaultGraphsPerLine;
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
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
        Assert.state(m_resourceService != null, "property resourceService must be set");
        Assert.state(m_defaultGraphsPerLine != 0, "property defaultGraphsPerLine must be set");
        
        m_executor = Executors.newSingleThreadExecutor(
            new LogPreservingThreadFactory(getClass().getSimpleName(), 1)
        );
    }

}
