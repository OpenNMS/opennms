//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.vacuumd;

import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Action;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.config.vacuumd.Trigger;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.utils.SingleResultQuerier;

public class VacuumdTest extends OpenNMSTestCase {

    private final static String VACUUMD_CONFIG = 
        "<VacuumdConfiguration period=\"86400000\" >\n" + 
        "   <statement>\n" + 
        "       <!-- this deletes all the nodes that have been marked as deleted - it relies on cascading deletes -->\n" + 
        "       DELETE FROM node WHERE node.nodeType = \'D\';\n" + 
        "   </statement>\n" + 
        "   <statement>\n" + 
        "       <!-- this deletes all the interfaces that have been marked as deleted - it relies on cascading deletes -->\n" + 
        "       DELETE FROM ipInterface WHERE ipInterface.isManaged = \'D\';\n" + 
        "   </statement>\n" + 
        "   <statement>\n" + 
        "      <!-- this deletes all the services that have been marked as deleted - it relies on cascading deletes -->\n" + 
        "      DELETE FROM ifServices WHERE ifServices.status = \'D\';\n" + 
        "   </statement>\n" + 
        "    <statement>\n" + 
        "       <!-- this deletes any events that are not associated with outages - Thanks to Chris Fedde for this -->\n" + 
        "      DELETE FROM events WHERE NOT EXISTS \n" + 
        "          (SELECT svclosteventid FROM outages WHERE svclosteventid = events.eventid  \n" + 
        "        UNION \n" + 
        "          SELECT svcregainedeventid FROM outages WHERE svcregainedeventid = events.eventid \n" + 
        "        UNION \n" + 
        "          SELECT eventid FROM notifications WHERE eventid = events.eventid) \n" + 
        "       AND eventtime &lt; now() - interval \'6 weeks\';\n" + 
        "    </statement>\n" +
        "    <automations>\n" + 
        "           <automation name=\"autoEscalate\" interval=\"10000\" trigger-name=\"selectWithCounter\" auto-event-name=\"escalationEvent\" action-name=\"escalate\" active=\"true\" />\n" + 
        "           <automation name=\"cleanUpAlarms\" interval=\"300000\" action-name=\"deleteDayOldAlarms\" active=\"true\" />\n" + 
        "    </automations>\n" + 
        "    <triggers>\n" + 
        "           <trigger name=\"selectAll\" operator=\"&gt;=\" row-count=\"1\" >\n" + 
        "               <statement>SELECT * FROM alarms</statement>\n" + 
        "           </trigger>\n" + 
        "           <trigger name=\"selectWithCounter\" >\n" + 
        "               <statement>SELECT alarmid FROM alarms WHERE counter &gt;= 2</statement>\n" + 
        "           </trigger>\n" + 
        "    </triggers>\n" + 
        "    <actions>\n" + 
        "           <action name=\"clear\" >\n" + 
        "               <statement>UPDATE alarms SET severity=2 WHERE alarmid = ${alarmid}</statement>\n" + 
        "           </action>\n" + 
        "           <action name=\"escalate\" >\n" + 
        "               <statement>UPDATE alarms SET severity = severity+1 WHERE alarmid = ${alarmid} and alarmAckUser is null and severity &lt; 7</statement>\n" + 
        "           </action>\n" + 
        "           <action name=\"delete\" >\n" + 
        "               <statement>DELETE FROM alarms WHERE nodeid = ${nodeid} and eventuei = ${eventuei}</statement>\n" + 
        "           </action>\n" + 
        "           <action name=\"deleteDayOldAlarms\" >\n" + 
        "               <statement>DELETE FROM alarms WHERE alarmAckUser IS NOT NULL AND lastEventTime &lt; CURRENT_TIMESTAMP</statement>\n" + 
        "           </action>\n" + 
        "    </actions>\n" + 
        "    <auto-events>\n" +
        "        <auto-event name=\"escalationEvent\" >\n" +
        "            <uei>uei.opennms.org/vacuumd/alarmEscalated</uei>\n" +
        "        </auto-event>\n" +
        "    </auto-events>\n" +
        "" + 
        "</VacuumdConfiguration>";
    
    private Vacuumd m_vacuumd;

    protected void setUp() throws Exception {
        
        super.setUp();
        Reader rdr = new StringReader(VACUUMD_CONFIG);
        VacuumdConfigFactory.setConfigReader(rdr);
        m_vacuumd = Vacuumd.getSingleton();
        m_vacuumd.setEventManager(m_eventdIpcMgr);
        m_vacuumd.init();
        
        //The rdr is closed by init, too, but doesn't hurt
        rdr.close();

        MockUtil.println("------------ Finished setup for: "+getName()+" --------------------------");
        
    }

    private void bringNodeDownCreatingEvent() {
        MockNode node = m_network.getNode(1);
        m_eventd.processEvent(node.createDownEvent());
    }

