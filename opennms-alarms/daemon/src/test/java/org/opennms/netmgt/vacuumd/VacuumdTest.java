/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vacuumd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.alarmd.Alarmd;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.config.vacuumd.Trigger;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests Vacuumd's execution of statements and automations
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @author <a href=mailto:brozow@opennms.org>Mathew Brozowski</a>
 *
 */

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
public class VacuumdTest implements TemporaryDatabaseAware<MockDatabase>, InitializingBean {
    private static final long TEAR_DOWN_WAIT_MILLIS = 1000;

    private Vacuumd m_vacuumd;

    @Autowired
    private Alarmd m_alarmd;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    @Autowired
    private MockEventIpcManager m_eventdIpcMgr;

    private MockNetwork m_network = new MockNetwork();

    private MockDatabase m_database;

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_database = database;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_network.createStandardNetwork();

        InputStream is = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/vacuumd/vacuumd-configuration.xml");
        try {
            VacuumdConfigFactory.setInstance(new VacuumdConfigFactory(is));
        } finally {
            IOUtils.closeQuietly(is);
        }

        m_eventdIpcMgr.setEventWriter(m_database);

        m_vacuumd = Vacuumd.getSingleton();
        m_vacuumd.setEventManager(m_eventdIpcMgr);
        m_vacuumd.init();

        // Insert some empty nodes to avoid foreign-key violations on subsequent events/alarms
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("default-1");
        m_nodeDao.save(node);

        node = new OnmsNode();
        node.setId(2);
        node.setLabel("default-2");
        m_nodeDao.save(node);

