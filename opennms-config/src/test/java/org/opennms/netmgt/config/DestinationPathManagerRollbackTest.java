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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.opennms.netmgt.config.destinationPaths.Path;

/**
 * Verifies that a mutation which would leave destinationPaths.xml unmarshallable
 * (removing the last path, which the schema forbids) rolls back the in-memory model
 * instead of leaving it diverged from disk.
 */
public class DestinationPathManagerRollbackTest {

    private static final String ONE_PATH =
            "<?xml version=\"1.0\"?>\n" +
            "<destinationPaths>\n" +
            "    <header>\n" +
            "        <rev>1.0</rev>\n" +
            "        <created>Wednesday, December 6, 2023 11:34:00 AM EST</created>\n" +
            "        <mstation>localhost</mstation>\n" +
            "    </header>\n" +
            "    <path name=\"foobar\" initial-delay=\"0s\">\n" +
            "        <target>\n" +
            "            <name>foo</name>\n" +
            "            <command>bar</command>\n" +
            "        </target>\n" +
            "    </path>\n" +
            "</destinationPaths>\n";

    /** In-memory manager: saveXML/update are no-ops so no file is touched. */
    private static final class InMemoryDestinationPathManager extends DestinationPathManager {
        InMemoryDestinationPathManager(final String xml) throws IOException {
            parseXML(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        }
        @Override protected void saveXML(final String writerString) { }
        @Override public void update() { }
    }

    @Test
    public void removingTheLastPathRollsBackInsteadOfCorrupting() throws IOException {
        final DestinationPathManager mgr = new InMemoryDestinationPathManager(ONE_PATH);
        assertEquals(1, mgr.getPaths().size());

        try {
            mgr.removePath("foobar");
            fail("removing the last destination path should fail schema validation on marshal");
        } catch (final RuntimeException | IOException expected) {
            // marshalling an empty <destinationPaths> violates the minOccurs=1 schema
        }

        // the in-memory model must be unchanged, not left empty and diverged from disk
        assertEquals(1, mgr.getPaths().size());
        assertTrue(mgr.getPaths().containsKey("foobar"));
    }

    @Test
    public void aValidRemovalStillApplies() throws IOException {
        final String twoPaths = ONE_PATH.replace(
                "    <path name=\"foobar\" initial-delay=\"0s\">\n"
                        + "        <target>\n            <name>foo</name>\n            <command>bar</command>\n        </target>\n"
                        + "    </path>\n",
                "    <path name=\"foobar\" initial-delay=\"0s\">\n"
                        + "        <target>\n            <name>foo</name>\n            <command>bar</command>\n        </target>\n"
                        + "    </path>\n"
                        + "    <path name=\"second\" initial-delay=\"0s\">\n"
                        + "        <target>\n            <name>baz</name>\n            <command>qux</command>\n        </target>\n"
                        + "    </path>\n");
        final DestinationPathManager mgr = new InMemoryDestinationPathManager(twoPaths);
        assertEquals(2, mgr.getPaths().size());

        mgr.removePath("second");

        assertEquals(1, mgr.getPaths().size());
        assertTrue(mgr.getPaths().containsKey("foobar"));
        final Path remaining = mgr.getPaths().get("foobar");
        assertEquals("foobar", remaining.getName());
    }
}
