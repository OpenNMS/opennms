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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.config.kscReports.ReportsList;

public class KSC_PerformanceReportFactory {
    /** The static singleton instance object */
    private static KSC_PerformanceReportFactory instance;

    /** File name of the KSC_PerformanceReport.xml */
    private static File KSC_PerformanceReportFile;

    /** An instance of the ReportsList configuration */
    private static ReportsList m_config;

    /** The input stream for the config file */
    private static InputStream configIn;

    /** Boolean indicating if the init() method has been called */
    private static boolean initialized = false;

    /** Last Modified timestamp */
    private static long m_lastModified;

    /**
     * The array of values that may be used in the timespan declaration of a
     * graph
     */
    public final String[] timespan_options = { "1_hour", "2_hour", "4_hour", "8_hour", "1_day", "2_day", "7_day", "1_month", "6_month", "1_year", "Today", "Yesterday", "This Week", "Last Week", "This Month", "Last Month", "This Quarter", "Last Quarter", "This Year", "Last Year" };

    /**
     * This is a working report that may be used to hold a report & its index
     * temporarily while moving between jsp's
     */
    private Report working_report = null;

    private int working_index = -1;

    /**
     * This is a working graph that may be used to hold a report graph & its
     * index temporarily while moving between jsp's
     */
    private Graph working_graph = null;

    private int graph_index = -1;

    /**
     * Empty Private Constructor. Cannot be instantiated outside itself.
     */
    private KSC_PerformanceReportFactory() {
    }

    /** Init routine. Must be called before calling getInstance() to instantiate * */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (instance == null) {
            instance = new KSC_PerformanceReportFactory();
            KSC_PerformanceReportFactory.reload();
            KSC_PerformanceReportFactory.initialized = true;
        }
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * KSC_PerformanceReportFactory
     * 
     * @return the single KSC_PerformanceReportFactory instance
     */
    public static synchronized KSC_PerformanceReportFactory getInstance() throws IllegalStateException {
        if (instance == null) {
            throw new IllegalStateException("KSC_PerformanceReportFactory.init() must be called before KSC_PerformanceReportFactory.getInstance().");
        }

        return instance;
    }

    /** Parses the KSC_PerformanceReport.xml via the Castor classes */
    public static synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        KSC_PerformanceReportFile = ConfigFileConstants.getFile(ConfigFileConstants.KSC_REPORT_FILE_NAME);

        InputStream configIn = new FileInputStream(KSC_PerformanceReportFile);
        m_lastModified = KSC_PerformanceReportFile.lastModified();

