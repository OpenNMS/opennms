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

import static org.junit.Assert.assertThat;

import java.nio.file.Path;

import org.hamcrest.core.Is;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.upgrade.api.OnmsUpgradeException;

import com.google.common.collect.Lists;

public class ClearKarafCacheMigratorOfflineIT {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        temporaryFolder.newFolder("data", "aaa");
        temporaryFolder.newFolder("data", "bbb");
        temporaryFolder.newFolder("etc");
        temporaryFolder.newFile("data/history.txt");
        temporaryFolder.newFile("data/foo.bar");
        temporaryFolder.newFile("etc/opennms.properties");
        temporaryFolder.newFile("etc/rrd-configuration.properties");
        System.setProperty("opennms.home", temporaryFolder.getRoot().getAbsolutePath());
    }

    @Test
    public void testDeletion() {
        final Path temporaryPath = temporaryFolder.getRoot().toPath();
        assertThat("temporary folder must exist", temporaryPath.toFile().exists(), Is.is(true));
        assertThat("data directory must exist", temporaryPath.resolve("data").toFile().exists(), Is.is(true));
        assertThat("data/history.txt file must exist", temporaryPath.resolve("data").resolve("history.txt").toFile().exists(), Is.is(true));
        assertThat("data/foo.bar file must exist", temporaryPath.resolve("data").resolve("foo.bar").toFile().exists(), Is.is(true));
        assertThat("data/aaa directory must exist", temporaryPath.resolve("data").resolve("aaa").toFile().exists(), Is.is(true));
        assertThat("data/bbb directory must exist", temporaryPath.resolve("data").resolve("bbb").toFile().exists(), Is.is(true));
        assertThat("etc/opennms.properties file must exist", temporaryPath.resolve("etc").resolve("opennms.properties").toFile().exists(), Is.is(true));
        assertThat("etc/rrd-configuration.properties file must exist", temporaryPath.resolve("etc").resolve("rrd-configuration.properties").toFile().exists(), Is.is(true));

        try {
            new ClearKarafCacheMigratorOffline().execute();
        } catch (OnmsUpgradeException e) {
            assertThat("Exception message must match `Karaf's data directory ... pruned'", e.getMessage(), StringContainsInOrder.stringContainsInOrder(Lists.newArrayList("Karaf's data directory", "pruned")));
        }

        assertThat("temporary folder must exist", temporaryPath.toFile().exists(), Is.is(true));
        assertThat("data directory must exist", temporaryPath.resolve("data").toFile().exists(), Is.is(true));
        assertThat("data/history.txt file must exist", temporaryPath.resolve("data").resolve("history.txt").toFile().exists(), Is.is(true));
        assertThat("data/foo.bar file must not exist", temporaryPath.resolve("data").resolve("foo.bar").toFile().exists(), Is.is(false));
        assertThat("data/aaa directory must not exist", temporaryPath.resolve("data").resolve("aaa").toFile().exists(), Is.is(false));
        assertThat("data/bbb directory must not exist", temporaryPath.resolve("data").resolve("bbb").toFile().exists(), Is.is(false));
        assertThat("etc/opennms.properties file must exist", temporaryPath.resolve("etc").resolve("opennms.properties").toFile().exists(), Is.is(true));
        assertThat("etc/rrd-configuration.properties file must exist", temporaryPath.resolve("etc").resolve("rrd-configuration.properties").toFile().exists(), Is.is(true));
    }
}
