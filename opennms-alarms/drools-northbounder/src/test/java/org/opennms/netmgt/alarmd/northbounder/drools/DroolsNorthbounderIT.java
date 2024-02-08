/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.alarmd.northbounder.drools;

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
 * The test Class for DroolsNorthbounder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DroolsNorthbounderIT {

    /** The Drools NBI. */
    private DroolsNorthbounder nbi;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        // Setup the Drools northbounder configuration DAO
        System.setProperty("opennms.home", "src/test/resources");
        DroolsNorthbounderConfigDao configDao = new DroolsNorthbounderConfigDao();
        configDao.setConfigResource(new FileSystemResource(new File("src/test/resources/etc/drools-northbounder-config.xml")));
        configDao.afterPropertiesSet();

        // Setup the northbounder
        nbi = new DroolsNorthbounder(null, configDao, null, "JUnit");
        nbi.afterPropertiesSet();
    }

    /**
     * Shutdown the test.
     *
     * @throws Exception the exception
     */
    @After
    public void shutdown() throws Exception {
        MockLogAppender.assertNoErrorOrGreater();
    }

    /**
     * Test northbounder.
     *
     * @throws Exception the exception
     */
    @Test(timeout=5*60*1000)
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
