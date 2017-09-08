/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.support.HierarchicalBeanContainer;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

public class NCSServiceContainer extends HierarchicalBeanContainer<Long, NCSServiceItem> {
    private static final Logger LOG = LoggerFactory.getLogger(NCSServiceContainer.class);

    private static final long serialVersionUID = 3245953234720320852L;
    private static final String FOREIGN_SOURCE_PROPERTY = "foreignSource";

    private final NCSComponentRepository m_dao;
    private final Set<NCSServiceItem> m_rootItems = new HashSet<>();

    public NCSServiceContainer(NCSComponentRepository dao) {
        super(NCSServiceItem.class);
        m_dao = dao;
        setBeanIdProperty("id");

        List<NCSComponent> services = m_dao.findByType("Service");
        createRootItems(services);
        addAll(m_rootItems);
        addAll(createListFromComponents(services));
    }


    private void createRootItems(List<NCSComponent> components) {
        Set<String> foreignSources = new HashSet<>();
        for(NCSComponent component : components) {
            if(!foreignSources.contains(component.getForeignSource())) {
                foreignSources.add(component.getForeignSource());
                m_rootItems.add(new NCSRootServiceItem(component));
            }
        }
    }


    private Collection<? extends NCSServiceItem> createListFromComponents(List<NCSComponent> ncsComponents) {
        Collection<NCSServiceItem> list = new ArrayList<>();
        for(NCSComponent ncsComponent : ncsComponents) {
            list.add(new NCSServiceItem(ncsComponent));
        }
        return list;
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        //Assert.isInstanceOf(Long.class, itemId);
        BeanItem<NCSServiceItem> component = getItem(itemId);
        return (Boolean) component.getItemProperty("childrenAllowed").getValue();
    }


    @Override
    public Collection<Long> getChildren(Object itemId) {
        //Assert.isInstanceOf(Long.class, itemId);
        BeanItem<NCSServiceItem> component = getItem(itemId);
        String foreignSource = (String) component.getItemProperty( FOREIGN_SOURCE_PROPERTY ).getValue();
        LOG.trace("entering method getChildren");
        List<Long> retval = new ArrayList<>();
        for (Long id : getAllItemIds()) {
            // Per talks with Paulo, only descend to the level of ServiceElement.
            // ServiceElementComponents have no representation on the current map
            // implementation.
            boolean isRoot = (Boolean) getItem(id).getItemProperty("isRoot").getValue();
            @SuppressWarnings("rawtypes")
            Property itemProperty = getItem(id).getItemProperty( FOREIGN_SOURCE_PROPERTY );
            String fSource = (String)itemProperty.getValue();
            if(!isRoot && fSource.equals(foreignSource)) {
                retval.add(id);
            }
        }
        return retval;
    }

    @Override
    public Long getParent(Object itemId) {
        //Assert.isInstanceOf(Long.class, itemId);
        BeanItem<NCSServiceItem> component = getItem(itemId);
        Object itemForeignSource = component.getItemProperty( FOREIGN_SOURCE_PROPERTY ).getValue();

        for(Long rootId : rootItemIds()) {
            BeanItem<NCSServiceItem> rootItem = getItem(rootId);

            String rootForeignSource = (String)rootItem.getItemProperty( FOREIGN_SOURCE_PROPERTY ).getValue();
            if(rootForeignSource.equals(itemForeignSource)) {
                return rootId;
            }
        }
        return null;
    }

    @Override
    public Collection<Long> rootItemIds() {
        List<Long> retval = new ArrayList<>();
        // Return all components of type "Service"
        for (NCSServiceItem item : m_rootItems) {
            retval.add(item.getId());
        }
        return retval;
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) 
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot setChildredAllowed() on NCSComponent type");
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId)
            throws UnsupportedOperationException {
        //Assert.isInstanceOf(Long.class, itemId);
        //Assert.isInstanceOf(Long.class, newParentId);
        //		Long id = (Long)itemId;
        //		Long parentId = (Long)newParentId;
        //		NCSComponent component = m_dao.load(id);
        //		Set<NCSComponent> parent = new HashSet<>();
        //		parent.add(m_dao.load(parentId));
        //		component.setParentComponents(parent);
        return true;
    }


}
