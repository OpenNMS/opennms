/*
 * This file is part of the OpenNMS(R) Application.
 * 
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jan 26: Don't call methods directly on Eventd to send events
 *              (they are moving, anyway)--use EventIpcManager. - dj@opennms.org
 * 2007 Jun 10: Use SimpleJdbcTemplate for queries. - dj@opennms.org
 * 2007 Jun 09: Move the config into a test resource. - dj@opennms.org
 * 2007 Mar 13: VacuumdConfigFactory.setConfigReader(Reader) is gone.  Use new VacuumdConfigFactory(Reader) and setInstance, instead. - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.                                                            
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *    
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.vacuumd;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.config.vacuumd.Trigger;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockUtil;

/**
 * Tests Vacuumd's execution of statements and automations
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @author <a href=mailto:brozow@opennms.org>Mathew Brozowski</a>
 *
 */
public class VacuumdTest extends OpenNMSTestCase {
    private static final long TEAR_DOWN_WAIT_MILLIS = 1000;
    
    private Vacuumd m_vacuumd;

    protected void setUp() throws Exception {
        super.setUp();
        
        InputStream is = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/vacuumd/vacuumd-configuration.xml");
        try {
            VacuumdConfigFactory.setInstance(new VacuumdConfigFactory(is));
        } finally {
            IOUtils.closeQuietly(is);
        }

        m_vacuumd = Vacuumd.getSingleton();
        m_vacuumd.setEventManager(m_eventdIpcMgr);
        m_vacuumd.init();

        MockUtil.println("------------ Finished setup for: "+getName()+" --------------------------");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        MockUtil.println("Sleeping for "+TEAR_DOWN_WAIT_MILLIS+" millis in tearDown...");
        Thread.sleep(TEAR_DOWN_WAIT_MILLIS);
    }
    
    /**
     * Test for running statments
     */
    public final void testRunStatements() {
    	m_vacuumd.executeStatements();
    }
    
