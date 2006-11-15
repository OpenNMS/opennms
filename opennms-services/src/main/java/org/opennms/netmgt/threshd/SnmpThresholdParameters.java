package org.opennms.netmgt.threshd;

import java.util.Map;

import org.opennms.netmgt.utils.ParameterMap;

public class SnmpThresholdParameters {
    
    private Map<String, String> m_parameters;
    /**
     * Default age before which a data point is considered "out of date"
     */
    
    static final int DEFAULT_RANGE = 0; // 300s or 5m
    /**
     * Default thresholding interval (in milliseconds).
     * 
     */
    static final int DEFAULT_INTERVAL = 300000; // 300s or 5m

    @SuppressWarnings("unchecked")
    public SnmpThresholdParameters(Map parameters) {
        m_parameters = parameters;
    }
    
    public Map<String, String> getParameters() {
        return m_parameters;
    }

    String getGroupName() {
        return ParameterMap.getKeyedString(getParameters(), "thresholding-group", "default");
    }

    int getInterval() {
        return ParameterMap.getKeyedInteger(getParameters(), "interval", SnmpThresholdParameters.DEFAULT_INTERVAL);
    }

    int getRange() {
        return ParameterMap.getKeyedInteger(getParameters(), "range", SnmpThresholdParameters.DEFAULT_RANGE);
    }

}
