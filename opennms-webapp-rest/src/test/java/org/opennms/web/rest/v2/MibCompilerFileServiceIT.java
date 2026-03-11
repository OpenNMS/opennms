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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"

})

@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class MibCompilerFileServiceIT {

    private File tempHome;
    @Autowired
    private MibCompilerFileService mibCompilerFileService;
    @Before
    public void setUp() throws Exception {
        tempHome = Files.createTempDirectory("opennms-home-test-").toFile();
        tempHome.deleteOnExit();

        System.setProperty("opennms.home", tempHome.getAbsolutePath());
    }


    @Test
    public void testEnsurePendingDirExists_createsShareMibsPending() {
        File pending = mibCompilerFileService.getPendingDir();
        assertFalse("pending dir should not exist before ensurePendingDirExists()", pending.exists());

        mibCompilerFileService.ensurePendingDirExists();

        assertTrue("pending dir should exist after ensurePendingDirExists()", pending.exists());
        assertTrue("pending should be a directory", pending.isDirectory());
    }

    @Test
    public void testSaveToPending_writesFileWithNormalizedNameAndContent() throws Exception {
        String baseName = "IF-MIB";
        String extension = "mib";
        byte[] payload = "hello-mib".getBytes(StandardCharsets.UTF_8);

        File saved = mibCompilerFileService.saveToPending(baseName, extension, new ByteArrayInputStream(payload));

        assertNotNull(saved);
        assertEquals("IF-MIB.mib", saved.getName());
        assertTrue("Saved file should exist on disk", saved.exists());
        assertTrue("Saved file should be a file", saved.isFile());

        byte[] readBack = Files.readAllBytes(saved.toPath());
        assertArrayEquals(payload, readBack);
    }

}
