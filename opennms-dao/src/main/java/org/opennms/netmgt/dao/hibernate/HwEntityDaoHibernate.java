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
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsHwEntityAlias;
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
    public OnmsHwEntity findRootEntityByNodeId(Integer nodeId) {
        List<OnmsHwEntity> entityList = getHibernateTemplate().execute(session -> (List<OnmsHwEntity>)
                session.createSQLQuery("select * FROM hwEntity where nodeid = " + nodeId)
                        .setResultTransformer(
                                new ResultTransformer() {
                                    @Override
                                    public Object transformTuple(Object[] tuple, String[] strings) {
                                        OnmsHwEntity onmsHwEntity = new OnmsHwEntity();
                                        onmsHwEntity.setId((Integer) tuple[0]);
                                        onmsHwEntity.setParentId((Integer) tuple[1]);
                                        onmsHwEntity.setNodeId((Integer) tuple[2]);
                                        onmsHwEntity.setEntPhysicalIndex((Integer) tuple[3]);
                                        onmsHwEntity.setEntPhysicalParentRelPos((Integer) tuple[4]);
                                        onmsHwEntity.setEntPhysicalName((String) tuple[5]);
                                        onmsHwEntity.setEntPhysicalDescr((String) tuple[6]);
                                        onmsHwEntity.setEntPhysicalAlias((String) tuple[7]);
                                        onmsHwEntity.setEntPhysicalVendorType((String) tuple[8]);
                                        onmsHwEntity.setEntPhysicalClass((String) tuple[9]);
                                        onmsHwEntity.setEntPhysicalMfgName((String) tuple[10]);
                                        onmsHwEntity.setEntPhysicalModelName((String) tuple[11]);
                                        onmsHwEntity.setEntPhysicalHardwareRev((String) tuple[12]);
                                        onmsHwEntity.setEntPhysicalFirmwareRev((String) tuple[13]);
                                        onmsHwEntity.setEntPhysicalSoftwareRev((String) tuple[14]);
                                        onmsHwEntity.setEntPhysicalSerialNum((String) tuple[15]);
                                        onmsHwEntity.setEntPhysicalAssetID((String) tuple[16]);
                                        onmsHwEntity.setEntPhysicalIsFRU((Boolean) tuple[17]);
                                        onmsHwEntity.setEntPhysicalMfgDate((Date) tuple[18]);
                                        onmsHwEntity.setEntPhysicalUris((String) tuple[19]);
                                        return onmsHwEntity;
                                    }

                                    @Override
                                    public List transformList(List list) {
                                        return list;
                                    }
                                }
                        ).list());

        Optional<OnmsHwEntity> optionalHwEntity =  entityList.stream().filter(hwEntity -> hwEntity.getParentId() == null).findFirst();
        if(optionalHwEntity.isPresent()) {
            OnmsHwEntity parent = optionalHwEntity.get();
            List<OnmsHwEntityAlias> hwEntityAliases = findHwEntityAlias(parent);
            parent.addHwEntAliasList(hwEntityAliases);
            findChildren(parent, entityList);
            return parent;
        }
        return null;
    }

    private void findChildren(OnmsHwEntity parent, List<OnmsHwEntity> hwEntityList) {

        Set<OnmsHwEntity> children = hwEntityList.stream().filter(hwEntity ->
                        hwEntity.getParentId() != null && hwEntity.getParentId().equals(parent.getId()))
                .collect(Collectors.toSet());
        children.forEach(hwEntity ->  {
            List<OnmsHwEntityAlias> onmsHwEntityAliases = findHwEntityAlias(hwEntity);
            hwEntity.addHwEntAliasList(onmsHwEntityAliases);
            findChildren(hwEntity, hwEntityList);
            parent.addChildEntity(hwEntity);
        });
    }

    private List<OnmsHwEntityAlias> findHwEntityAlias(OnmsHwEntity parent) {
         List<OnmsHwEntityAlias> hwEntityAliases = getHibernateTemplate().execute(session ->
                 (List<OnmsHwEntityAlias>) session.createSQLQuery(
                 "SELECT * FROM hwEntityAlias WHERE hwEntityId = " + parent.getId())
                 .setResultTransformer(new ResultTransformer() {
             @Override
             public Object transformTuple(Object[] tuple, String[] strings) {
                 OnmsHwEntityAlias onmsHwEntityAlias = new OnmsHwEntityAlias();
                 onmsHwEntityAlias.setId((Integer) tuple[0]);
                 onmsHwEntityAlias.setHwEntityId((Integer) tuple[1]);
                 onmsHwEntityAlias.setIndex((Integer) tuple[2]);
                 onmsHwEntityAlias.setOid((String) tuple[3]);
                 return onmsHwEntityAlias;
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