    protected void tearDown() throws Exception {
        assertTrue("Unexpected WARN or ERROR msgs in Log!", MockUtil.noWarningsOrHigherLogged());
        super.tearDown();
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
        bringNodeDownCreatingEvent();
        Thread.sleep(500);
        /*
         * Changes to the automations to the VACUUMD_CONFIG will
         * probably affect this.  There should be one node down
         * alarm.
         */
        assertEquals(1, verifyInitialAlarmState());


        //Create another node down event
        bringNodeDownCreatingEvent();
        /*
         * Sleep and wait for the alarm to be written
         */
        Thread.sleep(500);
        assertEquals(2, alarmDeDuplicated());
        
        /*
         * Sleep long enough for the automation to run.
         */
        Thread.sleep(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate").getInterval()+100);
        assertEquals(7, verifyAlarmEscalated());
                
        //Stop what you start.
        m_vacuumd.stop();

    }
    
    /**
     * Simple test on a helper method.
     */
    public final void testGetAutomations() {
        assertEquals(2, VacuumdConfigFactory.getInstance().getAutomations().size());
    }
    
    public final void testGetAutoEvents() {
        assertEquals(1, VacuumdConfigFactory.getInstance().getAutoEvents().size());
    }
    
    /**
     * Simple test on a helper method.
     */
    public final void testGetTriggers() {
        assertEquals(2,VacuumdConfigFactory.getInstance().getTriggers().size());
    }
    
    /**
     * Simple test on a helper method.
     */
    public final void testGetActions() {
        AutomationProcessor ap = new AutomationProcessor();
        assertEquals(4,VacuumdConfigFactory.getInstance().getActions().size());
        assertEquals(2, ap.getTokenCount(VacuumdConfigFactory.getInstance().getAction("delete").getStatement().getContent()));
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
        
        //Get all the triggers defined in the config
        ArrayList triggers = (ArrayList)VacuumdConfigFactory.getInstance().getTriggers();
        assertEquals(2, triggers.size());

        Querier q = null;

        Trigger trigger = VacuumdConfigFactory.getInstance().getTrigger("selectAll");
        String triggerSql = trigger.getStatement().getContent();
        MockUtil.println("Running trigger query: "+triggerSql);
        q = new Querier(m_db, triggerSql);
        q.execute();
        assertFalse("Testing the result rows:"+q.getCount()+" with the trigger operator "+trigger.getOperator()+" against the required rows:"+trigger.getRowCount(),
                AutomationProcessor.triggerRowCheck(trigger.getRowCount(), trigger.getOperator(), q.getCount()));
        assertEquals(0, q.getCount());
        
    }

    /**
     * This tests the running of automations directly as if they were scheduled.
     * @throws SQLException
     * @throws InterruptedException 
     */
    public final void testRunAutomation() throws SQLException, InterruptedException {

        final int major = 6;
        
        bringNodeDownCreatingEvent();
        Thread.sleep(500);
        
        assertEquals(1, verifyInitialAlarmState());
        assertEquals(major, getSingleResultSeverity());

        bringNodeDownCreatingEvent();
        Thread.sleep(500);

        AutomationProcessor ap = new AutomationProcessor();
        ap.setAutomation(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate"));
        Thread.sleep(500);
        assertTrue(ap.runAutomation(VacuumdConfigFactory.getInstance().getAutomation("autoEscalate")));        
        assertEquals(major+1, getSingleResultSeverity());
        
    }
    
    public final void testRunAutomationWithNoTrigger() throws InterruptedException, SQLException {
        
        bringNodeDownCreatingEvent();
        Thread.sleep(500);
        
        assertEquals(1, verifyInitialAlarmState());

        AutomationProcessor ap = new AutomationProcessor();
        ap.setAutomation(VacuumdConfigFactory.getInstance().getAutomation("cleanUpAlarms"));
        Thread.sleep(2000);
        assertTrue(ap.runAutomation(VacuumdConfigFactory.getInstance().getAutomation("cleanUpAlarms")));
    }

    /**
     * Test the ability to find tokens in a statement.
     */
    public void testGetTokenizedColumns() {
        
        AutomationProcessor ap = new AutomationProcessor();
        
        ArrayList actions = (ArrayList)VacuumdConfigFactory.getInstance().getActions();
        Collection tokens = ap.getTokenizedColumns(((Action)actions.get(0)).getStatement().getContent());

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
    

    /**
     * Really only elminated some duplication here.  This
     * method verifys that there is one alarm in the database
     * when a test begins (based on setUp()).
     * @return
     */
    private int verifyInitialAlarmState() {
        int verified = -1;
        Querier q = new Querier(m_db, "select * from alarms");
        q.execute();
        verified = q.getCount();
        MockUtil.println("verifyInitialSetup: Expecting rows in alarms table to be 1 and actually is: "+q.getCount());
        q = null;
        return verified;
    }

    /**
     * Verifys for the concurrency test that the alarm deduplicated.
     * @return
     */
    private int alarmDeDuplicated() {
        int verified = -1;
        //TODO: put check in to make sure there is only one alarm in the table
        SingleResultQuerier srq = new SingleResultQuerier(m_db, "select counter from alarms");
        srq.execute();
        verified = ((Integer)srq.getResult()).intValue();
        MockUtil.println("verifyInitialSetup: Expecting counter in alarms table to be 2 and actually is: "+((Integer)srq.getResult()).intValue());
        srq = null;
        return verified;
    }

    /**
     * Verifys for the concurrency test that the alarm escalated.
     * @return
     */
    private int verifyAlarmEscalated() {
        int verified = -1;
        //TODO: put check in to make sure there is only one alarm in the table
        SingleResultQuerier srq = new SingleResultQuerier(m_db, "select severity from alarms");
        srq.execute();
        verified = ((Integer)srq.getResult()).intValue();
        MockUtil.println("verifyAlarmEscalated: Expecting severity in alarms table to be 7 and actually is: "+((Integer)srq.getResult()).intValue());
        srq = null;
        return verified;
    }

    /**
     * Returns the severity of the alarm in the alarms table.
     * @return
     */
    private int getSingleResultSeverity() {
        SingleResultQuerier srq;
        srq = new SingleResultQuerier(m_db, "select severity from alarms");
        srq.execute();
        int severity = ((Integer)srq.getResult()).intValue();
        return severity;
    }
    
    public final void testGetTriggerSqlWithNoTriggerDefined() {
        Automation auto = VacuumdConfigFactory.getInstance().getAutomation("cleanUpAlarms");
        assertEquals(null, AutomationProcessor.getTriggerSQL(auto));
        
    }    

}
