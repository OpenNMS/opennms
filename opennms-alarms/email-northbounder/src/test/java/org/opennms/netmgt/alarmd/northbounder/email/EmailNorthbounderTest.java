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
package org.opennms.netmgt.alarmd.northbounder.email;

import java.io.File;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.dao.jaxb.DefaultJavamailConfigurationDao;
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
 * The Class EmailNorthbounderTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EmailNorthbounderTest {

    /** The Email NBI. */
    private EmailNorthbounder nbi;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        // Setup the Email northbounder configuration DAO
        System.setProperty("opennms.home", "src/test/resources");
        EmailNorthbounderConfigDao configDao = new EmailNorthbounderConfigDao();
        configDao.setConfigResource(new FileSystemResource(new File("src/test/resources/etc/email-northbounder-config.xml")));
        configDao.afterPropertiesSet();

        // Setup JavaMail configuration DAO
        DefaultJavamailConfigurationDao javaMailDao = new DefaultJavamailConfigurationDao();
        javaMailDao.setConfigResource(new FileSystemResource(new File("src/test/resources/etc/javamail-configuration.xml")));
        javaMailDao.afterPropertiesSet();

        // Setup the trap northbounder (overriding the settings of the first sink to use the test trap receiver)
        nbi = new EmailNorthbounder(configDao, javaMailDao, "google");
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
     * Test northbounder for servers.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNorthbounderForServers() throws Exception {
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
        onmsAlarm.setLastEvent(new OnmsEvent() {{
            this.setEventParameters(Lists.newArrayList(
                    new OnmsEventParameter(this, "alarmId", "10", "Int32"),
                    new OnmsEventParameter(this, "alarmMessage", "this is a test", "string")));
        }});
        onmsAlarm.setLogMsg("Test log message");
        onmsAlarm.setDescription("Test description");
        NorthboundAlarm alarm = new NorthboundAlarm(onmsAlarm);
        Assert.assertEquals(2, alarm.getEventParametersCollection().size());

        // Verify the nortbound alarm and send it to the test receiver
        Assert.assertTrue(nbi.accepts(alarm));
        SendmailConfig sendmail = nbi.getSendmailConfig(alarm);
        Assert.assertEquals("noc@networksRus.com", sendmail.getSendmailMessage().getTo());
        Assert.assertEquals("ALARM 100 FROM NODE my-test-server@Servers-MacOS", sendmail.getSendmailMessage().getSubject());
        Assert.assertEquals("ALARM 100 FROM NODE my-test-server@Servers-MacOS: Test log message\nDescription: Test description", sendmail.getSendmailMessage().getBody());
    }

    /**
     * Test northbounder for routers.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNorthbounderForRouters() throws Exception {
        // Setup test node
        OnmsNode node = new OnmsNode();
        node.setForeignSource("Routers-Cisco");
        node.setForeignId("1");
        node.setId(1);
        node.setLabel("my-test-router");
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
        onmsIf.setIpHostName("my-test-router");
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
        onmsAlarm.setLogMsg("Test log message");
        onmsAlarm.setDescription("Test description");
        NorthboundAlarm alarm = new NorthboundAlarm(onmsAlarm);
        Assert.assertEquals(2, alarm.getEventParametersCollection().size());

        // Verify the nortbound alarm and send it to the test receiver
        Assert.assertTrue(nbi.accepts(alarm));
        SendmailConfig sendmail = nbi.getSendmailConfig(alarm);
        Assert.assertEquals("tarus@opennms.org, jeff@opennms.org", sendmail.getSendmailMessage().getTo());
        Assert.assertEquals("my-test-router : Something is wrong!", sendmail.getSendmailMessage().getSubject());
        Assert.assertEquals("Test log message - Test description", sendmail.getSendmailMessage().getBody());
    }

}
