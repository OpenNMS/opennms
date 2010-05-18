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
// 2009 Jan 19: Organize imports. - dj@opennms.org
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultThresholdsDao implements ThresholdsDao, InitializingBean {
    private ThresholdingConfigFactory m_thresholdingConfigFactory;
    
    public ThresholdGroup get(String name) {
        return get(name, null);
    }

    public ThresholdGroup merge(ThresholdGroup group) {
        return get(group.getName(), group);
    }

    private ThresholdGroup get(String name, ThresholdGroup group) {
        boolean merge = group != null;
        ThresholdGroup newGroup = new ThresholdGroup(name);

        File rrdRepository = new File(getThresholdingConfigFactory().getRrdRepository(name));
        newGroup.setRrdRepository(rrdRepository);

        ThresholdResourceType nodeType = merge ? mergeType(name, "node", group.getNodeResourceType()) : createType(name, "node");
        newGroup.setNodeResourceType(nodeType);

        ThresholdResourceType ifType = merge ? mergeType(name, "if", group.getIfResourceType()): createType(name, "if");
        newGroup.setIfResourceType(ifType);

        for (Basethresholddef thresh : getThresholdingConfigFactory().getThresholds(name)) {
            String id = thresh.getDsType();
            if (!(id.equals("if") || id.equals("node") || newGroup.getGenericResourceTypeMap().containsKey(id))) {
                ThresholdResourceType genericType = merge ? mergeType(name, id, group.getGenericResourceTypeMap().get(id)) : createType(name, id);
                if (genericType.getThresholdMap().size() > 0) {
                    log().info("Adding " + name + "::" + id + " with " + genericType.getThresholdMap().size() + " elements");
                    newGroup.getGenericResourceTypeMap().put(id, genericType);
                }
            }
        }

        return newGroup;
    }
    
    private Map<String, Set<ThresholdEntity>> createThresholdStateMap(String groupName, String typeName) {
        Map<String, Set<ThresholdEntity>> thresholdMap = new HashMap<String, Set<ThresholdEntity>>();
        fillThresholdStateMap(groupName, typeName, thresholdMap);
        return thresholdMap;
    }

    private Map<String, Set<ThresholdEntity>> mergeThresholdStateMap(String groupName, ThresholdResourceType type) {
        Map<String, Set<ThresholdEntity>> thresholdMap = type.getThresholdMap();
        fillThresholdStateMap(groupName, type.getDsType(), thresholdMap);
        return thresholdMap;
    }

    private void fillThresholdStateMap(String groupName, String  typeName, Map<String, Set<ThresholdEntity>> thresholdMap) {
        boolean merge = !thresholdMap.isEmpty();
        for (Basethresholddef thresh : getThresholdingConfigFactory().getThresholds(groupName)) {
            // See if map entry already exists for this datasource; if not, create a new one.
            if (thresh.getDsType().equals(typeName)) {
                try {
                    BaseThresholdDefConfigWrapper wrapper=BaseThresholdDefConfigWrapper.getConfigWrapper(thresh);
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
                        if (merge) {
                            boolean updated = false;
                            for (ThresholdEntity e : thresholdEntitySet) {
                                // See implementation on BaseThresholdDefConfigWrapper equals are different from identical for this case.
                                if (thresholdEntity.getThresholdConfig().equals(e.getThresholdConfig())) {
                                    e.merge(thresholdEntity);
                                    updated = true;
                                }
                            }
                            if (!updated) // Does not exist!
                                thresholdEntitySet.add(thresholdEntity);
                        } else {
                            thresholdEntitySet.add(thresholdEntity);
                        }
                    } catch (IllegalStateException e) {
                        log().warn("fillThresholdStateMap: Encountered duplicate " + thresh.getType() + " for datasource " + wrapper.getDatasourceExpression() + ": " + e, e);
                    } 
                }
                catch (ThresholdExpressionException e) {
                    log().warn("fillThresholdStateMap: Could not parse threshold expression: "+e.getMessage(), e);
                }
            }
        }
        // Search for deleted configuration
        if (merge) {
            log().debug("fillThresholdStateMap(merge): checking if definitions that are no longer exist for group " + groupName + " using type " + typeName);
            for (String expression : thresholdMap.keySet()) {
                for (Iterator<ThresholdEntity> i = thresholdMap.get(expression).iterator(); i.hasNext();) {
                    ThresholdEntity entity = i.next();
                    boolean found = false;
                    for (Basethresholddef thresh : getThresholdingConfigFactory().getThresholds(groupName)) {
                        BaseThresholdDefConfigWrapper newConfig = null;
                        try {
                            newConfig = BaseThresholdDefConfigWrapper.getConfigWrapper(thresh);
                        } catch (ThresholdExpressionException e) {
                            log().warn("fillThresholdStateMap: Could not parse threshold expression: " + e.getMessage(), e);
                        }
                        if (newConfig.equals(entity.getThresholdConfig())) {
                            found = true;
                            continue;
                        }
                    }
                    if (!found) {
                        log().info("fillThresholdStateMap(merge): deleting entity " + entity);
                        entity.delete();
                        i.remove();
                    }
                }
            }
        }
    }

    private ThresholdResourceType createType(String groupName, String typeName) {
        ThresholdResourceType resourceType = new ThresholdResourceType(typeName);
        resourceType.setThresholdMap(createThresholdStateMap(groupName, typeName));
        return resourceType;
    }
    

    private ThresholdResourceType mergeType(String groupName, String typeName, ThresholdResourceType type) {
        ThresholdResourceType resourceType = new ThresholdResourceType(typeName);
        resourceType.setThresholdMap(mergeThresholdStateMap(groupName, type));
        return resourceType;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    public ThresholdingConfigFactory getThresholdingConfigFactory() {
        return m_thresholdingConfigFactory;
    }

    public void setThresholdingConfigFactory(ThresholdingConfigFactory thresholdingConfigFactory) {
        m_thresholdingConfigFactory = thresholdingConfigFactory;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_thresholdingConfigFactory != null, "thresholdingConfigFactory property not set");
    }

}
