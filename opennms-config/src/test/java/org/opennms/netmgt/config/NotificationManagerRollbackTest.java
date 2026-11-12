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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Rule;

/**
 * Verifies that a mutation which would leave notifications.xml unmarshallable
 * (removing the last notification, which the schema forbids) rolls back the
 * in-memory model instead of leaving it diverged from disk, and that a partial
 * field-by-field replace that throws mid-way does not leave a half-updated entry.
 */
public class NotificationManagerRollbackTest {

    private static final String ONE_NOTICE =
            "<?xml version=\"1.0\"?>\n" +
            "<notifications xmlns=\"http://xmlns.opennms.org/xsd/notifications\">\n" +
            "  <header>\n" +
            "    <rev>1.0</rev>\n" +
            "    <created>Wednesday, December 6, 2023 11:34:00 AM EST</created>\n" +
            "    <mstation>localhost</mstation>\n" +
            "  </header>\n" +
            "  <notification name=\"only\" status=\"on\">\n" +
            "    <uei>uei.opennms.org/nodes/nodeDown</uei>\n" +
            "    <rule>IPADDR IPLIKE *.*.*.*</rule>\n" +
            "    <destinationPath>Email-Admin</destinationPath>\n" +
            "    <text-message>node down</text-message>\n" +
            "    <subject>node down</subject>\n" +
            "  </notification>\n" +
            "</notifications>\n";

    /** In-memory manager: saveXML/update are no-ops so no file is touched. */
    private static final class InMemoryNotificationManager extends NotificationManager {
        InMemoryNotificationManager(final String xml) {
            super(null, null);
            parseXML(new StringReader(xml));
        }
        @Override protected void saveXML(final String xmlString) { }
        @Override public void update() { }
    }

    @Test
    public void removingTheLastNotificationRollsBackInsteadOfCorrupting() throws Exception {
        final NotificationManager mgr = new InMemoryNotificationManager(ONE_NOTICE);
        assertEquals(1, mgr.getNotifications().size());

        try {
            mgr.removeNotification("only");
            fail("removing the last notification should fail schema validation on marshal");
        } catch (final RuntimeException | IOException | ClassNotFoundException expected) {
            // marshalling an empty <notifications> violates the minOccurs=1 schema
        }

        // the in-memory model must be unchanged, not left empty and diverged from disk
        assertEquals(1, mgr.getNotifications().size());
        assertNotNull(mgr.getNotification("only"));
    }

    @Test
    public void aFailedReplaceLeavesNoHalfUpdatedEntry() throws Exception {
        final NotificationManager mgr = new InMemoryNotificationManager(ONE_NOTICE);

        // a replacement with status left unset (as JAXB would leave an omitted
        // field); replaceNotification writes uei/rule/.../subject onto the live
        // object first, then setStatus(null) throws — leaving it half-updated
        // unless the rollback restores it
        final Rule rule = new Rule();
        rule.setContent("IPADDR IPLIKE *.*.*.*");
        final Notification broken = new Notification();
        broken.setName("only");
        broken.setUei("uei.opennms.org/nodes/nodeUp");
        broken.setRule(rule);
        broken.setDestinationPath("Email-Admin");
        broken.setTextMessage("changed");
        broken.setSubject("changed");
        // status intentionally left null

        try {
            mgr.replaceNotification("only", broken);
            fail("replacing with a notification missing its status should fail");
        } catch (final RuntimeException | IOException | ClassNotFoundException expected) {
            // expected
        }

        // the original entry must be intact — not carrying the changed uei/subject
        final Notification after = mgr.getNotification("only");
        assertNotNull(after);
        assertEquals("uei.opennms.org/nodes/nodeDown", after.getUei());
        assertEquals("node down", after.getSubject().orElse(null));
    }

    @Test
    public void aValidRemovalStillApplies() throws Exception {
        final String twoNotices = ONE_NOTICE.replace(
                "  </notification>\n</notifications>",
                "  </notification>\n" +
                "  <notification name=\"second\" status=\"on\">\n" +
                "    <uei>uei.opennms.org/nodes/interfaceDown</uei>\n" +
                "    <rule>IPADDR IPLIKE *.*.*.*</rule>\n" +
                "    <destinationPath>Email-Admin</destinationPath>\n" +
                "    <text-message>iface down</text-message>\n" +
                "    <subject>iface down</subject>\n" +
                "  </notification>\n" +
                "</notifications>");
        final NotificationManager mgr = new InMemoryNotificationManager(twoNotices);
        assertEquals(2, mgr.getNotifications().size());

        mgr.removeNotification("second");

        assertEquals(1, mgr.getNotifications().size());
        assertNotNull(mgr.getNotification("only"));
        assertNull(mgr.getNotification("second"));
    }
}
