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
package org.opennms.web.rest.v2.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TempAreaCleanupTest {

    private static final String SHA_A = "a".repeat(64);
    private static final String SHA_B = "b".repeat(64);
    private static final String SHA_C = "c".repeat(64);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path dir;
    private GitHubReleasesTest.MutableClock clock;
    private final Map<String, Instant> deployed = new HashMap<>();
    private boolean registryBroken;
    private TempArea area;

    @Before
    public void setUp() throws IOException {
        dir = folder.getRoot().toPath().resolve("plugin-management");
        Files.createDirectories(dir);
        clock = new GitHubReleasesTest.MutableClock(Instant.now());
        area = new TempArea(dir, clock, () -> {
            if (registryBroken) {
                throw new IOException("registry unreadable");
            }
            return deployed;
        }, 1000);
    }

    @Test
    public void storedKarsExpireAfterAnHourUnlessDeployed() throws IOException {
        stored(SHA_A, 100, Duration.ofMinutes(61));
        stored(SHA_B, 100, Duration.ofMinutes(59));
        stored(SHA_C, 100, Duration.ofMinutes(90));
        deployed.put(SHA_C, clock.instant().minus(Duration.ofMinutes(30)));

        final TempArea.Cleanup cleanup = area.cleanup();

        assertFalse(Files.exists(dir.resolve(SHA_A + ".kar")));
        assertFalse(Files.exists(dir.resolve(SHA_A + ".name")));
        assertFalse(Files.exists(dir.resolve(SHA_A + ".source")));
        assertTrue(Files.exists(dir.resolve(SHA_B + ".kar")));
        assertTrue(Files.exists(dir.resolve(SHA_C + ".kar")));
        assertEquals(3, cleanup.getRemoved());
        assertEquals(100 + "a.kar".length() + "upload".length(), cleanup.getBytes());
    }

    @Test
    public void deployedCopyGoesAnHourAfterTheInstall() throws IOException {
        stored(SHA_C, 100, Duration.ofMinutes(120));
        deployed.put(SHA_C, clock.instant().minus(Duration.ofMinutes(61)));

        area.cleanup();

        assertFalse(Files.exists(dir.resolve(SHA_C + ".kar")));
        assertEquals(0, area.usage().getFiles());
    }

    @Test
    public void strayFilesGoAtOnce() throws IOException {
        file("notes.txt", 10, Duration.ZERO);
        file(".DS_Store", 10, Duration.ZERO);
        file("ABCDEF.kar", 10, Duration.ZERO);
        file(SHA_A + ".zip", 10, Duration.ZERO);
        stored(SHA_B, 10, Duration.ZERO);
        Files.createDirectories(dir.resolve("subdir"));

        final TempArea.Cleanup cleanup = area.cleanup();

        assertEquals(4, cleanup.getRemoved());
        assertEquals(40, cleanup.getBytes());
        assertTrue(Files.exists(dir.resolve(SHA_B + ".kar")));
        assertTrue(Files.isDirectory(dir.resolve("subdir")));
    }

    @Test
    public void partAndEmptyFilesGetFiveMinutes() throws IOException {
        file("upload-1.part", 10, Duration.ofMinutes(4));
        file("download-2.part", 10, Duration.ofMinutes(6));
        file(SHA_A + ".kar", 0, Duration.ofMinutes(6));
        file(SHA_B + ".kar", 0, Duration.ofMinutes(4));

        area.cleanup();

        assertTrue(Files.exists(dir.resolve("upload-1.part")));
        assertFalse(Files.exists(dir.resolve("download-2.part")));
        assertFalse(Files.exists(dir.resolve(SHA_A + ".kar")));
        assertTrue(Files.exists(dir.resolve(SHA_B + ".kar")));
    }

    @Test
    public void capEvictsTheOldestFirst() throws IOException {
        stored(SHA_A, 400, Duration.ofMinutes(30));
        stored(SHA_B, 400, Duration.ofMinutes(10));
        stored(SHA_C, 400, Duration.ofMinutes(20));
        final TempArea capped = new TempArea(dir, clock, () -> deployed, 700);

        final TempArea.Cleanup cleanup = capped.cleanup();

        assertFalse(Files.exists(dir.resolve(SHA_A + ".kar")));
        assertFalse(Files.exists(dir.resolve(SHA_C + ".kar")));
        assertTrue(Files.exists(dir.resolve(SHA_B + ".kar")));
        assertEquals(6, cleanup.getRemoved());
        assertTrue(capped.usage().getBytes() <= 700);
    }

    @Test
    public void ensureSpaceEvictsForTheIncomingFileOrRefuses() throws IOException, PluginSourceException {
        stored(SHA_A, 400, Duration.ofMinutes(30));
        stored(SHA_B, 400, Duration.ofMinutes(10));
        final Path part = area.newPartFile("upload-");

        area.ensureSpace(part, 500);

        assertFalse(Files.exists(dir.resolve(SHA_A + ".kar")));
        assertTrue(Files.exists(dir.resolve(SHA_B + ".kar")));
        assertEquals(500, area.reservedBytes());

        assertEquals(507, refused(part, 1001).getStatus());
        assertTrue("nothing else is evicted for a file that can never fit", Files.exists(dir.resolve(SHA_B + ".kar")));
        assertEquals("a refused reservation is not kept", 0, area.reservedBytes());
    }

    @Test
    public void transfersInFlightShareTheCap() throws IOException, PluginSourceException {
        final Path first = area.newPartFile("download-");
        final Path second = area.newPartFile("upload-");
        area.ensureSpace(first, 600);

        final PluginSourceException e = refused(second, 600);

        assertEquals(507, e.getStatus());
        assertTrue(e.getMessage(), e.getMessage().startsWith(TempArea.NO_SPACE + ": 0.0 MB would exceed the 0.0 MB cap of "));
        assertEquals(600, area.reservedBytes());

        area.ensureSpace(second, 400);
        assertEquals(1000, area.reservedBytes());
        area.ensureSpace(first, 300);
        assertEquals("a second reservation for the same part replaces the first", 700, area.reservedBytes());

        area.abort(first);
        assertFalse(Files.exists(first));
        assertEquals(400, area.reservedBytes());
        Files.write(second, new byte[400]);
        area.commit(second, SHA_A, "a.kar", "upload");
        assertEquals(0, area.reservedBytes());
        assertTrue(Files.exists(dir.resolve(SHA_A + ".kar")));
    }

    @Test
    public void unknownSizeReservesTheKarLimit() throws IOException, PluginSourceException {
        final TempArea roomy = new TempArea(dir, clock, () -> deployed, KarInspector.MAX_SIZE_BYTES + 100);
        final Path first = roomy.newPartFile("download-");
        final Path second = roomy.newPartFile("download-");

        roomy.ensureSpace(first, -1);
        assertEquals(KarInspector.MAX_SIZE_BYTES, roomy.reservedBytes());
        assertEquals(507, refused(roomy, second, 200).getStatus());
        roomy.ensureSpace(second, 100);

        assertEquals(507, refused(area, area.newPartFile("upload-"), 0).getStatus());
    }

    @Test
    public void cleanupReleasesTheReservationOfAPartItRemoves() throws IOException, PluginSourceException {
        final Path stale = area.newPartFile("download-");
        area.ensureSpace(stale, 900);
        Files.setLastModifiedTime(stale, FileTime.from(clock.instant().minus(Duration.ofMinutes(6))));

        area.cleanup();

        assertFalse(Files.exists(stale));
        assertEquals(0, area.reservedBytes());
    }

    @Test
    public void unreadableRegistryKeepsStoredKarsButStillDropsStrays() throws IOException {
        registryBroken = true;
        stored(SHA_A, 100, Duration.ofHours(5));
        file("junk", 5, Duration.ZERO);

        area.cleanup();

        assertTrue(Files.exists(dir.resolve(SHA_A + ".kar")));
        assertFalse(Files.exists(dir.resolve("junk")));
    }

    @Test
    public void usageCountsEveryRegularFile() throws IOException {
        stored(SHA_A, 100, Duration.ZERO);
        file("upload-1.part", 7, Duration.ZERO);

        final TempArea.Usage usage = area.usage();

        assertEquals(4, usage.getFiles());
        assertEquals(100 + 7 + "a.kar".length() + "upload".length(), usage.getBytes());
    }

    @Test
    public void missingDirectoryIsNotAnError() {
        final TempArea absent = new TempArea(dir.resolve("nope"), clock, HashMap::new, 1000);

        assertEquals(0, absent.cleanup().getRemoved());
        assertEquals(0, absent.usage().getFiles());
    }

    @Test
    public void schedulerStartsOnceAndStops() {
        area.ensureScheduled();
        area.ensureScheduled();
        assertTrue(Thread.getAllStackTraces().keySet().stream().anyMatch(t -> TempArea.THREAD_NAME.equals(t.getName()) && t.isDaemon()));

        area.shutdown();
        area.shutdown();
    }

    // --- fixtures --------------------------------------------------------------

    private PluginSourceException refused(final Path part, final long incoming) {
        return refused(area, part, incoming);
    }

    private static PluginSourceException refused(final TempArea area, final Path part, final long incoming) {
        try {
            area.ensureSpace(part, incoming);
            fail("expected 507");
            return null;
        } catch (final PluginSourceException e) {
            return e;
        }
    }

    private void stored(final String sha, final int karBytes, final Duration age) throws IOException {
        file(sha + ".kar", karBytes, age);
        write(sha + ".name", "a.kar".getBytes(), age);
        write(sha + ".source", "upload".getBytes(), age);
    }

    private void file(final String name, final int bytes, final Duration age) throws IOException {
        write(name, new byte[bytes], age);
    }

    private void write(final String name, final byte[] content, final Duration age) throws IOException {
        final Path p = dir.resolve(name);
        Files.write(p, content);
        Files.setLastModifiedTime(p, FileTime.from(clock.instant().minus(age)));
    }
}
