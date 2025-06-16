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

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>KscReportEditor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class KscReportEditor implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(KscReportEditor.class);

    private static final long serialVersionUID = 8825116321880022485L;

    /**
     * This is a working report that may be used to hold a report & its index
     * temporarily while moving between jsp's
     */
    private Report m_workingReport = null;

    private int m_workingGraphIndex = -1;

    /**
     * This is a working graph that may be used to hold a report graph & its index temporarily while moving between JSPs
     */
    private Graph m_workingGraph = null;

    /** Create a new blank report & initialize it */
    private static Report getNewReport() {
        Report new_report = new Report();
        new_report.setTitle("New Report Title");
        new_report.setShowGraphtypeButton(false);
        new_report.setShowTimespanButton(false);
        return new_report;
    }

    /**
     * Returns the working report object
     *
     * @return a {@link org.opennms.netmgt.config.kscReports.Report} object.
     */
    public Report getWorkingReport() {
        return m_workingReport;
    }

    /**
     * Returns the working graph object
     *
     * @return a {@link org.opennms.netmgt.config.kscReports.Graph} object.
     */
    public Graph getWorkingGraph() {
        return m_workingGraph;
    }

    /**
     * Returns the working graph index
     *
     * @return a int.
     */
    public int getWorkingGraphIndex() {
        return m_workingGraphIndex;
    }

    /**
     * Create a new blank graph & initialize it
     *
     * @return a {@link org.opennms.netmgt.config.kscReports.Graph} object.
     */
    private static Graph getNewGraph() {
        Graph new_graph = new Graph();
        new_graph.setTitle("");
        //new_graph.setGraphtype("mib2.bits");
        new_graph.setTimespan("7_day");
        return new_graph;
    }

    /**
     * Loads the indexed graph from the working report into the working graph
     * object or creates a new one if the object does not exist
     *
     * @param index a int.
     */
    public void loadWorkingGraph(int index) {
        int total_graphs = m_workingReport.getGraphs().size();
        m_workingGraphIndex = index;
        if ((m_workingGraphIndex < 0) || (m_workingGraphIndex >= total_graphs)) {
            // out of range... assume new report needs to be created
            m_workingGraph = getNewGraph();
            m_workingGraphIndex = -1;
        } else {
            // Create a new and unique instance of the graph for screwing around with
            final int index1 = m_workingGraphIndex;
            m_workingGraph = JaxbUtils.duplicateObject(m_workingReport.getGraphs().get(index1), Graph.class);
        }
    }

    /**
     * Unloads the working graph into the working report list at the requested
     * graph number. If the graph was modified from an existing graph, then the
     * old one is replaced. A new blank working graph is then created
     *
     * @param requested_graphnum a int.
     */
    public void unloadWorkingGraph(int requested_graphnum) {
        int total_graphs = m_workingReport.getGraphs().size();
        int insert_location = requested_graphnum--;

        // Check range for existing graph and delete if it is in the valid range
        if ((m_workingGraphIndex >= 0) && (m_workingGraphIndex < total_graphs)) {
            // in range... delete existing graph.
            final int index = m_workingGraphIndex;
            m_workingReport.removeGraph(m_workingReport.getGraphs().get(index));
        }

        // Check range for insertion point
        if ((insert_location < 0) || (insert_location >= total_graphs)) {
            // out of range... assume the new graph needs to be appended to list
            m_workingReport.addGraph(m_workingGraph);
        } else {
            // Insert the graph in the configuration within the working report
            m_workingReport.addGraph(insert_location, m_workingGraph);
        }

        // Create a new and unique instance of a report for screwing around with
        // as the working report
        m_workingGraph = getNewGraph();
        m_workingGraphIndex = -1;
    }

    /**
     * Loads the source report into the working report object as a new report.
     *
     * @param report a {@link org.opennms.netmgt.config.kscReports.Report} object.
     */
    public void loadWorkingReport(Report report) {
        m_workingReport = JaxbUtils.duplicateObject(report, Report.class);
        m_workingReport.setId(null);
    }

    /**
     * Loads the indexed report into the working report object.
     *
     * @param factory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     * @param index a int.
     */
    public void loadWorkingReport(KSC_PerformanceReportFactory factory, int index) {
        Report report = factory.getReportByIndex(index);
        if (report == null) {
            throw new IllegalArgumentException("Could not find report with ID " + index);
        }

        m_workingReport = JaxbUtils.duplicateObject(report, Report.class);
    }
    
    /**
     * Loads the indexed report into the working report object as a duplicate
     * report.  The ID in the loaded report will be removed so a new ID will
     * be created when the duplicated report is saved.
     *
     * @param factory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     * @param index a int.
     */
    public void loadWorkingReportDuplicate(KSC_PerformanceReportFactory factory, int index) {
        loadWorkingReport(factory, index);

        m_workingReport.setId(null);
    }
    
    /**
     * Loads a newly created report into the working report object.
     */
    public void loadNewWorkingReport() {
        m_workingReport = getNewReport();
        m_workingReport.setId(null);
    }

    /**
     * Unloads the working report into the indexed report list at the point
     * identified by working_index (this should have been set when the working
     * report was loaded), then create a new blank working report
     *
     * @param factory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     */
    public void unloadWorkingReport(KSC_PerformanceReportFactory factory) {
        final Report workingReport = getWorkingReport();
        LOG.debug("unloading working report: {}", workingReport);

        if (workingReport != null) {
            if (workingReport.getId() != null) {
                LOG.debug("working report has id: {}", workingReport.getId());
                factory.setReport(workingReport.getId(), workingReport);
            } else {
                LOG.debug("adding working report");
                factory.addReport(workingReport);
            }
        }
        
        // Create a new and unique instance of a report for screwing around with
        // as the working report
        loadNewWorkingReport();
    }

    /**
     * <p>getFromSession</p>
     *
     * @param session a {@link javax.servlet.http.HttpSession} object.
     * @param required a boolean.
     * @return a {@link org.opennms.web.controller.ksc.KscReportEditor} object.
     */
    public static KscReportEditor getFromSession(HttpSession session, boolean required) {
        String attributeName = KscReportEditor.class.getName();
        
        if (session.getAttribute(attributeName) == null) {
            if (required) {
                throw new IllegalStateException("The KSC report editing session is not open--please restart your edits.  This could be due to your session expiring on the server due to inactivity or the server being restarted.");
            } else {
                session.setAttribute(attributeName, new KscReportEditor());
            }
        }
        
        return (KscReportEditor) session.getAttribute(attributeName);
    }

    /**
     */
    public static void unloadFromSession(HttpSession session) {
        String attributeName = KscReportEditor.class.getName();
        session.removeAttribute(attributeName);
    }
}
