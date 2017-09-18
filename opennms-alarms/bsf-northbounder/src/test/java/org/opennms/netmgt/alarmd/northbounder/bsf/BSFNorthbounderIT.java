/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd.northbounder.bsf;

import java.io.File;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
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
 * The test Class for BSFNorthbounder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class BSFNorthbounderIT {

    /** The BSF NBI. */
    private BSFNorthbounder nbi;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        // Setup the BSF northbounder configuration DAO
        System.setProperty("opennms.home", "src/test/resources");
        BSFNorthbounderConfigDao configDao = new BSFNorthbounderConfigDao();
        configDao.setConfigResource(new FileSystemResource(new File("src/test/resources/etc/bsf-northbounder-config.xml")));
        configDao.afterPropertiesSet();

        // Setup the northbounder
        nbi = new BSFNorthbounder(configDao, "JUnit");
        nbi.afterPropertiesSet();
    }

    /**
     * Shutdown the test.
     *
     * @throws Exception the exception
     */
    @After
    public void shutdown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test northbounder.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNorthbounder() throws Exception {
        // Setup test node
        OnmsNode node = new OnmsNode();
        node.setForeignSource("Servers-MacOS");
        node.setForeignId("1");
        node.setId(1);
        node.setLabel("my-test-server");
        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 1);
        snmpInterface.setId(1);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:01");
        InetAddress address = InetAddress.getByName("10.0.1.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(1);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("my-test-server");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);
        node.getIpInterfaces().add(onmsIf);

        // Setup test alarm
        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setId(100);
        onmsAlarm.setNode(node);
        onmsAlarm.setIpAddr(address);
        onmsAlarm.setUei("uei.opennms.org/trap/myTrap1");
        OnmsEvent event = new OnmsEvent();
        event.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event, "alarmId", "10", "Int32"),
                new OnmsEventParameter(event, "alarmMessage", "this is a test", "string")));
        onmsAlarm.setLastEvent(event);
        onmsAlarm.setLogMsg("Test log message");
        onmsAlarm.setDescription("Test description");
        NorthboundAlarm alarm = new NorthboundAlarm(onmsAlarm);
        Assert.assertEquals(2, alarm.getEventParametersCollection().size());

        Assert.assertTrue(nbi.accepts(alarm));
        nbi.forwardAlarms(Lists.newArrayList(alarm));
        nbi.stop();
    }

}
