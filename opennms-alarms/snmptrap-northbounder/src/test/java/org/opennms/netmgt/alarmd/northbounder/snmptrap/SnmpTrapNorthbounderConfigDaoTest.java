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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.io.FileSystemResource;

import com.google.common.collect.Lists;

/**
 * The Test Class for SnmpTrapNorthbounderConfigDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapNorthbounderConfigDaoTest {

    /** The temporary folder. */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /** The configuration DAO. */
    private SnmpTrapNorthbounderConfigDao configDao;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), tempFolder.newFolder("etc"));
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());

        // Setup the configuration DAO
        FileSystemResource resource = new FileSystemResource(new File(tempFolder.getRoot(), "etc/snmptrap-northbounder-config.xml"));
        configDao = new SnmpTrapNorthbounderConfigDao();
        configDao.setConfigResource(resource);
        configDao.afterPropertiesSet();
    }

    /**
     * Test configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testConfiguration() throws Exception {
        // Perform a basic check, specially for the external mapping
        SnmpTrapNorthbounderConfig config = configDao.getConfig();
        Assert.assertNotNull(config);
        Assert.assertEquals(2, config.getSnmpTrapSinks().size());
        SnmpTrapSink sink1 = config.getSnmpTrapSink("localTest1");
        Assert.assertNotNull(sink1);
        Assert.assertEquals(1, sink1.getMappings().size());
        Assert.assertEquals(2, sink1.getMappings().get(0).getMappings().size());
        SnmpTrapSink sink2 = config.getSnmpTrapSink("localTest2");
        Assert.assertNotNull(sink2);
        Assert.assertEquals(4, sink2.getMappings().size());
        Assert.assertEquals(1, sink2.getMappings().get(0).getMappings().size());

        NorthboundAlarm alarm = createAlarm();

        // Verify Filters
        Assert.assertTrue(sink1.accepts(alarm));
        Assert.assertTrue(sink2.accepts(alarm));
        SnmpTrapConfig trapConfig1 = sink1.createTrapConfig(alarm);
        Assert.assertNotNull(trapConfig1);
        System.out.println(trapConfig1);
        Assert.assertEquals(2, trapConfig1.getParameters().size());
        Assert.assertEquals("99", trapConfig1.getParameterValue(".1.2.3.4.5.6.7.8.1"));
        Assert.assertEquals("this is just a test", trapConfig1.getParameterValue(".1.2.3.4.5.6.7.8.2"));

        SnmpTrapConfig trapConfig2 = sink2.createTrapConfig(alarm);
        Assert.assertNotNull(trapConfig2);
        System.out.println(trapConfig2);
        Assert.assertEquals(2, trapConfig2.getParameters().size());
        Assert.assertEquals("AAA11122", trapConfig2.getParameterValue(".1.3.6.1.4.1.5.6.7.8.1000.1.1"));
        Assert.assertEquals("everything is good", trapConfig2.getParameterValue(".1.3.6.1.4.1.5.6.7.8.1000.1.2"));
    }

    /**
     * Test modify configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testModifyConfiguration() throws Exception {
        SnmpTrapSink sink = configDao.getConfig().getSnmpTrapSink("localTest2");
        Assert.assertNotNull(sink);
        Assert.assertEquals(4, sink.getImportMappings().size());
        sink.setIpAddress("192.168.0.1");
        configDao.save();
        configDao.reload();
        Assert.assertEquals("192.168.0.1", configDao.getConfig().getSnmpTrapSink("localTest2").getIpAddress());

        // Verifies SnmpTrapSink.cleanMappingGroups()
        SnmpTrapNorthbounderConfig cfg = JaxbUtils.unmarshal(SnmpTrapNorthbounderConfig.class, new File(tempFolder.getRoot(), "etc/snmptrap-northbounder-config.xml"));
        Assert.assertNotNull(cfg);
        Assert.assertTrue(cfg.getSnmpTrapSink("localTest2").getMappings().isEmpty());
    }

    /**
     * Test bean wrapper.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBeanWrapper() throws Exception {
        SnmpTrapSink sink = configDao.getConfig().getSnmpTrapSink("localTest2");
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(sink);
        Map<String,String> params = new HashMap<String,String>();
        params.put("ipAddress", "192.168.0.1");
        Assert.assertEquals("127.0.0.2", sink.getIpAddress());
        boolean modified = false;
        for (final String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                final String stringValue = params.get(key);
                final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
                modified = true;
            }
        }
        Assert.assertTrue(modified);
        Assert.assertEquals("192.168.0.1", sink.getIpAddress());
        configDao.save();
        configDao.reload();
        Assert.assertEquals("192.168.0.1", configDao.getConfig().getSnmpTrapSink("localTest2").getIpAddress());
    }

    /**
     * Test SNMP configuration with FQDN.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSnmpConfigWithFqdn() throws Exception {
        SnmpTrapNorthbounderConfig config = configDao.getConfig();
        SnmpTrapSink sink1 = config.getSnmpTrapSink("localTest1");
        NorthboundAlarm alarm = createAlarm();
        sink1.setIpAddress("www.opennms.org");
        SnmpTrapConfig trapConfig = sink1.createTrapConfig(alarm);
        Assert.assertNotNull(trapConfig);
        Assert.assertNotNull(trapConfig.getDestinationAddress());
        Assert.assertNotNull(trapConfig.getHostAddress());
    }

    /**
     * Test SNMP versions.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSnmpVersions() throws Exception {
        String config = "<snmp-trap-sink><name>test</name><ip-address>127.0.0.1</ip-address><version>v1</version></snmp-trap-sink>";
        SnmpTrapSink sink = JaxbUtils.unmarshal(SnmpTrapSink.class, config);
        Assert.assertEquals(SnmpVersion.V1, sink.getVersion());
        config = "<snmp-trap-sink><name>test</name><ip-address>127.0.0.1</ip-address><version>v2-inform</version></snmp-trap-sink>";
        sink = JaxbUtils.unmarshal(SnmpTrapSink.class, config);
        Assert.assertEquals(SnmpVersion.V2_INFORM, sink.getVersion());
    }

    /**
     * Creates the alarm.
     *
     * @return the northbound alarm
     * @throws UnknownHostException the unknown host exception
     */
    private NorthboundAlarm createAlarm() throws UnknownHostException {
        // Build a test node
        OnmsNode node = new OnmsNode();
        node.setForeignSource("Servers");
        node.setForeignId("AAA11122");
        node.setId(1);
        node.setLabel("my-server");

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
        onmsAlarm.setLogMsg("everything is good");
        onmsAlarm.setLastEvent(new OnmsEvent() {{
            this.setEventParameters(Lists.newArrayList(
                    new OnmsEventParameter(this, "alarmId", "99", "Int32"),
                    new OnmsEventParameter(this, "alarmMessage", "this is just a test", "String"),
                    new OnmsEventParameter(this, "forwardAlarmToUserSnmpTrap", "true", "String")));
        }});

        // Build a test northbound alarm
        NorthboundAlarm alarm = new NorthboundAlarm(onmsAlarm);
        Assert.assertEquals(node.getForeignSource(), alarm.getForeignSource());
        Assert.assertEquals(node.getForeignId(), alarm.getForeignId());
        return alarm;
    }

}
