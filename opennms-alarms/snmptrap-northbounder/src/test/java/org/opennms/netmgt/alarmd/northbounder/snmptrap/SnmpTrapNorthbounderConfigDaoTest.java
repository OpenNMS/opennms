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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.io.File;
import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.springframework.core.io.FileSystemResource;

/**
 * The Test Class for SnmpTrapNorthbounderConfigDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapNorthbounderConfigDaoTest {

    /**
     * Test configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testConfiguration() throws Exception {
        // Setup the configuration DAO
        System.setProperty("opennms.home", "src/test/resources");
        FileSystemResource resource = new FileSystemResource(new File("src/test/resources/etc/snmptrap-northbounder-config.xml"));
        SnmpTrapNorthbounderConfigDao configDao = new SnmpTrapNorthbounderConfigDao();
        configDao.setConfigResource(resource);
        configDao.afterPropertiesSet();

        // Perform a basic check, specially for the external mapping
        SnmpTrapNorthbounderConfig config = configDao.getConfig();
        Assert.assertNotNull(config);
        Assert.assertEquals(2, config.getSnmpTrapSinks().size());
        SnmpTrapSink sink1 = config.getTrapSink("localTest1");
        Assert.assertNotNull(sink1);
        Assert.assertEquals(1, sink1.getMappings().size());
        Assert.assertEquals(2, sink1.getMappings().get(0).getMappings().size());
        SnmpTrapSink sink2 = config.getTrapSink("localTest2");
        Assert.assertNotNull(sink2);
        Assert.assertEquals(1, sink2.getMappings().size());
        Assert.assertEquals(1, sink2.getMappings().get(0).getMappings().size());

        // Build a test node
        OnmsNode node = new OnmsNode("my-server");
        node.setForeignSource("Server-MacOS");
        node.setForeignId("1");
        node.setId(1);

        // Build a test SNMP interface
        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 1);
        snmpInterface.setId(1);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:01");

        // Build a test IP interface
        InetAddress address = InetAddress.getByName("10.0.1.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(1);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("p-brane");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);
        node.addIpInterface(onmsIf);

        // Build a test alarm
        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setId(10);
        onmsAlarm.setNode(node);
        onmsAlarm.setUei("uei.opennms.org/trap/myTrap1");
        onmsAlarm.setEventParms("alarmId=99(Int32,text);alarmMessage=this is just a test(string,text)");

        // Build a test northbound alarm
        NorthboundAlarm alarm = new NorthboundAlarm(onmsAlarm);
        Assert.assertEquals(node.getForeignSource(), alarm.getForeignSource());

        // Verify Filters
        Assert.assertTrue(sink1.accepts(alarm));
        Assert.assertFalse(sink2.accepts(alarm));
        SnmpTrapConfig trapConfig = sink1.createTrapConfig(alarm);
        Assert.assertNotNull(trapConfig);
        Assert.assertEquals(2, trapConfig.getParameters().size());
        Assert.assertEquals("99", trapConfig.getParameterValue(".1.2.3.4.5.6.7.8.1"));
        Assert.assertEquals("this is just a test", trapConfig.getParameterValue(".1.2.3.4.5.6.7.8.2"));
    }

}
