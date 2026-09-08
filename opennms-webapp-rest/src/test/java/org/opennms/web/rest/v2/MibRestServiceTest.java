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

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MibRestServiceTest {

    private static final File MIB_FIXTURES = new File("src/test/resources/mibs");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private MibRestService restService;
    private File pendingDir;
    private File compiledDir;

    @Before
    public void setUp() throws Exception {
        final File mibsRoot = tempFolder.newFolder("mibs");
        pendingDir = new File(mibsRoot, MibFileService.PENDING);
        compiledDir = new File(mibsRoot, MibFileService.COMPILED);
        final MibFileService fileService = new MibFileService();
        fileService.setMibsRootDir(mibsRoot);
        fileService.setGraphTemplatesDir(tempFolder.newFolder("graphs"));
        restService = new MibRestService();
        restService.setMibFileService(fileService);
    }

    private static Attachment attachment(String fileName, byte[] content) {
        return new Attachment("upload", new ByteArrayInputStream(content),
                new org.apache.cxf.jaxrs.ext.multipart.ContentDisposition(
                        "form-data; name=\"upload\"; filename=\"" + fileName + "\""));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUploadReportsPerFileResults() throws Exception {
        final byte[] mib = Files.readAllBytes(new File(MIB_FIXTURES, "SNMPv2-SMI.txt").toPath());
        // second attachment reuses the same name and must be rejected as a duplicate
        final Response response = restService.uploadMibFiles(List.of(
                attachment("SNMPv2-SMI.txt", mib),
                attachment("SNMPv2-SMI.txt", mib)));
        assertEquals(200, response.getStatus());
        final Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals(1, ((List<?>) entity.get("success")).size());
        assertEquals(1, ((List<?>) entity.get("errors")).size());
        assertTrue(new File(pendingDir, "SNMPv2-SMI.txt").isFile());
    }

    @Test
    public void testUploadWithoutFilesIsBadRequest() {
        assertEquals(400, restService.uploadMibFiles(List.of()).getStatus());
        assertEquals(400, restService.uploadMibFiles(null).getStatus());
    }

    @Test
    public void testStatusCodeMapping() throws Exception {
        // 404 for a missing file, 400 for an invalid directory or name
        assertEquals(404, restService.getMibFileContent(MibFileService.PENDING, "NO-SUCH.mib").getStatus());
        assertEquals(400, restService.getMibFileContent("etc", "IF-MIB.txt").getStatus());
        assertEquals(400, restService.getMibFileContent(MibFileService.PENDING, "../users.xml").getStatus());
        assertEquals(404, restService.deleteMibFile(MibFileService.PENDING, "NO-SUCH.mib").getStatus());
        assertEquals(404, restService.compileMibFile("NO-SUCH.mib", false).getStatus());
    }

    @Test
    public void testDeleteReturnsNoContent() throws Exception {
        pendingDir.mkdirs();
        Files.writeString(new File(pendingDir, "X.mib").toPath(), "x", StandardCharsets.UTF_8);
        assertEquals(204, restService.deleteMibFile(MibFileService.PENDING, "X.mib").getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCompileConflictReturns409() throws Exception {
        pendingDir.mkdirs();
        compiledDir.mkdirs();
        for (final String dep : new String[]{"SNMPv2-SMI.txt", "SNMPv2-TC.txt", "SNMPv2-CONF.txt", "SNMPv2-MIB.txt", "IANAifType-MIB.txt"}) {
            Files.copy(new File(MIB_FIXTURES, dep).toPath(), new File(compiledDir, dep).toPath());
        }
        Files.copy(new File(MIB_FIXTURES, "IF-MIB.txt").toPath(), new File(pendingDir, "IF-MIB.txt").toPath());
        Files.writeString(new File(compiledDir, "IF-MIB.mib").toPath(), "-- placeholder", StandardCharsets.UTF_8);

        final Response conflict = restService.compileMibFile("IF-MIB.txt", false);
        assertEquals(409, conflict.getStatus());
        final Map<String, Object> entity = (Map<String, Object>) conflict.getEntity();
        assertEquals("IF-MIB", entity.get("mibName"));
        assertEquals("IF-MIB.mib", entity.get("targetFile"));

        assertEquals(200, restService.compileMibFile("IF-MIB.txt", true).getStatus());
    }
}
