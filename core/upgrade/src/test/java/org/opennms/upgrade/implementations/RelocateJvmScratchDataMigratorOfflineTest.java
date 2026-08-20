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
package org.opennms.upgrade.implementations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RelocateJvmScratchDataMigratorOfflineTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path home;

    @Before
    public void before() throws Exception {
        temporaryFolder.newFolder("data", "tmp", "activemq", "kahadb");
        temporaryFolder.newFile("data/tmp/activemq/kahadb/db.data");
        temporaryFolder.newFile("data/tmp/opennms.drools.correlation.default.state");
        temporaryFolder.newFile("data/tmp/opennms.drools.nbi.default.state");
        temporaryFolder.newFile("data/tmp/some-unrelated-scratch-file.tmp");
        temporaryFolder.newFolder("etc");
        temporaryFolder.newFile("etc/opennms.properties");
        temporaryFolder.newFile("etc/rrd-configuration.properties");
        home = temporaryFolder.getRoot().toPath();
        System.setProperty("opennms.home", home.toAbsolutePath().toString());
    }

    @Test
    public void relocatesActivemqStoreAndDroolsState() throws Exception {
        new RelocateJvmScratchDataMigratorOffline().execute();

        assertTrue("var/activemq/kahadb must exist", home.resolve("var/activemq/kahadb").toFile().isDirectory());
        assertTrue("var/activemq/kahadb/db.data must exist", home.resolve("var/activemq/kahadb/db.data").toFile().isFile());
        assertFalse("old data/tmp/activemq must no longer exist", home.resolve("data/tmp/activemq").toFile().exists());

        assertTrue("tmp/opennms.drools.correlation.default.state must exist",
                home.resolve("tmp/opennms.drools.correlation.default.state").toFile().isFile());
        assertTrue("tmp/opennms.drools.nbi.default.state must exist",
                home.resolve("tmp/opennms.drools.nbi.default.state").toFile().isFile());
        assertFalse("old data/tmp/opennms.drools.correlation.default.state must no longer exist",
                home.resolve("data/tmp/opennms.drools.correlation.default.state").toFile().exists());

        // Unrelated scratch files are left in place for ClearKarafCacheMigratorOffline to prune.
        assertTrue("unrelated scratch file must be untouched",
                home.resolve("data/tmp/some-unrelated-scratch-file.tmp").toFile().exists());
    }

    @Test
    public void doesNothingWhenNoOldDataExists() throws Exception {
        FileUtils.deleteDirectory(home.resolve("data").toFile());

        new RelocateJvmScratchDataMigratorOffline().execute();

        assertTrue("tmp must still be created", home.resolve("tmp").toFile().isDirectory());
        assertFalse("var/activemq must not be created when there is nothing to move",
                home.resolve("var/activemq").toFile().exists());
    }

    @Test
    public void doesNotOverwriteExistingNewActivemqStore() throws Exception {
        final File existingNewStore = temporaryFolder.newFolder("var", "activemq", "kahadb");
        final File marker = new File(existingNewStore, "already-here.data");
        FileUtils.write(marker, "keep-me", "UTF-8");

        new RelocateJvmScratchDataMigratorOffline().execute();

        assertTrue("pre-existing var/activemq content must be preserved", marker.exists());
        assertTrue("old data/tmp/activemq must be left alone since new store already exists",
                home.resolve("data/tmp/activemq").toFile().exists());
    }
}
