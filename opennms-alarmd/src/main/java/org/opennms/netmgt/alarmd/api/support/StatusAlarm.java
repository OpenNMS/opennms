package org.opennms.netmgt.alarmd.api.support;


import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.alarmd.Resolver;
import org.opennms.netmgt.alarmd.api.Alarm;

/**
 * An event used to represent the Status of Alarm Sync
 *
 * @author brozow
 */
public class StatusAlarm implements Alarm {

    public static StatusAlarm createStartMessage() {
        StatusAlarm a = new StatusAlarm("uei.opennms.org/external/nnm/opennmsdStart");
        try {
                a.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception excp) {
    
        }
        return a;
    }

    public static StatusAlarm createStopMessage() {
        StatusAlarm a = new StatusAlarm("uei.opennms.org/external/nnm/opennmsdStop");
        try {
                a.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception excp) {
    
        }
        return a;
    }

    public static StatusAlarm createSyncLostMessage() {
        StatusAlarm a = new StatusAlarm("uei.opennms.org/external/nnm/opennmsdSyncLost");
        a.setPreserved(true);
        
        try {
            a.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception excp) {

        }
        
        return a;
    }

  
    
    private String m_uei;
    private Date m_timeStamp;
    private boolean m_preserved;
    private String m_agentAddress;

    // This fields caches the resolved agentAddress for using in forwarding
    private String m_nodeLabel;

    
    public String getAgentAddress() {
                return m_agentAddress;
        }

        public void setAgentAddress(String address) {
                m_agentAddress = address;
        }

        public String getNodeLabel() {
                return m_nodeLabel;
        }

        public void setNodeLabel(String label) {
                m_nodeLabel = label;
        }

        public StatusAlarm(String uei) {
        this(uei, new Date());
    }

    public StatusAlarm(String uei, Date timeStamp) {
        m_uei = uei;
        m_timeStamp = timeStamp;
    }
    
    public String getUei() {
        return m_uei;
    }
    
    public Date getTimeStamp() {
        return m_timeStamp;
    }

    public void setTimeStamp(Date timestamp) {
        m_timeStamp = timestamp;
    }

    public boolean isPreserved() {
        return m_preserved;
    }
    
    public void setPreserved(boolean preserved) {
        m_preserved = preserved;
    }

    public String resolveNodeLabel(Resolver r) {
        if (m_nodeLabel == null) {
            m_nodeLabel = r.resolveAddress(getAgentAddress());
        }
        return m_nodeLabel;
    }

}