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
import org.opennms.netmgt.config.vacuumd.VacuumdConfiguration;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.utils.Querier;

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
        "           <automation name=\"auto1\" interval=\"60000\" trigger-name=\"trigger1\" action-name=\"action1\" />\n" + 
        "           <automation name=\"auto2\" interval=\"90000\" trigger-name=\"trigger2\" action-name=\"action2\" />\n" + 
        "    </automations>\n" + 
        "    <triggers>\n" + 
        "           <trigger name=\"trigger1\" row-count=\"2\" operator=\"&gt;\">\n" + 
        "               <statement>SELECT * FROM alarms</statement>\n" + 
        "           </trigger>\n" + 
        "           <trigger name=\"trigger2\" >\n" + 
        "               <statement>SELECT alarmid FROM alarms WHERE counter &gt; 2</statement>\n" + 
        "           </trigger>\n" + 
        "    </triggers>\n" + 
        "    <actions>\n" + 
        "           <action name=\"action1\" >\n" + 
        "               <statement>DELETE FROM alarms WHERE nodeid = ${nodeid} and eventuei = ${eventuei}</statement>\n" + 
        "           </action>\n" + 
        "           <action name=\"action2\" >\n" + 
        "               <statement>UPDATE alarms SET severity=serverity+1, WHERE alarmid = ${alarmid}</statement>\n" + 
        "           </action>\n" + 
        "    </actions>\n" + 
        "" + 
        "</VacuumdConfiguration>";
    
    private Vacuumd m_vacuumd;
    private VacuumdConfiguration m_config;

    protected void setUp() throws Exception {
        super.setUp();
        Reader rdr = new StringReader(VACUUMD_CONFIG);
        VacuumdConfigFactory.setConfigReader(rdr);
        m_vacuumd = Vacuumd.getSingleton();
        m_vacuumd.init();
        m_vacuumd.start();
        
        //The rdr is closed by init, too, but doesn't hurt
        rdr.close();
        //Get an alarm in the db
        MockNode node = m_network.getNode(1);
        m_eventd.processEvent(node.createDownEvent());
        Thread.sleep(200);
        
    }

    protected void tearDown() throws Exception {
        assertTrue("Unexpected WARN or ERROR msgs in Log!", MockUtil.noWarningsOrHigherLogged());
        m_vacuumd.stop();
        super.tearDown();
    }
    
    public final void testGetAutomations() {
        assertEquals(2, VacuumdConfigFactory.getInstance().getAutomations().size());
    }
    
    public final void testGetTriggers() {
        assertEquals(2,VacuumdConfigFactory.getInstance().getTriggers().size());
    }
    
    public final void testGetActions() {
        
        AutoProcessor ap = new AutoProcessor();
        assertEquals(2,VacuumdConfigFactory.getInstance().getActions().size());
        assertEquals(2, ap.getTokenCount(((Action)((ArrayList)VacuumdConfigFactory.getInstance().getActions()).get(0)).getStatement().getContent()));
    }
    
    public final void testGetTrigger() {
        assertNotNull(VacuumdConfigFactory.getInstance().getTrigger("trigger1"));
        assertEquals(2, VacuumdConfigFactory.getInstance().getTrigger("trigger1").getRowCount());
        assertEquals(">", VacuumdConfigFactory.getInstance().getTrigger("trigger1").getOperator());
        assertNotNull(VacuumdConfigFactory.getInstance().getTrigger("trigger2"));
    }
    
    public final void testGetAction() {
        assertNotNull(VacuumdConfigFactory.getInstance().getAction("action1"));
        assertNotNull(VacuumdConfigFactory.getInstance().getAction("action2"));
    }
    
    public final void testGetAutomation() {
        assertNotNull(VacuumdConfigFactory.getInstance().getAutomation("auto1"));
        assertNotNull(VacuumdConfigFactory.getInstance().getAutomation("auto2"));
    }

    public final void testRunTrigger() throws InterruptedException {
        
        //Get all the triggers defined in the config
        ArrayList triggers = (ArrayList)VacuumdConfigFactory.getInstance().getTriggers();
        assertEquals(2, triggers.size());

        Querier q = null;

        MockUtil.println("Running trigger query: "+((Trigger)triggers.get(0)).getStatement().getContent());
        q = new Querier(m_db, ((Trigger)triggers.get(0)).getStatement().getContent());
        q.execute();
        assertEquals(1, q.getCount());
        
    }
    
    public final void testRunAutomation() throws SQLException {

        ArrayList autos = (ArrayList)VacuumdConfigFactory.getInstance().getAutomations();
        Automation auto = (Automation)autos.get(0);
        
        AutoProcessor ap = new AutoProcessor();
        ap.setAutomation(auto);
        //ap.run();
        assertTrue(ap.runAutomation(auto));
        
        Querier q = new Querier(m_db, "select * from alarms");
        q.execute();
        assertEquals(0, q.getCount());

    }

    public void testGetTokenizedColumns() {
        
        AutoProcessor ap = new AutoProcessor();
        
        ArrayList actions = (ArrayList)VacuumdConfigFactory.getInstance().getActions();
        Collection tokens = ap.getTokenizedColumns(((Action)actions.get(0)).getStatement().getContent());

        //just this for now
        assertFalse(tokens.isEmpty());
        
    }
    
    public final void testPauseResume() throws InterruptedException {
        
        Thread.sleep(200);
        assertEquals(Fiber.RUNNING, m_vacuumd.getStatus());
        
        m_vacuumd.pause();
        assertEquals(PausableFiber.PAUSED, m_vacuumd.getStatus());
        
        m_vacuumd.resume();
        Thread.sleep(500);
        assertEquals(Fiber.RUNNING, m_vacuumd.getStatus());
    }

    public final void testGetName() {
        assertEquals("OpenNMS.Vacuumd", m_vacuumd.getName());
    }

    public final void testRunUpdate() {
        //TODO Implement runUpdate().
    }
    
}
