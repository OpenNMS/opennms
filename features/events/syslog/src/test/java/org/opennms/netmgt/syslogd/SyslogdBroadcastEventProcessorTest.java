/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.support.InterfaceToNodeCacheEventProcessor;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-daoEvents.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SyslogdBroadcastEventProcessorTest {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogdBroadcastEventProcessorTest.class);
    private static final String s_systemId = DistPollerDao.DEFAULT_DIST_POLLER_ID;
    private static final String s_location = MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;

    @Autowired
    private InterfaceToNodeCacheEventProcessor m_processor;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        loadSyslogConfiguration("/etc/syslogd-rfc-configuration.xml");
        m_databasePopulator.populateDatabase();
    }

    private SyslogdConfigFactory loadSyslogConfiguration(final String configuration) throws IOException, MarshalException, ValidationException {
        try (InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, configuration)) {
            return new SyslogdConfigFactory(stream);
        }
    }

    @Test
    @Transactional
    public void testUpdateSyslogIPMgr() throws Exception {
        final byte[] bytes = "<34>1 2010-08-19T22:14:15.000Z localhost - - - - BOMfoo0: load test 0 on tty1\0".getBytes();
        final DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, InetAddressUtils.ONE_TWENTY_SEVEN, SyslogClient.PORT);
        final InterfaceToNodeCacheEventProcessor eventProcessor = m_processor;
        final Integer nodeId = m_databasePopulator.getNode1().getId();

        // Create a mock SyslogdConfig
        SyslogdConfigFactory config = loadSyslogConfiguration("/etc/syslogd-rfc-configuration.xml");

        // Create a new SyslogConnection and call it to create the processed event
        SyslogConnection connection = new SyslogConnection(pkt, config, s_systemId, s_location);
        SyslogProcessor processor = connection.call();
        // The node is not present so nodeID should be blank
        Long foundid = processor.getEvent().getNodeid();
        LOG.debug("found id: {}", foundid);
        assertTrue("Node ID was unexpectedly present: " + processor.getEvent().getNodeid(), foundid < 1);

        // Simulate a nodeGainedInterface event
        EventBuilder builder = new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, getClass().getSimpleName());
        builder.setNodeid(nodeId);
        builder.setInterface(InetAddressUtils.ONE_TWENTY_SEVEN);
        eventProcessor.handleNodeGainedInterface(builder.getEvent());

        connection = new SyslogConnection(pkt, config, s_systemId, s_location);
        processor = connection.call();
        // Assert that the event was associated with the node correctly
        foundid = processor.getEvent().getNodeid();
        LOG.debug("found id: {}", foundid);
        assertEquals("Node ID was not present: " + processor.getEvent().getNodeid(), Long.valueOf(nodeId.longValue()), foundid);

        // Simulate an interfaceDeleted event
        builder = new EventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, getClass().getSimpleName());
        builder.setNodeid(nodeId);
        builder.setInterface(InetAddressUtils.ONE_TWENTY_SEVEN);
        eventProcessor.handleInterfaceDeleted(builder.getEvent());

        connection = new SyslogConnection(pkt, config, s_systemId, s_location);
        processor = connection.call();
        // Assert that the event is no longer associated with the node
        assertTrue("Node ID was unexpectedly present: " + processor.getEvent().getNodeid(), processor.getEvent().getNodeid() < 1);
    }
}
