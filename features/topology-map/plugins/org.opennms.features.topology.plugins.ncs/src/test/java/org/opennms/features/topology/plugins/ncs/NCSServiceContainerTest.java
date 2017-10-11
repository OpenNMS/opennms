/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.util.BeanItem;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

public class NCSServiceContainerTest {
    
    private class TestRepository implements NCSComponentRepository{
        
        List<NCSComponent> m_componentList = new ArrayList<>();
        public TestRepository() {
            m_componentList.add(createNCSComponent(1537, "MplsLSP", "ServiceElementComponent", "space_TransportActivate"));
            m_componentList.add(createNCSComponent(1538, "RpdLSP", "ServiceElementComponent", "space_TransportActivate"));
            m_componentList.add(createNCSComponent(1539, "", "ServiceElement", "space_TransportActivate"));
            m_componentList.add(createNCSComponent(1540, "Delhi_Bagmane", "Service", "space_TransportActivate"));
            m_componentList.add(createNCSComponent(1939, "Synce_Service", "Service", "space_Timing"));
            m_componentList.add(createNCSComponent(2007, "VpnIf", "ServiceElementComponent", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2008, "VpnPW", "ServiceElementComponent", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2009, "", "ServiceElement", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2010, "VpnIf", "ServiceElementComponent", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2011, "VpnPW", "ServiceElementComponent", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2012, "", "ServiceElement", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2013, "P2P_Single_Vlan", "Service", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2264, "RDI", "ServiceElementComponent", "space_OAM"));
            m_componentList.add(createNCSComponent(2265, "RMEP", "ServiceElementComponent", "space_OAM"));
            m_componentList.add(createNCSComponent(2266, "ERROR", "ServiceElementComponent", "space_OAM"));
            m_componentList.add(createNCSComponent(2267, "", "ServiceElement", "space_OAM"));
            m_componentList.add(createNCSComponent(2268, "RDI", "ServiceElementComponent", "space_OAM"));
            m_componentList.add(createNCSComponent(2269, "RMEP", "ServiceElementComponent", "space_OAM"));
            m_componentList.add(createNCSComponent(2270, "ERROR", "ServiceElementComponent", "space_OAM"));
            m_componentList.add(createNCSComponent(2271, "", "ServiceElement", "space_OAM"));
            m_componentList.add(createNCSComponent(2272, "Test_OAM", "Service", "space_OAM"));
            m_componentList.add(createNCSComponent(2273, "VpnPW", "ServiceElementComponent", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2274, "", "ServiceElement", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2275, "VpnPW", "ServiceElementComponent", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2276, "", "ServiceElement", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2277, "VpnPW", "ServiceElementComponent", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2278, "", "ServiceElement", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2279, "VpnPW", "ServiceElementComponent", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2280, "", "ServiceElement", "space_ServiceProvisioning"));
            m_componentList.add(createNCSComponent(2281, "VPLS_Multi_Point", "Service", "space_ServiceProvisioning"));
            
        }
        
        private NCSComponent createNCSComponent(long id, String name, String type , String foreignSource) {
            NCSComponent component = new NCSComponent();
            component.setId(id);
            component.setName(name);
            component.setType(type);
            component.setForeignSource(foreignSource);
            return component;
        }

        @Override
        public void lock() {}

        @Override
        public void initialize(Object obj) {}

        @Override
        public void flush() {}

        @Override
        public void clear() {}

        @Override
        public int countAll() {
            return 0;
        }

        @Override
        public void delete(NCSComponent component) {}

        @Override
        public List<NCSComponent> findAll() {
            return null;
        }

        @Override
        public List<NCSComponent> findMatching(OnmsCriteria criteria) { return null; }

        @Override
        public int countMatching(OnmsCriteria onmsCrit) { return 0; }

        @Override
        public NCSComponent get(Long id) {
            return null;
        }

        @Override
        public NCSComponent load(Long id) {
            return null;
        }

        @Override
        public Long save(NCSComponent component) { return null; }

        @Override
        public void saveOrUpdate(NCSComponent component) {}

        @Override
        public void update(NCSComponent component) {}

        @Override
        public List<NCSComponent> findByType(String type) {
            List<NCSComponent> retVal = new ArrayList<>();
            for(NCSComponent component : m_componentList) {
                if(component.getType().equals(type)) {
                    retVal.add(component);
                }
            }
            return retVal;
        }

        @Override
        public NCSComponent findByTypeAndForeignIdentity(String type, String foreignSource, String foreignId) {
            return null;
        }

        @Override
        public List<NCSComponent> findComponentsThatDependOn( NCSComponent component) {
            return null;
        }

        @Override
        public List<NCSComponent> findComponentsWithAttribute(String attrKey, String attrValue) {
            return null;
        }

        @Override
        public List<NCSComponent> findComponentsByNodeId(int nodeid) {
            return null;
        }
        
    }

    private NCSServiceContainer m_container;
    
    @Before
    public void setUp() {
        m_container = new NCSServiceContainer(new TestRepository());
    }
    
    @Test
    public void testRootIds() {
        Collection<Long> rootIds = m_container.rootItemIds();
        assertEquals(4, rootIds.size());
        BeanItem<NCSServiceItem> item = m_container.getItem(rootIds.iterator().next());
        System.out.println(item.getItemProperty("id").getValue());
    }
    
    @Test
    public void testGetChildrenForItemId() {
        Collection<Long> rootItemIds = m_container.rootItemIds();
        Long parentId = rootItemIds.iterator().next();
        Collection<Long> children = m_container.getChildren(parentId);
        for(Long id : children) {
            assertFalse(parentId == id);
        }
    }
    
    @Test
    public void testAreChildrenAllowed() {
        Collection<Long> allItemIds = m_container.getItemIds();
        
        for(Long itemId : allItemIds) {
            BeanItem<NCSServiceItem> item = m_container.getItem(itemId);
            boolean isRoot = (Boolean) item.getItemProperty("isRoot").getValue();
            if(isRoot) {
                assertTrue(m_container.areChildrenAllowed(itemId));
            } else {
                assertFalse(m_container.areChildrenAllowed(itemId));
            }
        }
    }
    
}
