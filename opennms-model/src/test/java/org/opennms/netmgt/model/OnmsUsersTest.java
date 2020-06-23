/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
