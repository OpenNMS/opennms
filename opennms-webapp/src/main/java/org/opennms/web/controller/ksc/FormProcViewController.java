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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.api.KscReportService;
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

    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;

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
                    editor.loadWorkingReport(m_kscReportService.buildNodeReport(reportId));
                } else if (reportType.equals("nodeSource")) {
  
                    editor.loadWorkingReport(m_kscReportService.buildNodeSourceReport(reportIdString));
                } else if (reportType.equals("domain")) {
                    editor.loadWorkingReport(m_kscReportService.buildDomainReport(reportIdString));
                } else { 
                    editor.loadWorkingReport(getKscReportFactory(), reportId);
                }
                
                // Now inject any override characteristics into the working report model
                Report working_report = editor.getWorkingReport();
                for (int i=0; i<working_report.getGraphCount(); i++) {
                    Graph working_graph = working_report.getGraph(i);
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
            return new ModelAndView("redirect:/KSC/index.htm");
        } else {
            throw new IllegalArgumentException("Parameter action of '" + reportAction + "' is not supported.  Must be one of: Update, Customize, or Exit");
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
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_kscReportFactory != null, "property kscReportFactory must be set");
        Assert.state(m_kscReportService != null, "property kscReportService must be set");
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

    

}
