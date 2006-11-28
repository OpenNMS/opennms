package org.opennms.netmgt.threshd;

import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class ThresholdResourceType {

    private String m_dsType;

    private Map<String, ThresholdEntity> m_thresholdMap;
    
    public ThresholdResourceType(String type) {
        m_dsType = type;
    }

    public String getDsType() {
        return m_dsType;
    }
    
    public Map<String, ThresholdEntity> getThresholdMap() {
        return m_thresholdMap;
    }
    
    public void setThresholdMap(Map<String, ThresholdEntity> thresholdMap) {
    	m_thresholdMap = thresholdMap;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    

}
