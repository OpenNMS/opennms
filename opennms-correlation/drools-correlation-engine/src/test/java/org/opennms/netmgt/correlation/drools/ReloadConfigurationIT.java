/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.correlation.drools;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.Correlator;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

/**
 * The Class ReloadConfigurationIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-correlator.xml",
        "classpath*:META-INF/opennms/correlation-engine.xml",
        "classpath:/test-reload-context.xml"
})
@JUnitConfigurationEnvironment(systemProperties={"org.opennms.activemq.broker.disable=true"})
@JUnitTemporaryDatabase
public class ReloadConfigurationIT {

    /** The drools home. */
    static File DROOLS_HOME = new File("target/opennms-home/etc/drools-engine.d");

    /** The drools source configuration. */
    static File DROOLS_SRC = new File("src/test/opennms-home/etc/drools-engine.d");

    /** The drools complementary source configuration. */
    static File DROOLS_SRC2 = new File("src/test/opennms-home/etc/reload-tests");

    /** The correlator. */
    @Autowired
    private Correlator m_correlator;

    /** The event IPC manager. */
    @Autowired
    @Qualifier("eventProxy")
    private EventIpcManager m_eventIpcMgr;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        System.err.println("Building directory " + DROOLS_HOME);
        if (DROOLS_HOME.exists()) {
            FileUtils.deleteDirectory(DROOLS_HOME);
        }
        Assert.assertFalse(DROOLS_HOME.exists());
        Assert.assertTrue(DROOLS_HOME.mkdirs());
    }

    /**
     * Adds a default engine.
     *
     * @throws Exception the exception
     */
    @Before
    public void addEngine() throws Exception {
        FileUtils.copyDirectory(new File(DROOLS_SRC, "simpleRules"), new File(DROOLS_HOME, "simpleRules"));
        sendReloadDaemonConfig();
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        m_correlator.stop();
        FileUtils.deleteDirectory(DROOLS_HOME);
        DROOLS_HOME.mkdirs();
    }

    /**
     * Test add and remove engine.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAddAndRemoveEngine() throws Exception {
        Collection<CorrelationEngine> engines = m_correlator.getEngines();
        Assert.assertNotNull(engines);
        Assert.assertEquals(1, engines.size());

        FileUtils.copyDirectory(new File(DROOLS_SRC, "persistState"), new File(DROOLS_HOME, "persistState"));
        sendReloadDaemonConfig();

        engines = m_correlator.getEngines();
        Assert.assertEquals(2, engines.size());

        FileUtils.deleteDirectory(new File(DROOLS_HOME, "simpleRules"));
        sendReloadDaemonConfig();

        engines = m_correlator.getEngines();
        Assert.assertEquals(1, engines.size());
    }

    /**
     * Test edit existing engine with valid content.
     *
     * @throws Exception the exception
     */
    @Test
    public void testEditExistingEngineWithValidContent() throws Exception {
        Collection<CorrelationEngine> engines = m_correlator.getEngines();
        Assert.assertNotNull(engines);
        Assert.assertEquals(1, engines.size());

        FileUtils.copyDirectory(new File(DROOLS_SRC2, "simpleRules"), new File(DROOLS_HOME, "simpleRules"));
        sendReloadDaemonConfig();

        EventBuilder eb = new EventBuilder("uei.opennms.org/junit/myTestEvent", "Junit");
        m_eventIpcMgr.send(eb.getEvent());
        Thread.sleep(1000);

        File file = new File("target/sample-file.txt");
        Assert.assertTrue(file.exists());
        file.delete();
    }

    /**
     * Test edit existing engine with invalid content.
     *
     * @throws Exception the exception
     */
    @Test
    public void testEditExistingEngineWithInvalidContent() throws Exception {
        Collection<CorrelationEngine> engines = m_correlator.getEngines();
        Assert.assertNotNull(engines);
        Assert.assertEquals(1, engines.size());

        FileUtils.copyDirectory(new File(DROOLS_SRC2, "brokenRules"), new File(DROOLS_HOME, "brokenRules"));
        sendReloadDaemonConfig();

        engines = m_correlator.getEngines();
        Assert.assertEquals(1, engines.size());
    }

    /**
     * Test adding engine with invalid content.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAddingEngineWithInvalidContent() throws Exception {
        Collection<CorrelationEngine> engines = m_correlator.getEngines();
        Assert.assertNotNull(engines);
        Assert.assertEquals(1, engines.size());

        FileUtils.copyDirectory(new File(DROOLS_SRC2, "brokenRules"), new File(DROOLS_HOME, "brokenRules"));
        sendReloadDaemonConfig();

        engines = m_correlator.getEngines();
        Assert.assertEquals(1, engines.size()); // Instead of 2
    }

    /**
     * Send reload daemon configuration.
     *
     * @throws Exception the exception
     */
    void sendReloadDaemonConfig() throws Exception {
        EventBuilder eb = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "Junit");
        eb.addParam("daemonName", "DroolsCorrelationEngine");
        m_eventIpcMgr.send(eb.getEvent());
        Thread.sleep(1000);
    }
}
