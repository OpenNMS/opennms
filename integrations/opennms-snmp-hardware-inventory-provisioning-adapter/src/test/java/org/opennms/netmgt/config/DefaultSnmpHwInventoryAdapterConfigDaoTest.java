/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
