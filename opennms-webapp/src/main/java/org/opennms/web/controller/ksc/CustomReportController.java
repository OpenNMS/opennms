/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.controller.ksc;

import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.GraphCollectionConfigFactory;
import org.opennms.netmgt.config.graphcollections.Graph;
import org.opennms.netmgt.config.graphcollections.GraphCollection;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.graph.KscResultSet;
import org.opennms.web.svclayer.api.GraphCollectionService;
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
    
    private GraphCollectionConfigFactory m_graphCollectionConfigFactory;
    private GraphCollectionService m_graphCollectionService;
    private ResourceService m_resourceService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Get Form Variables
        GraphCollection report = KscReportEditor.getFromSession(request.getSession(), true).getWorkingReport();
        if (report == null) {
            throw new IllegalStateException("There is no working report");
        }
//        int report_index = getReportFactory().getWorkingReportIndex();      
//        String number_graphs[] = {"1", "2", "3", "4", "5", "6"};
        
        ArrayList<KscResultSet> resultSets = new ArrayList<KscResultSet>(report.getGraphs().size());
        for (int i = 0; i < report.getGraphs().size(); i++) { 
            final int index = i;
            Graph current_graph = report.getGraphs().get(index); 
            PrefabGraph display_graph = getResourceService().getPrefabGraph(current_graph.getGraphtype());
            
            OnmsResource resource = getGraphCollectionService().getResourceFromGraph(current_graph);

            Calendar begin_time = Calendar.getInstance();
            Calendar end_time = Calendar.getInstance();
            GraphCollectionConfigFactory.getBeginEndTime(current_graph.getTimespan(), begin_time, end_time); 

            KscResultSet resultSet = new KscResultSet(current_graph.getTitle(), begin_time.getTime(), end_time.getTime(), resource, display_graph);
            resultSets.add(resultSet);
        }

        ModelAndView modelAndView = new ModelAndView("KSC/customReport");

        modelAndView.addObject("showTimeSpan", report.getShowTimespanButton().orElse(null));
        modelAndView.addObject("showGraphType", report.getShowGraphtypeButton().orElse(null));
        modelAndView.addObject("graphsPerLine", report.getGraphsPerLine().orElse(0));
        
        modelAndView.addObject("title", report.getTitle());
        modelAndView.addObject("resultSets", resultSets);
        
        return modelAndView;
    }

    /**
     * <p>getGraphCollectionConfigFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.GraphCollectionConfigFactory} object.
     */
    public GraphCollectionConfigFactory getGraphCollectionConfigFactory() {
        return m_graphCollectionConfigFactory;
    }

    /**
     * <p>setGraphCollectionConfigFactory</p>
     *
     * @param graphCollectionConfigFactory a {@link org.opennms.netmgt.config.GraphCollectionConfigFactory} object.
     */
    public void setGraphCollectionConfigFactory(GraphCollectionConfigFactory graphCollectionConfigFactory) {
        m_graphCollectionConfigFactory = graphCollectionConfigFactory;
    }

    /**
     * <p>getGraphCollectionService</p>
     *
     * @return a {@link org.opennms.web.svclayer.api.GraphCollectionService} object.
     */
    public GraphCollectionService getGraphCollectionService() {
        return m_graphCollectionService;
    }

    /**
     * <p>setGraphCollectionService</p>
     *
     * @param graphCollectionService a {@link org.opennms.web.svclayer.api.GraphCollectionService} object.
     */
    public void setGraphCollectionService(GraphCollectionService graphCollectionService) {
        m_graphCollectionService = graphCollectionService;
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
        Assert.state(m_graphCollectionConfigFactory != null, "property graphCollectionConfigFactory must be set");
        Assert.state(m_graphCollectionService != null, "property graphCollectionService must be set");
        Assert.state(m_resourceService != null, "property resourceService must be set");
    }

}
