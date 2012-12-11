/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.accesspointmonitor;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.accesspointmonitor.AccessPointMonitord;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.netmgt.filter.JdbcFilterDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.events.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.AccessPointStatus;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfigFactory;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml", "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml", "classpath:/META-INF/opennms/applicationContext-daemon.xml", "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml", "classpath:META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:META-INF/opennms/applicationContext-accesspointmonitord.xml", "classpath:META-INF/opennms/smallEventConfDao.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase()
public class AccessPointMonitordTest implements InitializingBean, TemporaryDatabaseAware<TemporaryDatabase> {

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private AccessPointDao m_accessPointDao;

    @Autowired
    AccessPointMonitord m_apm;

    AnnotationBasedEventListenerAdapter m_adapter;
    AccessPointMonitorConfigFactory m_apmdConfigFactory;

    private MockEventIpcManager m_eventMgr;
    private EventAnticipator m_anticipator;
    private TemporaryDatabase m_database;

    private final static String AP1_MAC = "00:01:02:03:04:05";
    private final static String AP2_MAC = "07:08:09:0A:0B:0C";
    private final static String AP3_MAC = "F0:05:BA:11:00:FF";
    private final static int PACKAGE_SCAN_INTERVAL = 1000;

    public void setTemporaryDatabase(TemporaryDatabase database) {
        m_database = database;
    }

    @Override
    public void afterPropertiesSet() {
        assertNotNull(m_transactionManager);
        assertNotNull(m_nodeDao);
        assertNotNull(m_ipInterfaceDao);
        assertNotNull(m_serviceTypeDao);
        assertNotNull(m_apm);
    }

    @Before
    public void setUp() throws Exception {
        // Initialise the JdbcFilterDao so that it will connect to the correct
        // database
        DatabaseSchemaConfigFactory.init();
        JdbcFilterDao jdbcFilterDao = new JdbcFilterDao();
        jdbcFilterDao.setDataSource(m_database);
        jdbcFilterDao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        jdbcFilterDao.afterPropertiesSet();
        FilterDaoFactory.setInstance(jdbcFilterDao);

        // Create our event manager and anticipator
        m_anticipator = new EventAnticipator();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.setSynchronous(true);

        // Ensure our annotations are called
        m_adapter = new AnnotationBasedEventListenerAdapter(m_apm, m_eventMgr);
    }

    @After
    public void tearDown() throws Exception {
        if (m_apm.getStatus() == AccessPointMonitord.RUNNING) {
            m_apm.stop();
            LogUtils.debugf(this, "AccessPointMonitord stopped");
        }
    }

    private void initApmdWithConfig(String config) throws Exception {
        m_apm.setEventManager(m_eventMgr);

        // Convert the string to an input stream
        InputStream is = new ByteArrayInputStream(config.getBytes("UTF-8"));
        m_apmdConfigFactory = new AccessPointMonitorConfigFactory(0, is);
        m_apm.setPollerConfig(m_apmdConfigFactory.getConfig());

        // Initialise, but do not start
        m_apm.init();
    }

    private void updateConfigAndReloadDaemon(String config, Boolean anticipateEvents) throws Exception {
        // Build an input stream from the string
        InputStream is = new ByteArrayInputStream(config.getBytes("UTF-8"));

        // Get the latest time-stamp on the configuration file so that the
        // factory will use our string and not the file
        File cfgFile = ConfigFileConstants.getConfigFileByName(AccessPointMonitorConfigFactory.getDefaultConfigFilename());

        // Update the configuration factory
        AccessPointMonitorConfigFactory.setInstance(new AccessPointMonitorConfigFactory(cfgFile.lastModified(), is));

        // Anticipate the reload successful event
        if (anticipateEvents) {
            EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, m_apm.getName());
            bldr.setParam(EventConstants.PARM_DAEMON_NAME, "AccessPointMonitor");
            m_anticipator.anticipateEvent(bldr.getEvent());
        }

        // Anticipate the reload event
        EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");
        bldr.setParam(EventConstants.PARM_DAEMON_NAME, "AccessPointMonitor");
        if (anticipateEvents) {
            m_anticipator.anticipateEvent(bldr.getEvent());
        }

