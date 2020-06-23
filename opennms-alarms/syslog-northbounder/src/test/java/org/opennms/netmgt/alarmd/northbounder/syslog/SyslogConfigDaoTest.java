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

package org.opennms.netmgt.alarmd.northbounder.syslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;

import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * The Test Class for SyslogNorthbounderConfigDao.
 */
public class SyslogConfigDaoTest {

    /** The XML. */
    String xml = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<syslog-northbounder-config>" +
            "  <enabled>true</enabled>" +
            "  <nagles-delay>10000</nagles-delay>" +
            "  <batch-size>10</batch-size>" +
            "  <queue-size>100</queue-size>" +
            "  <message-format>ALARM ID:${alarmId} NODE:${nodeLabel}</message-format>" +
            "  <date-format>yyyy-MM-dd HH:mm:ss</date-format>" +
            ">\n" +
            "  <destination>" +
            "    <destination-name>test-host</destination-name>" +
            "    <host>127.0.0.2</host>" +
            "    <port>10514</port>" +
            "    <ip-protocol>TCP</ip-protocol>" +
            "    <facility>LOCAL0</facility>" +
            "    <max-message-length>512</max-message-length>" +
            "    <send-local-name>false</send-local-name>" +
            "    <send-local-time>false</send-local-time>" +
            "    <truncate-message>true</truncate-message>" +
            ">\n" +
            "   </destination>" +
            "	<uei>uei.opennms.org/nodes/nodeDown</uei>\n" +
            "	<uei>uei.opennms.org/nodes/nodeUp</uei>\n" +
            "</syslog-northbounder-config>\n" +
            "";

    /** The XML no UEIs. */
    String xmlNoUeis = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<syslog-northbounder-config>" +
            "  <enabled>true</enabled>" +
            "  <nagles-delay>10000</nagles-delay>" +
            "  <batch-size>10</batch-size>" +
            "  <queue-size>100</queue-size>" +
            "  <message-format>ALARM ID:${alarmId} NODE:${nodeLabel}</message-format>" +
            "  <date-format>yyyy-MM-dd HH:mm:ss.SSS</date-format>" +
            ">\n" +
            "  <destination>" +
            "    <destination-name>test-host</destination-name>" +
            "    <host>127.0.0.2</host>" +
            "    <port>10514</port>" +
            "    <ip-protocol>TCP</ip-protocol>" +
            "    <facility>LOCAL0</facility>" +
            "    <max-message-length>512</max-message-length>" +
            "    <send-local-name>false</send-local-name>" +
            "    <send-local-time>false</send-local-time>" +
            "    <truncate-message>true</truncate-message>" +
            "    <first-occurrence-only>true</first-occurrence-only>" +
            ">\n" +
            "   </destination>" +
            "</syslog-northbounder-config>\n" +
            "";

    /** The XML with filters. */
    String xmlWithFilters = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<syslog-northbounder-config>" +
            "  <enabled>true</enabled>" +
            "  <nagles-delay>10000</nagles-delay>" +
            "  <batch-size>10</batch-size>" +
            "  <queue-size>100</queue-size>" +
            "  <message-format>ALARM ID:${alarmId} NODE:${nodeLabel}</message-format>" +
            "  <date-format>yyyy-MM-dd HH:mm:ss.SSSZ</date-format>" +
            ">\n" +
            "  <destination>" +
            "    <destination-name>test-host</destination-name>" +
            "    <host>127.0.0.2</host>" +
            "    <port>10514</port>" +
            "    <ip-protocol>TCP</ip-protocol>" +
            "    <facility>LOCAL0</facility>" +
            "    <max-message-length>512</max-message-length>" +
            "    <send-local-name>false</send-local-name>" +
            "    <send-local-time>false</send-local-time>" +
            "    <truncate-message>true</truncate-message>" +
            "    <first-occurrence-only>true</first-occurrence-only>" +
            "    <filter name=\"filter-1\">" +
            "      <rule>uei matches '^.*traps.*'</rule>" +
            "      <message-format>ALARM ${alarmId} ON node ${nodeLabel}@${foreignSource}</message-format>" +
            "    </filter>" +
            "    <filter name=\"filter-2\" enabled=\"false\">" +
            "      <rule>uei matches '^.*traps.*'</rule>" +
            "    </filter>" +
            ">\n" +
            "  </destination>" +
            "</syslog-northbounder-config>\n" +
            "";

