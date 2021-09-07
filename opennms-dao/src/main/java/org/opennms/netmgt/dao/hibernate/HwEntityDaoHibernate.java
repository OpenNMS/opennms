/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import org.hibernate.transform.ResultTransformer;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.model.HwEntity;
import org.opennms.netmgt.model.HwEntityAlias;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Class HwEntityDaoHibernate.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HwEntityDaoHibernate extends AbstractDaoHibernate<OnmsHwEntity, Integer> implements HwEntityDao {

    /**
     * The Constructor.
     */
    public HwEntityDaoHibernate() {
        super(OnmsHwEntity.class);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#findRootByNodeId(java.lang.Integer)
     */
    @Override
    public OnmsHwEntity findRootByNodeId(Integer nodeId) {
        return (OnmsHwEntity) findUnique("from OnmsHwEntity e where e.parent is null and e.node.id = ?", nodeId);
    }

    @Override
    public HwEntity findRootEntityByNodeId(Integer nodeId) {
        List<HwEntity> entityList = getHibernateTemplate().execute(session -> (List<HwEntity>)
                session.createSQLQuery("select * FROM hwEntity where nodeid = " + nodeId)
                        .setResultTransformer(
                                new ResultTransformer() {
                                    @Override
                                    public Object transformTuple(Object[] tuple, String[] strings) {
                                        return new HwEntity((Integer) tuple[0], (Integer) tuple[1], (Integer) tuple[2], (Integer) tuple[3], (Integer) tuple[4],
                                                (String) tuple[5], (String) tuple[6], (String) tuple[7], (String) tuple[8], (String) tuple[9],
                                                (String) tuple[10], (String) tuple[11], (String) tuple[12], (String) tuple[13], (String) tuple[14],
                                                (String) tuple[15], (String) tuple[16], (Boolean) tuple[17], (Date) tuple[18], (String) tuple[19]);
                                    }

                                    @Override
                                    public List transformList(List list) {
                                        return list;
                                    }
                                }
                        ).list());
        Optional<HwEntity> optionalHwEntity =  entityList.stream().filter(hwEntity -> hwEntity.getParentId() == null).findFirst();
        if(optionalHwEntity.isPresent()) {
            findChildren(optionalHwEntity.get(), entityList);
            List<HwEntityAlias> hwEntityAliases = findHwEntityAlias(optionalHwEntity.get());
            optionalHwEntity.get().addHwEntityAliasList(hwEntityAliases);
            return optionalHwEntity.get();
        }
        return null;
    }

    private void findChildren(HwEntity parent, List<HwEntity> hwEntityList) {

        Set<HwEntity> children = hwEntityList.stream().filter(hwEntity -> {
                    if (hwEntity.getParentId() != null) {
                        return hwEntity.getParentId().equals(parent.getId());
                    }
                    return false;
                }
        ).collect(Collectors.toSet());
        children.forEach(hwEntity ->  {
            findChildren(hwEntity, hwEntityList);
            List<HwEntityAlias> hwEntityAliases = findHwEntityAlias(hwEntity);
            hwEntity.addHwEntityAliasList(hwEntityAliases);
            parent.addChild(hwEntity);
        });
    }

    private List<HwEntityAlias> findHwEntityAlias(HwEntity parent) {
         List<HwEntityAlias> hwEntityAliases = getHibernateTemplate().execute(session ->
                 (List<HwEntityAlias>) session.createSQLQuery(
                 "SELECT * FROM hwEntityAlias WHERE hwEntityId = " + parent.getParentId())
                 .setResultTransformer(new ResultTransformer() {
             @Override
             public Object transformTuple(Object[] tuple, String[] strings) {
                 return new HwEntityAlias((Integer) tuple[0], (Integer) tuple[1], (Integer) tuple[2], (String) tuple[3]);
             }

             @Override
             public List transformList(List list) {
                 return list;
             }
         }).list());
        return  hwEntityAliases;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#findEntityByIndex(java.lang.Integer, java.lang.Integer)
     */
    @Override
    public OnmsHwEntity findEntityByIndex(Integer nodeId, Integer entPhysicalIndex) {
        return (OnmsHwEntity) findUnique("from OnmsHwEntity e where e.node.id = ? and e.entPhysicalIndex = ?", nodeId, entPhysicalIndex);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#findEntityByName(java.lang.Integer, java.lang.String)
     */
    @Override
    public OnmsHwEntity findEntityByName(Integer nodeId, String entPhysicalName) {
        return (OnmsHwEntity) findUnique("from OnmsHwEntity e where e.node.id = ? and e.entPhysicalName = ?", nodeId, entPhysicalName);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#getAttributeValue(java.lang.Integer, java.lang.Integer, java.lang.String)
     */
    @Override
    public String getAttributeValue(Integer nodeId, Integer entPhysicalIndex, String attributeName) {
        OnmsHwEntity e = findEntityByIndex(nodeId, entPhysicalIndex);
        return e == null ? null : getAttributeValue(e, attributeName);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#getAttributeValue(java.lang.Integer, java.lang.String, java.lang.String)
     */
    @Override
    public String getAttributeValue(Integer nodeId, String nameSource, String attributeName) {
        boolean isRegex = nameSource.startsWith("~");
        if (isRegex) {
            OnmsHwEntity r = findRootByNodeId(nodeId);
            return r == null ? null : findAttribute(r, nameSource.substring(1), attributeName);
        }
        OnmsHwEntity e = findEntityByName(nodeId, nameSource);
        return e == null ? null : getAttributeValue(e, attributeName);
    }

    /**
     * Find attribute.
     *
     * @param entity the entity
     * @param nameRegex the regular expression for entPhysicalName
     * @param attributeName the attribute name
     * @return the string
     */
    private String findAttribute(OnmsHwEntity entity, String nameRegex, String attributeName) {
        if (entity.getEntPhysicalName().matches(nameRegex)) {
            return getAttributeValue(entity, attributeName);
        }
        for (OnmsHwEntity c : entity.getChildren()) {
            String v = findAttribute(c, nameRegex, attributeName);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    /**
     * Gets the attribute value.
     *
     * @param entity the entity
     * @param attributeName the attribute name
     * @return the attribute value
     */
    private String getAttributeValue(OnmsHwEntity entity, String attributeName) {
        if (attributeName.startsWith("entPhysical")) {
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
            if (wrapper.isWritableProperty(attributeName)) {
                return (String) wrapper.getPropertyValue(attributeName);
            }
        } else {
            return entity.getAttributeValue(attributeName);
        }
        return null;
    }
}
