//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Oct 19: Remove warning on CastorUtils.unmarshal(Class, Reader). - dj@opennms.org
// 2008 Feb 03: Pulled the report/graph editing code out of
//              KSC_PerformanceReportFactory into the new KscReportEditor class.
//              Made KscReportEditor session-based. - dj@opennms.org
// 2005 Jan 18: Changed the default report to "mib2.bits".
// 2003 Apr 24: Changed the default report to "bits".
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
package org.opennms.netmgt.config;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpSession;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * <p>KscReportEditor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class KscReportEditor implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * This is a working report that may be used to hold a report & its index
     * temporarily while moving between jsp's
     */
    private Report m_workingReport = null;

    private int m_workingGraphIndex = -1;

    /**
     * This is a working graph that may be used to hold a report graph & its index temporarily while moving between jsp's
     */
    private Graph m_workingGraph = null;

    /** Create a new blank report & initialize it */
    private static Report getNewReport() {
        Report new_report = new Report();
        new_report.setTitle("New Report Title");
        new_report.setShow_graphtype_button(false);
        new_report.setShow_timespan_button(false);
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

    private void setWorkingReport(Report report) {
        m_workingReport = report;
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
    public static Graph getNewGraph() {
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
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void loadWorkingGraph(int index) throws MarshalException, ValidationException {
        int total_graphs = m_workingReport.getGraphCount();
        m_workingGraphIndex = index;
        if ((m_workingGraphIndex < 0) || (m_workingGraphIndex >= total_graphs)) {
            // out of range... assume new report needs to be created
            m_workingGraph = getNewGraph();
            m_workingGraphIndex = -1;
        } else {
            // Create a new and unique instance of the graph for screwing around with
            m_workingGraph = CastorUtils.duplicateObject(m_workingReport.getGraph(m_workingGraphIndex), Graph.class);
        }
    }

    /**
     * Unloads the working graph into the working report list at the requested
     * graph number. If the graph was modified from an existing graph, then the
     * old one is replaced. A new blank working graph is then created
     *
     * @param requested_graphnum a int.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void unloadWorkingGraph(int requested_graphnum) throws MarshalException, ValidationException {
        int total_graphs = m_workingReport.getGraphCount();
        int insert_location = requested_graphnum--;

        // Check range for existing graph and delete if it is in the valid range
        if ((m_workingGraphIndex >= 0) && (m_workingGraphIndex < total_graphs)) {
            // in range... delete existing graph.
            m_workingReport.removeGraph(m_workingReport.getGraph(m_workingGraphIndex));
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
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void loadWorkingReport(Report report) throws MarshalException, ValidationException {
        setWorkingReport(CastorUtils.duplicateObject(report, Report.class));
        getWorkingReport().deleteId();
    }

    /**
     * Loads the indexed report into the working report object.
     *
     * @param factory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     * @param index a int.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void loadWorkingReport(KSC_PerformanceReportFactory factory, int index) throws MarshalException, ValidationException {
        Report report = factory.getReportByIndex(index);
        if (report == null) {
            throw new IllegalArgumentException("Could not find report with ID " + index);
        }

        setWorkingReport(CastorUtils.duplicateObject(report, Report.class));
    }
    
    /**
     * Loads the indexed report into the working report object as a duplicate
     * report.  The ID in the loaded report will be removed so a new ID will
     * be created when the duplicated report is saved.
     *
     * @param factory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     * @param index a int.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void loadWorkingReportDuplicate(KSC_PerformanceReportFactory factory, int index) throws MarshalException, ValidationException {
        loadWorkingReport(factory, index);

        getWorkingReport().deleteId();
    }
    
    /**
     * Loads a newly created report into the working report object.
     */
    public void loadNewWorkingReport() {
        setWorkingReport(getNewReport());
        getWorkingReport().deleteId();
    }

    /**
     * Unloads the working report into the indexed report list at the point
     * identified by working_index (this should have been set when the working
     * report was loaded), then create a new blank working report
     *
     * @param factory a {@link org.opennms.netmgt.config.KSC_PerformanceReportFactory} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void unloadWorkingReport(KSC_PerformanceReportFactory factory) throws MarshalException, ValidationException {
        if (getWorkingReport().hasId()) {
            factory.setReport(getWorkingReport().getId(), getWorkingReport());
        } else {
            factory.addReport(getWorkingReport());
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
     * @return a {@link org.opennms.netmgt.config.KscReportEditor} object.
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
}
