package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Threshold;

public class DefaultThresholdsDao implements ThresholdsDao {
	
	public DefaultThresholdsDao() {
		
	}

	public ThresholdGroup get(String name) {
		
		File rrdRepository = new File(ThresholdingConfigFactory.getInstance().getRrdRepository(name));

		ThresholdGroup group = new ThresholdGroup(name);
		group.setRrdRepository(rrdRepository);
		
		ThresholdResourceType nodeType = createType(name, "node");
        group.setNodeResourceType(nodeType);
        
        ThresholdResourceType ifType = createType(name, "if");
        group.setIfResourceType(ifType);

        return group;

	}

	private ThresholdResourceType createType(String groupName, String type) {
		ThresholdResourceType resourceType = new ThresholdResourceType(type);
		resourceType.setThresholdMap(createThresholdStateMap(type, groupName));
		return resourceType;
	}

	public Map<String, ThresholdEntity> createThresholdStateMap(String type, String groupName) {
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

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

}