        // Send the reload event
        m_eventMgr.send(bldr.getEvent());
    }

    private void addNewAccessPoint(String name, String mac, String pkg) {
        NetworkBuilder nb = new NetworkBuilder();

        nb.addNode(name).setForeignSource("apmd").setForeignId(name);
        nb.addInterface("169.254.0.1");
        m_nodeDao.save(nb.getCurrentNode());

        final OnmsAccessPoint ap1 = new OnmsAccessPoint(mac, nb.getCurrentNode().getId(), pkg);
        ap1.setStatus(AccessPointStatus.UNKNOWN);
        m_accessPointDao.save(ap1);

        m_nodeDao.flush();
        m_accessPointDao.flush();
    }

    @Test
    public void testDynamicPackages() throws Exception {
        // A package name that matches the mask
        addNewAccessPoint("ap1", AP1_MAC, "dynamic-pkg-1");

        initApmdWithConfig(getDynamicPackageConfig());
        m_apm.start();
        sleep(PACKAGE_SCAN_INTERVAL * 2);
        assertEquals(1, m_apm.getActivePackageNames().size());

        // Another package name that matches the mask
        addNewAccessPoint("ap2", AP2_MAC, "dynamic-pkg-2");

        // A package name that does not match the mask
        addNewAccessPoint("ap3", AP3_MAC, "default");

        sleep(PACKAGE_SCAN_INTERVAL * 2);
        assertEquals(2, m_apm.getActivePackageNames().size());

        // Change the package name for AP1 - the package should be unscheduled
        OnmsAccessPoint ap1 = m_accessPointDao.findByPhysAddr(AP1_MAC);
        ap1.setPollingPackage("default");
        m_accessPointDao.update(ap1);
        m_accessPointDao.flush();

        List<String> packageNames = m_accessPointDao.findDistinctPackagesLike("dynamic-pkg-%");
        assertEquals(1, packageNames.size());

        sleep(PACKAGE_SCAN_INTERVAL * 2);
        assertEquals(1, m_apm.getActivePackageNames().size());

        // Change the package name for AP1 - the package should be unscheduled
        ap1.setPollingPackage("dynamic-pkg-2");
        m_accessPointDao.update(ap1);
        m_accessPointDao.flush();

        sleep(PACKAGE_SCAN_INTERVAL * 2);
        assertEquals(1, m_apm.getActivePackageNames().size());

        // Reload the daemon
        updateConfigAndReloadDaemon(getDynamicPackageConfig(), true);

        sleep(PACKAGE_SCAN_INTERVAL * 2);

        assertEquals(1, m_apm.getActivePackageNames().size());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    private String getDynamicPackageConfig() {
    	return "<?xml version=\"1.0\"?>\n" +
    	"<access-point-monitor-configuration threads=\"30\" package-scan-interval=\"" + PACKAGE_SCAN_INTERVAL + "\">\n" +
		"   	<service-template name=\"Aruba-AP-IsAdoptedOnController\" interval=\"" + PACKAGE_SCAN_INTERVAL + "\" status=\"off\">\n" +
    	"                        <parameter key=\"retry\" value=\"3\"/>\n" +
    	"                        <parameter key=\"oid\" value=\".1.3.6.1.4.1.14823.2.2.1.5.2.1.4.1.19\"/>\n" +
    	"                        <parameter key=\"operator\" value=\"=\"/>\n" +
    	"                        <parameter key=\"operand\" value=\"1\"/>\n" +
    	"                        <parameter key=\"match\" value=\"true\"/>\n" +
		"		</service-template>" +
    	"       <package name=\"dynamic-pkg-%\">\n" +
    	"               <filter>IPADDR != '0.0.0.0'</filter>\n" +
    	"               <service name=\"Aruba-AP-IsAdoptedOnController\" status=\"on\"/>" +
    	"       </package>\n" +
    	"       <monitor service=\"Aruba-AP-IsAdoptedOnController\" class-name=\"org.opennms.netmgt.accesspointmonitor.poller.InstanceStrategy\" />\n" +
    	"</access-point-monitor-configuration>\n" +
    	"";
    }
}
