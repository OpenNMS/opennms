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
package org.opennms.features.mibcompiler.rest.internal;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.rest.model.MibCompilerFileInfo;
import org.opennms.features.mibcompiler.rest.model.MibCompilerFileText;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.springframework.transaction.support.TransactionOperations;

import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.apache.activemq.util.IOHelper.deleteChildren;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class MibCompilerRestServiceImplIT {

    private File tempHome;
    private File pendingDir;
    private File compiledDir;
    private static File classHome;

    private MibCompilerRestServiceImpl service;


    @BeforeClass
    public static void beforeClass() throws Exception {
        classHome = Files.createTempDirectory("mibcompiler-rest-it-home-").toFile();
        System.setProperty("opennms.home", classHome.getAbsolutePath());
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("opennms.home");
        deleteRecursively(classHome);
    }



    @Before
    public void setUp() throws Exception {
        File mibsRoot = new File(classHome, "share/mibs");
        pendingDir = new File(mibsRoot, "pending");
        compiledDir = new File(mibsRoot, "compiled");

        pendingDir.mkdirs();
        compiledDir.mkdirs();

        deleteChildren(pendingDir);
        deleteChildren(compiledDir);

        MibParser parser = mock(MibParser.class);
        EventConfSourceDao eventConfSourceDao = mock(EventConfSourceDao.class);
        EventConfEventDao eventConfEventDao = mock(EventConfEventDao.class);
        EventConfDao eventConfDao = mock(EventConfDao.class);
        TransactionOperations operations = mock(TransactionOperations.class);


        service = new MibCompilerRestServiceImpl(parser,eventConfSourceDao,eventConfEventDao,eventConfDao,operations);
    }

    @After
    public void tearDown() {
        System.clearProperty("opennms.home");
        deleteRecursively(tempHome);
    }

    @Test
    public void listPendingAndCompiledFiles_shouldReturn200AndIncludePendingAndCompiled() throws Exception {
        Files.writeString(new File(pendingDir, "p1.mib").toPath(), "pending", StandardCharsets.UTF_8);
        Files.writeString(new File(compiledDir, "c1.mib").toPath(), "compiled", StandardCharsets.UTF_8);

        Response r = service.listPendingAndCompiledFiles();

        assertEquals(200, r.getStatus());
        assertNotNull(r.getEntity());

        @SuppressWarnings("unchecked")
        List<MibCompilerFileInfo> list = (List<MibCompilerFileInfo>) r.getEntity();

        assertEquals(2, list.size());
    }

    @Test
    public void listPendingAndCompiledFiles_shouldReturn200AndEmptyListWhenNoFiles() {
        Response r = service.listPendingAndCompiledFiles();

        assertEquals(200, r.getStatus());
        assertNotNull(r.getEntity());

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) r.getEntity();

        assertTrue(list.isEmpty());
    }

    @Test
    public void deleteFile_shouldReturn204WhenPendingFileDeleted() throws Exception {
        File f = new File(pendingDir, "to-delete.txt");
        Files.writeString(f.toPath(), "x", StandardCharsets.UTF_8);
        assertTrue(f.exists());

        Response r = service.deleteFile("pending", "to-delete.txt");

        assertEquals(204, r.getStatus());
        assertFalse("file should be deleted", f.exists());
    }

    @Test
    public void deleteFile_shouldReturn404WhenFileMissing() {
        Response r = service.deleteFile("pending", "missing.txt");
        assertEquals(404, r.getStatus());
    }

    @Test
    public void deleteFile_shouldReturn400ForInvalidFileNameTraversal() {
        Response r = service.deleteFile("pending", "../evil.txt");
        assertEquals(400, r.getStatus());
    }

    @Test
    public void getFileText_shouldReturn200AndFileTextForPending() throws Exception {
        Files.writeString(new File(pendingDir, "file1.txt").toPath(),
                "opennms\nmibcompiler testing", StandardCharsets.UTF_8);

        Response r = service.getFileText("pending", "file1.txt");

        assertEquals(200, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof MibCompilerFileText);

        MibCompilerFileText body = (MibCompilerFileText) r.getEntity();
        assertEquals("file1.txt", body.getName());
        assertEquals("pending", body.getLocation());
        assertEquals("opennms\nmibcompiler testing", body.getContents());
    }

    @Test
    public void getFileText_shouldReturn404WhenMissing() throws Exception {
        Response r = service.getFileText("compiled", "missing.mib");
        assertEquals(404, r.getStatus());
    }

    @Test
    public void getFileText_shouldReturn404ForInvalidFileName() throws Exception {
        // This test requires validateFileNameAndLocation(...) result to be returned by getFileText()
        Response r = service.getFileText("pending", "..\\evil.txt");
        assertEquals(404, r.getStatus());
    }

    @Test
    public void setFileText_shouldReturn200AndOverwritePendingFile() throws Exception {
        Files.writeString(new File(pendingDir, "edit.txt").toPath(),
                "old", StandardCharsets.UTF_8);

        byte[] content = "new\ntext".getBytes(StandardCharsets.UTF_8);

        Response r = service.setFileText("edit.txt", content);

        assertEquals(200, r.getStatus());
        assertNotNull(r.getEntity());
        // Verify content changed on disk
        String updated = Files.readString(new File(pendingDir, "edit.txt").toPath(), StandardCharsets.UTF_8);
        assertEquals("new\ntext", updated);

    }


    @Test
    public void setFileText_shouldReturn400WhenPendingFileMissing() {
        byte[] content = "SOME MIB CONTENT".getBytes(StandardCharsets.UTF_8);
        Response r = service.setFileText("missing.txt", content);
        assertEquals(400, r.getStatus());
    }

    @Test
    public void uploadMib_shouldReturn201AndSaveToPending() throws Exception {
        byte[] content = "SOME MIB CONTENT".getBytes(StandardCharsets.UTF_8);

        Response r = service.uploadMib(content, "IF-MIB.mib");

        assertEquals(201, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) r.getEntity();

        assertTrue(payload.containsKey("success"));
        assertTrue(payload.containsKey("errors"));

        File[] pendingFiles = pendingDir.listFiles();
        assertNotNull(pendingFiles);
        assertEquals(1, pendingFiles.length);
        assertEquals("IF-MIB.mib", pendingFiles[0].getName());
    }

    @Test
    public void uploadMib_shouldReturn400ForEmptyContent() throws Exception {
        Response r = service.uploadMib(new byte[0], "IF-MIB.mib");
        assertEquals(400, r.getStatus());
    }

    @Test
    public void uploadMib_shouldReturn409WhenBaseNameAlreadyExistsInPending() throws Exception {
        Files.writeString(new File(pendingDir, "IF-MIB.mib").toPath(), "existing", StandardCharsets.UTF_8);
        Response r = service.uploadMib("new".getBytes(StandardCharsets.UTF_8), "IF-MIB.txt");
        assertEquals(409, r.getStatus());
    }

    private static void deleteRecursively(File f) {
        if (f == null || !f.exists()) return;
        File[] kids = f.listFiles();
        if (kids != null) {
            for (File k : kids) {
                deleteRecursively(k);
            }
        }
        f.delete();
    }
}