        MockUtil.println("------------ Finished setup for: "+ this.getClass().getName() +" --------------------------");
    }

    @After
    public void tearDown() throws Exception {
        m_alarmd.destroy();
        MockUtil.println("Sleeping for "+TEAR_DOWN_WAIT_MILLIS+" millis in tearDown...");
        Thread.sleep(TEAR_DOWN_WAIT_MILLIS);
    }
    
    /**
     * Test for running statments
     */
    @Test
    public final void testRunStatements() {
    	m_vacuumd.executeStatements();
    }
    
    /**
     * This is an attempt at testing scheduled automations.
     * @throws InterruptedException
     */
    @Test
    public final void testConcurrency() throws InterruptedException {
        try {
        /*
         * Test status of threads
         */
        assertEquals(Fiber.START_PENDING, m_vacuumd.getStatus());
        assertEquals(Fiber.START_PENDING, m_vacuumd.getScheduler().getStatus());
        
        /*
         * Testing the start
         */
        m_vacuumd.start();
        assertTrue(m_vacuumd.getStatus() >= 1);
        Thread.sleep(200);
        assertEquals(Fiber.RUNNING, m_vacuumd.getStatus());
        assertEquals(Fiber.RUNNING, m_vacuumd.getScheduler().getStatus());
        
        /*
         * Testing the pause
         */
        m_vacuumd.pause();
        Thread.sleep(200);
        assertEquals(PausableFiber.PAUSED, m_vacuumd.getStatus());
        assertEquals(PausableFiber.PAUSED, m_vacuumd.getScheduler().getStatus());

        m_vacuumd.resume();
        Thread.sleep(200);
        assertEquals(PausableFiber.RUNNING, m_vacuumd.getStatus());
        assertEquals(PausableFiber.RUNNING, m_vacuumd.getScheduler().getStatus());
        
        // Get an alarm in the DB
        bringNodeDownCreatingEvent(1);
        // There should be one node down alarm
        assertEquals("count of nodeDown events", 1, m_jdbcTemplate.queryForInt("select count(*) from events where eventuei = '" + EventConstants.NODE_DOWN_EVENT_UEI + "'"));
        assertEquals("alarm count", 1, countAlarms());
        assertEquals("counter in the alarm", 1, m_jdbcTemplate.queryForInt("select counter from alarms where eventuei = '" + EventConstants.NODE_DOWN_EVENT_UEI + "'"));
        // Fetch the initial severity of the alarm
        int currentSeverity = m_jdbcTemplate.queryForInt("select severity from alarms");

        // Create another node down event
        bringNodeDownCreatingEvent(1);
        assertEquals("count of nodeDown events", 2, m_jdbcTemplate.queryForInt("select count(*) from events where eventuei = '" + EventConstants.NODE_DOWN_EVENT_UEI + "'"));
        // Make sure there's still one alarm...
        assertEquals("alarm count", 1, countAlarms());
        // ... with a counter value of 2
        assertEquals("counter in the alarm", 2, m_jdbcTemplate.queryForInt("select counter from alarms"));

        // Sleep long enough for the escalation automation to run, then check that it was escalated
        Thread.sleep(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate").getInterval() + 500);
        assertEquals("alarm severity wrong, should have been escalated", currentSeverity+1, verifyAlarmEscalated());
        } finally {
        // Stop what you start
        m_vacuumd.stop();
        }
    }

    public final void testConfigReload() {
        // TODO: Check configuration before and after
        EventBuilder builder = new EventBuilder(EventConstants.RELOAD_VACUUMD_CONFIG_UEI, "test");
        Event e = builder.getEvent();
        m_eventdIpcMgr.sendNow(e);
    }

    /**
     * Test resultSetHasRequiredActionColumns method
     * @throws SQLException 
     */
    @Test
    public final void testResultSetHasRequiredActionColumns() throws SQLException {
        Connection conn = null;
        try {
            conn = DataSourceFactory.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from events");
            Collection<String> columns = new ArrayList<String>();
            AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
            assertTrue(ap.getAction().resultSetHasRequiredActionColumns(rs, columns));
        } finally {
            conn.close();
        }
    }

    /**
     * Simple test on a helper method.
     */
    @Test
    public final void testGetAutomations() {
        assertEquals(19, VacuumdConfigFactory.getInstance().getAutomations().size());
    }
    
    @Test
    public final void testGetAutoEvents() {
        assertEquals(2, VacuumdConfigFactory.getInstance().getAutoEvents().size());
    }
    
    /**
     * Simple test on a helper method.
     */
    @Test
    public final void testGetTriggers() {
        assertEquals(14,VacuumdConfigFactory.getInstance().getTriggers().size());
    }
    
    /**
     * Simple test on a helper method.
     */
    @Test
    public final void testGetActions() {
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        
        assertEquals(18,VacuumdConfigFactory.getInstance().getActions().size());
        assertEquals(2, ap.getAction().getTokenCount(VacuumdConfigFactory.getInstance().getAction("delete").getStatement().getContent()));
    }
    
    /**
     * Simple test on a helper method.
     */
    @Test
    public final void testGetTrigger() {
        assertNotNull(VacuumdConfigFactory.getInstance().getTrigger("selectAll"));
        assertEquals(1, VacuumdConfigFactory.getInstance().getTrigger("selectAll").getRowCount());
        assertEquals(">=", VacuumdConfigFactory.getInstance().getTrigger("selectAll").getOperator());
        assertNotNull(VacuumdConfigFactory.getInstance().getTrigger("selectWithCounter"));
        assertNull(VacuumdConfigFactory.getInstance().getTrigger("selectWithCounter").getOperator());
        assertEquals(0,VacuumdConfigFactory.getInstance().getTrigger("selectWithCounter").getRowCount());
    }
    
    /**
     * Simple test on a helper method.
     */
    @Test
    public final void testGetAction() {
        assertNotNull(VacuumdConfigFactory.getInstance().getAction("clear"));
        assertNotNull(VacuumdConfigFactory.getInstance().getAction("escalate"));
        assertNotNull(VacuumdConfigFactory.getInstance().getAction("delete"));
    }
    
    /**
     * Simple test on a helper method.
     */
    @Test
    public final void testGetAutomation() {
        assertNotNull(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate"));
    }

    /**
     * Simple test running a trigger.
     */
    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public final void testRunTrigger() throws InterruptedException {
        Trigger trigger = VacuumdConfigFactory.getInstance().getTrigger("selectAll");
        String triggerSql = trigger.getStatement().getContent();
        MockUtil.println("Running trigger query: "+triggerSql);
        
        int count = m_jdbcTemplate.queryForList(triggerSql).size();
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        assertFalse("Testing the result rows:"+count+" with the trigger operator "+trigger.getOperator()+" against the required rows:"+trigger.getRowCount(),
                ap.getTrigger().triggerRowCheck(trigger.getRowCount(), trigger.getOperator(), count));
        assertEquals(0, count);
    }

    /**
     * This tests the running of automations directly as if they were scheduled.
     * @throws SQLException
     * @throws InterruptedException 
     */
    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class) // Relies on records created in @Before so we need a fresh database
    public final void testRunAutomation() throws SQLException, InterruptedException {
        final int major = OnmsSeverity.MAJOR.getId();
        
        bringNodeDownCreatingEvent(1);
        Thread.sleep(1000);
        
        assertEquals(1, countAlarms());
        assertEquals(major, getSingleResultSeverity());
        assertEquals("counter in the alarm", 1, m_jdbcTemplate.queryForInt("select counter from alarms"));

        bringNodeDownCreatingEvent(1);
        Thread.sleep(1000);

        assertEquals(1, countAlarms());
        assertEquals(major, getSingleResultSeverity());
        assertEquals("counter in the alarm", 2, m_jdbcTemplate.queryForInt("select counter from alarms"));

        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate"));
        assertTrue(ap.runAutomation());
        Thread.sleep(1000);
        assertEquals(major+1, getSingleResultSeverity());
    }
    
    @Test
    public final void testRunAutomationWithNoTrigger() throws InterruptedException, SQLException {
        bringNodeDownCreatingEvent(1);
        Thread.sleep(1000);
        
        assertEquals(1, countAlarms());

        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cleanUpAlarms"));
        Thread.sleep(1000);
        assertTrue(ap.runAutomation());
    }
    
    @Test
    public final void testRunAutomationWithZeroResultsFromTrigger() throws InterruptedException, SQLException {
        bringNodeDownCreatingEvent(1);
        Thread.sleep(1000);
        assertEquals(1, countAlarms());
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("testZeroResults"));
        Thread.sleep(1000);
        assertTrue(ap.runAutomation());        
    }
    
    /**
     * This tests the capabilities of the cosmicClear automation as shipped in the standard build.
     * @throws InterruptedException 
     */
    @Test
    public final void testCosmicClearAutomation() throws InterruptedException {
        // create node down events with severity 6
        bringNodeDownCreatingEvent(1);
        bringNodeDownCreatingEvent(2);
        Thread.sleep(1000);
        // create node up event with severity 3
        bringNodeUpCreatingEvent(1);
        Thread.sleep(1000);
        
        // should have three alarms, one for each event
        assertEquals("should have one alarm for each event", 3, m_jdbcTemplate.queryForLong("select count(*) from alarms"));

        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        ap.run();
        Thread.sleep(1000);
        
        // the automation should have cleared the nodeDown for node 1 so it should now have severity CLEARED == 2
        assertEquals("alarms with severity == 2", 1, m_jdbcTemplate.queryForLong("select count(*) from alarms where severity = 2"));

        // There should still be a nodeUp alarm and an uncleared nodeDown alarm
        assertEquals("alarms with severity > 2", 2, m_jdbcTemplate.queryForLong("select count(*) from alarms where severity > 2"));

        // run this automation again and make sure nothing happens since we've already processed the clear
        ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        ap.run();
        Thread.sleep(1000);
        
        // same as above
        assertEquals("alarms with severity == 2", 1, m_jdbcTemplate.queryForLong("select count(*) from alarms where severity = 2"));

        // save as above
        assertEquals("alarms with severity > 2", 2, m_jdbcTemplate.queryForLong("select count(*) from alarms where severity > 2"));
    }

    /**
     * @throws InterruptedException 
     */
    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class) // Relies on records created in @Before so we need a fresh database
    public final void testSendEventWithParms() throws InterruptedException {
        // create node down events with severity 6
        bringNodeDownCreatingEventWithReason(1, "Testing node1");
        Thread.sleep(1000);
        new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("escalate")).run();
        Thread.sleep(1000);
        Map<String, Object> queryResult = m_jdbcTemplate.queryForMap("SELECT eventuei, eventparms FROM events WHERE eventuei = 'uei.opennms.org/vacuumd/alarmEscalated'");
        // If the add-all-parms="true" is set on the action-event, the parms will turn out like this
        // assertEquals("Parameter list sent from action event doesn't match", "eventReason=Testing node1(string,text);alarmId=1(string,text);alarmEventUei=uei.opennms.org/nodes/nodeDown(string,text)", queryResult.get("eventParms"));
        assertEquals("Parameter list sent from action event doesn't match", "alarmId=1(string,text);alarmEventUei=uei.opennms.org/nodes/nodeDown(string,text)", queryResult.get("eventParms"));
    }
    
    /**
     * Test the ability to find tokens in a statement.
     */
    @Test
    public void testGetTokenizedColumns() {
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        
        Collection<String> tokens = ap.getAction().getActionColumns();

        //just this for now
        assertFalse(tokens.isEmpty());
    }
    
    /**
     * Why not.
     */
    @Test
    public final void testGetName() {
        assertEquals("vacuumd", m_vacuumd.getName());
    }

    /**
     * 
     */
    @Test
    public final void testRunUpdate() {
        //TODO Implement runUpdate().
    }
    
    @Test
    public final void testGetTriggerSqlWithNoTriggerDefined() {
        Automation auto = VacuumdConfigFactory.getInstance().getAutomation("cleanUpAlarms");
        AutomationProcessor ap = new AutomationProcessor(auto);
        assertEquals(null, ap.getTrigger().getTriggerSQL());
    }
    
    private int countAlarms() {
        return (int) m_jdbcTemplate.queryForLong("select count(*) from alarms");
    }

    /**
     * Verifies for the concurrency test that the alarm escalated.
     * @return
     */
    private int verifyAlarmEscalated() {
        return m_jdbcTemplate.queryForInt("select severity from alarms");
    }

    /**
     * Returns the severity of the alarm in the alarms table.
     * @return
     */
    private int getSingleResultSeverity() {
        return m_jdbcTemplate.queryForInt("select severity from alarms");
    }
    
    private void bringNodeDownCreatingEvent(int nodeid) {
        MockNode node = m_network.getNode(nodeid);
        m_eventdIpcMgr.sendNow(node.createDownEvent());
    }

    private void bringNodeDownCreatingEventWithReason(int nodeid, String reason) {
        MockNode node = m_network.getNode(nodeid);
        m_eventdIpcMgr.sendNow(node.createDownEventWithReason(reason));
    }
    
    private void bringNodeUpCreatingEvent(int nodeid) {
        MockNode node = m_network.getNode(nodeid);
        m_eventdIpcMgr.sendNow(node.createUpEvent());
    }
}
