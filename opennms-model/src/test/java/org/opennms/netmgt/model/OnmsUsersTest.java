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

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class OnmsUsersTest extends XmlTestNoCastor<OnmsUserList> {

    public OnmsUsersTest(final OnmsUserList sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final OnmsUserList userList = new OnmsUserList();

        final OnmsUser user = new OnmsUser();
        user.setFullName("Foo Barson");
        user.setUsername("foo");
        user.setEmail("foo@example.com");
        userList.add(user);

        final OnmsUser otherUser = new OnmsUser();
        otherUser.setFullName("Blah Blahtonen");
        otherUser.setUsername("blah");
        otherUser.setEmail("blah@example.com");
        userList.add(otherUser);

        return Arrays.asList(new Object[][] {
            {
                new OnmsUserList(),
                "<users offset=\"0\"></users>"
            },
            {
                userList,
                "<users count=\"2\" totalCount=\"2\" offset=\"0\">"
                + "<user>"
                + "  <user-id>foo</user-id>"
                + "  <full-name>Foo Barson</full-name>"
                + "  <email>foo@example.com</email>"
                + "</user>"
                + "<user>"
                + "  <user-id>blah</user-id>"
                + "  <full-name>Blah Blahtonen</full-name>"
                + "  <email>blah@example.com</email>"
                + "</user>"
                + "</users>"
            }
        });
    }


}
