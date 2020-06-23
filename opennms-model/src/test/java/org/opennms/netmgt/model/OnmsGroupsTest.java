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
