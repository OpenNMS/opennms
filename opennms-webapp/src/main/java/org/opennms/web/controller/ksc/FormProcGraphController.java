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
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customGraphChooseResource.jsp"); // TODO We need to tune the initialization
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
