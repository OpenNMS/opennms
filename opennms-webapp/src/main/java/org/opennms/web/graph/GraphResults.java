package org.opennms.web.graph;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

import org.opennms.web.element.NetworkElementFactory;

public class GraphResults {
    private GraphModel m_model = null;
    private int m_nodeId = -1;
    private String m_domain = null;
    private String m_resource = null;
    private String[] m_reports = null;
    private Date m_start = null;
    private Date m_end = null;
    private String m_relativeTime = null;
    private Graph[] m_graphs = null;
    private RelativeTimePeriod[] m_relativeTimePeriods = null;
    private String m_resourceType;
    private String m_resourceTypeLabel;
    private String m_resourceLabel;
    private String m_nodeLabel;

    public void setModel(GraphModel model) {
        m_model = model;
    }

    public GraphModel getModel() {
        return m_model;
    }

    public void instantiateModel(String modelClass)
		throws ClassNotFoundException, InstantiationException,
		       IllegalAccessException {
        Class c = Class.forName(modelClass);
        setModel((GraphModel)c.newInstance());
    }

    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }

    public int getNodeId() {
        return m_nodeId;
    }
    
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setDomain(String domain) {
        m_domain = domain;
    }

    public String getDomain() {
        return m_domain;
    }

    public void setResource(String resource) {
        m_resource = resource;
    }

    public String getResource() {
        return m_resource;
    }

    public void setResourceLabel(String resourceLabel) {
        m_resourceLabel = resourceLabel;
    }

    public String getResourceLabel() {
        return m_resourceLabel;
    }

    public String getHumanReadableNameForIfLabel() throws SQLException {
        return m_model.getHumanReadableNameForIfLabel(m_nodeId, m_resource);
    }

    /*
    public String getNodeLabel() throws SQLException {
        return NetworkElementFactory.getNodeLabel(m_nodeId);
    }
    */

    public void setReports(String[] reports) {
        m_reports = reports;
    }

    public String[] getReports() {
        return m_reports;
    }

    public void setStart(Date start) {
        m_start = start;
    }

    public Date getStart() {
        return m_start;
    }

    public void setEnd(Date end) {
        m_end = end;
    }

    public Date getEnd() {
        return m_end;
    }

    public void setRelativeTime(String relativeTime) {
        m_relativeTime = relativeTime;
    }

    public String getRelativeTime() {
        return m_relativeTime;
    }

    /**
     * Convert the report names to graph objects.
     */
    public void initializeGraphs() {
	m_graphs = new Graph[m_reports.length];

	for (int i=0; i < m_reports.length; i++) {
	    PrefabGraph prefabGraph = m_model.getQuery(m_reports[i]);

	    if (prefabGraph == null) {
		throw new IllegalArgumentException("Unknown report name: " +
		    m_reports[i]);
	    }

	    m_graphs[i] = new Graph(m_model, prefabGraph, m_nodeId, m_resource,
                                    m_resourceType,
				    m_start, m_end);
        }

	/*
	 * Sort the graphs by their order in the properties file.
	 * PrefabGraph implements the Comparable interface.
	 */
	Arrays.sort(m_graphs);
    }

    /**
     * Convert the report names to graph objects for domain graphs.
     */
    public void initializeDomainGraphs() {
	m_graphs = new Graph[m_reports.length];

	for (int i=0; i < m_reports.length; i++) {
	    PrefabGraph prefabGraph = m_model.getQuery(m_reports[i]);

	    if (prefabGraph == null) {
		throw new IllegalArgumentException("Unknown report name: " +
		    m_reports[i]);
	    }
	    m_graphs[i] = new Graph(m_model, prefabGraph, m_domain, m_resource,
				    m_resourceType, m_start, m_end);
        }

	/*
	 * Sort the graphs by their order in the properties file.
	 * PrefabGraph implements the Comparable interface.
	 */
	Arrays.sort(m_graphs);
    }

    public Graph[] getGraphs() {
	return m_graphs;
    }

    public void setRelativeTimePeriods(RelativeTimePeriod[]
				       relativeTimePeriods) {
	m_relativeTimePeriods = relativeTimePeriods;
    }

    public RelativeTimePeriod[] getRelativeTimePeriods() {
	return m_relativeTimePeriods;
    }

    public void setResourceType(String resourceType) {
        m_resourceType = resourceType;
    }
    
    public String getResourceType() {
        return m_resourceType;
    }

    public void setResourceTypeLabel(String resourceTypeLabel) {
        m_resourceTypeLabel = resourceTypeLabel;
    }
    
    public String getResourceTypeLabel() {
        return m_resourceTypeLabel;
    }

}
