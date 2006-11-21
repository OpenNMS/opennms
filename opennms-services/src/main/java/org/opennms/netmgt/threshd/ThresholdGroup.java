package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Threshold;

public class ThresholdGroup {

	private String m_name;

	public ThresholdGroup(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public File getRrdRepository() {
		return new File(ThresholdingConfigFactory.getInstance().getRrdRepository(getName()));
	}

	public Map<String, ThresholdEntity> createThresholdStateMap(final String type) {
	    Map<String, ThresholdEntity> thresholdMap = new HashMap<String, ThresholdEntity>();
	    Iterator iter = ThresholdingConfigFactory.getInstance().getThresholds(getName()).iterator();
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

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

}
