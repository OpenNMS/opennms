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
package org.opennms.netmgt.model;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class OnmsGroupsTest extends XmlTestNoCastor<OnmsGroupList> {

    public OnmsGroupsTest(final OnmsGroupList sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final OnmsGroupList groupList = new OnmsGroupList();

        final OnmsGroup group = new OnmsGroup();
        group.setName("Admin");
        group.setUsers(Collections.singletonList("admin"));
        groupList.add(group);

        final OnmsGroup otherGroup = new OnmsGroup();
        otherGroup.setName("Blah");
        otherGroup.setComments("Comments!!! OMG!!!1!1one");
        otherGroup.setUsers(Collections.singletonList("chewie"));
        groupList.add(otherGroup);

        return Arrays.asList(new Object[][] {
            {
                new OnmsGroupList(),
                "<groups offset=\"0\"></groups>"
            },
            {
                groupList,
                "<groups count=\"2\" totalCount=\"2\" offset=\"0\">"
                + "<group>"
                + "  <name>Admin</name>"
                + "  <user>admin</user>"
                + "</group>"
                + "<group>"
                + "  <name>Blah</name>"
                + "  <comments>Comments!!! OMG!!!1!1one</comments>"
                + "  <user>chewie</user>"
                + "</group>"
                + "</groups>"
            }
        });
    }


}
