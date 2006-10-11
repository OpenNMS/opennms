package org.opennms.netmgt.poller.remote.support;

import org.opennms.netmgt.poller.remote.PollerSettings;

public class DefaultPollerSettings implements PollerSettings {
    
    Integer m_monitorId = null;

    public Integer getMonitorId() {
        return m_monitorId;    
    }

    public void setMonitorId(Integer monitorId) {
        m_monitorId = monitorId;
    }

}
