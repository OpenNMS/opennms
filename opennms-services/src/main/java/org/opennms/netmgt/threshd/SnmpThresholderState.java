package org.opennms.netmgt.threshd;

import java.util.Map;

import org.opennms.netmgt.poller.NetworkInterface;

public class SnmpThresholderState {
    
    private static final String KEY = SnmpThresholderState.class.getName();

    public static SnmpThresholderState get(NetworkInterface netIface, Map parms) {
        SnmpThresholderState state = (SnmpThresholderState)netIface.getAttribute(KEY);
        if (state == null) {
            state = new SnmpThresholderState();
            netIface.setAttribute(KEY, state);
        }
        return state;
        
    }

    private Map<String, Map<String, ThresholdEntity>> m_allInterfaceMap;

    public void setAllInterfaceMap(Map<String, Map<String, ThresholdEntity>> allInterfaceMap) {
        m_allInterfaceMap = allInterfaceMap;
    }
    
    public Map<String, Map<String, ThresholdEntity>> getAllInterfaceMap() {
        return m_allInterfaceMap;
    }

}