    /**
     * Test load.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void testLoad() throws InterruptedException {
        Resource resource = new ByteArrayResource(xml.getBytes());

        SyslogNorthbounderConfigDao dao = new SyslogNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        SyslogNorthbounderConfig config = dao.getConfig();
        assertNotNull(config);

        assertEquals(true, config.isEnabled());
        assertEquals(new Integer("10000"), config.getNaglesDelay());
        assertEquals(new Integer(10), config.getBatchSize());
        assertEquals(new Integer(100), config.getQueueSize());
        assertEquals("ALARM ID:${alarmId} NODE:${nodeLabel}", config.getMessageFormat());
        assertEquals("yyyy-MM-dd HH:mm:ss", config.getDateFormat());

        SyslogDestination syslogDestination = config.getDestinations().get(0);
        assertNotNull(syslogDestination);
        assertEquals("test-host", syslogDestination.getName());
        assertEquals("127.0.0.2", syslogDestination.getHost());
        assertEquals(new Integer(10514), syslogDestination.getPort());
        assertEquals(SyslogDestination.SyslogProtocol.TCP, syslogDestination.getProtocol());
        assertEquals(SyslogDestination.SyslogFacility.LOCAL0, syslogDestination.getFacility());
        assertEquals(new Integer(512), syslogDestination.getMaxMessageLength());
        assertEquals(false, syslogDestination.isSendLocalName());
        assertEquals(false, syslogDestination.isSendLocalTime());
        assertEquals(true, syslogDestination.isTruncateMessage());

        assertEquals(false, syslogDestination.isFirstOccurrenceOnly());

        assertEquals("uei.opennms.org/nodes/nodeDown", config.getUeis().get(0));
        assertEquals("uei.opennms.org/nodes/nodeUp", config.getUeis().get(1));
    }

    /**
     * Test load no UEIs.
     */
    @Test
    public void testLoadNoUeis() {
        Resource resource = new ByteArrayResource(xmlNoUeis.getBytes());

        SyslogNorthbounderConfigDao dao = new SyslogNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        SyslogNorthbounderConfig config = dao.getConfig();
        assertNotNull(config);
        assertEquals(null, config.getUeis());
        assertTrue(config.getDestinations().get(0).isFirstOccurrenceOnly());
        assertEquals("yyyy-MM-dd HH:mm:ss.SSS", config.getDateFormat());
    }

    /**
     * Test filters and reload.
     *
     * @throws Exception the exception
     */
    @Test
    public void testFiltersAndReload() throws Exception {
        File configFile = new File("target/syslog-northbounder-test.xml");
        FileWriter writer = new FileWriter(configFile);
        writer.write(xmlWithFilters);
        writer.close();
        Resource resource = new FileSystemResource(configFile);

        SyslogNorthbounderConfigDao dao = new SyslogNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        assertNotNull(dao.getConfig());
        SyslogDestination dst = dao.getConfig().getSyslogDestination("test-host");
        assertNotNull(dst);
        assertEquals(2, dst.getFilters().size());
        assertEquals(true, dst.getFilters().get(0).isEnabled());
        assertEquals(false,dst.getFilters().get(1).isEnabled());
        assertEquals("yyyy-MM-dd HH:mm:ss.SSSZ", dao.getConfig().getDateFormat());
        
        writer = new FileWriter(configFile);
        writer.write(xmlNoUeis);
        writer.close();
        dao.reload();

        dst = dao.getConfig().getSyslogDestination("test-host");
        assertNotNull(dst);
        assertTrue(dst.getFilters().isEmpty());
        configFile.delete();
    }

    /**
     * Test modify configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testModifyConfiguration() throws Exception {
        File configFile = new File("target/syslog-northbounder-test.xml");
        FileWriter writer = new FileWriter(configFile);
        writer.write(xmlWithFilters);
        writer.close();
        Resource resource = new FileSystemResource(configFile);

        SyslogNorthbounderConfigDao dao = new SyslogNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        assertNotNull(dao.getConfig());
        SyslogDestination dst = dao.getConfig().getSyslogDestination("test-host");
        assertNotNull(dst);
        dst.setHost("192.168.0.1");
        dao.save();
        dao.reload();

        assertEquals("192.168.0.1", dao.getConfig().getSyslogDestination("test-host").getHost());
        configFile.delete();
    }

}
