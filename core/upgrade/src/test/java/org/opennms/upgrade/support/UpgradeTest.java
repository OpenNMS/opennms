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
package org.opennms.upgrade.support;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.opennms.upgrade.tests.TestUpgradeA;
import org.opennms.upgrade.tests.TestUpgradeB;
import org.opennms.upgrade.tests.TestUpgradeEverytime;
import org.opennms.upgrade.tests.TestUpgradeExecuted;
import org.opennms.upgrade.tests.TestUpgradeNothing;
import org.opennms.upgrade.tests.bad.TestUpgradeWIthException;

/**
 * The Class UpgradeTest.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class UpgradeTest {

    /** The status file. */
    private File statusFile;

    /** The upgrade status. */
    private UpgradeStatus upgradeStatus;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        statusFile = new File("target/status.properties");
        Properties p = new Properties();
        p.put(new TestUpgradeExecuted().getId(), new Date().toString());
        p.store(new FileWriter(statusFile), null);
        upgradeStatus = new UpgradeStatus(statusFile);
        UpgradeHelper.executed.clear();
        UpgradeHelper.rolledback.clear();
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        statusFile.delete();
    }

    /**
     * Test upgrade.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpgrade() throws Exception {
        Assert.assertFalse(upgradeStatus.wasExecuted(new TestUpgradeNothing()));
        performUpgrade("org.opennms.upgrade.tests");
        Assert.assertTrue(upgradeStatus.wasExecuted(new TestUpgradeNothing()));
        Assert.assertEquals(4, UpgradeHelper.getExecutedList().size());
        Assert.assertEquals(TestUpgradeNothing.class.getName(), UpgradeHelper.getExecutedList().get(0));
        Assert.assertEquals(TestUpgradeA.class.getName(), UpgradeHelper.getExecutedList().get(1));
        Assert.assertEquals(TestUpgradeB.class.getName(), UpgradeHelper.getExecutedList().get(2));
        Assert.assertEquals(1, UpgradeHelper.getRolledBackList().size());
        Assert.assertEquals(TestUpgradeWIthException.class.getName(), UpgradeHelper.getRolledBackList().get(0));
    }

    @Test
    public void testUpgradeMoreThanOnce() throws Exception {
        Assert.assertFalse(upgradeStatus.wasExecuted(new TestUpgradeNothing()));
        Assert.assertEquals(0, UpgradeHelper.getExecutedList().stream().filter(c -> TestUpgradeA.class.getName().equals(c)).count());
        Assert.assertEquals(0, UpgradeHelper.getExecutedList().stream().filter(c -> TestUpgradeEverytime.class.getName().equals(c)).count());
        performUpgrade("org.opennms.upgrade.tests");
        Assert.assertEquals(1, UpgradeHelper.getExecutedList().stream().filter(c -> TestUpgradeA.class.getName().equals(c)).count());
        Assert.assertEquals(1, UpgradeHelper.getExecutedList().stream().filter(c -> TestUpgradeEverytime.class.getName().equals(c)).count());
        performUpgrade("org.opennms.upgrade.tests");
        performUpgrade("org.opennms.upgrade.tests");
        Assert.assertEquals(1, UpgradeHelper.getExecutedList().stream().filter(c -> TestUpgradeA.class.getName().equals(c)).count());
        Assert.assertEquals(3, UpgradeHelper.getExecutedList().stream().filter(c -> TestUpgradeEverytime.class.getName().equals(c)).count());
    }

    /**
     * Perform upgrade.
     *
     * @param scope the scope
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void performUpgrade(String scope) throws OnmsUpgradeException {
        Upgrade upgrade = new Upgrade();
        upgrade.setClassScope(scope);
        upgrade.setUpgradeStatus(upgradeStatus);
        upgrade.execute();
    }
}
