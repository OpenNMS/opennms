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
package org.opennms.netmgt.config;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.notifd.mock.MockGroupManager;
import org.opennms.netmgt.notifd.mock.MockUserManager;

import junit.framework.TestCase;

public class UserManagerTest extends TestCase {
    
    public static final String GROUP_MANAGER = "<?xml version=\"1.0\"?>\n" + 
    "<groupinfo>\n" + 
    "    <header>\n" + 
    "        <rev>1.3</rev>\n" + 
    "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
    "        <mstation>dhcp-219.internal.opennms.org</mstation>\n" + 
    "    </header>\n" + 
    "    <groups>\n" + 
    "        <group>\n" + 
    "            <name>InitialGroup</name>\n" + 
    "            <comments>The group that gets notified first</comments>\n" + 
    "            <user>admin</user>" + 
    "            <user>brozow</user>" + 
    "        </group>\n" + 
    "        <group>\n" + 
    "            <name>EscalationGroup</name>\n" + 
    "            <comments>The group things escalate to</comments>\n" +
    "            <user>brozow</user>" + 
    "            <user>david</user>" + 
    "        </group>\n" + 
    "        <group>\n" + 
    "            <name>UpGroup</name>\n" + 
    "            <comments>The group things escalate to</comments>\n" +
    "            <user>upUser</user>" + 
    "        </group>\n" + 
    "    </groups>\n" + 
    "</groupinfo>\n" + 
    "";
public static final String USER_MANAGER = "<?xml version=\"1.0\"?>\n" + 
    "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\">\n" + 
    "   <header>\n" + 
    "       <rev>.9</rev>\n" + 
    "           <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
    "       <mstation>master.nmanage.com</mstation>\n" + 
    "   </header>\n" + 
    "   <users>\n" + 
    "       <user>\n" + 
    "           <user-id>brozow</user-id>\n" + 
    "           <full-name>Mathew Brozowski</full-name>\n" + 
    "           <user-comments>Test User</user-comments>\n" +
    "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
    "           <contact type=\"email\" info=\"brozow@opennms.org\"/>\n" + 
    "       </user>\n" + 
    "       <user>\n" + 
    "           <user-id>admin</user-id>\n" + 
    "           <full-name>Administrator</full-name>\n" + 
    "           <user-comments>Default administrator, do not delete</user-comments>\n" +
    "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
    "           <contact type=\"email\" info=\"admin@opennms.org\"/>\n" + 
    "       </user>\n" + 
    "       <user>\n" + 
    "           <user-id>upUser</user-id>\n" + 
    "           <full-name>User that receives up notifications</full-name>\n" + 
    "           <user-comments>Default administrator, do not delete</user-comments>\n" +
    "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
    "           <contact type=\"email\" info=\"up@opennms.org\"/>\n" + 
    "       </user>\n" + 
    "       <user>\n" + 
    "           <user-id>david</user-id>\n" + 
    "           <full-name>David Hustace</full-name>\n" + 
    "           <user-comments>A cool dude!</user-comments>\n" + 
    "           <password>18126E7BD3F84B3F3E4DF094DEF5B7DE</password>\n" + 
    "           <contact type=\"email\" info=\"david@opennms.org\"/>\n" + 
    "           <contact type=\"numericPage\" info=\"6789\" serviceProvider=\"ATT\"/>\n" + 
    "           <contact type=\"textPage\" info=\"9876\" serviceProvider=\"Sprint\"/>\n" + 
    "           <duty-schedule>MoTuWeThFrSaSu800-2300</duty-schedule>\n" + 
    "       </user>\n" + 
    "   </users>\n" + 
    "</userinfo>\n" + 
    "";
private MockGroupManager m_groupManager;
private MockUserManager m_userManager;


    public static void main(String[] args) {
        junit.textui.TestRunner.run(UserManagerTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        m_groupManager = new MockGroupManager(GROUP_MANAGER);
        m_userManager = new MockUserManager(m_groupManager, USER_MANAGER);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetUserNames() throws Exception {
        List userNameList = m_userManager.getUserNames();
        assertEquals(4, userNameList.size());
        assertTrue(userNameList.contains("admin"));
        assertTrue(userNameList.contains("brozow"));
        assertTrue(userNameList.contains("upUser"));
        assertTrue(userNameList.contains("david"));
    }
    
    public void testSaveUser() throws Exception {
        User brozow = m_userManager.getUser("brozow");
        
        Date night = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("21-FEB-2005 23:59:56");
        Calendar nightCal = Calendar.getInstance();
        nightCal.setTime(night);

        Date day = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("21-FEB-2005 11:59:56");
        Calendar dayCal = Calendar.getInstance();
        dayCal.setTime(day);
        
        // initial has no duty schedule so always on duty
        assertTrue(m_userManager.isUserOnDuty("brozow", dayCal));
        assertTrue(m_userManager.isUserOnDuty("brozow", nightCal));

        brozow.addDutySchedule("MoTuWeThFr0900-1700");
        m_userManager.saveUser("brozow", brozow);
        
        // now user is on duty only from 9-5
        assertTrue(m_userManager.isUserOnDuty("brozow", dayCal));
        assertFalse(m_userManager.isUserOnDuty("brozow", nightCal));
        
    }
    
    

}
