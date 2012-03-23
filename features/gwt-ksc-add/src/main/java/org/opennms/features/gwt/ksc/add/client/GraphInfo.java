package org.opennms.features.gwt.ksc.add.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;

public final class GraphInfo {
    private String m_report     = null;
    private String m_title      = null;
    private String m_resourceId = null;
    private String m_timespan   = null;
    private Integer m_startTime = null;
    private Integer m_endTime   = null;

    public GraphInfo(final Element elem) {
        // required
        m_report               = elem.getAttribute("report");
        m_resourceId           = elem.getAttribute("resourceId");
        m_timespan             = elem.getAttribute("timespan");
        if (m_report == null) {
            GWT.log("element missing the required report tag!");
        }
        if (m_resourceId == null) {
            GWT.log("element missing the required resourceId tag!");
        }
        if (m_timespan == null) {
            GWT.log("element missing the required timespan tag!");
        }

        // optional
        m_title                = elem.getAttribute("graphTitle");
        
        if ("lastday".equals(m_timespan)) {
            m_timespan = "1_day";
        } else if ("lastweek".equals(m_timespan)) {
            m_timespan = "7_day";
        } else if ("lastmonth".equals(m_timespan)) {
            m_timespan = "1_month";
        } else if ("lastyear".equals(m_timespan)) {
            m_timespan = "1_year";
        } else {
            GWT.log("invalid timespan '" + m_timespan + "', using 7_day");
        }
    }

    public String getReportName() {
        return m_report;
    }
    
    public String getResourceId() {
        return m_resourceId;
    }
    
    public String getTimespan() {
        return m_timespan;
    }

    public String getTitle() {
        return m_title;
    }

    public Integer getStartTime() {
        return m_startTime;
    }
    
    public Integer getEndTime() {
        return m_endTime;
    }
}