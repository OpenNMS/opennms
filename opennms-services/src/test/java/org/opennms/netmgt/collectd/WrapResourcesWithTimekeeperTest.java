/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.easymock.EasyMock;
import org.jrobin.core.RrdDb;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.collection.support.ConstantTimeKeeper;
import org.opennms.netmgt.collection.support.builder.AttributeType;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.test.FileAnticipator;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Validates that we can successfully wrap collection sets with a custom time-keeper,
 * allowing us to override the timestamp of the attributes within the collection set.
 */
public class WrapResourcesWithTimekeeperTest {
    private FileAnticipator m_fileAnticipator;
    private File m_snmpDirectory;
    private OnmsIpInterface m_intf;
    private OnmsNode m_node;
    private PlatformTransactionManager m_transMgr = new MockPlatformTransactionManager();

    private IpInterfaceDao m_ifDao;
    private RrdStrategy<?, ?> m_rrdStrategy;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_rrdStrategy = new JRobinRrdStrategy();
        m_fileAnticipator = new FileAnticipator();

        // Set up node and interface
        m_intf = new OnmsIpInterface();
        m_node = new OnmsNode();
        m_node.setId(1);
        m_intf.setNode(m_node);
        m_intf.setIpAddress(InetAddressUtils.addr("1.1.1.1"));
        m_intf.setId(27);

        m_ifDao = EasyMock.createMock(IpInterfaceDao.class);
        EasyMock.expect(m_ifDao.load(m_intf.getId())).andReturn(m_intf).anyTimes();
        EasyMock.replay(m_ifDao);
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        m_fileAnticipator.deleteExpected();
        m_fileAnticipator.tearDown();
    }

    @Test
    public void testUsingCustomTimekeeper() throws Exception {
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), m_node.getId().toString());
        File jrbFile = m_fileAnticipator.expecting(nodeDir, "myCounter" + m_rrdStrategy.getDefaultFileExtension());
        m_fileAnticipator.expecting(nodeDir, "myCounter" + ".meta");

        RrdRepository repository = createRrdRepository();
        SnmpCollectionAgent agent = getCollectionAgent();
        RrdPersisterFactory persisterFactory = new RrdPersisterFactory();
        persisterFactory.setRrdStrategy(m_rrdStrategy);
        ServiceParameters params = new ServiceParameters(new HashMap<String, Object>());

        // Create a collection set
        NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
        CollectionSet collectionSet = new CollectionSetBuilder(agent)
            .withNumericAttribute(nodeResource, "mibGroup", "myCounter", 1000, AttributeType.COUNTER)
            .build();

        // Visit set with wrapped persister
        CollectionSetVisitor persister = persisterFactory.createPersister(params, repository, false, false, false);
        final ConstantTimeKeeper timeKeeper = new ConstantTimeKeeper(new Date(1234000));
        persister = CollectableService.wrapResourcesWithTimekeeper(persister, timeKeeper);
        collectionSet.visit(persister);

        // Verify the last update time matches the timekeeper time (in seconds)
        RrdDb rrdDb = new RrdDb(jrbFile);
        Assert.assertEquals(1234, rrdDb.getLastUpdateTime());
    }

    private SnmpCollectionAgent getCollectionAgent() {
        return DefaultCollectionAgent.create(m_intf.getId(), m_ifDao, m_transMgr);
    }

    private RrdRepository createRrdRepository() throws IOException {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(getSnmpRrdDirectory());
        repository.setHeartBeat(600);
        repository.setStep(300);
        repository.setRraList(Collections.singletonList("RRA:AVERAGE:0.5:1:100"));
        return repository;
    }

    private File getSnmpRrdDirectory() throws IOException {
        if (m_snmpDirectory == null) {
            m_snmpDirectory = m_fileAnticipator.tempDir("snmp");
        }
        return m_snmpDirectory;
    }
}
