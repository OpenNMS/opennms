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
import java.util.Collections;

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

import org.junit.Assert;

import com.google.common.collect.Lists;

/**
 * The Test Class for SnmpTrapNorthbounder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapNorthbounderTest extends AbstractTrapReceiverTest {

    /**
     * Tests the trap northbounder.
     *
     * @throws Exception the exception
     */
    // FIXME Verify the content of the trap sent.
    @Test
    public void testNorthbounder() throws Exception {
        // Setup the configuration DAO
        FileSystemResource resource = new FileSystemResource(new File("src/test/resources/etc/snmptrap-northbounder-config.xml"));
        SnmpTrapNorthbounderConfigDao configDao = new SnmpTrapNorthbounderConfigDao();
        configDao.setConfigResource(resource);
        configDao.afterPropertiesSet();

        // Setup the trap northbounder (overriding the settings of the first sink to use the test trap receiver)
        SnmpTrapSink sink = configDao.getConfig().getSnmpTrapSink("localTest1");
        sink.setIpAddress(TRAP_DESTINATION.getHostAddress());
        sink.setPort(TRAP_PORT);
        SnmpTrapNorthbounder nbi = new SnmpTrapNorthbounder(configDao, sink.getName());
        nbi.afterPropertiesSet();

        // Setup test node
        OnmsNode node = new OnmsNode();
        node.setForeignSource("Server-MacOS");
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
        onmsAlarm.setLastEvent(new OnmsEvent() {{
            this.setEventParameters(Lists.newArrayList(
                    new OnmsEventParameter(this, "alarmId", "10", "Int32"),
                    new OnmsEventParameter(this, "alarmMessage", "this is a test", "string")));
        }});
        NorthboundAlarm alarm = new NorthboundAlarm(onmsAlarm);
        Assert.assertEquals(2, alarm.getEventParametersCollection().size());

        // Verify the nortbound alarm and send it to the test receiver
        Assert.assertTrue(nbi.accepts(alarm));
        nbi.forwardAlarms(Collections.singletonList(alarm));
        Thread.sleep(5000); // Introduce a delay to make sure the trap was sent and received.
        Assert.assertEquals(1, getTrapsReceivedCount());
    }

}
