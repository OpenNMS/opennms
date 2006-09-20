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
package org.opennms.web.webtests;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import net.sourceforge.jwebunit.WebTestCase;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.opennms.netmgt.config.common.BasicSchedule;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.test.mock.MockUtil;

import com.meterware.httpunit.BlockElement;
import com.meterware.httpunit.WebTable;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class ScheduleEditorWebTest extends WebTestCase {
    
    private MockNetwork m_network;

    private MockDatabase m_db;

    private ServletRunner m_servletRunner;

    private ServletUnitClient m_servletClient;
    private TestDialogResponder m_testResponder;

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
        
        m_testResponder = new TestDialogResponder();
        m_servletClient.setDialogResponder(m_testResponder);
        
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
        m_outagesFile.deleteOnExit();
        
        System.err.println(m_outagesFile);
        FileWriter writer = new FileWriter(m_outagesFile);
        Marshaller.marshal(outages, writer);
        writer.close();

    }

    protected void tearDown() throws Exception {
        m_servletRunner.shutDown();
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
    }
    
    public void testScheduleDisplay() throws Exception {
        beginAt("/admin/schedule/schedule-editor?file=" + m_outagesFile.getAbsolutePath());
        assertTitleEquals("Schedule Editor");
        getTester().dumpResponse();
        checkScheduleTable(getCurrentSchedules());
        
        // assert that the new schedule form exists
        assertFormPresent("newScheduleForm");
        setWorkingForm("newScheduleForm");
        assertFormElementEquals("op", "newSchedule");
        assertFormElementPresent("name");
        assertFormElementPresent("type");
        assertFormElementPresent("submit");
        
        
        
        
    }
    
    public void testDelete() throws Exception {
        beginAt("/admin/schedule/schedule-editor?file=" + m_outagesFile.getAbsolutePath());
        assertTitleEquals("Schedule Editor");
        BasicSchedule[] schedules = getCurrentSchedules();
        
        int deleteIndex = 1;
        String deleteForm = "schedule["+deleteIndex+"].deleteForm";
        
        // first test canceling the confirm dialog
        m_testResponder.anticipateConfirmation("Are you sure you wish to delete this schedule?", false);
        setWorkingForm(deleteForm);
        submit();
        
        // since we canceled everything should be the same
        BasicSchedule[] canceledSchedules = getCurrentSchedules();
        assertEquals(schedules.length, canceledSchedules.length);
        
        // noew test ok'ing the confirm dialog
        m_testResponder.anticipateConfirmation("Are you sure you wish to delete this schedule?", true);
        setWorkingForm(deleteForm);
        submit();
        
        // since we canceled everything should be the same
        BasicSchedule[] newSchedules = getCurrentSchedules();
        assertEquals(schedules.length-1, newSchedules.length);
        
        int newIndex = 0;
        for(int oldIndex = 0; oldIndex < schedules.length; oldIndex++) {
            if (oldIndex == deleteIndex) continue;
            assertEquals(schedules[oldIndex].getName(), newSchedules[newIndex].getName());
            newIndex++;
        }
        
    }
    
    public void testEdit() throws Exception {
        beginAt("/admin/schedule/schedule-editor?file=" + m_outagesFile.getAbsolutePath());
        assertTitleEquals("Schedule Editor");
        BasicSchedule[] schedules = getCurrentSchedules();
        
        int editIndex = 1;
        String editForm = "schedule["+editIndex+"].editForm";

        setWorkingForm(editForm);
        submit();
        
        // TODO: This should go back to the main page
        checkEditPage(schedules[editIndex], editIndex);
        getTester().dumpResponse();
        
        // since we canceled everything should be the same
        BasicSchedule[] canceledSchedules = getCurrentSchedules();
        assertEquals(schedules.length, canceledSchedules.length);
        
    }
    
    public void testNewSchedule() throws Exception {
        beginAt("/admin/schedule/schedule-editor?file=" + m_outagesFile.getAbsolutePath());
        assertTitleEquals("Schedule Editor");
        BasicSchedule[] schedules = getCurrentSchedules();
        
        setWorkingForm("newScheduleForm");
        submit();
        
        List newSchedules = new ArrayList(Arrays.asList(schedules));
        BasicSchedule sched = new Outage();
        sched.setName("Schedule Name");
        sched.setType("specific");
        Time time = new Time();
        time.setBegins(new Date().toString());
        time.setEnds(new Date().toString());
        sched.addTime(time);
        
        newSchedules.add(sched);
        
        checkScheduleTable((BasicSchedule[]) newSchedules.toArray(new BasicSchedule[newSchedules.size()]));
        
        
        
    }
    
    public void testAddTime() throws Exception {
        beginAt("/admin/schedule/schedule-editor?file=" + m_outagesFile.getAbsolutePath());
        assertTitleEquals("Schedule Editor");
        BasicSchedule[] schedules = getCurrentSchedules();
        
        int index = 1;
        String addTimeForm = "schedule["+index+"].addTimeForm";

        setWorkingForm(addTimeForm);
        submit();
        
    }

    // TODO: Edit times in place
    // TODO: Test with empty schedule list
    // TODO: have the jsp modify the schedule and save it
    // TODO: test loading data from factory
    // TODO: test inclusion in user page


    private void checkEditPage(BasicSchedule schedule, int index) {
        getTester().dumpResponse();
        assertTitleEquals("Edit Schedule");
        assertTextPresent("Name:");
        assertTextPresent(schedule.getName());
        assertTextPresent("Type:");
        assertTextPresent(schedule.getType());
        
        // TODO: save changes
        // TODO: cancel changes
        
        // TODO: Add readonly name and type
        // TODO: Display times
        // TODO: Edit times
        // TODO: Add Times
        // TODO: Delete Times
        
    }

    private void checkScheduleTable(BasicSchedule[] schedules) {
        WebTable table = getDialog().getWebTableBySummaryOrId("schedules");
        assertNotNull(table);
        assertEquals(schedules.length, table.getRowCount());
        for (int i = 0; i < schedules.length; i++) {
            BasicSchedule schedule = schedules[i];
            
            String schedId = "schedule["+i+"]";
            assertTextInElement(schedId+".name", schedule.getName());
            assertTextInElement(schedId+".type", schedule.getType());

            assertElementPresent(schedId+".times");
            checkScheduleTimes(schedule, schedId, table.getTableCellWithID(schedId+".times"));
            
            assertScheduleFormPresent(i, "addTime");
            assertScheduleFormPresent(i, "edit");
            assertScheduleFormPresent(i, "delete");

        }
    }

    private void assertScheduleFormPresent(int scheduleIndex, String op) {
        String schedId = "schedule["+scheduleIndex+"]";
        assertFormPresent(schedId+"."+op+"Form");
        setWorkingForm(schedId+"."+op+"Form");
        assertFormElementEquals("op", op);
        assertFormElementEquals("scheduleIndex", String.valueOf(scheduleIndex));
        assertFormElementPresent("submit");
    }

    private void checkScheduleTimes(BasicSchedule schedule, String schedId, BlockElement timesCell) {
        Time[] times = schedule.getTime();
        for(int i = 0; i < times.length; i++) {
            Time time = times[i];
            String idPrefix = schedId+".time["+i+"]";
            if ("specific".equals(schedule.getType())) {
                assertElementNotPresent(idPrefix+".day");
            } else {
                assertTextInElement(idPrefix+".day", time.getDay());
            }
            assertTextInElement(idPrefix+".begins", time.getBegins());
            assertTextInElement(idPrefix+".ends", time.getEnds());
            
        }
    }
    
    private BasicSchedule[] getCurrentSchedules() throws Exception {
        FileReader reader = new FileReader(m_outagesFile);
        Outages outages = (Outages)Unmarshaller.unmarshal(Outages.class, reader);
        reader.close();
        return outages.getOutage();
    }



}