        m_config = (ReportsList) Unmarshaller.unmarshal(ReportsList.class, new InputStreamReader(configIn));
    }

    /** Saves the KSC_PerformanceReport.xml data */
    public synchronized void saveCurrent() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (instance == null) {
            throw new IllegalStateException("KSC_PerformanceReportFactory.init() must be called before KSC_PerformanceReportFactory.saveCurrent().");
        }
        sortByTitle();
        // Marshall to a string first, then to file. This way the original
        // config isn't lost if teh xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);
        if (stringWriter.toString() != null) {
            FileWriter fileWriter = new FileWriter(KSC_PerformanceReportFile);
            fileWriter.write(stringWriter.toString());
            fileWriter.flush();
            fileWriter.close();
        }
        reload();
    }

    /** Sorts the Reports List by their title. Simple bubble sort. */
    public void sortByTitle() {
        Report[] report_array = m_config.getReport();
        for (int j = 0; j < report_array.length; j++) {
            for (int i = j + 1; i < report_array.length; i++) {
                if (report_array[i].getTitle().compareTo(report_array[j].getTitle()) < 0) {
                    Report temp_report = report_array[j];
                    report_array[j] = report_array[i];
                    report_array[i] = temp_report;
                }
            }
        }
        m_config.setReport(report_array); // write back the sorted list
    }

    /** Returns the KSC_PerformanceReport configuration object */
    public static ReportsList getConfiguration() {
        return m_config;
    }

    /** Deletes the indexed report and updates file configuration */
    public void deleteReportAndSave(int index) throws ArrayIndexOutOfBoundsException, IOException, FileNotFoundException, MarshalException, ValidationException {
        int total_reports = m_config.getReportCount();
        if ((index < 0) || (index >= total_reports)) {
            // Out of range. Throw range error.
            throw new ArrayIndexOutOfBoundsException("Reports List index to be deleted is out of bounds.");
        } else {
            Report removee = m_config.getReport(index);
            m_config.removeReport(removee);
            saveCurrent();
            reload(); // ensure consistent state with file
        }
    }

    /** Returns the working report object */
    public Report getWorkingReport() {
        return working_report;
    }

    /** Loads the source report into the working report object */
    public void loadWorkingReport(Report source_report) throws MarshalException, ValidationException {
        // Create a new and unique instance of the report for screwing around
        // with
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(source_report, stringWriter);
        StringReader stringReader = new StringReader(stringWriter.toString());
        working_report = (Report) Unmarshaller.unmarshal(Report.class, stringReader);
    }

    /**
     * Loads the indexed report into the working report object or creates a new
     * one if the object does not exist
     */
    public void loadWorkingReport(int index) throws MarshalException, ValidationException {
        int total_reports = m_config.getReportCount();
        working_index = index;
        if ((working_index < 0) || (working_index >= total_reports)) {
            // Out of range. Assume new report needs to be created.
            working_report = KSC_PerformanceReportFactory.getNewReport();
            working_index = -1;
        } else {
            loadWorkingReport(m_config.getReport(working_index));
        }
    }

    /**
     * Unloads the working report into the indexed report list at the point
     * identified by working_index (this should have been set when the working
     * report was loaded), then create a new blank working report
     */
    public void unloadWorkingReport() throws MarshalException, ValidationException {
        int total_reports = m_config.getReportCount();
        if ((working_index < 0) || (working_index >= total_reports)) {
            // out of range... assume the new report needs to be appended to
            // list
            m_config.addReport(working_report);
        } else {
            // Replace the report in the configuration with the working report
            m_config.setReport(working_index, working_report);
        }
        // Create a new and unique instance of a report for screwing around with
        // as the working report
        working_report = KSC_PerformanceReportFactory.getNewReport();
        working_index = -1;
    }

    /** Create a new blank report & initialize it */
    public static Report getNewReport() {
        Report new_report = new Report();
        new_report.setTitle("New Report Title");
        new_report.setShow_graphtype_button(false);
        new_report.setShow_timespan_button(false);
        return new_report;
    }

    /** Returns the working report index */
    public int getWorkingReportIndex() {
        return working_index;
    }

    /** Sets the working report index */
    public void setWorkingReportIndex(int v_index) {
        working_index = v_index;
    }

    /** Returns the working graph object */
    public Graph getWorkingGraph() {
        return working_graph;
    }

    /** Returns the working graph index */
    public int getWorkingGraphIndex() {
        return graph_index;
    }

    /** Create a new blank graph & initialize it */
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
     */
    public void loadWorkingGraph(int index) throws MarshalException, ValidationException {
        int total_graphs = working_report.getGraphCount();
        graph_index = index;
        if ((graph_index < 0) || (graph_index >= total_graphs)) {
            // out of range... assume new report needs to be created
            working_graph = KSC_PerformanceReportFactory.getNewGraph();
            graph_index = -1;
        } else {
            // Create a new and unique instance of the graph for screwing around
            // with
            StringWriter stringWriter = new StringWriter();
            Marshaller.marshal(working_report.getGraph(graph_index), stringWriter);
            StringReader stringReader = new StringReader(stringWriter.toString());
            working_graph = (Graph) Unmarshaller.unmarshal(Graph.class, stringReader);
        }
    }

    /**
     * Unloads the working graph into the working report list at the requested
     * graph number. If the graph was modified from an existing graph, then the
     * old one is replaced. A new blank working graph is then created
     */
    public void unloadWorkingGraph(int requested_graphnum) throws MarshalException, ValidationException {
        int total_graphs = working_report.getGraphCount();
        int insert_location = requested_graphnum--;
        boolean replace_graph = false;

        // Check range for existing graph and delete if it is in the valid range
        if ((graph_index >= 0) && (graph_index < total_graphs)) {
            // in range... delete existing graph.
            working_report.removeGraph(working_report.getGraph(graph_index));
        }

        // Check range for insertion point
        if ((insert_location < 0) || (insert_location >= total_graphs)) {
            // out of range... assume the new graph needs to be appended to list
            working_report.addGraph(working_graph);
        } else {
            // Insert the graph in the configuration within the working report
            working_report.addGraph(insert_location, working_graph);
        }

        // Create a new and unique instance of a report for screwing around with
        // as the working report
        working_graph = KSC_PerformanceReportFactory.getNewGraph();
        graph_index = -1;
    }

    public static synchronized void getBeginEndTime(String interval, Calendar begin_time, Calendar end_time) throws IllegalArgumentException
    /**
     * This method requires begin time and end time to be set to the current
     * time prior to call. The start and stop times are relative to this time.
     * Init values as follows: begin_time = Calendar.getInstance(); end_time =
     * Calendar.getInstance();
     */
    {
        if (interval.equals("1_hour")) {
            begin_time.add(Calendar.HOUR, -1);
        } else if (interval.equals("2_hour")) {
            begin_time.add(Calendar.HOUR, -2);
        } else if (interval.equals("4_hour")) {
            begin_time.add(Calendar.HOUR, -4);
        } else if (interval.equals("8_hour")) {
            begin_time.add(Calendar.HOUR, -8);
        } else if (interval.equals("1_day")) {
            begin_time.add(Calendar.DATE, -1);
        } else if (interval.equals("2_day")) {
            begin_time.add(Calendar.DATE, -2);
        } else if (interval.equals("7_day")) {
            begin_time.add(Calendar.DATE, -7);
        } else if (interval.equals("1_month")) {
            begin_time.add(Calendar.DATE, -30);
        } else if (interval.equals("6_month")) {
            begin_time.add(Calendar.DATE, -183);
        } else if (interval.equals("1_year")) {
            begin_time.add(Calendar.DATE, -365);
        } else {
            // From current time, lets zero out the small components
            begin_time.set(Calendar.HOUR_OF_DAY, 0);
            begin_time.set(Calendar.MINUTE, 0);
            begin_time.set(Calendar.SECOND, 0);
            end_time.set(Calendar.HOUR_OF_DAY, 0);
            end_time.set(Calendar.MINUTE, 0);
            end_time.set(Calendar.SECOND, 0);

            if (interval.equals("Today")) {
                end_time.add(Calendar.DATE, 1);
            } else if (interval.equals("Yesterday")) {
                begin_time.add(Calendar.DATE, -1);
            } else if (interval.equals("This Week") || interval.equals("Last Week")) {
                begin_time.set(Calendar.DAY_OF_WEEK, 1);
                end_time.set(Calendar.DAY_OF_WEEK, 7);
                end_time.set(Calendar.HOUR_OF_DAY, 23);
                end_time.set(Calendar.MINUTE, 59);
                if (interval.equals("Last Week")) {
                    begin_time.add(Calendar.DATE, -7);
                    end_time.add(Calendar.DATE, -7);
                }
            } else if (interval.equals("This Month")) {
                begin_time.set(Calendar.DATE, 1);
                end_time.add(Calendar.MONTH, 1);
                end_time.set(Calendar.DATE, 1);
            } else if (interval.equals("Last Month")) {
                begin_time.add(Calendar.MONTH, -1);
                begin_time.set(Calendar.DATE, 1);
                end_time.set(Calendar.DATE, 1);
            } else if (interval.equals("This Quarter") || interval.equals("Last Quarter")) {
                begin_time.set(Calendar.DATE, 1);
                end_time.set(Calendar.DATE, 1);

                switch (begin_time.get(Calendar.MONTH)) {
                case 0:
                case 1:
                case 2:
                    begin_time.set(Calendar.MONTH, 0);
                    end_time.set(Calendar.MONTH, 3);
                    break;
                case 3:
                case 4:
                case 5:
                    begin_time.set(Calendar.MONTH, 3);
                    end_time.set(Calendar.MONTH, 6);
                    break;
                case 6:
                case 7:
                case 8:
                    begin_time.set(Calendar.MONTH, 6);
                    end_time.set(Calendar.MONTH, 9);
                    break;
                case 9:
                case 10:
                case 11:
                    begin_time.set(Calendar.MONTH, 9);
                    end_time.set(Calendar.MONTH, 0);
                    end_time.add(Calendar.YEAR, 1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Calendar Month " + begin_time.get(Calendar.MONTH));
                }
                if (interval.equals("Last Quarter")) {
                    begin_time.add(Calendar.MONTH, -3);
                    end_time.add(Calendar.MONTH, -3);
                }
            } else if (interval.equals("This Year")) {
                begin_time.set(Calendar.MONTH, 0);
                begin_time.set(Calendar.DATE, 1);
                end_time.set(Calendar.MONTH, 0);
                end_time.set(Calendar.DATE, 1);
                end_time.add(Calendar.YEAR, 1);
            } else if (interval.equals("Last Year")) {
                begin_time.set(Calendar.MONTH, 0);
                begin_time.set(Calendar.DATE, 1);
                begin_time.add(Calendar.YEAR, -1);
                end_time.set(Calendar.MONTH, 0);
                end_time.set(Calendar.DATE, 1);
            } else {
                throw new IllegalArgumentException("Unknown graph timespan: " + interval);
            }
        }
    } // getBeginEndTime()

}
