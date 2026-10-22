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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.GraphCollectionConfigFactory;
import org.opennms.netmgt.config.graphcollections.Graph;
import org.opennms.netmgt.config.graphcollections.GraphCollection;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.api.GraphCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>FormProcViewController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class FormProcViewController extends AbstractController implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(FormProcViewController.class);


    public enum Parameters {
        action,
        timespan,
        type,
        report,
        graphtype
    }

    public enum Actions {
        Customize,
        Update,
        Exit
    }

    private GraphCollectionConfigFactory m_graphCollectionConfigFactory;
    private GraphCollectionService m_graphCollectionService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get Form Variables
        int reportId = -1; 
        String overrideTimespan = null;
        String overrideGraphType = null;
        String reportAction = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.action.toString()));
        String reportIdString = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.report.toString()));
        String reportType = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.type.toString()));
        if (reportAction == null) {
            throw new MissingParameterException ("action", new String[] {"action", "report", "type"});
        }
        if (reportType == null) {
            throw new MissingParameterException ("type", new String[] {"action", "report", "type"});
        }
        if (reportIdString == null) {
            throw new MissingParameterException ("report", new String[] {"action", "report", "type"});
            
        }

        if (Actions.Customize.toString().equals(reportAction) || Actions.Update.toString().equals(reportAction)) {
            if (reportType.equals("node") || reportType.equals("custom")) {
                reportId = WebSecurityUtils.safeParseInt(reportIdString);
            }
            overrideTimespan = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.timespan.toString()));
            if ((overrideTimespan == null) || overrideTimespan.equals("null")) {
                overrideTimespan = "none";
            }
            overrideGraphType = WebSecurityUtils.sanitizeString(request.getParameter(Parameters.graphtype.toString()));
            if (overrideGraphType == null || overrideGraphType.equals("null")) {
                overrideGraphType = "none";
            }
            if (Actions.Customize.toString().equals(reportAction)) {
             // Fetch the KscReportEditor or create one if there isn't one already
                KscReportEditor editor = KscReportEditor.getFromSession(request.getSession(), false);
                
                LOG.debug("handleRequestInternal: build report for reportType {}", reportType);
                if (reportType.equals("node")) {
                    editor.loadWorkingReport(m_graphCollectionService.buildNodeCollection(reportId));
                } else if (reportType.equals("nodeSource")) {
  
                    editor.loadWorkingReport(m_graphCollectionService.buildNodeSourceCollection(reportIdString));
                } else if (reportType.equals("domain")) {
                    editor.loadWorkingReport(m_graphCollectionService.buildDomainCollection(reportIdString));
                } else { 
                    editor.loadWorkingReport(getGraphCollectionConfigFactory(), reportId);
                }
                
                // Now inject any override characteristics into the working report model
                GraphCollection working_report = editor.getWorkingReport();
                for (int i=0; i<working_report.getGraphs().size(); i++) {
                    final int index = i;
                    Graph working_graph = working_report.getGraphs().get(index);
                    if (!overrideTimespan.equals("none")) { 
                        working_graph.setTimespan(overrideTimespan); 
                    }
                    if (!overrideGraphType.equals("none")) { 
                        working_graph.setGraphtype(overrideGraphType); 
                    }
                }
            }
        } else { 
            if (!Actions.Exit.toString().equals(reportAction)) {
                throw new ServletException ("Invalid Parameter contents for report_action");
            }
        }
        
        if (Actions.Update.toString().equals(reportAction)) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customView.htm");
            modelAndView.addObject("type", reportType);

            if (reportIdString != null) {
                modelAndView.addObject("report", reportIdString);
            }
            if (overrideTimespan != null) { 
                modelAndView.addObject("timespan", overrideTimespan);
            }
            if (overrideGraphType != null) { 
                modelAndView.addObject("graphtype", overrideGraphType);
            }

            return modelAndView;
        } else if (Actions.Customize.toString().equals(reportAction)) { 
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (Actions.Exit.toString().equals(reportAction)) {
            return new ModelAndView("redirect:/KSC/index.jsp");
        } else {
            throw new IllegalArgumentException("Parameter action of '" + reportAction + "' is not supported.  Must be one of: Update, Customize, or Exit");
        }
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
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_graphCollectionConfigFactory != null, "property graphCollectionConfigFactory must be set");
        Assert.state(m_graphCollectionService != null, "property graphCollectionService must be set");
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

    

}
