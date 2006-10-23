package org.opennms.web.graph;

import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.utils.IfLabel;

public class Graph implements Comparable {
    private GraphModel m_model = null;
    private PrefabGraph m_graph = null;
    private int m_nodeId = -1;
    private String m_domain = null;
    private String m_resource = null;
    private Date m_start = null;
    private Date m_end = null;
    private String m_resourceType;

    public Graph(GraphModel model, PrefabGraph graph, int nodeId, String resource,
		String resourceType, Date start, Date end) {
	m_model = model;
	m_graph = graph;
	m_nodeId = nodeId;
        m_resource = resource;
        m_resourceType = resourceType;
	m_start = start;
	m_end = end;
    }

    public Graph(GraphModel model, PrefabGraph graph, String domain, String resource,
		String resourceType, Date start, Date end) {
	m_model = model;
	m_graph = graph;
	m_domain = domain;
	m_resource = resource;
        m_resourceType = resourceType;
	m_start = start;
	m_end = end;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getDomain() {
        return m_domain;
    }

    public String getResource() {
        return m_resource;
    }

    public String getResourceType() {
        return m_resourceType;
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
	       + "?"
               + "report=" + getName()
               + "&"
               + "resourceType=" + getResourceType()
               + "&"
               + "type=" + m_model.getType()
	       + "&"
               + "start=" + m_start.getTime()
	       + "&"
               + "end=" + m_end.getTime()
	       + "&"
               + rrdParm
	       + "&"
               + externalValuesParm;

	// XXX This is such a hack.  This logic should *not* be in here.
	if (m_model.getType() == "performance") {
            if (m_nodeId > -1) {
	        url = url + "&" + m_nodeId + "/strings.properties";
            } else if (m_domain != null) {
	        url = url + "&" + m_domain + "/strings.properties";
            }
	}

	return url;
    }
    
    private String[] getRRDNames() {
        String[] columns = m_graph.getColumns();
        String[] rrds = new String[columns.length];

        for (int i=0; i < columns.length; i++) {
            if(m_nodeId > -1) {
                rrds[i] = m_model.getRelativePathForAttribute(m_resourceType, Integer.toString(m_nodeId), m_resource, columns[i]);
            } else {
                rrds[i] = m_model.getRelativePathForAttribute(m_resourceType, m_domain, m_resource, columns[i]);
            }
        }

        return rrds;
    }

    private String getRRDParmString() throws UnsupportedEncodingException {
        String[] rrds = getRRDNames();
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
        
        Map intfInfo = IfLabel.getInterfaceInfoFromIfLabel(m_nodeId, m_resource);

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