    /**
     * This is an attempt at testing scheduled automations.
     * @throws InterruptedException
     */
    public final void testConcurrency() throws InterruptedException {
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
        
        //Get an alarm in the db
        bringNodeDownCreatingEvent(1);
        Thread.sleep(5000);
        
        /*
         * Changes to the automations to the config will
         * probably affect this.  There should be one node down
         * alarm.
         */
        assertEquals("alarm count", 1, verifyInitialAlarmState());

        // Create another node down event
        bringNodeDownCreatingEvent(1);
        
        // Sleep and wait for the alarm to be written
        Thread.sleep(1500);
        assertEquals("counter in the alarm", 2, getJdbcTemplate().queryForInt("select counter from alarms"));
                
        /*
         * Get the current severity, sleep long enough for the escalation
         * automation to run, then check that it was escalated.
         */
        int currentSeverity = getJdbcTemplate().queryForInt("select severity from alarms");
        Thread.sleep(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate").getInterval()+100);
        assertEquals("alarm severity -- should have been excalated", currentSeverity+1, verifyAlarmEscalated());
        
        EventBuilder builder = new EventBuilder(Vacuumd.RELOAD_CONFIG_UEI, "test");
        Event e = builder.getEvent();
        m_eventdIpcMgr.sendNow(e);
        
        Thread.sleep(2000);
                
        //Stop what you start.
        m_vacuumd.stop();
    }
    
    /**
     * Test resultSetHasRequiredActionColumns method
     * @throws SQLException 
     */
    public final void testResultSetHasRequiredActionColumns() throws SQLException {
        Connection conn = DataSourceFactory.getInstance().getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from events");
        Collection<String> columns = new ArrayList<String>();
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        assertTrue(ap.getAction().resultSetHasRequiredActionColumns(rs, columns));
    }

    /**
     * Simple test on a helper method.
     */
    public final void testGetAutomations() {
        assertEquals(7, VacuumdConfigFactory.getInstance().getAutomations().size());
    }
    
    public final void testGetAutoEvents() {
        assertEquals(2, VacuumdConfigFactory.getInstance().getAutoEvents().size());
    }
    
    /**
     * Simple test on a helper method.
     */
    public final void testGetTriggers() {
        assertEquals(6,VacuumdConfigFactory.getInstance().getTriggers().size());
    }
    
    /**
     * Simple test on a helper method.
     */
    public final void testGetActions() {
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        
        assertEquals(6,VacuumdConfigFactory.getInstance().getActions().size());
        assertEquals(2, ap.getAction().getTokenCount(VacuumdConfigFactory.getInstance().getAction("delete").getStatement().getContent()));
    }
    
    /**
     * Simple test on a helper method.
     */
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
    public final void testGetAction() {
        assertNotNull(VacuumdConfigFactory.getInstance().getAction("clear"));
        assertNotNull(VacuumdConfigFactory.getInstance().getAction("escalate"));
        assertNotNull(VacuumdConfigFactory.getInstance().getAction("delete"));
    }
    
    /**
     * Simple test on a helper method.
     */
    public final void testGetAutomation() {
        assertNotNull(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate"));
    }

    /**
     * Simple test running a trigger.
     */
    public final void testRunTrigger() throws InterruptedException {
        // Get all the triggers defined in the config
        Collection<Trigger> triggers = VacuumdConfigFactory.getInstance().getTriggers();
        assertEquals(6, triggers.size());

        Trigger trigger = VacuumdConfigFactory.getInstance().getTrigger("selectAll");
        String triggerSql = trigger.getStatement().getContent();
        MockUtil.println("Running trigger query: "+triggerSql);
        
        int count = getJdbcTemplate().queryForList(triggerSql).size();
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
    public final void testRunAutomation() throws SQLException, InterruptedException {
        final int major = 6;
        
        bringNodeDownCreatingEvent(1);
        Thread.sleep(500);
        
        assertEquals(1, verifyInitialAlarmState());
        assertEquals(major, getSingleResultSeverity());

        bringNodeDownCreatingEvent(1);
        Thread.sleep(500);

        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate"));
        Thread.sleep(500);
        assertTrue(ap.runAutomation());        
        assertEquals(major+1, getSingleResultSeverity());
    }
    
    public final void testRunAutomationWithNoTrigger() throws InterruptedException, SQLException {
        bringNodeDownCreatingEvent(1);
        Thread.sleep(500);
        
        assertEquals(1, verifyInitialAlarmState());

        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cleanUpAlarms"));
        Thread.sleep(2000);
        assertTrue(ap.runAutomation());
    }
    
    public final void testRunAutomationWithZeroResultsFromTrigger() throws InterruptedException, SQLException {
        bringNodeDownCreatingEvent(1);
        Thread.sleep(500);
        assertEquals(1, verifyInitialAlarmState());
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("testZeroResults"));
        Thread.sleep(200);
        assertTrue(ap.runAutomation());        
    }
    
    /**
     * This tests the capabilities of the cosmicClear autmation as shipped in the standard build.
     * @throws InterruptedException 
     */
    public final void testCosmicClearAutomation() throws InterruptedException {
        // create node down events with severity 6
        bringNodeDownCreatingEvent(1);
        bringNodeDownCreatingEvent(2);
        Thread.sleep(1000);
        // create node up event with severity 3
        bringNodeUpCreatingEvent(1);
        Thread.sleep(1000);
        
        assertEquals("clearUei for nodeUp", "uei.opennms.org/nodes/nodeDown", getJdbcTemplate().queryForObject("select clearUei from alarms where eventUei = ?", String.class, "uei.opennms.org/nodes/nodeUp"));
        
        // should have three alarms, one for each event
        assertEquals("should have one alarm for each event", 3, getJdbcTemplate().queryForLong("select count(*) from alarms"));

        // the automation should have cleared the nodeDown for node 1 so it should now have severity CLEARED == 2
        assertEquals("alarms with severity == 2", 1, getJdbcTemplate().queryForLong("select count(*) from alarms where severity = 2"));

        // There should still be a nodeUp alarm and an uncleared nodeDown alarm
        assertEquals("alarms with severity > 2", 2, getJdbcTemplate().queryForLong("select count(*) from alarms where severity > 2"));

        // run this automation again and make sure nothing happens since we've already processed the clear
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        ap.run();
        Thread.sleep(1000);
        
        // same as above
        assertEquals("alarms with severity == 2", 1, getJdbcTemplate().queryForLong("select count(*) from alarms where severity = 2"));

        // save as above
        assertEquals("alarms with severity > 2", 2, getJdbcTemplate().queryForLong("select count(*) from alarms where severity > 2"));
    }

    /**
     * This tests the capabilities of the cosmicClear automation as shipped in the standard build.
     * @throws InterruptedException 
     */
    public final void testSendEventWithParms() throws InterruptedException {
        // create node down events with severity 6
        bringNodeDownCreatingEventWithReason(1, "Testing node1");
        Thread.sleep(1000);
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("escalate"));
        ap.run();
        Thread.sleep(500);
        Map<String, Object> queryResult = getJdbcTemplate().queryForMap("SELECT eventuei, eventparms FROM events WHERE eventuei = 'uei.opennms.org/vacuumd/alarmEscalated'");
        assertEquals("Parmater list sent from action event doesn't match", queryResult.get("eventParms"), "eventReason=Testing node1(string,text);alarmId=1(string,text)");
    }
    
    /**
     * Test the ability to find tokens in a statement.
     */
    public void testGetTokenizedColumns() {
        AutomationProcessor ap = new AutomationProcessor(VacuumdConfigFactory.getInstance().getAutomation("cosmicClear"));
        
        Collection<String> tokens = ap.getAction().getActionColumns();

        //just this for now
        assertFalse(tokens.isEmpty());
    }
    
    /**
     * Why not.
     */
    public final void testGetName() {
        assertEquals("OpenNMS.Vacuumd", m_vacuumd.getName());
    }

    /**
     * 
     */
    public final void testRunUpdate() {
        //TODO Implement runUpdate().
    }
    
    public final void testGetTriggerSqlWithNoTriggerDefined() {
        Automation auto = VacuumdConfigFactory.getInstance().getAutomation("cleanUpAlarms");
        AutomationProcessor ap = new AutomationProcessor(auto);
        assertEquals(null, ap.getTrigger().getTriggerSQL());
    }
    
    /**
     * Really only elminated some duplication here.  This
     * method verifies that there is one alarm in the database
     * when a test begins (based on setUp()).
     * @return
     */
    private int verifyInitialAlarmState() {
        return (int) getJdbcTemplate().queryForLong("select count(*) from alarms");
    }

    /**
     * Verifys for the concurrency test that the alarm escalated.
     * @return
     */
    private int verifyAlarmEscalated() {
        return getJdbcTemplate().queryForInt("select severity from alarms");
    }

    /**
     * Returns the severity of the alarm in the alarms table.
     * @return
     */
    private int getSingleResultSeverity() {
        return getJdbcTemplate().queryForInt("select severity from alarms");
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
