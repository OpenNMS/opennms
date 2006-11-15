package org.opennms.netmgt.threshd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Threshold;

public class SnmpThresholdConfig {
    
    private ThresholdingConfigFactory m_thresholdConfig;

    public SnmpThresholdConfig(ThresholdingConfigFactory instance) {
        m_thresholdConfig = instance;
    }
    
    ThresholdingConfigFactory getConfig() {
        return m_thresholdConfig;
    }

    Map<String, ThresholdEntity> createThresholdMap(String groupName, String dsType) {
        Map<String, ThresholdEntity> thresholdMap = new HashMap<String, ThresholdEntity>();
        Iterator iter = getConfig().getThresholds(groupName).iterator();
        while (iter.hasNext()) {
            Threshold thresh = (Threshold) iter.next();

            // See if map entry already exists for this datasource
            // If not, create a new one.
            if (thresh.getDsType().equals(dsType)) {
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

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    String getRrdRepository(String groupName) {
        return getConfig().getRrdRepository(groupName);
    }

}
