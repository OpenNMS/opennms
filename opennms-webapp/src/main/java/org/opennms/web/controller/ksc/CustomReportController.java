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
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.graph.KscResultSet;
import org.opennms.web.svclayer.api.KscReportService;
import org.opennms.web.svclayer.api.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>CustomReportController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class CustomReportController extends AbstractController implements InitializingBean {
    
    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;
    private ResourceService m_resourceService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Get Form Variables
        Report report = KscReportEditor.getFromSession(request.getSession(), true).getWorkingReport();
        if (report == null) {
            throw new IllegalStateException("There is no working report");
        }
//        int report_index = getReportFactory().getWorkingReportIndex();      
//        String number_graphs[] = {"1", "2", "3", "4", "5", "6"};
        
        ArrayList<KscResultSet> resultSets = new ArrayList<KscResultSet>(report.getGraphCount());
        for (int i = 0; i < report.getGraphCount(); i++) { 
            Graph current_graph = report.getGraph(i); 
            PrefabGraph display_graph = getResourceService().getPrefabGraph(current_graph.getGraphtype());
            
            OnmsResource resource = getKscReportService().getResourceFromGraph(current_graph);

            Calendar begin_time = Calendar.getInstance();
            Calendar end_time = Calendar.getInstance();
            KSC_PerformanceReportFactory.getBeginEndTime(current_graph.getTimespan(), begin_time, end_time); 

            KscResultSet resultSet = new KscResultSet(current_graph.getTitle(), begin_time.getTime(), end_time.getTime(), resource, display_graph);
            resultSets.add(resultSet);
        }

        ModelAndView modelAndView = new ModelAndView("KSC/customReport");

        modelAndView.addObject("showTimeSpan", report.getShow_timespan_button());
        modelAndView.addObject("showGraphType", report.getShow_graphtype_button());
        modelAndView.addObject("graphsPerLine", report.getGraphs_per_line());
        
        modelAndView.addObject("title", report.getTitle());
        modelAndView.addObject("resultSets", resultSets);
        
        return modelAndView;
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
    }

}
