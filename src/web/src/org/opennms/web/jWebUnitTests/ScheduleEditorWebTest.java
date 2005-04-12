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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.web.jWebUnitTests;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import net.sourceforge.jwebunit.WebTestCase;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.opennms.netmgt.config.common.BasicSchedule;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockUtil;

import com.meterware.httpunit.BlockElement;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebTable;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class ScheduleEditorWebTest extends WebTestCase {

    private MockNetwork m_network;

    private MockDatabase m_db;

    private ServletRunner m_servletRunner;

    private ServletUnitClient m_servletClient;

    private File m_outagesFile;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ScheduleEditorWebTest.class);
    }

    protected void setUp() throws Exception {
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");

        // m_servletRunner = new ServletRunner();
        // m_servletRunner.registerServlet("/admin/schedule/schedule-editor",
        // ScheduleEditorServlet.class.getName());
        // m_servletClient = m_servletRunner.newClient();
        // getTestContext().setBaseUrl("http://localhost:8080/");

        m_servletRunner = new ServletRunner(new File("WEB-INF/web.xml"));
        ServletUnitClient client = m_servletRunner.newClient();

        m_servletClient = m_servletRunner.newClient();
        getTestContext().setBaseUrl("http://localhost:8080/");

        getTestContext().setWebClient(m_servletClient);
        getTestContext().setAuthorization("admin", "admin");

        Outages outages = new Outages();

        Outage outage = new Outage();
        outage.setName("outage1");
        outage.setType("weekly");
        
        Time time = new Time();
        time.setDay("sunday");
        time.setBegins("00:00:00");
        time.setEnds("23:59:59");
        outage.addTime(time);
        
        Time time2 = new Time();
        time2.setDay("wednesday");
        time2.setBegins("19:00:00");
        time2.setEnds("23:00:00");
        outage.addTime(time2);
        
        outages.addOutage(outage);

        Outage outage2 = new Outage();
        outage2.setName("outage2");
        outage2.setType("monthly");
        
        Time timeA = new Time();
        timeA.setDay("5");
        timeA.setBegins("05:00:00");
        timeA.setEnds("07:00:00");
        outage2.addTime(timeA);

        outages.addOutage(outage2);

        Outage outage3 = new Outage();
        outage3.setName("outage3");
        outage3.setType("specific");
        
        Time timeX = new Time();
        timeX.setBegins("07-Apr-2005 10:00:00");
        timeX.setEnds("09-Apr-2005 11:00:00");
        outage3.addTime(timeX);
        
        outages.addOutage(outage3);
        
        m_outagesFile = File.createTempFile("outages-", ".xml");
        System.err.println(m_outagesFile);
        FileWriter writer = new FileWriter(m_outagesFile);
        Marshaller.marshal(outages, writer);
        writer.close();

    }

    protected void tearDown() throws Exception {
        m_servletRunner.shutDown();
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
    }
    
    private BasicSchedule[] getCurrentSchedules() throws Exception {
        FileReader reader = new FileReader(m_outagesFile);
        Outages outages = (Outages)Unmarshaller.unmarshal(Outages.class, reader);
        reader.close();
        return outages.getOutage();
    }

    public void testScheduleDisplay() throws Exception {
        beginAt("/admin/schedule/schedule-editor?file=" + m_outagesFile.getAbsolutePath());
        assertTitleEquals("Schedule Editor");
        getTester().dumpResponse();
        checkScheduleTable(getCurrentSchedules());
        
    }
    
    public void testDeleteLink() throws Exception {
        beginAt("/admin/schedule/schedule-editor?file=" + m_outagesFile.getAbsolutePath());
        assertTitleEquals("Schedule Editor");
        BasicSchedule[] schedules = getCurrentSchedules();
        checkScheduleTable(schedules);
        
        clickLink("sched.2.delete");
        getTester().dumpResponse();
        
        BasicSchedule[] newSchedules = getCurrentSchedules();
        assertEquals(schedules.length-1, newSchedules.length);
    }

    private void checkScheduleTable(BasicSchedule[] schedules) {
        WebTable table = getDialog().getWebTableBySummaryOrId("schedules");
        assertNotNull(table);
        assertEquals(schedules.length, table.getRowCount());
        for (int i = 0; i < schedules.length; i++) {
            BasicSchedule schedule = schedules[i];
            
            String schedId = "sched."+(i+1);
            assertTextInElement(schedId+".name", schedule.getName());
            assertTextInElement(schedId+".type", schedule.getType());

            assertElementPresent(schedId+".times");
            checkScheduleTimes(schedule, schedId, table.getTableCellWithID(schedId+".times"));
            
            assertLinkPresent(schedId+".edit");
            assertLinkPresent(schedId+".delete");
            
            
        }
        
        
    }
    
    

    private void checkScheduleTimes(BasicSchedule schedule, String schedId, BlockElement timesCell) {
        Time[] times = schedule.getTime();
        for(int i = 0; i < times.length; i++) {
            Time time = times[i];
            String idPrefix = schedId+".time."+(i+1);
            if ("specific".equals(schedule.getType())) {
                assertElementNotPresent(idPrefix+".day");
            } else {
                assertTextInElement(idPrefix+".day", time.getDay());
            }
            assertTextInElement(idPrefix+".begins", time.getBegins());
            assertTextInElement(idPrefix+".ends", time.getEnds());
            
        }
    }

    //.TODO: Delete an entry and make sure it gets saved 
    // TODO: Make sure a confirmation happens before deletng
    // TODO: have the jsp modify the schedule and save it
}
