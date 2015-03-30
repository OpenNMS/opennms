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

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;


import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.StpNodeDao;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsStpNode.BridgeBaseType;
import org.opennms.netmgt.model.OnmsStpNode.StpProtocolSpecification;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsStpNode;

import org.opennms.test.JUnitConfigurationEnvironment;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class OnmsStpNodeDaoHibernateTest implements InitializingBean {
    @Autowired
    private NodeDao m_nodeDao;
    
	@Autowired
    private StpNodeDao m_stpNodeDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Before
	public void setUp() {
	    for (final OnmsNode node : m_nodeDao.findAll()) {
	        m_nodeDao.delete(node);
	    }
	    m_nodeDao.flush();
		m_databasePopulator.populateDatabase();
	}


    @Test
    public void testSaveOnmsStpNode() {
        // Create a new data link interface and save it.
        OnmsStpNode stpnode = new OnmsStpNode(m_databasePopulator.getNode2(),1);

        stpnode.setBaseBridgeAddress("0a0112345678");
        stpnode.setBaseNumPorts(20);
        stpnode.setBaseType(BridgeBaseType.TRANSPARENT_ONLY);
        stpnode.setBaseVlanName("default");
        stpnode.setLastPollTime(new Date());
        stpnode.setStatus(StatusType.ACTIVE);
        stpnode.setStpDesignatedRoot("80000a0112345670");
        stpnode.setStpPriority(28532);
        stpnode.setStpProtocolSpecification(StpProtocolSpecification.IEEE8021D);
        stpnode.setStpRootCost(128);
        stpnode.setStpRootPort(15);
        m_stpNodeDao.save(stpnode);

        assertEquals(1, m_stpNodeDao.countAll());
        OnmsStpNode dbStpNode = m_stpNodeDao.findByNodeAndVlan(m_databasePopulator.getNode2().getId(), 1);
        assertEquals(stpnode.getBaseBridgeAddress(), dbStpNode.getBaseBridgeAddress());
        assertEquals("transparent-only",BridgeBaseType.getBridgeBaseTypeString(dbStpNode.getBaseType().getIntCode()));
        assertEquals("ieee8021d",StpProtocolSpecification.getStpProtocolSpecificationString(dbStpNode.getStpProtocolSpecification().getIntCode()));
    }

}
