/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>DefaultThresholdsDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultThresholdsDao implements ThresholdsDao, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultThresholdsDao.class);
    
    private ThresholdingConfigFactory m_thresholdingConfigFactory;
    
    /** {@inheritDoc} */
    @Override
    public ThresholdGroup get(String name) {
        return get(name, null);
    }

    /** {@inheritDoc} */
    @Override
    public ThresholdGroup merge(ThresholdGroup group) {
        return get(group.getName(), group);
    }

    private ThresholdGroup get(String name, ThresholdGroup group) {
        boolean merge = group != null;
        ThresholdGroup newGroup = new ThresholdGroup(name);

        File rrdRepository = new File(getThresholdingConfigFactory().getRrdRepository(name));
        newGroup.setRrdRepository(rrdRepository);

        ThresholdResourceType nodeType = getThresholdResourceType(name, "node", merge ? group.getNodeResourceType() : null);
        newGroup.setNodeResourceType(nodeType);

        ThresholdResourceType ifType = getThresholdResourceType(name, "if", merge ? group.getIfResourceType() : null);
        newGroup.setIfResourceType(ifType);

        for (Basethresholddef thresh : getThresholdingConfigFactory().getThresholds(name)) {
            final String id = thresh.getDsType();
            if (!(id.equals("if") || id.equals("node") || newGroup.getGenericResourceTypeMap().containsKey(id))) {
                ThresholdResourceType genericType = getThresholdResourceType(name, id, merge ? group.getGenericResourceTypeMap().get(id) : null);
                if (genericType.getThresholdMap().size() > 0) {
                    LOG.info("Adding {}::{} with {} elements", name, id, genericType.getThresholdMap().size());
                    newGroup.getGenericResourceTypeMap().put(id, genericType);
                }
            }
        }

        return newGroup;
    }

    private ThresholdResourceType getThresholdResourceType(String groupName, String typeName, ThresholdResourceType type) {
        ThresholdResourceType resourceType = new ThresholdResourceType(typeName);
        Map<String, Set<ThresholdEntity>> thresholdMap = null;
        if (type == null) {
            thresholdMap = new HashMap<String, Set<ThresholdEntity>>();
            fillThresholdStateMap(groupName, typeName, thresholdMap);
        } else {
            thresholdMap = type.getThresholdMap();
            fillThresholdStateMap(groupName, type.getDsType(), thresholdMap);

        }
        resourceType.setThresholdMap(thresholdMap);
        return resourceType;
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
                        LOG.warn("fillThresholdStateMap: Encountered duplicate {} for datasource {}", thresh.getType(), wrapper.getDatasourceExpression(), e);
                    } 
                }
                catch (ThresholdExpressionException e) {
                    LOG.warn("fillThresholdStateMap: Could not parse threshold expression", e);
                }
            }
        }
        // Search for deleted configuration
        if (merge) {
            LOG.debug("fillThresholdStateMap(merge): checking if definitions that are no longer exist for group {} using type {}", groupName, typeName);
            for (final Entry<String, Set<ThresholdEntity>> entry : thresholdMap.entrySet()) {
                final Set<ThresholdEntity> value = entry.getValue();
                for (final Iterator<ThresholdEntity> thresholdIterator = value.iterator(); thresholdIterator.hasNext();) {
                    final ThresholdEntity entity = thresholdIterator.next();
                    boolean found = false;
                    for (final Basethresholddef thresh : getThresholdingConfigFactory().getThresholds(groupName)) {
                        BaseThresholdDefConfigWrapper newConfig = null;
                        try {
                            newConfig = BaseThresholdDefConfigWrapper.getConfigWrapper(thresh);
                        } catch (ThresholdExpressionException e) {
                            LOG.warn("fillThresholdStateMap: Could not parse threshold expression", e);
                        }
                        if (newConfig != null && newConfig.equals(entity.getThresholdConfig())) {
                            found = true;
                            continue;
                        }
                    }
                    if (!found) {
                        LOG.info("fillThresholdStateMap(merge): deleting entity {}", entity);
                        entity.delete();
                        thresholdIterator.remove();
                    }
                }
            }
        }
    }

    /**
     * <p>getThresholdingConfigFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.ThresholdingConfigFactory} object.
     */
    @Override
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
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_thresholdingConfigFactory != null, "thresholdingConfigFactory property not set");
    }

}
