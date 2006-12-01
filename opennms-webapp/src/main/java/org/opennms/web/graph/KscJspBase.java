package org.opennms.web.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.utils.IfLabel;
import org.opennms.web.CustomJspBase;
import org.opennms.web.Util;
import org.opennms.web.performance.PerformanceModel;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

abstract public class KscJspBase extends CustomJspBase {

    public PerformanceModel model = null;

    protected KSC_PerformanceReportFactory reportFactory = null;

    public void customInit() throws ServletException {
        try {
            initPerfModel();
            initReportFactory();
        } catch (Exception e) {
            throw new ServletException(
                    "Could not initialize the Graph Form Page", e);
        }
    }

    public void initPerfModel() {
        WebApplicationContext m_webAppContext = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        model = (PerformanceModel) m_webAppContext.getBean("performanceModel",
                PerformanceModel.class);
    }

    public void initReportFactory() throws MarshalException,
            ValidationException, FileNotFoundException, IOException {
        KSC_PerformanceReportFactory.init();
        this.reportFactory = KSC_PerformanceReportFactory.getInstance();
    }

    public String encodeRRDNamesAsParmString(String[] rrds) {
        if (rrds == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String parmString = "";

        if (rrds.length > 0) {
            StringBuffer buffer = new StringBuffer("rrd=");
            String encodedRrd = Util.encode(rrds[0]);
            buffer.append(encodedRrd);

            for (int i = 1; i < rrds.length; i++) {
                buffer.append("&rrd=");
                buffer.append(Util.encode(rrds[i]));
            }

            parmString = buffer.toString();
        }

        return parmString;
    }

    /** intf can be null */
    public String[] getRRDNames(int nodeId, String intf, PrefabGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String[] columns = graph.getColumns();
        String[] rrds = new String[columns.length];

        for (int i = 0; i < columns.length; i++) {
            StringBuffer buffer = new StringBuffer();

            if (nodeId >= 0) {
                buffer.append(nodeId);
                buffer.append(File.separator);
            }

            boolean addInterface = false;
            if (intf != null) {
                if (nodeId < 0) {
                    // Response time graph, it's always interface specific
                    addInterface = true;
                } else if (PerformanceModel.INTERFACE_GRAPH_TYPE.equals(graph
                        .getType())) {
                    // Performance graph where type == interface
                    addInterface = true;
                }

                if (addInterface) {
                    buffer.append(intf);
                    buffer.append(File.separator);
                }
            }

            buffer.append(columns[i]);
            buffer.append(org.opennms.netmgt.utils.RrdFileConstants
                    .getRrdSuffix());

            rrds[i] = buffer.toString();
        }

        return rrds;
    }

    public String[] getRRDNames(String dom, String intf, PrefabGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String[] columns = graph.getColumns();
        String[] rrds = new String[columns.length];

        for (int i = 0; i < columns.length; i++) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(dom);
            buffer.append(File.separator);
            buffer.append(intf);
            buffer.append(File.separator);
            buffer.append(columns[i]);
            buffer.append(org.opennms.netmgt.utils.RrdFileConstants
                    .getRrdSuffix());

            rrds[i] = buffer.toString();
        }

        return rrds;
    }

    /**
     * currently only know how to handle ifSpeed external value; intf can be
     * null
     */
    public String encodeExternalValuesAsParmString(int nodeId, String intf,
            PrefabGraph graph) throws SQLException {
        if (graph == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String parmString = "";
        String[] externalValues = graph.getExternalValues();

        if (externalValues != null && externalValues.length > 0) {
            StringBuffer buffer = new StringBuffer();

            for (int i = 0; i < externalValues.length; i++) {
                if ("ifSpeed".equals(externalValues[i])) {
                    String speed = this.getIfSpeed(nodeId, intf);

                    if (speed != null) {
                        buffer.append(externalValues[i]);
                        buffer.append("=");
                        buffer.append(speed);
                        buffer.append("&");
                    }
                } else {
                    throw new IllegalStateException(
                            "Unsupported external value name: "
                                    + externalValues[i]);
                }
            }

            parmString = buffer.toString();
        }

        return parmString;
    }

    public String getIfSpeed(int nodeId, String intf) throws SQLException {
        if (intf == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String speed = null;

        try {
            Map intfInfo = IfLabel.getInterfaceInfoFromIfLabel(nodeId, intf);

            // if the extended information was found correctly
            if (intfInfo != null) {
                speed = (String) intfInfo.get("snmpifspeed");
            }
        } catch (SQLException e) {
            log("SQLException while trying to fetch extended interface info", e);
        }

        return speed;
    }

    public class TimePeriod {
        private String m_id = null;

        private String m_name = null;

        private int m_offsetField = Calendar.DATE;

        private int m_offsetAmount = -1;

        public TimePeriod() {
        }

        public TimePeriod(String id, String name, int offsetField,
                int offsetAmount) {
            m_id = id;
            m_name = name;
            m_offsetField = offsetField;
            m_offsetAmount = offsetAmount;
        }

        public String getId() {
            return m_id;
        }

        public void setId(String id) {
            m_id = id;
        }

        public String getName() {
            return m_name;
        }

        public void setName(String name) {
            m_name = name;
        }

        public int getOffsetField() {
            return m_offsetField;
        }

        public void setOffsetField(int offsetField) {
            m_offsetField = offsetField;
        }

        public int getOffsetAmount() {
            return m_offsetAmount;
        }

        public void setOffsetAmount(int offsetAmount) {
            m_offsetAmount = offsetAmount;
        }
    }

    protected TimePeriod[] m_periods;

    protected void initPeriods() {
        m_periods = new TimePeriod[] {
                new TimePeriod("lastday", "Last Day", Calendar.DATE, -1),
                new TimePeriod("lastweek", "Last Week", Calendar.DATE, -7),
                new TimePeriod("lastmonth", "Last Month", Calendar.DATE, -31),
                new TimePeriod("lastyear", "Last Year", Calendar.DATE, -366) };
    }

    public Report buildDomainReport(String domain) {
        Report domain_report = new Report();
        String report_title = "Domain Report for Domain " + domain;
        domain_report.setTitle(report_title);
        domain_report.setShow_timespan_button(true);
        domain_report.setShow_graphtype_button(true);

        ArrayList<String> query_interface = this.model
                .getQueryableInterfacesForDomain(domain);
        Collections.sort(query_interface);
        for (Iterator i = query_interface.iterator(); i.hasNext();) {
            String intf = (String) i.next();
            Graph graph = new Graph();
            graph.setTitle("");
            graph.setDomain(domain);
            graph.setInterfaceId(intf);
            graph.setTimespan("7_day");
            graph.setGraphtype("none");
            domain_report.addGraph(graph);
        }
        return domain_report;
    }

    public Report buildNodeReport(int node_id) {
        Report node_report = new Report();
        String report_title = "Node Report for Node Number " + node_id;
        node_report.setTitle(report_title);
        node_report.setShow_timespan_button(true);
        node_report.setShow_graphtype_button(true);

        ArrayList<String> query_interface = this.model
                .getQueryableInterfacesForNode(node_id);
        Collections.sort(query_interface);
        for (Iterator i = query_interface.iterator(); i.hasNext();) {
            String intf = (String) i.next();
            Graph graph = new Graph();
            graph.setTitle("");
            graph.setNodeId(String.valueOf(node_id));
            graph.setInterfaceId(intf);
            graph.setTimespan("7_day");
            graph.setGraphtype("none");
            node_report.addGraph(graph);
        }
        return node_report;
    }

}
