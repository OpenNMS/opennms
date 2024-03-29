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
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
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
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
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
    private MockEventIpcManager m_eventIpcMgr;

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
    
    
    @Test
    public void testExceptionInDroolsReloadsEngine() throws Exception {
        Collection<CorrelationEngine> engines = m_correlator.getEngines();
        Assert.assertNotNull(engines);
        Assert.assertEquals(1, engines.size());
        FileUtils.copyDirectory(new File(DROOLS_SRC, "droolsFusion"), new File(DROOLS_HOME, "droolsFusion"));
        sendReloadDaemonConfig();
        engines = m_correlator.getEngines();
        Assert.assertEquals(2, engines.size());
        // Reset anticipated events, verify everything here after.
        m_eventIpcMgr.getEventAnticipator().reset();
        // Anticipate reload and engine exception related events.
        anticipate(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "Junit").getEvent());
        anticipate(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "droolsFusion").getEvent());
        anticipate(new EventBuilder(EventConstants.DROOLS_ENGINE_ENCOUNTERED_EXCEPTION, "droolsFusion").getEvent());
        // Correlate event that will throw exception and cause engine to reload.
        DroolsCorrelationEngine engine = findEngineByName("droolsFusion");
        Event event = new EventBuilder("uei.opennms.org/triggerTestForFusion", "Junit").getEvent();
        engine.correlate(event);
        // verify
        m_eventIpcMgr.getEventAnticipator().verifyAnticipated(5000, 0, 0, 0, 0);
        // Engine reloaded, sending NodeLostEvent should create NodeUp Event on from rule.
        anticipate(createNodeUpEvent(1));
        engine.correlate(createNodeLostServiceEvent(1, "ICMP"));
        // verify again for NodeUp Event
        m_eventIpcMgr.getEventAnticipator().verifyAnticipated(3000, 0, 0, 0, 0);
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
    
    protected DroolsCorrelationEngine findEngineByName(String engineName) {
        return (DroolsCorrelationEngine) m_correlator.findEngineByName(engineName);
    }
    

    private void sendNodeLostServiceEvent(int nodeid, String serviceName) throws EventProxyException {
        Event event = createNodeLostServiceEvent(nodeid, serviceName);
        m_eventIpcMgr.send(event);
    }
    private Event createNodeLostServiceEvent(int nodeid, String serviceName) {
        return new EventBuilder(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, serviceName)
                .setNodeid(nodeid)
                .getEvent();
    }
    
    private Event createNodeUpEvent(int nodeid) {
        return new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "test")
            .setNodeid(nodeid)
            .getEvent();
    }
    
    private void anticipate(Event event) {
        m_eventIpcMgr.getEventAnticipator().anticipateEvent(event);
    }
    
    
    void sendEvent(String  eventUei) throws EventProxyException {
        Event event = new EventBuilder(eventUei, "Junit").getEvent();
        m_eventIpcMgr.send(event);
    }
}
