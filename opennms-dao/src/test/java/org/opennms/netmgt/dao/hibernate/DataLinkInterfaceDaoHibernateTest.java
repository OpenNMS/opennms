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

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
public class DataLinkInterfaceDaoHibernateTest implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DataLinkInterfaceDaoHibernateTest.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

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
    @JUnitTemporaryDatabase
    @Transactional
    public void testFindById() throws Exception {
        // Note: This ID is based upon the creation order in DatabasePopulator - if you change
        // the DatabasePopulator by adding additional new objects that use the onmsNxtId sequence
        // before the creation of this object then this ID may change and this test will fail.
        //
        int id = 64;
        DataLinkInterface dli = m_dataLinkInterfaceDao.findById(id);
        if (dli == null) {
            List<DataLinkInterface> dlis = m_dataLinkInterfaceDao.findAll();
            StringBuffer ids = new StringBuffer();
            for (DataLinkInterface current : dlis) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(current.getId());
            }
            fail("No DataLinkInterface record with ID " + id + " was found, the only IDs are: " + ids.toString());
        }
        assertNotNull(dli);
        assertEquals(m_databasePopulator.getNode1().getId(), dli.getNode().getId());
        assertEquals(Integer.valueOf(1), dli.getIfIndex());
        assertEquals(dli.getSource(), "linkd");
    }

    @Test
    @Transactional
    public void testFindByCriteria() throws Exception {
        OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.or(
                                     Restrictions.eq("node.id", m_databasePopulator.getNode1().getId()),
                                     Restrictions.eq("nodeParentId", m_databasePopulator.getNode1().getId())
                ));

        final List<DataLinkInterface> dlis = m_dataLinkInterfaceDao.findMatching(criteria);
        for (final DataLinkInterface iface : dlis) {
            LOG.debug("dli = {}", iface);
        }
        assertEquals(3, dlis.size());
    }

    @Test
    @Transactional
    public void testFindByStatus() throws Exception {
        OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.add(Restrictions.eq("status", StatusType.ACTIVE));

        final List<DataLinkInterface> dlis = m_dataLinkInterfaceDao.findMatching(criteria);
        for (final DataLinkInterface iface : dlis) {
            LOG.debug("dli = {}", iface);
        }
        assertEquals(3, dlis.size());
    }

    @Test
    @Transactional
    public void testFindByNodeIdAndifIndex() {
        Collection<DataLinkInterface> dlfindbynodeidifindex = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(m_databasePopulator.getNode2().getId(), 1);
        assertEquals(1, dlfindbynodeidifindex.size());
        for (DataLinkInterface link: dlfindbynodeidifindex) {
            assertEquals(m_databasePopulator.getNode2().getId(), link.getNodeId());
            assertEquals(1, link.getIfIndex().intValue());
        }

        Collection<DataLinkInterface> node1ifindex1 = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(m_databasePopulator.getNode1().getId(), 2); 
        assertEquals(1,node1ifindex1.size());
        for (DataLinkInterface link: node1ifindex1) {
            assertEquals(m_databasePopulator.getNode1().getId(), link.getNodeId());
            assertEquals(2, link.getIfIndex().intValue());
        }
        
        Collection<DataLinkInterface> node1ifindex1parent = m_dataLinkInterfaceDao.findByParentNodeIdAndIfIndex(m_databasePopulator.getNode1().getId(), 1); 
        assertEquals(3,node1ifindex1parent.size());
        for (DataLinkInterface link: node1ifindex1parent) {
            assertEquals(m_databasePopulator.getNode1().getId(), link.getNodeParentId());
            assertEquals(1, link.getParentIfIndex().intValue());
        }
        
    }

    @Test
    @Transactional // why is this necessary?
    public void testSaveDataLinkInterface() {
        // Create a new data link interface and save it.
        DataLinkInterface dli = new DataLinkInterface(m_databasePopulator.getNode2(), 2, m_databasePopulator.getNode1().getId(), 1, StatusType.UNKNOWN, new Date());
        dli.setLinkTypeId(101);
        dli.setProtocol(DiscoveryProtocol.NA);
        m_dataLinkInterfaceDao.save(dli);
        m_dataLinkInterfaceDao.flush();

        assertNotNull(m_dataLinkInterfaceDao.get(dli.getId()));

        DataLinkInterface dli2 = m_dataLinkInterfaceDao.findById(dli.getId());
        assertSame(dli, dli2);
        assertEquals(dli.getId(), dli2.getId());
        assertEquals(dli.getNode().getId(), dli2.getNode().getId());
        assertEquals(dli.getIfIndex(), dli2.getIfIndex());
        assertEquals(dli.getNodeParentId(), dli2.getNodeParentId());
        assertEquals(dli.getParentIfIndex(), dli2.getParentIfIndex());
        assertEquals(dli.getStatus(), dli2.getStatus());
        assertEquals(dli.getLinkTypeId(), dli2.getLinkTypeId());
        assertEquals(dli.getLastPollTime(), dli2.getLastPollTime());
        assertEquals(dli.getProtocol(), dli2.getProtocol());
        assertEquals(dli.getSource(), "linkd");
    }

    @Test
    @Transactional // why is this necessary?
    public void testSaveDataLinkInterface2() {
        // Create a new data link interface and save it.
        DataLinkInterface dli = new DataLinkInterface(m_databasePopulator.getNode3(), -1, m_databasePopulator.getNode1().getId(), 3, StatusType.UNKNOWN, new Date());
        dli.setLinkTypeId(101);
        dli.setSource("rest");
        m_dataLinkInterfaceDao.save(dli);
        m_dataLinkInterfaceDao.flush();

        assertNotNull(m_dataLinkInterfaceDao.get(dli.getId()));

        DataLinkInterface dli2 = m_dataLinkInterfaceDao.findById(dli.getId());
        assertSame(dli, dli2);
        assertEquals(dli.getId(), dli2.getId());
        assertEquals(dli.getNode().getId(), dli2.getNode().getId());
        assertEquals(dli.getIfIndex(), dli2.getIfIndex());
        assertEquals(dli.getNodeParentId(), dli2.getNodeParentId());
        assertEquals(dli.getParentIfIndex(), dli2.getParentIfIndex());
        assertEquals(dli.getStatus(), dli2.getStatus());
        assertEquals(dli.getLinkTypeId(), dli2.getLinkTypeId());
        assertEquals(dli.getLastPollTime(), dli2.getLastPollTime());
        assertNull(dli2.getProtocol());
        assertEquals(dli.getSource(), "rest");
    }

    @Test
    @Transactional // why is this necessary?
    public void testUpdate() {
        // Create a new data link interface and save it.
        DataLinkInterface dli = new DataLinkInterface(m_databasePopulator.getNode4(), -1, m_databasePopulator.getNode1().getId(), 3, StatusType.UNKNOWN, new Date());
        dli.setLinkTypeId(101);
        dli.setSource("updatetest");
        m_dataLinkInterfaceDao.save(dli);
        m_dataLinkInterfaceDao.flush();

        m_dataLinkInterfaceDao.setStatusForNode(m_databasePopulator.getNode4().getId(), "updatetest",StatusType.DELETED);

        assertNotNull(m_dataLinkInterfaceDao.get(dli.getId()));

        DataLinkInterface dli2 = m_dataLinkInterfaceDao.findById(dli.getId());
        assertSame(dli, dli2);
        assertEquals(dli.getId(), dli2.getId());
        assertEquals(dli.getNode().getId(), dli2.getNode().getId());
        assertEquals(dli.getIfIndex(), dli2.getIfIndex());
        assertEquals(dli.getNodeParentId(), dli2.getNodeParentId());
        assertEquals(dli.getParentIfIndex(), dli2.getParentIfIndex());
        assertEquals(StatusType.DELETED, dli2.getStatus());
        assertEquals(dli.getLinkTypeId(), dli2.getLinkTypeId());
        assertEquals(dli.getLastPollTime(), dli2.getLastPollTime());
        assertEquals(dli.getSource(), "updatetest");
    }

}
