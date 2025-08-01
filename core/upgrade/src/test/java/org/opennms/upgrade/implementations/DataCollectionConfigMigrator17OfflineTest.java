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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Class DataCollectionConfigMigrator17OfflineTest.
 */
public class DataCollectionConfigMigrator17OfflineTest {

    /** The temporary folder. */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc2"), tempFolder.newFolder("etc"));
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());
    }

    /**
     * Can fix.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void canFix() throws OnmsUpgradeException, IOException {
        DataCollectionConfigMigrator17Offline task = new DataCollectionConfigMigrator17Offline();
        task.preExecute();
        task.execute();
        task.postExecute();

        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, new File(tempFolder.getRoot(), "etc/datacollection-config.xml"));
        Assert.assertNotNull(config);
        config.getSnmpCollections().forEach(s -> {
            Assert.assertTrue(s.getIncludeCollections().stream().filter(i -> i.getDataCollectionGroup().equals("SNMP-Informant")).findFirst().isPresent());
        });
    }
}
