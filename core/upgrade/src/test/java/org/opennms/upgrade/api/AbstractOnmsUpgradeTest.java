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
