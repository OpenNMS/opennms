/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class
})
public class MockForeignSourceRepositoryTest {
    private String m_defaultForeignSourceName;

    private ForeignSourceRepository m_repository;

    @Before
    public void setUp() {
        m_repository = new MockForeignSourceRepository();
        m_defaultForeignSourceName = new ModelImport().getForeignSource();
    }
    
    private Requisition createRequisition() throws Exception {
        return m_repository.importResourceRequisition(new ClassPathResource("/requisition-test.xml"));
    }

    private ForeignSource createForeignSource(String foreignSource) throws Exception {
        ForeignSource fs = new ForeignSource(foreignSource);
        fs.addDetector(new PluginConfig("HTTP", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));
        fs.addPolicy(new PluginConfig("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        m_repository.save(fs);
        return fs;
    }

    @Test
    public void testRequisition() throws Exception {
        createRequisition();
        Requisition r = m_repository.getRequisition(m_defaultForeignSourceName);
        TestVisitor v = new TestVisitor();
        r.visit(v);
        assertEquals("number of nodes visited", 1, v.getNodeReqs().size());
        assertEquals("node name matches", "apknd", v.getNodeReqs().get(0).getNodeLabel());
    }

    @Test
    public void testForeignSource() throws Exception {
        createRequisition();
        ForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        List<ForeignSource> foreignSources = new ArrayList<ForeignSource>(m_repository.getForeignSources());
        assertEquals("number of foreign sources", 1, foreignSources.size());
        assertEquals("getAll() foreign source name matches", m_defaultForeignSourceName, foreignSources.get(0).getName());
        assertEquals("get() returns the foreign source", foreignSource, m_repository.getForeignSource(m_defaultForeignSourceName));
    }

    @Test
    public void testGetRequisition() throws Exception {
        Requisition requisition = createRequisition();
        ForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        assertEquals("foreign sources match", m_repository.getRequisition(m_defaultForeignSourceName), m_repository.getRequisition(foreignSource));
        assertEquals("foreign source is expected one", requisition, m_repository.getRequisition(foreignSource));
    }

}
