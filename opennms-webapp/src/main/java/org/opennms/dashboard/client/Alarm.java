package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Alarm implements IsSerializable {
    
    private String m_descrption;
    private String m_severity;
    private int m_count;
    private String m_nodeLabel;
    private int m_nodeId;
    private String m_ipAddress;
    private String m_svcName;
    
    public Alarm() {
        
    }
    
    public Alarm(String severity, String nodeLabel, String description, int count) {
        m_severity = severity;
        m_nodeLabel = nodeLabel;
        m_descrption = description;
        m_count = count;
    }
    public int getCount() {
        return m_count;
    }
    public void setCount(int count) {
        m_count = count;
    }
    public String getDescrption() {
        return m_descrption;
    }
    public void setDescrption(String descrption) {
        m_descrption = descrption;
    }
    public String getIpAddress() {
        return m_ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        m_ipAddress = ipAddress;
    }
    public int getNodeId() {
        return m_nodeId;
    }
    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }
    public String getNodeLabel() {
        return m_nodeLabel;
    }
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }
    public String getSeverity() {
        return m_severity;
    }
    public void setSeverity(String severity) {
        m_severity = severity;
    }
    public String getSvcName() {
        return m_svcName;
    }
    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }
    
    

}
