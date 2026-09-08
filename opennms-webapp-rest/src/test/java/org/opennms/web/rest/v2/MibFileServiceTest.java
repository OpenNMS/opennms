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
package org.opennms.web.rest.v2;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.web.rest.v2.model.mibcompiler.MibCompileResultDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibDataCollectionPreviewDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibEventsPreviewDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibFileDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibGraphTemplatesDto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class MibFileServiceTest {

    private static final File MIB_FIXTURES = new File("src/test/resources/mibs");
    private static final String[] DEPENDENCY_MIBS = {
            "SNMPv2-SMI.txt", "SNMPv2-TC.txt", "SNMPv2-CONF.txt", "SNMPv2-MIB.txt", "IANAifType-MIB.txt"
    };

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private MibFileService service;
    private File pendingDir;
    private File compiledDir;
    private File graphDir;

    @Before
    public void setUp() throws Exception {
        final File mibsRoot = tempFolder.newFolder("mibs");
        graphDir = tempFolder.newFolder("snmp-graph.properties.d");
        pendingDir = new File(mibsRoot, MibFileService.PENDING);
        compiledDir = new File(mibsRoot, MibFileService.COMPILED);
        service = new MibFileService();
        service.setMibsRootDir(mibsRoot);
        service.setGraphTemplatesDir(graphDir);
        // seed the compiled directory with the standard MIBs IF-MIB depends on
        compiledDir.mkdirs();
        for (final String mib : DEPENDENCY_MIBS) {
            Files.copy(new File(MIB_FIXTURES, mib).toPath(), new File(compiledDir, mib).toPath());
        }
    }

    private void addPending(String fixture) throws IOException {
        pendingDir.mkdirs();
        Files.copy(new File(MIB_FIXTURES, fixture).toPath(), new File(pendingDir, fixture).toPath());
    }

    private InputStream fixtureStream(String fixture) throws IOException {
        return new ByteArrayInputStream(Files.readAllBytes(new File(MIB_FIXTURES, fixture).toPath()));
    }

    @Test
    public void testUploadAndList() throws Exception {
        service.saveUpload("IF-MIB.txt", fixtureStream("IF-MIB.txt"));
        final List<MibFileDto> pending = service.listMibFiles(MibFileService.PENDING);
        assertEquals(List.of("IF-MIB.txt"), pending.stream().map(MibFileDto::getName).collect(Collectors.toList()));
        assertTrue(pending.get(0).getSize() > 0);
        assertEquals(DEPENDENCY_MIBS.length, service.listMibFiles(MibFileService.COMPILED).size());
    }

    @Test
    public void testUploadRejectsDuplicates() throws Exception {
        service.saveUpload("IF-MIB.txt", fixtureStream("IF-MIB.txt"));
        assertThrows(IllegalArgumentException.class, () -> service.saveUpload("IF-MIB.txt", fixtureStream("IF-MIB.txt")));
        // name collision against the compiled directory is also rejected
        assertThrows(IllegalArgumentException.class, () -> service.saveUpload("SNMPv2-SMI.txt", fixtureStream("SNMPv2-SMI.txt")));
    }

    @Test
    public void testUploadRejectsOversizedStreamWithoutBuffering() {
        // a stream that would produce far more than the 10 MB cap; the reject must
        // happen while reading, before the whole stream is buffered
        final InputStream oversized = new InputStream() {
            private long produced = 0;

            @Override
            public int read() {
                return ++produced <= 64L * 1024 * 1024 ? 'a' : -1;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> service.saveUpload("HUGE-MIB.txt", oversized));
        assertTrue(service.listMibFiles(MibFileService.PENDING).isEmpty());
    }

    @Test
    public void testInvalidNamesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.readMibFile(MibFileService.PENDING, "../users.xml"));
        assertThrows(IllegalArgumentException.class, () -> service.readMibFile(MibFileService.PENDING, "a/b.mib"));
        assertThrows(IllegalArgumentException.class, () -> service.readMibFile(MibFileService.PENDING, "a\\b.mib"));
        assertThrows(IllegalArgumentException.class, () -> service.readMibFile(MibFileService.PENDING, ".hidden"));
        assertThrows(IllegalArgumentException.class, () -> service.readMibFile(MibFileService.PENDING, ""));
        assertThrows(IllegalArgumentException.class, () -> service.readMibFile("etc", "IF-MIB.txt"));
    }

    @Test
    public void testReadUpdateDelete() throws Exception {
        addPending("IF-MIB.txt");
        final String content = service.readMibFile(MibFileService.PENDING, "IF-MIB.txt");
        assertTrue(content.contains("IF-MIB DEFINITIONS"));

        service.updatePendingMibFile("IF-MIB.txt", "-- edited\n" + content);
        assertTrue(service.readMibFile(MibFileService.PENDING, "IF-MIB.txt").startsWith("-- edited"));
        assertThrows(FileNotFoundException.class, () -> service.updatePendingMibFile("NO-SUCH.mib", "x"));

        service.deleteMibFile(MibFileService.PENDING, "IF-MIB.txt");
        assertTrue(service.listMibFiles(MibFileService.PENDING).isEmpty());
        assertThrows(FileNotFoundException.class, () -> service.deleteMibFile(MibFileService.PENDING, "IF-MIB.txt"));
    }

    @Test
    public void testCompileSuccessMovesFile() throws Exception {
        addPending("IF-MIB.txt");
        final MibCompileResultDto result = service.compile("IF-MIB.txt", false);
        assertTrue(result.isSuccess());
        assertEquals("IF-MIB", result.getMibName());
        assertEquals("IF-MIB.mib", result.getTargetFile());
        assertFalse(new File(pendingDir, "IF-MIB.txt").exists());
        assertTrue(new File(compiledDir, "IF-MIB.mib").exists());
    }

    @Test
    public void testCompileReportsMissingDependencies() throws Exception {
        addPending("SONUS-COMMON-MIB.txt");
        final MibCompileResultDto result = service.compile("SONUS-COMMON-MIB.txt", false);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(List.of("SONUS-SMI", "SONUS-TC"), result.getMissingDependencies());
        // failed compilation must leave the pending file in place
        assertTrue(new File(pendingDir, "SONUS-COMMON-MIB.txt").exists());
    }

    @Test
    public void testCompileOverwriteConflict() throws Exception {
        addPending("IF-MIB.txt");
        Files.writeString(new File(compiledDir, "IF-MIB.mib").toPath(), "-- placeholder", StandardCharsets.UTF_8);
        assertThrows(MibFileService.MibExistsException.class, () -> service.compile("IF-MIB.txt", false));
        assertTrue(new File(pendingDir, "IF-MIB.txt").exists());

        final MibCompileResultDto result = service.compile("IF-MIB.txt", true);
        assertTrue(result.isSuccess());
        assertTrue(Files.readString(new File(compiledDir, "IF-MIB.mib").toPath()).contains("IF-MIB DEFINITIONS"));
    }

    @Test
    public void testGenerateEventsWithDefaultUeiBase() throws Exception {
        compileIfMib();
        final MibEventsPreviewDto result = service.generateEvents("IF-MIB.mib", null);
        assertTrue(result.isSuccess());
        assertEquals("IF-MIB", result.getMibName());
        assertEquals("uei.opennms.org/traps/IF-MIB", result.getUeiBase());
        assertEquals(2, result.getEventCount());
        assertEquals("IF-MIB.events.xml", result.getSuggestedFileName());
        final Events events = JaxbUtils.unmarshal(Events.class, result.getEventsXml());
        assertEquals(2, events.getEvents().size());
        assertTrue(events.getEvents().get(0).getUei().startsWith("uei.opennms.org/traps/IF-MIB/"));
    }

    @Test
    public void testGenerateEventsWithCustomUeiBase() throws Exception {
        compileIfMib();
        final MibEventsPreviewDto result = service.generateEvents("IF-MIB.mib", "uei.opennms.org/vendor/acme");
        assertTrue(result.isSuccess());
        assertEquals("uei.opennms.org/vendor/acme", result.getUeiBase());
        final Events events = JaxbUtils.unmarshal(Events.class, result.getEventsXml());
        assertTrue(events.getEvents().get(0).getUei().startsWith("uei.opennms.org/vendor/acme/"));
    }

    @Test
    public void testGenerateDataCollection() throws Exception {
        compileIfMib();
        final MibDataCollectionPreviewDto result = service.generateDataCollection("IF-MIB.mib");
        assertTrue(result.isSuccess());
        assertEquals("IF-MIB", result.getMibName());
        assertTrue(result.getGroupCount() > 0);
        assertEquals("IF-MIB.xml", result.getSuggestedFileName());
        final DatacollectionGroup group = JaxbUtils.unmarshal(DatacollectionGroup.class, result.getDataCollectionXml());
        assertEquals("IF-MIB", group.getName());
        assertFalse(group.getGroups().isEmpty());
    }

    @Test
    public void testGenerateGraphTemplates() throws Exception {
        compileIfMib();
        final MibGraphTemplatesDto dryRun = service.generateGraphTemplates("IF-MIB.mib", true);
        assertTrue(dryRun.isSuccess());
        assertTrue(dryRun.getGraphCount() > 0);
        assertEquals("IF-MIB-graph.properties", dryRun.getFileName());
        assertTrue(dryRun.getContent().contains("reports="));
        assertFalse(dryRun.isWritten());
        assertFalse(new File(graphDir, "IF-MIB-graph.properties").exists());

        final MibGraphTemplatesDto written = service.generateGraphTemplates("IF-MIB.mib", false);
        assertTrue(written.isWritten());
        assertTrue(new File(graphDir, "IF-MIB-graph.properties").exists());
    }

    @Test
    public void testGenerateAgainstMissingFile() {
        assertThrows(FileNotFoundException.class, () -> service.generateEvents("NO-SUCH.mib", null));
        assertThrows(FileNotFoundException.class, () -> service.generateDataCollection("NO-SUCH.mib"));
        assertThrows(FileNotFoundException.class, () -> service.generateGraphTemplates("NO-SUCH.mib", true));
    }

    private void compileIfMib() throws IOException {
        addPending("IF-MIB.txt");
        final MibCompileResultDto result = service.compile("IF-MIB.txt", false);
        assertTrue("IF-MIB should compile against the seeded dependencies", result.isSuccess());
    }
}
