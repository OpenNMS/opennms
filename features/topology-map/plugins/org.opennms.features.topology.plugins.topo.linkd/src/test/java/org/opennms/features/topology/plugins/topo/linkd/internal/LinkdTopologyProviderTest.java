/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.linkd.internal;



import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;
import org.opennms.features.topology.plugins.topo.linkd.internal.operations.RefreshOperation;
import org.opennms.features.topology.plugins.topo.linkd.internal.operations.SaveOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-mock.xml"
        })

public class LinkdTopologyProviderTest {
    @Autowired
    private RefreshOperation m_refreshOperation;
    
    @Autowired
    private SaveOperation m_saveOperation;
    
    @Autowired
    private OperationContext m_operationContext;

    @Autowired
    private LinkdTopologyProvider m_topologyProvider;
    
    @Autowired
     private EasyMockDataPopulator m_databasePopulator;
    
    @Before
    public void setUp() {
        m_databasePopulator.populateDatabase();
        m_databasePopulator.setUpMock();
    }

    @After
    public void tearDown() {
        m_databasePopulator.tearDown();
    }
        @Test 
        public void testGetIcon() {
            Assert.assertTrue("snmp:1.3.6.1.4.1.5813.1.25".equals(m_topologyProvider.getIconName(m_databasePopulator.getNode1())));
            Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(m_topologyProvider.getIconName(m_databasePopulator.getNode2())));
            Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(m_topologyProvider.getIconName(m_databasePopulator.getNode3())));
            Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(m_topologyProvider.getIconName(m_databasePopulator.getNode4())));
            Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(m_topologyProvider.getIconName(m_databasePopulator.getNode5())));
            Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(m_topologyProvider.getIconName(m_databasePopulator.getNode6())));
            Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(m_topologyProvider.getIconName(m_databasePopulator.getNode7())));
            Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(m_topologyProvider.getIconName(m_databasePopulator.getNode8())));

        }
    
	@Test
	public void testLoad() {
		m_topologyProvider.load(null);
		m_databasePopulator.check(m_topologyProvider);
	}

	@Test
	public void testSave() {
	    m_topologyProvider.save("target/test-map.xml");	    
            m_databasePopulator.check(m_topologyProvider);
	}
	
	@Test
	public void testOperationRefresh() {
	    m_refreshOperation.execute(null, m_operationContext);
            m_databasePopulator.check(m_topologyProvider);
	}

	@Test
	public void testOperationSave() {
            List<Object> targets = new ArrayList<Object>(1);
            targets.add("target/test-graph.xml");
            m_saveOperation.execute(targets, m_operationContext);	            
	}	
}
