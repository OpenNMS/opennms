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
package org.opennms.netmgt.alarmd.northbounder.drools;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.alarmd.northbounder.drools.DroolsEngine;
import org.opennms.netmgt.alarmd.northbounder.drools.DroolsNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.drools.DroolsNorthbounderConfigDao;
import org.springframework.core.io.FileSystemResource;

/**
 * The test Class for DroolsNorthbounderConfigDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DroolsNorthbounderConfigDaoIT {

    /** The temporary folder. */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /** The configuration DAO. */
    private DroolsNorthbounderConfigDao configDao;

    /**
     * Sets up the test..
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), tempFolder.newFolder("etc"));
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());

        // Setup the configuration DAO
        FileSystemResource resource = new FileSystemResource(new File(tempFolder.getRoot(), "etc/drools-northbounder-config.xml"));
        configDao = new DroolsNorthbounderConfigDao();
        configDao.setConfigResource(resource);
        configDao.afterPropertiesSet();
    }

    /**
     * Test configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testConfiguration() throws Exception {
        DroolsNorthbounderConfig config = configDao.getConfig();
        Assert.assertNotNull(config);
        DroolsEngine engine = config.getEngine("JUnit");
        Assert.assertNotNull(engine);
    }

}
