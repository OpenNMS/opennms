/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import org.junit.Test;

public class DutyScheduleLoadTest {

    private static final String GROUPS_XML =
            "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">\n"
            + "  <header><rev>1.0</rev><created>now</created><mstation>test</mstation></header>\n"
            + "  <groups>\n"
            + "    <group><name>a</name><comments>c</comments>"
            + "<duty-schedule>garbage</duty-schedule><duty-schedule>MoTuWeThFr800-1700</duty-schedule></group>\n"
            + "    <group><name>b</name><comments>c</comments><duty-schedule>Xx800-1700</duty-schedule></group>\n"
            + "  </groups>\n"
            + "</groupinfo>";

    private static final String USERS_XML =
            "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\">\n"
            + "  <header><rev>1.0</rev><created>now</created><mstation>test</mstation></header>\n"
            + "  <users>\n"
            + "    <user><user-id>a</user-id><full-name>A</full-name><password>x</password>"
            + "<duty-schedule>garbage</duty-schedule><duty-schedule>MoTuWeThFr800-1700</duty-schedule></user>\n"
            + "    <user><user-id>b</user-id><full-name>B</full-name><password>x</password><duty-schedule>Xx800-1700</duty-schedule></user>\n"
            + "  </users>\n"
            + "</userinfo>";

    private static final class TestGroupManager extends GroupManager {
        TestGroupManager(final String xml) throws IOException {
            parseXml(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public void update() {
        }

        @Override
        protected void saveXml(final String data) {
        }
    }

    private static final class TestUserManager extends UserManager {
        TestUserManager(final GroupManager gm, final String xml) {
            super(gm);
            parseXML(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        protected void saveXML(final String writerString) {
        }

        @Override
        protected void doUpdate() throws IOException, FileNotFoundException {
        }

        @Override
        public boolean isUpdateNeeded() {
            return false;
        }

        @Override
        public long getLastModified() {
            return 0;
        }

        @Override
        public long getFileSize() {
            return 0;
        }

        @Override
        public void reload() {
        }
    }

    private static Calendar monday(final int hour) {
        final Calendar c = Calendar.getInstance();
        c.set(2024, Calendar.JANUARY, 1, hour, 0, 0); // 2024-01-01 is a Monday
        return c;
    }

    @Test
    public void testUnparseableDutyScheduleIsSkippedOnLoad() throws Exception {
        final UserManager um = new TestUserManager(new TestGroupManager(GROUPS_XML), USERS_XML);
        assertTrue(um.isUserOnDuty("a", monday(9)));
        assertFalse(um.isUserOnDuty("a", monday(20)));
        assertTrue(um.isUserOnDuty("b", monday(9)));
        assertTrue(um.isUserOnDuty("b", monday(20)));
    }

    @Test
    public void testUnparseableGroupDutyScheduleIsSkippedOnLoad() throws Exception {
        final GroupManager gm = new TestGroupManager(GROUPS_XML);
        assertTrue(gm.isGroupOnDuty("a", monday(9)));
        assertFalse(gm.isGroupOnDuty("a", monday(20)));
        assertTrue(gm.isGroupOnDuty("b", monday(9)));
        assertTrue(gm.isGroupOnDuty("b", monday(20)));
    }
}
