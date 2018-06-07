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

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.springframework.core.io.FileSystemResource;

import com.google.common.collect.Lists;

/**
 * Tests the Syslog North Bound Interface with filters.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SyslogNorthBounderWithFiltersTest extends SyslogNorthBounderTest {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.northbounder.syslog.SyslogNorthBounderTest#testForwardAlarms()
     */
    @Test
    @Override
    public void testForwardAlarms() throws Exception {
        // Initialize the configuration
        File configFile = new File("target/syslog-northbounder-config.xml");
        FileUtils.copyFile(new File("src/test/resources/syslog-northbounder-config1.xml"), configFile);

        // Initialize the configuration DAO
        SyslogNorthbounderConfigDao dao = new SyslogNorthbounderConfigDao();
        dao.setConfigResource(new FileSystemResource(configFile));
        dao.afterPropertiesSet();

        // Initialize the Syslog northbound interfaces
        List<SyslogNorthbounder> nbis = new LinkedList<>();
        for (SyslogDestination syslogDestination : dao.getConfig().getDestinations()) {
            SyslogNorthbounder nbi = new SyslogNorthbounder(dao, syslogDestination.getName());
            nbi.afterPropertiesSet();
            nbis.add(nbi);
        }

        // Add a sample node to the database
        OnmsNode node = new OnmsNode();
        node.setForeignSource("TestGroup");
        node.setForeignId("1");
        node.setId(m_nodeDao.getNextNodeId());
        node.setLabel("agalue");
        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 1);
        snmpInterface.setId(1);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:01");
        Set<OnmsIpInterface> ipInterfaces = new LinkedHashSet<>();
        InetAddress address = InetAddress.getByName("10.0.1.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(1);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("agalue");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);
        ipInterfaces.add(onmsIf);
        node.setIpInterfaces(ipInterfaces);
        m_nodeDao.save(node);
        m_nodeDao.flush();

        // Create a sample Alarm
        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setId(10);
        onmsAlarm.setUei("uei.opennms.org/nodes/interfaceDown");
        onmsAlarm.setNode(node);
        onmsAlarm.setSeverityId(6);
        onmsAlarm.setIpAddr(address);
        onmsAlarm.setCounter(1);
        onmsAlarm.setLogMsg("Interface Down");
        onmsAlarm.setLastEvent(new OnmsEvent() {{
            this.setEventParameters(Lists.newArrayList(
                    new OnmsEventParameter(this, "owner", "agalue", "String")));
        }});
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date firstOccurence = simpleDateFormat.parse("2017-3-1 11:59:59");
        Date lastOccurence = simpleDateFormat.parse("2018-3-1 11:59:59");
        onmsAlarm.setFirstEventTime(firstOccurence);
        onmsAlarm.setLastEventTime(lastOccurence);
        NorthboundAlarm nbAlarm = new NorthboundAlarm(onmsAlarm);
        List<NorthboundAlarm> alarms = new LinkedList<>();
        alarms.add(nbAlarm);

        // Verify filters and send alarms to the northbound interfaces
        for (SyslogNorthbounder nbi : nbis) {
            Assert.assertTrue(nbi.accepts(nbAlarm));
            nbi.forwardAlarms(alarms);
        }

        Thread.sleep(100); // Induce a delay (based on the parent code)

        // Extract the log messages and verify the content
        BufferedReader reader = new BufferedReader(new StringReader(m_logStream.readStream()));
        List<String> messages = getMessagesFromBuffer(reader);
        Assert.assertTrue("Log messages sent: 2, Log messages received: " + messages.size(), 2 == messages.size());
        Assert.assertTrue(messages.get(0).contains("ALARM 10 FROM NODE agalue@TestGroup"));
        Assert.assertTrue(messages.get(1).contains("ALARM 10 FROM INTERFACE 10.0.1.1"));
        Assert.assertTrue(messages.get(0).contains("FIRST:2017-03-01 11:59:59"));
        Assert.assertTrue(messages.get(0).contains("LAST:2018-03-01 11:59:59"));
        reader.close();
        // Remove the temporary configuration file
        configFile.delete();
    }

    /**
     * Gets the messages from buffer.
     *
     * @param reader the reader
     * @return the messages from buffer
     * @throws Exception the exception
     */
    private List<String> getMessagesFromBuffer(BufferedReader reader) throws Exception {
        List<String> messages = new LinkedList<>();
        String line = null;
        while ((line = reader.readLine()) != null) {
            messages.add(line);
            Thread.sleep(10);
        }
        return messages;
    }

}
