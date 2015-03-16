/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsResource;

/**
 * <p>GraphResults class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
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

    private List<String> m_resourceTypes =
            new LinkedList<String>();

    private Map<String,List<GraphResultSet>> m_graphResultMap =
            new HashMap<String,List<GraphResultSet>>();

    private int m_graphTopOffsetWithText;
    private int m_graphLeftOffset;
    private int m_graphRightOffset;
    private int m_resourceIndex = 0;
    
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

    /**
     * <p>setStart</p>
     *
     * @param start a {@link java.util.Date} object.
     */
    public void setStart(Date start) {
        m_start = start;
    }

    /**
     * <p>getStart</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getStart() {
        return m_start;
    }
    
    /**
     * <p>getStartCalendar</p>
     *
     * @return a {@link org.opennms.web.svclayer.model.GraphResults.BeanFriendlyCalendar} object.
     */
    public BeanFriendlyCalendar getStartCalendar() {
        return new BeanFriendlyCalendar(m_start);
    }

    /**
     * <p>setEnd</p>
     *
     * @param end a {@link java.util.Date} object.
     */
    public void setEnd(Date end) {
        m_end = end;
    }

    /**
     * <p>getEnd</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getEnd() {
        return m_end;
    }

    /**
     * <p>getEndCalendar</p>
     *
     * @return a {@link org.opennms.web.svclayer.model.GraphResults.BeanFriendlyCalendar} object.
     */
    public BeanFriendlyCalendar getEndCalendar() {
        return new BeanFriendlyCalendar(m_end);
    }

    /**
     * <p>setRelativeTime</p>
     *
     * @param relativeTime a {@link java.lang.String} object.
     */
    public void setRelativeTime(String relativeTime) {
        m_relativeTime = relativeTime;
    }

    /**
     * <p>getRelativeTime</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRelativeTime() {
        return m_relativeTime;
    }

    /**
     * <p>setRelativeTimePeriods</p>
     *
     * @param relativeTimePeriods an array of {@link org.opennms.web.graph.RelativeTimePeriod} objects.
     */
    public void setRelativeTimePeriods(RelativeTimePeriod[]
				       relativeTimePeriods) {
	m_relativeTimePeriods = relativeTimePeriods;
    }

    /**
     * <p>getRelativeTimePeriods</p>
     *
     * @return an array of {@link org.opennms.web.graph.RelativeTimePeriod} objects.
     */
    public RelativeTimePeriod[] getRelativeTimePeriods() {
	return m_relativeTimePeriods;
    }
    
    /**
     * <p>getMonths</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] getMonths() {
        return s_months;
    }
    
    /**
     * <p>getMonthMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, String> getMonthMap() {
        return s_monthMap;
    }
    
    /**
     * <p>getHours</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] getHours() {
        return s_hours;
    }
    
    /**
     * <p>getHourMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, String> getHourMap() {
        return s_hourMap;
    }
    
    /**
     * <p>addGraphResultSet</p>
     *
     * @param resultSet a {@link org.opennms.web.svclayer.model.GraphResults.GraphResultSet} object.
     */
    public void addGraphResultSet(GraphResultSet resultSet) {
        resultSet.setIndex(m_resourceIndex++);
        m_graphResultSets.add(resultSet);
        String resourceType = resultSet.getResource().getResourceType().getLabel();
        if (!m_resourceTypes.contains(resourceType)) {
            m_resourceTypes.add(resourceType);
        }
        if (!m_graphResultMap.containsKey(resourceType)) {
            m_graphResultMap.put(resourceType, new LinkedList<GraphResultSet>());
        }
        m_graphResultMap.get(resourceType).add(resultSet);
    }

    /**
     * <p>getGraphResultSets</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getResourceTypes() {
        return m_resourceTypes;
    }

    /**
     * <p>getGraphResultSets</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<GraphResultSet> getGraphResultSets() {
        return m_graphResultSets;
    }
    
    /**
     * <p>getGraphResultMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,List<GraphResultSet>> getGraphResultMap() {
        return m_graphResultMap;
    }
    
    /**
     * <p>getReports</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getReports() {
        return m_reports;
    }

    /**
     * <p>setReports</p>
     *
     * @param reports an array of {@link java.lang.String} objects.
     */
    public void setReports(String[] reports) {
        m_reports = reports;
    }

    public class GraphResultSet {
        private List<Graph> m_graphs = null;
        
        private OnmsResource m_resource;
        
        private int m_index;
        
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

        public int getIndex() {
            return m_index;
        }

        public void setIndex(int index) {
            m_index = index;
        }
    }

    public static class BeanFriendlyCalendar extends GregorianCalendar {
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

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    public int getGraphLeftOffset() {
        return m_graphLeftOffset;
    }

    /**
     * <p>setGraphLeftOffset</p>
     *
     * @param graphLeftOffset a int.
     */
    public void setGraphLeftOffset(int graphLeftOffset) {
        m_graphLeftOffset = graphLeftOffset;
        
    }
    
    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    public int getGraphRightOffset() {
        return m_graphRightOffset;
    }

    /**
     * <p>setGraphRightOffset</p>
     *
     * @param graphRightOffset a int.
     */
    public void setGraphRightOffset(int graphRightOffset) {
        m_graphRightOffset = graphRightOffset;
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    public int getGraphTopOffsetWithText() {
        return m_graphTopOffsetWithText;
    }

    /**
     * <p>setGraphTopOffsetWithText</p>
     *
     * @param graphTopOffsetWithText a int.
     */
    public void setGraphTopOffsetWithText(int graphTopOffsetWithText) {
        m_graphTopOffsetWithText = graphTopOffsetWithText;
    }

}
