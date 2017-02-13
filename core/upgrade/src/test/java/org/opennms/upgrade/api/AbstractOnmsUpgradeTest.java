/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.api;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * The Test Class for AbstractOnmsUpgrade.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class AbstractOnmsUpgradeTest {

    /** The task. */
    private MockOnmsUpgrade task;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        System.setProperty("opennms.home", "src/test/resources");
        task = new MockOnmsUpgrade();
    }

    /**
     * Test properties.
     *
     * @throws Exception the exception
     */
    @Test
    public void testProperties() throws Exception {
        Assert.assertEquals(task.getClass().getSimpleName(), task.getId());
        Assert.assertEquals("src/test/resources", task.getHomeDirectory());
        Assert.assertEquals("1.12.2", task.getOpennmsVersion());
        Assert.assertEquals(".jrb", task.getRrdExtension());
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(1, 8, 17));
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(1, 10, 13));
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(1, 12, 1));
        Assert.assertFalse(task.isInstalledVersionGreaterOrEqual(1, 12, 3));
        Assert.assertFalse(task.isInstalledVersionGreaterOrEqual(1, 13, 0));
        Assert.assertFalse(task.isInstalledVersionGreaterOrEqual(14, 0, 0));
        Assert.assertFalse(task.isRrdToolEnabled());
        Assert.assertFalse(task.isStoreByGroupEnabled());
    }

    /**
     * Test meridian.
     *
     * @throws Exception the exception
     */
    @Test
    public void testMeridian() throws Exception {
        task.setProductName("meridian");
        task.setProductDescription("OpenNMS Meridian");
        task.setVersion("1.0.0");
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(1, 8, 17));
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(1, 12, 1));
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(14, 0, 0));
        Assert.assertFalse(task.isInstalledVersionGreaterOrEqual(16, 0, 0));
    }

    /**
     * Test horizon.
     *
     * @throws Exception the exception
     */
    @Test
    public void testHorizon() throws Exception {
        task.setProductName("horizon");
        task.setProductDescription("OpenNMS Horizon");
        task.setVersion("15.0.0");
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(1, 8, 17));
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(1, 12, 1));
        Assert.assertTrue(task.isInstalledVersionGreaterOrEqual(14, 0, 0));
        Assert.assertFalse(task.isInstalledVersionGreaterOrEqual(16, 0, 0));
    }

    /**
     * Test ZIP and UNZIP directory.
     *
     * @throws Exception the exception
     */
    @Test
    public void testZipAndUnzipDirectory() throws Exception {
        task.testZipAndUnzipDirectory();
    }

    /**
     * Test ZIP and UNZIP file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testZipAndUnzipFile() throws Exception {
        task.testZipAndUnzipFile();
    }

}
