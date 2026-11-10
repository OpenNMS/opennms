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
