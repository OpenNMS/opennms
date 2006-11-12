package org.opennms.web.graph;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphResults {
    //note these run from 0-11, this is because of java.util.Calendar!
    private static final String[] s_months = new String[] {
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    };
    private static final Map<Integer, String> s_monthMap;
    
    private static final String[] s_hours;
    private static final Map<Integer, String> s_hourMap;

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
    private String m_parentResourceType;
    private String m_type;
    private String m_parentResourceTypeLabel;
    private String m_parentResource;
    private String m_parentResourceLabel;
    private String m_parentResourceLink;
    private String m_resourceType;
    private String m_resourceTypeLabel;
    private String m_resourceLabel;
    private String m_resourceLink;
    private String m_nodeLabel;
    
    static {
        s_monthMap = new LinkedHashMap<Integer, String>();
        for (int i = 0; i < s_months.length; i++) {
            s_monthMap.put(i, s_months[i]);
        }
        
        s_hours = new String[24];
        for (int i = 0; i < s_hours.length; i++) {
            int hour = i % 12;
            if (hour == 0) {
                hour = 12;
            }
            s_hours[i] = hour + " " + (i < 12 ? "AM" : "PM");
        }
        
        s_hourMap = new LinkedHashMap<Integer, String>();
        for (int i = 0; i < s_hours.length; i++) {
            s_hourMap.put(i, s_hours[i]);
        }
        
    }

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
    
    public BeanFriendlyCalendar getStartCalendar() {
        return new BeanFriendlyCalendar(m_start);
    }

    public void setEnd(Date end) {
        m_end = end;
    }

    public Date getEnd() {
        return m_end;
    }

    public BeanFriendlyCalendar getEndCalendar() {
        return new BeanFriendlyCalendar(m_end);
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
	    PrefabGraph prefabGraph = m_model.getQuery(m_resourceType,
                                                       m_reports[i]);

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
	    PrefabGraph prefabGraph = m_model.getQuery(m_resourceType,
                                                       m_reports[i]);

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

    public String getParentResourceType() {
        return m_parentResourceType;
    }

    public void setParentResourceType(String parentResourceType) {
        m_parentResourceType = parentResourceType;
    }

    public String getParentResource() {
        return m_parentResource;
    }

    public void setParentResource(String parentResource) {
        m_parentResource = parentResource;
    }

    public String getParentResourceTypeLabel() {
        return m_parentResourceTypeLabel;
    }

    public void setParentResourceTypeLabel(String parentResourceTypeLabel) {
        m_parentResourceTypeLabel = parentResourceTypeLabel;
    }

    public String getResourceLink() {
        return m_resourceLink;
    }

    public void setResourceLink(String resourceLink) {
        m_resourceLink = resourceLink;
    }

    public String getParentResourceLink() {
        return m_parentResourceLink;
    }

    public void setParentResourceLink(String parentResourceLink) {
        m_parentResourceLink = parentResourceLink;
    }

    public String getParentResourceLabel() {
        return m_parentResourceLabel;
    }

    public void setParentResourceLabel(String parentResourceLabel) {
        m_parentResourceLabel = parentResourceLabel;
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }
    
    public static String[] getMonths() {
        return s_months;
    }
    
    public Map<Integer, String> getMonthMap() {
        return s_monthMap;
    }
    
    public static String[] getHours() {
        return s_hours;
    }
    
    public Map<Integer, String> getHourMap() {
        return s_hourMap;
    }

    public class BeanFriendlyCalendar extends GregorianCalendar {
        /**
         * 
         */
        private static final long serialVersionUID = -4145668894553732167L;

        public BeanFriendlyCalendar(Date date) {
            super();
            setTime(date);
        }
        
        public int getYear() {
            return get(Calendar.YEAR);
        }
        
        public int getMonth() {
            return get(Calendar.MONTH); 
        }
        
        public int getDate() {
            return get(Calendar.DATE); 
        }
        
        public int getHourOfDay() {
            return get(Calendar.HOUR_OF_DAY); 
        }
    }
}
