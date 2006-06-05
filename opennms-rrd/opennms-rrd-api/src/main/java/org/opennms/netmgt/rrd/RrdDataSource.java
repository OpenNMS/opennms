package org.opennms.netmgt.rrd;

public class RrdDataSource {
    private String m_name;
    private String m_type;
    private int m_heartBeat;
    private String m_min;
    private String m_max;
    
    public RrdDataSource(String name, String type, int heartBeat, String min, String max) {
        m_name = name;
        m_type = type;
        m_heartBeat = heartBeat;
        m_min = min;
        m_max = max;
    }

    public int getHeartBeat() {
        return m_heartBeat;
    }

    public String getMax() {
        return m_max;
    }

    public String getMin() {
        return m_min;
    }

    public String getName() {
        return m_name;
    }

    public String getType() {
        return m_type;
    }

}
