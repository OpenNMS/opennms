package org.opennms.netmgt.threshd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Threshold;

public class ThresholdResourceType {

    private String m_dsType;
    private String m_groupName;
    private Map<String, ThresholdEntity> m_thresholdMap;
    
    public ThresholdResourceType(String type, String groupName) {
        m_dsType = type;
        m_groupName = groupName;
        m_thresholdMap = createThresholdMap(m_dsType, m_groupName);
    }

    public String getDsType() {
        return m_dsType;
    }
    
    public String getGroupName() {
        return m_groupName;
    }
    
    public Map<String, ThresholdEntity> getThresholdMap() {
        return m_thresholdMap;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public Map<String, ThresholdEntity> createThresholdMap(final String type, String groupName) {
        Map<String, ThresholdEntity> thresholdMap = new HashMap<String, ThresholdEntity>();
        Iterator iter = ThresholdingConfigFactory.getInstance().getThresholds(groupName).iterator();
        while (iter.hasNext()) {
            Threshold thresh = (Threshold) iter.next();
        
            // See if map entry already exists for this datasource
            // If not, create a new one.
            if (thresh.getDsType().equals(type)) {
                ThresholdEntity thresholdEntity = thresholdMap.get(thresh.getDsName());
        
                // Found entry?
                if (thresholdEntity == null) {
                    // Nope, create a new one
                    thresholdEntity = new ThresholdEntity();
                    thresholdMap.put(thresh.getDsName(), thresholdEntity);
                }
        
                try {
                    thresholdEntity.setThreshold(thresh);
                } catch (IllegalStateException e) {
                    log().warn("Encountered duplicate " + thresh.getType() + " for datasource " + thresh.getDsName(), e);
                }
        
            }
        }
        return thresholdMap;
    }
    
    

}
