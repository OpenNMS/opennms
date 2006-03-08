package org.opennms.web.graph;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.opennms.core.utils.BundleLists;
import org.opennms.netmgt.utils.IfLabel;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.performance.PerformanceModel;

public class Graph implements Comparable {
    private GraphModel m_model = null;
    private PrefabGraph m_graph = null;
    private int m_nodeId = -1;
    private String m_domain = null;
    private String m_intf = null;
    private Date m_start = null;
    private Date m_end = null;

    public Graph(GraphModel model, PrefabGraph graph, int nodeId, String intf,
		Date start, Date end) {
	m_model = model;
	m_graph = graph;
	m_nodeId = nodeId;
	m_intf = intf;
	m_start = start;
	m_end = end;
    }

    public Graph(GraphModel model, PrefabGraph graph, String domain, String intf,
		Date start, Date end) {
	m_model = model;
	m_graph = graph;
	m_domain = domain;
	m_intf = intf;
	m_start = start;
	m_end = end;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getDomain() {
        return m_domain;
    }

    public String getIntf() {
        return m_intf;
    }

    public Date getStart() {
        return m_start;
    }

    public Date getEnd() {
        return m_end;
    }

    public String getName() {
        return m_graph.getName();
    }

    public String getGraphURL()
                throws SQLException, UnsupportedEncodingException {

	String rrdParm = getRRDParmString();
	String externalValuesParm = encodeExternalValuesAsParmString();

	String url = "graph/graph.png"
	       + "?report="
	       + getName()
	       + "&type=" + m_model.getType()
	       + "&start=" + m_start.getTime()
	       + "&end=" + m_end.getTime()
	       + "&" + rrdParm
	       + "&" + externalValuesParm;

	// XXX This is such a hack.  This logic should *not* be in here.
	if (m_model.getType() == "performance") {
            if (m_nodeId > -1) {
	        url = url + "&" + m_nodeId + "/strings.properties";
            }
	}

	return url;
    }

    /** intf can be null */
    private String[] getRRDNames(boolean encodeNodeIdInRRDParm) {
        String[] columns = m_graph.getColumns();
        String[] rrds = new String[columns.length];

        for (int i=0; i < columns.length; i++) {
            StringBuffer buffer = new StringBuffer();

            if (encodeNodeIdInRRDParm) {
		// We don't include node IDs on response graphs
                // Make sure we have a valid nodeId, else append domain
                if (m_nodeId > -1) {
                    buffer.append(m_nodeId);
                } else {
                    buffer.append(m_domain);
                }
                buffer.append(File.separator);
            }

            boolean addInterface = false;
            if (m_intf != null) {
                if (!encodeNodeIdInRRDParm) {
                    // Response time graph, it's always interface specific
                    addInterface = true;
                } else if (PerformanceModel.INTERFACE_GRAPH_TYPE.equals(m_graph.getType())) {
                    // Performance graph where type == interface
                    addInterface = true;
                }

                if (addInterface) {
                    buffer.append(m_intf);
                    buffer.append(File.separator);
                }
            }

            buffer.append(columns[i]);
            buffer.append(RrdFileConstants.RRD_SUFFIX);

            rrds[i] = buffer.toString();
        }

        return rrds;
    }

    private String getRRDParmString() throws UnsupportedEncodingException {
	int node;
        String[] rrds = getRRDNames(m_model.encodeNodeIdInRRDParm());
	return encodeRRDNamesAsParmString(rrds);
    }

    private String encodeRRDNamesAsParmString(String[] rrds)
		throws UnsupportedEncodingException {
        if (rrds == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String parmString = "";

        if(rrds.length > 0) {
            StringBuffer buffer = new StringBuffer("rrd=");
            buffer.append(URLEncoder.encode(rrds[0], "UTF-8"));

            for(int i=1; i < rrds.length; i++ ) {
                buffer.append("&rrd=");
                buffer.append(URLEncoder.encode(rrds[i], "UTF-8"));
            }

            parmString = buffer.toString();
        }

        return parmString;
    }

    /**
     * currently only know how to handle ifSpeed external value;
     * intf can be null
     */
    private String encodeExternalValuesAsParmString() throws SQLException {
        String parmString = "";        
        String[] externalValues = m_graph.getExternalValues();
        
        if (externalValues != null && externalValues.length > 0) {
            StringBuffer buffer = new StringBuffer();
            
            for(int i=0; i < externalValues.length; i++) {
                if ("ifSpeed".equals(externalValues[i])) {
                    String speed = getIfSpeed();
                    
                    if (speed != null) {
                        buffer.append(externalValues[i]);
                        buffer.append("=");                        
                        buffer.append(speed);   
                        buffer.append("&");                        
                    }
                } else {
                    throw new IllegalStateException("Unsupported external value name: " + externalValues[i]);
                }                
            }
            
            parmString = buffer.toString();
        }        
        
        return parmString;
    }

    private String getIfSpeed() throws SQLException {
        String speed = null;
        
        Map intfInfo = IfLabel.getInterfaceInfoFromIfLabel(m_nodeId, m_intf);

        // if the extended information was found correctly
        if (intfInfo != null) {
            speed = (String)intfInfo.get("snmpifspeed");
        }

        return speed;
    }


    public int compareTo(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (!(obj instanceof Graph)) {
            throw new IllegalArgumentException("Can only compare to Graph objects.");
        }

        Graph otherGraph = (Graph) obj;

	return m_graph.compareTo(otherGraph.m_graph);
    }

}
