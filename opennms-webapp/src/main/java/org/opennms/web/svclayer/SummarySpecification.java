package org.opennms.web.svclayer;

public class SummarySpecification {
    private String m_filterRule;
    private long m_startTime;
    private long m_endTime;
    private String m_attributeSieve;
    
    public String getFilterRule() {
        return m_filterRule;
    }
    public void setFilterRule(String filterRule) {
        m_filterRule = filterRule;
    }
    public long getStartTime() {
        return m_startTime;
    }
    public void setStartTime(long startTime) {
        m_startTime = startTime;
    }
    public long getEndTime() {
        return m_endTime;
    }
    public void setEndTime(long endTime) {
        m_endTime = endTime;
    }
    
    public String getAttributeSieve() {
        return m_attributeSieve;
    }
    
    public void setAttributeSieve(String attributeSieve) {
        m_attributeSieve = attributeSieve;
    }
}

