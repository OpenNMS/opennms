package org.opennms.netmgt.threshd;

import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class ThresholdResourceType {

    private String m_dsType;
	private ThresholdGroup m_group;

    private Map<String, ThresholdEntity> m_thresholdMap;
    
    public ThresholdResourceType(String type, ThresholdGroup group) {
        m_dsType = type;
        m_group = group;
        m_thresholdMap = m_group.createThresholdStateMap(m_dsType);
    }

    public String getDsType() {
        return m_dsType;
    }
    
    public String getGroupName() {
        return m_group.getName();
    }
    
    public Map<String, ThresholdEntity> getThresholdMap() {
        return m_thresholdMap;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    

}
