package org.opennms.web.graph;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.web.Util;

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

    private String[] m_reports;
    
    private Date m_start;
    private Date m_end;
    private String m_relativeTime;
    private RelativeTimePeriod[] m_relativeTimePeriods;
    
    private List<GraphResultSet> m_graphResultSets =
        new LinkedList<GraphResultSet>();
    
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

    public void setRelativeTimePeriods(RelativeTimePeriod[]
				       relativeTimePeriods) {
	m_relativeTimePeriods = relativeTimePeriods;
    }

    public RelativeTimePeriod[] getRelativeTimePeriods() {
	return m_relativeTimePeriods;
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
    
    public void addGraphResultSet(GraphResultSet resultSet) {
        m_graphResultSets.add(resultSet);
    }
    
    public List<GraphResultSet> getGraphResultSets() {
        return m_graphResultSets;
    }
    
    public class GraphResultSet {
        private Graph[] m_graphs;
        
        private String m_parentResourceType;
        private String m_parentResourceTypeLabel;
        private String m_parentResource;
        private String m_parentResourceLabel;
        private String m_parentResourceLink;
        private String m_resourceType;
        private String m_resourceTypeLabel;
        private String m_resource = null;
        private String m_resourceLabel;
        private String m_resourceLink;
        

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
        
        public String getResourceId() {
            return Util.encode(m_parentResourceType) + "[" + Util.encode(m_parentResource) + "]."
                   + Util.encode(m_resourceType) + "[" + Util.encode(m_resource) + "]";
        }

        /**
         * Convert the report names to graph objects.
         */
        public void initializeGraphs(GraphModel model, String[] reports) {
            m_graphs = new Graph[reports.length];

            for (int i=0; i < reports.length; i++) {
                PrefabGraph prefabGraph = model.getQuery(m_resourceType,
                                                         reports[i]);

                if (prefabGraph == null) {
                    throw new IllegalArgumentException("Unknown report name: " +
                                                       reports[i]);
                }

                m_graphs[i] = new Graph(prefabGraph, m_parentResourceType, m_parentResource, m_resourceType, m_resource,
                                        m_start, m_end);
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

    public String[] getReports() {
        return m_reports;
    }

    public void setReports(String[] reports) {
        m_reports = reports;
    }
}
