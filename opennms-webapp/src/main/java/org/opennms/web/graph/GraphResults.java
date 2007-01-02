package org.opennms.web.graph;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsResource;

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
    
    // FIXME: this is very US-centric; can we have it use the system locale?
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
    
    public String[] getReports() {
        return m_reports;
    }

    public void setReports(String[] reports) {
        m_reports = reports;
    }

    public class GraphResultSet {
        private List<Graph> m_graphs = null;
        
        private OnmsResource m_resource;
        
        public GraphResultSet() {
        }
        
        public void setResource(OnmsResource resource) {
            m_resource = resource;
        }
        
        public OnmsResource getResource() {
            return m_resource;
        }

        public List<Graph> getGraphs() {
            return m_graphs;
        }

        public void setGraphs(List<Graph> graphs) {
            m_graphs = graphs;
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
}
