package org.opennms.netmgt.correlation.drools;

public class TimerExpired {
    
    private Integer m_id;
    
    TimerExpired(Integer id) {
        m_id = id;
    }
    
    public Integer getId() {
        return m_id;
    }

}
