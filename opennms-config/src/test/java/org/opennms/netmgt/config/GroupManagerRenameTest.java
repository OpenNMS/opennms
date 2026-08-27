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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class GroupManagerRenameTest {

    private static final String GROUPS_XML =
            "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">\n"
            + "  <header><rev>1.0</rev><created>now</created><mstation>test</mstation></header>\n"
            + "  <groups>\n"
            + "    <group><name>oldgroup</name><comments>c</comments><user>admin</user></group>\n"
            + "  </groups>\n"
            + "</groupinfo>";

    /** A GroupManager with no file backing whose save can be flipped to fail. */
    private static final class TestGroupManager extends GroupManager {
        private boolean m_failSave = false;

        TestGroupManager(final String xml) throws IOException {
            parseXml(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        }

        void failNextSave() {
            m_failSave = true;
        }

        @Override
        public void update() {
            // nothing on disk to reconcile against
        }

        @Override
        protected void saveXml(final String data) throws IOException {
            if (m_failSave) {
                throw new IOException("simulated save failure");
            }
        }
    }

    @Test
    public void renameRollsBackWhenSaveFails() throws Exception {
        final TestGroupManager gm = new TestGroupManager(GROUPS_XML);
        gm.failNextSave();
        try {
            gm.renameGroup("oldgroup", "newgroup");
            fail("the save failure should have propagated");
        } catch (final IOException expected) {
            // the in-memory map must keep reflecting groups.xml, not a phantom rename
        }
        assertTrue("the old group must survive a failed save", gm.hasGroup("oldgroup"));
        assertFalse("a failed save must not leave a phantom renamed group", gm.hasGroup("newgroup"));
        assertEquals("oldgroup", gm.getGroup("oldgroup").getName());
    }

    @Test
    public void renameCommitsWhenSaveSucceeds() throws Exception {
        final TestGroupManager gm = new TestGroupManager(GROUPS_XML);
        gm.renameGroup("oldgroup", "newgroup");
        assertTrue(gm.hasGroup("newgroup"));
        assertFalse(gm.hasGroup("oldgroup"));
        assertEquals("newgroup", gm.getGroup("newgroup").getName());
    }
}
