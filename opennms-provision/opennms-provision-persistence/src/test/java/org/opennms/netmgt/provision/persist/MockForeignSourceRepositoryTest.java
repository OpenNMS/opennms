/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.io.ClassPathResource;

public class MockForeignSourceRepositoryTest extends ForeignSourceRepositoryTestCase {
    private String m_defaultForeignSourceName;

    private ForeignSourceRepository m_foreignSourceRepository;

    @Before
    public void setUp() {
        m_foreignSourceRepository = new MockForeignSourceRepository();
        m_defaultForeignSourceName = "imported:";
        m_foreignSourceRepository.clear();
        m_foreignSourceRepository.flush();
    }
    
    private Requisition createRequisition() throws Exception {
        return m_foreignSourceRepository.importResourceRequisition(new ClassPathResource("/requisition-test.xml"));
    }

    private ForeignSource createForeignSource(String foreignSource) throws Exception {
        ForeignSource fs = new ForeignSource(foreignSource);
        fs.addDetector(new PluginConfig("HTTP", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));
        fs.addPolicy(new PluginConfig("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        m_foreignSourceRepository.save(fs);
        m_foreignSourceRepository.flush();
        return fs;
    }

    @Test
    public void testRequisition() throws Exception {
        createRequisition();
        Requisition r = m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName);
        TestVisitor v = new TestVisitor();
        r.visit(v);
        assertEquals("number of nodes visited", 2, v.getNodeReqs().size());
        assertEquals("node name matches", "apknd", v.getNodeReqs().get(0).getNodeLabel());
    }

    @Test
    public void testForeignSource() throws Exception {
        createRequisition();
        ForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        List<ForeignSource> foreignSources = new ArrayList<ForeignSource>(m_foreignSourceRepository.getForeignSources());
        assertEquals("number of foreign sources", 1, foreignSources.size());
        assertEquals("getAll() foreign source name matches", m_defaultForeignSourceName, foreignSources.get(0).getName());
        assertEquals("get() returns the foreign source", foreignSource, m_foreignSourceRepository.getForeignSource(m_defaultForeignSourceName));
    }

    /**
     * This test ensures that the Spring Bean accessor classes work properly
     * since our REST implementation uses bean access to update the values.
     */
    @Test
    public void testBeanWrapperAccess() throws Exception {
        createRequisition();
        Requisition r = m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName);
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(r);
        assertEquals("AC", wrapper.getPropertyValue("node[0].category[0].name"));
        assertEquals("UK", wrapper.getPropertyValue("node[0].category[1].name"));
        assertEquals("low", wrapper.getPropertyValue("node[0].category[2].name"));
        
        try {
            wrapper.getPropertyValue("node[1].category[0].name");
            fail("Did not catch expected InvalidPropertyException exception");
        } catch (InvalidPropertyException e) {
            // Expected failure
        }
        
        assertEquals(0, ((RequisitionCategory[])wrapper.getPropertyValue("node[1].category")).length);
        
        wrapper.setPropertyValue("node[1].categories[0]", new RequisitionCategory("Hello world"));
        wrapper.setPropertyValue("node[1].categories[1]", new RequisitionCategory("Hello again"));
        
        assertEquals(2, ((RequisitionCategory[])wrapper.getPropertyValue("node[1].category")).length);
    }

    @Test
    public void testGetRequisition() throws Exception {
        Requisition requisition = createRequisition();
        ForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        assertRequisitionsMatch("foreign sources must match", m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName), m_foreignSourceRepository.getRequisition(foreignSource));
        assertRequisitionsMatch("foreign source is expected one", requisition, m_foreignSourceRepository.getRequisition(foreignSource));
    }

}
