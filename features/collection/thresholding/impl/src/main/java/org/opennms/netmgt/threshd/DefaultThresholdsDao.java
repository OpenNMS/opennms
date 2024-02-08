/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.netmgt.config.dao.thresholding.api.ReadableThresholdingDao;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.threshd.api.ThresholdingEventProxy;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
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

    private ThresholdingEventProxy m_eventProxy;
    
    private ReadableThresholdingDao m_thresholdingDao;
    
    private EntityScopeProvider m_entityScopeProvider;

    /** {@inheritDoc} */
    @Override
    public ThresholdGroup get(String name, ThresholdingSession thresholdingSession) {
        return get(name, null, thresholdingSession);
    }

    /** {@inheritDoc} */
    @Override
    public ThresholdGroup merge(ThresholdGroup group, ThresholdingSession thresholdingSession) {
        return get(group.getName(), group, thresholdingSession);
    }

    private ThresholdGroup get(String name, ThresholdGroup group, ThresholdingSession thresholdingSession) {
        boolean merge = group != null;
        ThresholdGroup newGroup = new ThresholdGroup(name);

        File rrdRepository = new File(m_thresholdingDao.getReadOnlyConfig().getGroup(name).getRrdRepository());
        newGroup.setRrdRepository(rrdRepository);

        ThresholdResourceType nodeType = getThresholdResourceType(name, "node", merge ? group.getNodeResourceType() : null, thresholdingSession);
        newGroup.setNodeResourceType(nodeType);

        ThresholdResourceType ifType = getThresholdResourceType(name, "if", merge ? group.getIfResourceType() : null, thresholdingSession);
        newGroup.setIfResourceType(ifType);

        for (Basethresholddef thresh : m_thresholdingDao.getReadOnlyConfig().getGroup(name).getThresholdsAndExpressions()) {
            final String id = thresh.getDsType();
            if (!(id.equals("if") || id.equals("node") || newGroup.getGenericResourceTypeMap().containsKey(id))) {
                ThresholdResourceType genericType = getThresholdResourceType(name, id, merge ? group.getGenericResourceTypeMap().get(id) : null, thresholdingSession);
                if (genericType.getThresholdMap().size() > 0) {
                    LOG.info("Adding {}::{} with {} elements", name, id, genericType.getThresholdMap().size());
                    newGroup.getGenericResourceTypeMap().put(id, genericType);
                }
            }
        }

        return newGroup;
    }

    private ThresholdResourceType getThresholdResourceType(String groupName, String typeName, ThresholdResourceType type, ThresholdingSession thresholdingSession) {
        ThresholdResourceType resourceType = new ThresholdResourceType(typeName);
        Map<String, Set<ThresholdEntity>> thresholdMap = null;
        if (type == null) {
            thresholdMap = new HashMap<String, Set<ThresholdEntity>>();
            fillThresholdStateMap(groupName, typeName, thresholdMap, thresholdingSession);
        } else {
            thresholdMap = type.getThresholdMap();
            fillThresholdStateMap(groupName, type.getDsType(), thresholdMap, thresholdingSession);

        }
        resourceType.setThresholdMap(thresholdMap);
        return resourceType;
    }

    private void fillThresholdStateMap(String groupName, String  typeName, Map<String, Set<ThresholdEntity>> thresholdMap, ThresholdingSession thresholdingSession) {
        boolean merge = !thresholdMap.isEmpty();
        for (Basethresholddef thresh : m_thresholdingDao.getReadOnlyConfig().getGroup(groupName).getThresholdsAndExpressions()) {
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
                        ThresholdEntity thresholdEntity = new ThresholdEntity(m_entityScopeProvider);
                        thresholdEntity.setEventProxy(m_eventProxy);
                        thresholdEntity.addThreshold(wrapper, thresholdingSession);
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
                    for (final Basethresholddef thresh : m_thresholdingDao.getReadOnlyConfig().getGroup(groupName).getThresholdsAndExpressions()) {
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

    public void setThresholdingDao(ReadableThresholdingDao thresholdingDao) {
        m_thresholdingDao = Objects.requireNonNull(thresholdingDao);
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_thresholdingDao != null, "thresholdingDao property not set");
        Assert.state(m_entityScopeProvider != null, "entityScopeProvider property not set");
    }

    public void setEventProxy(ThresholdingEventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }

    public void setEntityScopeProvider(EntityScopeProvider entityScopeProvider) {
        m_entityScopeProvider = Objects.requireNonNull(entityScopeProvider);
    }
}
