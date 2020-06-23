/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.groups;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class GroupsTest extends XmlTestNoCastor<Groupinfo> {

    public GroupsTest(final Groupinfo sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/groups.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Groupinfo gi = new Groupinfo();
        gi.setHeader(new Header("1.3", "Wednesday, February 6, 2002 10:10:00 AM EST", "dhcp-219.internal.opennms.org"));

        final Group admin = new Group();
        admin.setName("Admin");
        admin.setComments("The administrators");
        admin.addUser("admin");
        admin.addDutySchedule("\n    MoTuWeThFrSaSu800-2300\n    "); // make sure duty schedules get trimmed properly
        gi.addGroup(admin);

        final Group remoting = new Group();
        remoting.setName("Remoting Users");
        remoting.setComments("Users with access for submitting remote poller management data.");
        remoting.addUser("remoting");
        gi.addGroup(remoting);

        final Groupinfo gi2 = new Groupinfo();
        gi2.setHeader(gi.getHeader());
        final Group admin2 = new Group("Admin", "admin");
        admin2.setDutySchedules(Arrays.asList(" \n MoSa1300-1500 \n "));
        gi2.addGroup(admin2);

        return Arrays.asList(new Object[][] {
            {
                gi,
                "<groupinfo  xmlns=\"http://xmlns.opennms.org/xsd/groups\">\n" + 
                "        <header>\n" + 
                "                <rev>1.3</rev>\n" + 
                "                <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "                <mstation>dhcp-219.internal.opennms.org</mstation>\n" + 
                "        </header>\n" + 
                "        <groups>\n" + 
                "                <group>\n" + 
                "                        <name>Admin</name>\n" + 
                "                        <comments>The administrators</comments>\n" + 
                "                        <user>admin</user>\n" + 
                "                        <duty-schedule>MoTuWeThFrSaSu800-2300</duty-schedule>\n" +
                "                </group>\n" + 
                "                <group>\n" + 
                "                        <name>Remoting Users</name>\n" + 
                "                        <comments>Users with access for submitting remote poller management data.</comments>\n" + 
                "                        <user>remoting</user>\n" + 
                "                </group>\n" + 
                "        </groups>\n" + 
                "</groupinfo>"
            },
            {
                gi2,
                "<groupinfo  xmlns=\"http://xmlns.opennms.org/xsd/groups\">\n" +
                "        <header>\n" +
                "                <rev>1.3</rev>\n" +
                "                <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" +
                "                <mstation>dhcp-219.internal.opennms.org</mstation>\n" +
                "        </header>\n" +
                "        <groups>\n" +
                "                <group>\n" +
                "                        <name>Admin</name>\n" +
                "                        <user>admin</user>\n" +
                "                        <duty-schedule>MoSa1300-1500</duty-schedule>\n" +
                "                </group>\n" +
                "        </groups>\n" +
                "</groupinfo>"
            }
        });
    }
}
