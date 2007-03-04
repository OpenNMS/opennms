package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NodeRtc implements IsSerializable {

    private String m_nodeLabel;
    private String m_availability;
    private int m_downServiceCount;
    private int m_serviceCount;
    private String m_serviceStyle;
    private String m_availabilityStyle;

    public void setNodeLabel(String label) {
        m_nodeLabel = label;
    }

    public void setAvailability(String availability) {
        m_availability = availability;
    }

    public void setDownServiceCount(int downServiceCount) {
        m_downServiceCount = downServiceCount;
    }

    public void setServiceCount(int serviceCount) {
        m_serviceCount = serviceCount;
    }

    public String getAvailability() {
        return m_availability;
    }

    public int getDownServiceCount() {
        return m_downServiceCount;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public int getServiceCount() {
        return m_serviceCount;
    }

    public String getServiceStyle() {
        return m_serviceStyle;
    }

    public String getAvailabilityStyle() {
        return m_availabilityStyle;
    }

    public void setServiceStyle(String serviceStyle) {
        m_serviceStyle = serviceStyle;
    }

    public void setAvailabilityStyle(String availabilityStyle) {
        m_availabilityStyle = availabilityStyle;
    }

}
