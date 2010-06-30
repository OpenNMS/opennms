//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jan 29: Indent, convert to use Java 5 generics, use dependency injection for ThresholdingConfigFactory - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>DefaultThresholdsDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultThresholdsDao implements ThresholdsDao, InitializingBean {
    private ThresholdingConfigFactory m_thresholdingConfigFactory;
    
    /**
     * <p>Constructor for DefaultThresholdsDao.</p>
     */
    public DefaultThresholdsDao() {
        
    }

    /** {@inheritDoc} */
    public ThresholdGroup get(String name) {
	ThresholdGroup group = new ThresholdGroup(name);

        File rrdRepository = new File(getThresholdingConfigFactory().getRrdRepository(name));
	group.setRrdRepository(rrdRepository);
	
	ThresholdResourceType nodeType = createType(name, "node");
	group.setNodeResourceType(nodeType);
    
	ThresholdResourceType ifType = createType(name, "if");
	group.setIfResourceType(ifType);

	for (Basethresholddef thresh : getThresholdingConfigFactory().getThresholds(name)) {
	    String id = thresh.getDsType();
	    if (!(id.equals("if") || id.equals("node") || group.getGenericResourceTypeMap().containsKey(id))) {
	        ThresholdResourceType genericType = createType(name, id);
	        if (genericType.getThresholdMap().size() > 0) {
	            log().info("Adding " + name + "::" + id + " with " + genericType.getThresholdMap().size() + " elements");
	            group.getGenericResourceTypeMap().put(id, genericType);
	        }
	    }
	}
            
	return group;
    }

    private Map<String, Set<ThresholdEntity>> createThresholdStateMap(String type, String groupName) {
        Map<String, Set<ThresholdEntity>> thresholdMap = new HashMap<String, Set<ThresholdEntity>>();
        
        for (Basethresholddef thresh : getThresholdingConfigFactory().getThresholds(groupName)) {
            // See if map entry already exists for this datasource
            // If not, create a new one.
            if (thresh.getDsType().equals(type)) {
                try {
                    BaseThresholdDefConfigWrapper wrapper=BaseThresholdDefConfigWrapper.getConfigWrapper(thresh);
                    //ThresholdEntity thresholdEntity = thresholdMap.get(wrapper.getDatasourceExpression());
                    Set<ThresholdEntity> thresholdEntitySet = thresholdMap.get(wrapper.getDatasourceExpression());
            
                    // Found set for this DS type?
                    if (thresholdEntitySet == null) {
                        // Nope, create a new set
                        thresholdEntitySet = new LinkedHashSet<ThresholdEntity>();
                        thresholdMap.put(wrapper.getDatasourceExpression(), thresholdEntitySet);
                    }
            
                    try {
                    	ThresholdEntity thresholdEntity = new ThresholdEntity();
                    	thresholdEntity.addThreshold(wrapper);
                    	thresholdMap.get(wrapper.getDatasourceExpression()).add(thresholdEntity);
                    } catch (IllegalStateException e) {
                        log().warn("Encountered duplicate " + thresh.getType() + " for datasource " + wrapper.getDatasourceExpression() + ": " + e, e);
                    } 
                }
                catch (ThresholdExpressionException e) {
                    log().warn("Could not parse threshold expression: "+e.getMessage(), e);
                }

            }
        }
        
        return thresholdMap;
    }
    
    ThresholdResourceType createType(String groupName, String type) {
        ThresholdResourceType resourceType = new ThresholdResourceType(type);
        resourceType.setThresholdMap(createThresholdStateMap(type, groupName));
        return resourceType;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>getThresholdingConfigFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.ThresholdingConfigFactory} object.
     */
    public ThresholdingConfigFactory getThresholdingConfigFactory() {
        return m_thresholdingConfigFactory;
    }

    /**
     * <p>setThresholdingConfigFactory</p>
     *
     * @param thresholdingConfigFactory a {@link org.opennms.netmgt.config.ThresholdingConfigFactory} object.
     */
    public void setThresholdingConfigFactory(ThresholdingConfigFactory thresholdingConfigFactory) {
        m_thresholdingConfigFactory = thresholdingConfigFactory;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_thresholdingConfigFactory != null, "thresholdingConfigFactory property not set");
    }

}
