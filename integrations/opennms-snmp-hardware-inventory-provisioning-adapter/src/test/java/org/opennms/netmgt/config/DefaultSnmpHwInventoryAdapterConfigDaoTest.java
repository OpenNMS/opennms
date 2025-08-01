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

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.hardware.HwExtension;
import org.opennms.netmgt.config.hardware.HwInventoryAdapterConfiguration;
import org.springframework.core.io.FileSystemResource;

/**
 * The Class DefaultSnmpHwInventoryAdapterConfigDaoTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultSnmpHwInventoryAdapterConfigDaoTest {

    private DefaultSnmpHwInventoryAdapterConfigDao dao;

    /**
     * Sets up the test
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoErrorOrGreater();
    }

    /**
     * Test duplicate OID.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDuplicateOid() throws Exception {
        initDao("src/test/resources/test-config-01.xml");
        HwInventoryAdapterConfiguration cfg = dao.getConfiguration();
        HwExtension ext = cfg.getExtensions().get(0);
        Assert.assertEquals("CISCO-ENTITY-EXT-MIB", ext.getName());
        Assert.assertEquals(5, ext.getMibObjects().size());
        Assert.assertNull(ext.getMibObjectByAlias("ceExtNVRAMSize"));
    }

    /**
     * Test duplicate Alias.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDuplicateAlias() throws Exception {
        initDao("src/test/resources/test-config-02.xml");
        HwInventoryAdapterConfiguration cfg = dao.getConfiguration();
        HwExtension ext = cfg.getExtensions().get(0);
        Assert.assertEquals("CISCO-ENTITY-EXT-MIB", ext.getName());
        Assert.assertEquals(5, ext.getMibObjects().size());
        Assert.assertNull(ext.getMibObjectByOid(".1.3.6.1.4.1.9.9.195.1.1.1.2"));
    }

    /**
     * Test invalid replace property.
     *
     * @throws Exception the exception
     */
    @Test
    public void testInvalidReplaceProperty() throws Exception {
        initDao("src/test/resources/test-config-01.xml");
        emulateReload("src/test/resources/test-config-invalid-01.xml");
        HwInventoryAdapterConfiguration cfg = dao.getConfiguration();
        HwExtension ext = cfg.getExtensions().get(0);
        Assert.assertEquals("CISCO-ENTITY-EXT-MIB", ext.getName());
        Assert.assertNotNull(cfg);
    }

    /**
     * Test invalid replace property.
     *
     * @throws Exception the exception
     */
    @Test
    public void testInvalidOid() throws Exception {
        initDao("src/test/resources/test-config-01.xml");
        emulateReload("src/test/resources/test-config-invalid-02.xml");
        HwInventoryAdapterConfiguration cfg = dao.getConfiguration();
        HwExtension ext = cfg.getExtensions().get(0);
        Assert.assertEquals("CISCO-ENTITY-EXT-MIB", ext.getName());
        Assert.assertNotNull(cfg);
    }

    /**
     * Emulate reload.
     *
     * @param fileName the file name
     */
    private void emulateReload(String fileName) {
        boolean exception = false;
        try {
            dao.setConfigResource(new FileSystemResource(new File(fileName)));
            dao.afterPropertiesSet();
        } catch (Exception e) {
            System.err.println(e);
            exception = true;
        }
        Assert.assertTrue(exception);
    }

    /**
     * Initialized the DAO
     *
     * @param configFile the configuration file
     * @return the configuration
     * @throws Exception the exception
     */
    private void initDao(String configFile) throws Exception {
        File file = new File(configFile);
        Assert.assertTrue(file.exists());
        dao = new DefaultSnmpHwInventoryAdapterConfigDao();
        dao.setConfigResource(new FileSystemResource(file));
        dao.afterPropertiesSet();
    }
}
