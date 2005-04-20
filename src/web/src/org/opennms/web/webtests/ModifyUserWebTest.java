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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.Unmarshaller;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.common.BasicSchedule;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.OncallSchedule;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.config.users.Users;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.web.admin.users.parsers.DutySchedule;

import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class ModifyUserWebTest extends OpenNMSWebTestCase {
    
    private MockNetwork m_network;

    private MockDatabase m_db;

    private ServletRunner m_servletRunner;

    private ServletUnitClient m_servletClient;
    private TestDialogResponder m_testResponder;

    private File m_outagesFile;
    
    private String m_usersFile = "../../etc/users.xml";
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ScheduleEditorWebTest.class);
    }

    protected void setUp() throws Exception {
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        
        // save of the users.xml file so we can restore it after the test
        copyFile(m_usersFile, m_usersFile+"."+getName()+"-sav");
        
        m_servletRunner = new ServletRunner(new File("WEB-INF/web.xml"));
        ServletUnitClient client = m_servletRunner.newClient();

        m_servletClient = m_servletRunner.newClient();
        
        m_testResponder = new TestDialogResponder();
        m_servletClient.setDialogResponder(m_testResponder);
        
        getTestContext().setBaseUrl("http://localhost:8080/");

        getTestContext().setWebClient(m_servletClient);
        getTestContext().setAuthorization("admin", "OpenNMS Administrator");

        // We need to do this to ensure that the factory reloads the new users.xml file
        UserFactory.init();
        UserFactory.getInstance().reload();


    }

    protected void tearDown() throws Exception {
        m_servletRunner.shutDown();
        copyFile(m_usersFile+"."+getName()+"-sav", m_usersFile);
        (new File(m_usersFile+"."+getName()+"-sav")).delete();
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
    }
    
    public void testListUsers() throws Exception {
        beginAt("/admin/userGroupView/users/list.jsp");
        List users = getCurrentUsers();
        assertUsersList(users);
    }

    private void assertUsersList(List users) {
        getTester().dumpResponse();
        assertTitleEquals("List | User Admin | OpenNMS Web Console");
        assertHeaderPresent("User Configuration", null, new String[] {"Home", "Admin", "Users and Groups", "User List"});
        assertFooterPresent(null);
        assertElementPresent("doNewUser");
        for(int i = 0; i < users.size(); i++) {
            User user = (User)users.get(i);
            String userPrefix = "users("+user.getUserId()+")";
            assertElementPresent(userPrefix+".doDelete");
            assertElementPresent(userPrefix+".doModify");
            assertElementPresent(userPrefix+".doRename");
            assertTextInElement(userPrefix+".doDetails", user.getUserId());
            assertTextInElement(userPrefix+".fullName", user.getFullName());
            assertTextInElement(userPrefix+".email", getContact(user, "email"));
            assertTextInElement(userPrefix+".pagerEmail", getContact(user, "pagerEmail"));
            assertTextInElement(userPrefix+".xmppAddress", getContact(user, "xmppAddress"));
            // Other contacts could/should be here on this page?
            assertTextInElement(userPrefix+".userComments", user.getUserComments());
        }
    }
    
    public void testCancelAddUser() throws Exception {
        beginAt("/admin/userGroupView/users/list.jsp");
        List users = getCurrentUsers();
        
        clickLink("doNewUser");
        
        assertNewUserPage("", "", "");
        
        // cancel the page.. should go back to users list
        clickButton("doCancel");

        // after canceling assert the the users list page has the same contents as originally
        assertUsersList(users);
    }

    public void testPreventAdminUserDup() throws Exception {

        beginAt("/admin/userGroupView/users/list.jsp");
        
        // try again this time try to create named admin
        clickLink("doNewUser");
        
        setWorkingForm("newUserForm");
        setFormElement("userID", "Admin");
        setFormElement("pass1", "password");
        setFormElement("pass2", "password");
        clickButton("doOK");
        verifyAlert("The user ID 'Admin' cannot be used. It may be confused with the administration user ID 'admin'.");
        
        assertNewUserPage("Admin", "password", "password");
    }
    
    public void testMistypedPasswords() throws Exception {
        beginAt("/admin/userGroupView/users/list.jsp");
        
        // try again this time try to create named admin
        clickLink("doNewUser");
        
        setWorkingForm("newUserForm");
        setFormElement("userID", "newUser");
        setFormElement("pass1", "password");
        setFormElement("pass2", "passwodr");  // note the typo here(It is intentional)
        clickButton("doOK");
        verifyAlert("The two password fields do not match!");
        
        assertNewUserPage("newUser", "", "");
    }
    
    public void testCancelModifyUser() throws Exception {
        List users = getCurrentUsers();
        String userID = "tempuser";
        User user = findUser(userID, users);
        assertNotNull("Unable to find user "+userID, user);
        user = cloneUser(user);

        beginAt("/admin/userGroupView/users/list.jsp");
        
        clickLink("users("+userID+").doModify");
        
        assertModifyUserPage(user);
        
        user.setFullName("Mr. Personality");
        user.setUserComments("That down right rude SOB who expects me to RTFM!");
        setContact(user, "email", "brozow@opennms.org");
        
        fillModifyFormFromUser(user);
        
        clickButton("cancelButton");
        
        assertUsersList(users);
        
        
    }

    public void testModifyUserAndSave() throws Exception {
        List users = getCurrentUsers();
        String userID = "tempuser";
        User user = findUser(userID, users);
        assertNotNull("Unable to find user "+userID, user);
        //user = cloneUser(user);

        beginAt("/admin/userGroupView/users/list.jsp");
        
        clickLink("users("+userID+").doModify");
        
        assertModifyUserPage(user);
        
        user.setFullName("Mr. Personality");
        user.setUserComments("That down right rude SOB who expects me to RTFM!");
        setContact(user, "email", "brozow@opennms.org");
        
        fillModifyFormFromUser(user);
        
        clickButton("saveUserButton");
        
        getTester().dumpResponse();
        
        assertUsersList(users);
        
        
    }
    
    // TODO: add test for modifying dutySchedule
    // TODO: add tests for deleting dutySchedule
    // TODO: add tests for adding dutySchedule
    
    public void testNewUserCancelOnModifyPage() throws Exception {
        beginAt("/admin/userGroupView/users/list.jsp");
        List users = getCurrentUsers();
        
        // try again this time try to create named admin
        clickLink("doNewUser");
        
        setWorkingForm("newUserForm");
        setFormElement("userID", "newUser");
        setFormElement("pass1", "password");
        setFormElement("pass2", "password");  // note the typo here
        clickButton("doOK");
        assertNoAlerts();
        
        User newUser = new User();
        newUser.setUserId("newUser");
        assertModifyUserPage(newUser);
        
        clickButton("cancelButton");
        
        assertUsersList(users);
        
        
    }
    
    private void fillModifyFormFromUser(User user) {
        setWorkingForm("modifyUser");
        setFormElement("fullName", user.getFullName());
        setFormElement("userComments", user.getUserComments());
        setFormElement("email", getContact(user, "email"));
        setFormElement("pemail", getContact(user, "pemail"));
        setFormElement("xmppAddress", getContact(user, "xmppAddress"));
        setFormElement("numericalService", getServiceProvider(user, "numericPage"));
        setFormElement("numericalPin", getContact(user, "numericPage"));
        setFormElement("textService", getServiceProvider(user, "textPage"));
        setFormElement("textPin", getContact(user, "textPage"));
    }


    private void assertModifyUserPage(User user) {
        assertNotNull(user.getUserId());
        assertFalse("".equals(user.getUserId()));
        
        assertTitleEquals("Modify User | User Admin | OpenNMS Web Console");
        assertHeaderPresent("Modify User", null, new String[] {"Home", "Admin", "Users and Groups", "User List", "Modify User"});
        assertFooterPresent(null);

        assertTextPresent("Modify User: "+user.getUserId());
        
        setWorkingForm("modifyUser");
        assertFormElementEquals("userID", user.getUserId());
        assertFormElementEquals("fullName", (user.getFullName() == null ? "" : user.getFullName()));
        assertFormElementEquals("userComments", (user.getUserComments() == null ? "" : user.getUserComments()));
        assertFormElementEquals("email", getContact(user, "email"));
        assertFormElementEquals("pemail", getContact(user, "pemail"));
        assertFormElementEquals("xmppAddress", getContact(user, "xmppAddress"));
        assertFormElementEquals("numericalService", getServiceProvider(user, "numericPage"));
        assertFormElementEquals("numericalPin", getContact(user, "numericPage"));
        assertFormElementEquals("textService", getServiceProvider(user, "textPage"));
        assertFormElementEquals("textPin", getContact(user, "textPage"));
        
        String[] dutySchedules = user.getDutySchedule();
        assertFormElementEquals("dutySchedules", String.valueOf(dutySchedules.length));
        for(int i = 0; i < dutySchedules.length; i++) {
            DutySchedule sched = new DutySchedule(dutySchedules[i]);
            assertFormElementPresent("deleteDuty"+i);
            assertCheckboxSelection("duty"+i+"Mo", sched.hasDay(DutySchedule.MONDAY));
            assertCheckboxSelection("duty"+i+"Tu", sched.hasDay(DutySchedule.TUESDAY));
            assertCheckboxSelection("duty"+i+"We", sched.hasDay(DutySchedule.WEDNESDAY));
            assertCheckboxSelection("duty"+i+"Th", sched.hasDay(DutySchedule.THURSDAY));
            assertCheckboxSelection("duty"+i+"Fr", sched.hasDay(DutySchedule.FRIDAY));
            assertCheckboxSelection("duty"+i+"Sa", sched.hasDay(DutySchedule.SATURDAY));
            assertCheckboxSelection("duty"+i+"Su", sched.hasDay(DutySchedule.SUNDAY));
            assertFormElementEquals("duty"+i+"Begin", String.valueOf(sched.getStartTime()));
            assertFormElementEquals("duty"+i+"End", String.valueOf(sched.getStopTime()));
        }
        
        
        
        // TODO: add OncallSchedule
        //fail("We don't do OncallSchedules yet!");
        
        
        assertButtonPresent("addSchedulesButton");
        assertButtonPresent("removeSchedulesButton");
        assertButtonPresent("saveUserButton");
        assertButtonPresent("cancelButton");
        
        
        
        
        
    }

    private void assertCheckboxSelection(String checkBoxName, boolean isSelected) {
        if (isSelected)
            assertCheckboxSelected(checkBoxName);
        else
            assertCheckboxNotSelected(checkBoxName);
    }

    private User findUser(String userID, List users) {
        for(int i = 0; i < users.size(); i++) {
            User user = (User)users.get(i);
            if (userID.equals(user.getUserId())) return user;
        }
        return null;
    }

    private void assertNoAlerts() {
        String alert = m_servletClient.getNextAlert();
        assertNull("Unexpected alert: '"+alert+"'", alert);
    }

    private void assertNewUserPage(String userID, String pass1, String pass2) {
        assertTitleEquals("New User Info | User Admin | OpenNMS Web Console");
        assertHeaderPresent("New User", null, new String[] {"Home", "Admin", "Users and Groups", "User List", "New User"});
        assertFooterPresent(null);
        assertFormPresent("newUserForm");
        assertFormElementEquals("userID", userID);
        assertFormElementEquals("pass1", pass1);
        assertFormElementEquals("pass2", pass2);
        assertButtonPresent("doOK");
        assertButtonPresent("doCancel");
    }
    
    private void verifyAlert(String alertText) {
        assertEquals(alertText, m_servletClient.popNextAlert());
    }

    // TODO: Edit times in place
    // TODO: Test with empty schedule list
    // TODO: have the jsp modify the schedule and save it
    // TODO: test loading data from factory
    // TODO: test inclusion in user page

    private String getServiceProvider(User user, String contactType) {
        Contact[] contacts = user.getContact();
        for(int i = 0; i < contacts.length; i++ ) {
            Contact contact = contacts[i];
            if (contactType.equals(contact.getType())) {
                return contact.getServiceProvider();
            }
        }
        return "";
    }

    private void setServiceProvider(User user, String contactType, String svcProvider) {
        Contact[] contacts = user.getContact();
        for(int i = 0; i < contacts.length; i++ ) {
            Contact contact = contacts[i];
            if (contactType.equals(contact.getType())) {
                contact.setServiceProvider(svcProvider);
                return;
            }
        }
        
        Contact contact = new Contact();
        contact.setType(contactType);
        contact.setServiceProvider(svcProvider);
        user.addContact(contact);
        
    }

    private String getContact(User user, String contactType) {
        Contact[] contacts = user.getContact();
        for(int i = 0; i < contacts.length; i++ ) {
            Contact contact = contacts[i];
            if (contactType.equals(contact.getType())) {
                return contact.getInfo();
            }
        }
        return "";
    }
    
    private void setContact(User user, String contactType, String info) {
        Contact[] contacts = user.getContact();
        for(int i = 0; i < contacts.length; i++ ) {
            Contact contact = contacts[i];
            if (contactType.equals(contact.getType())) {
                contact.setInfo(info);
                return;
            }
        }
        
        // if we got here then we didn't find a contact of this type
        Contact newContact = new Contact();
        newContact.setType(contactType);
        newContact.setInfo(info);
        user.addContact(newContact);
    }



    private BasicSchedule[] getCurrentSchedules() throws Exception {
        FileReader reader = new FileReader(m_outagesFile);
        Outages outages = (Outages)Unmarshaller.unmarshal(Outages.class, reader);
        reader.close();
        return outages.getOutage();
    }
    
    private List getCurrentUsers() throws Exception {
        FileReader reader = new FileReader(m_usersFile);
        Userinfo userinfo = (Userinfo) Unmarshaller.unmarshal(Userinfo.class, reader);
        Users users = userinfo.getUsers();
        return new ArrayList(Arrays.asList(users.getUser()));
    
    }
    
    private User cloneUser(User user) {
        User newUser = new User();
        newUser.setFullName(user.getFullName());
        newUser.setPassword(user.getPassword());
        newUser.setUserComments(user.getUserComments());
        newUser.setUserId(user.getUserId());
        
        newUser.clearDutySchedule();
        newUser.setDutySchedule(user.getDutySchedule());
        
        newUser.clearContact();
        List contacts = user.getContactCollection();
        for (Iterator it = contacts.iterator(); it.hasNext();) {
            Contact contact = (Contact) it.next();
            newUser.addContact(cloneContact(contact));
        }
        
        newUser.clearOncallSchedule();
        List schedules = user.getOncallScheduleCollection();
        for (Iterator it = schedules.iterator(); it.hasNext();) {
            OncallSchedule sched = (OncallSchedule) it.next();
            newUser.addOncallSchedule(cloneOncallSchedule(sched));
        }
        
        return newUser;
        
    }

    private OncallSchedule cloneOncallSchedule(OncallSchedule sched) {
        OncallSchedule newSched = new OncallSchedule();
        newSched.setName(sched.getName());
        newSched.setType(sched.getType());
        
        newSched.clearTime();
        List times = sched.getTimeCollection();
        for (Iterator it = times.iterator(); it.hasNext();) {
            Time time = (Time) it.next();
            newSched.addTime(cloneTime(time));
        }
        
        return newSched;
    }

    private Time cloneTime(Time time) {
        Time newTime = new Time();
        newTime.setBegins(time.getBegins());
        newTime.setDay(time.getDay());
        newTime.setEnds(time.getEnds());
        return newTime;
    }

    private Contact cloneContact(Contact contact) {
        Contact newContact = new Contact();
        newContact.setInfo(contact.getInfo());
        newContact.setType(contact.getType());
        newContact.setServiceProvider(contact.getServiceProvider());
        return newContact;
    }
    
    



}
