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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;

import org.apache.commons.io.IOUtils;
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
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.UeiList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManagerFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/testEventProxy.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SyslogdBroadcastEventProcessorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogdBroadcastEventProcessorTest.class);

    private static final String MATCH_PATTERN = "^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)";
    private static final int HOST_GROUP = 6;
    private static final int MESSAGE_GROUP = 8;
    private static final HideMessage HIDE_MESSAGE = new HideMessage();
    private static final String DISCARD_UEI = "DISCARD-MATCHING-MESSAGES";
    private static final UeiList UEI_LIST = new UeiList();

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        loadSyslogConfiguration("/etc/syslogd-rfc-configuration.xml");
    }

    private void loadSyslogConfiguration(final String configuration) throws IOException, MarshalException, ValidationException {
        InputStream stream = null;
        try {
            stream = ConfigurationTestUtils.getInputStreamForResource(this, configuration);
            SyslogdConfigFactory cf = new SyslogdConfigFactory(stream);
            SyslogdConfigFactory.setInstance(cf);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @Test
    @Transactional
    public void testUpdateSyslogIPMgr() throws Exception {
        final byte[] bytes = "<34>1 2010-08-19T22:14:15.000Z localhost - - - - BOMfoo0: load test 0 on tty1\0".getBytes();
        final DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, InetAddressUtils.ONE_TWENTY_SEVEN, SyslogClient.PORT);
        final BroadcastEventProcessor eventProcessor = new BroadcastEventProcessor();

        // Create a new SyslogConnection and call it to create the processed event
        SyslogConnection connection = new SyslogConnection(pkt, MATCH_PATTERN, HOST_GROUP, MESSAGE_GROUP, UEI_LIST, HIDE_MESSAGE, DISCARD_UEI);
        SyslogProcessor processor = connection.call();
        // The node is not present so nodeID should be blank
        assertTrue("Node ID was unexpectedly present: " + processor.getConvertToEvent().getEvent().getNodeid(), processor.getConvertToEvent().getEvent().getNodeid() < 1);

        // Simulate a nodeGainedInterface event
        EventBuilder builder = new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, getClass().getSimpleName());
        builder.setNodeid(5467);
        builder.setInterface(InetAddressUtils.ONE_TWENTY_SEVEN);
        eventProcessor.onEvent(builder.getEvent());

        connection = new SyslogConnection(pkt, MATCH_PATTERN, HOST_GROUP, MESSAGE_GROUP, UEI_LIST, HIDE_MESSAGE, DISCARD_UEI);
        processor = connection.call();
        // Assert that the event was associated with the node correctly
        assertEquals("Node ID was not present: " + processor.getConvertToEvent().getEvent().getNodeid(), Long.valueOf(5467), processor.getConvertToEvent().getEvent().getNodeid());

        // Simulate an interfaceDeleted event
        builder = new EventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, getClass().getSimpleName());
        builder.setNodeid(5467);
        builder.setInterface(InetAddressUtils.ONE_TWENTY_SEVEN);
        eventProcessor.onEvent(builder.getEvent());

        connection = new SyslogConnection(pkt, MATCH_PATTERN, HOST_GROUP, MESSAGE_GROUP, UEI_LIST, HIDE_MESSAGE, DISCARD_UEI);
        processor = connection.call();
        // Assert that the event is no longer associated with the node
        assertTrue("Node ID was unexpectedly present: " + processor.getConvertToEvent().getEvent().getNodeid(), processor.getConvertToEvent().getEvent().getNodeid() < 1);
    }
}
