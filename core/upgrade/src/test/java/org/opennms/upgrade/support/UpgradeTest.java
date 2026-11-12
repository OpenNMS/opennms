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
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.opennms.upgrade.criticaltests.TestCriticalUpgradeFailsInExecute;
import org.opennms.upgrade.criticaltests.TestCriticalUpgradeFailsInPreExecute;
import org.opennms.upgrade.criticaltests.TestUpgradeAfterTheCriticalOnes;
import org.opennms.upgrade.skippedtests.executed.TestOfflineCriticalUpgradeExecuted;
import org.opennms.upgrade.skippedtests.pending.TestOfflineCriticalUpgradePending;
import org.opennms.upgrade.skippedtests.pending.TestOnlineCriticalUpgradePending;
import org.opennms.upgrade.skippedtests.unknown.TestOfflineCriticalUpgradeUnknown;
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

    @Rule
    public TemporaryFolder home = new TemporaryFolder();

    /** The installer's etc/configured, in a temporary OpenNMS home. */
    private File configured;

    private String previousHome;

    /** What the upgrade logged. */
    private final StringBuilder output = new StringBuilder();

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
        previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", home.getRoot().getAbsolutePath());
        configured = new File(home.newFolder("etc"), "configured");
        Assert.assertTrue(configured.createNewFile());
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        statusFile.delete();
        if (previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", previousHome);
        }
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
        // the failure of a task that is not critical does not fail the upgrade
        Assert.assertTrue(configured.exists());
    }

    @Test
    public void testACriticalFailureFailsTheUpgradeAfterTheOtherTasks() throws Exception {
        final OnmsUpgradeException e = Assert.assertThrows(OnmsUpgradeException.class, () -> performUpgrade("org.opennms.upgrade.criticaltests"));

        Assert.assertTrue(e.getMessage(), e.getMessage().contains(TestCriticalUpgradeFailsInExecute.class.getName()));
        Assert.assertTrue(e.getMessage(), e.getMessage().contains(TestCriticalUpgradeFailsInPreExecute.class.getName()));
        // the other tasks still run
        Assert.assertEquals(List.of(TestUpgradeAfterTheCriticalOnes.class.getName()), UpgradeHelper.getExecutedList());
        // a failed execute() is rolled back, as before; a failed preExecute() ran nothing to roll back
        Assert.assertEquals(List.of(TestCriticalUpgradeFailsInExecute.class.getName()), UpgradeHelper.getRolledBackList());
        // OpenNMS does not start until the installer runs again
        Assert.assertFalse(configured.exists());
        Assert.assertFalse(upgradeStatus.wasExecuted(new TestCriticalUpgradeFailsInExecute()));
        Assert.assertTrue(upgradeStatus.wasExecuted(new TestUpgradeAfterTheCriticalOnes()));
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

    @Test
    public void testACriticalTaskSkippedWhileOpenNMSRunsFailsTheUpgradeIfItHasWorkLeft() throws Exception {
        final OnmsUpgradeException e = Assert.assertThrows(OnmsUpgradeException.class,
                () -> performUpgrade("org.opennms.upgrade.skippedtests.pending", true));

        Assert.assertTrue(e.getMessage(), e.getMessage().contains(TestOfflineCriticalUpgradePending.class.getName()));
        // the task that requires OpenNMS to be running ran
        Assert.assertFalse(e.getMessage(), e.getMessage().contains(TestOnlineCriticalUpgradePending.class.getName()));
        Assert.assertEquals(List.of(TestOnlineCriticalUpgradePending.class.getName()), UpgradeHelper.getExecutedList());
        Assert.assertFalse(configured.exists());
    }

    @Test
    public void testSkippingACriticalTaskThatRequiresOpenNMSRunningWhileItIsStoppedDoesNotFail() throws Exception {
        // the failure would remove etc/configured, so the admin could not start OpenNMS to run the task
        performUpgrade("org.opennms.upgrade.skippedtests.pending", false);

        Assert.assertEquals(List.of(TestOfflineCriticalUpgradePending.class.getName()), UpgradeHelper.getExecutedList());
        Assert.assertTrue(configured.exists());
    }

    @Test
    public void testSkippingACriticalTaskThatRanOnceDoesNotFail() throws Exception {
        // the task keeps the default isPending(), true, but a task that runs only once and has run has no work left
        upgradeStatus.markAsExecuted(new TestOfflineCriticalUpgradeExecuted());

        performUpgrade("org.opennms.upgrade.skippedtests.executed", true);

        Assert.assertTrue(UpgradeHelper.getExecutedList().isEmpty());
        Assert.assertTrue(configured.exists());
    }

    @Test
    public void testACriticalTaskSkippedWhileOpenNMSRunsThatCannotTellWhetherItHasWorkLeftFailsTheUpgradeWithTheReason() throws Exception {
        final OnmsUpgradeException e = Assert.assertThrows(OnmsUpgradeException.class,
                () -> performUpgrade("org.opennms.upgrade.skippedtests.unknown", true));

        Assert.assertTrue(e.getMessage(), e.getMessage().contains(TestOfflineCriticalUpgradeUnknown.class.getName()));
        Assert.assertTrue(output.toString(), output.toString().contains("can't tell whether the task has work left: the database is down\n"
                + "  Fix the cause, stop OpenNMS, then run the installer again."));
        Assert.assertFalse(output.toString(), output.toString().contains("OpenNMS cannot run without its work"));
        Assert.assertFalse(configured.exists());
        // the admin learns that the running OpenNMS won't start again
        Assert.assertTrue(output.toString(), output.toString().contains("OpenNMS is still running"));
    }

    @Test
    public void testACriticalTaskWhoseExecutionStatusCannotBeReadFailsTheUpgradeWithThatReason() throws Exception {
        // the status file is read on the first use; a directory in its place can't be read
        Assert.assertTrue(new File(configured.getParentFile(), UpgradeStatus.STATUS_FILE).mkdir());

        final OnmsUpgradeException e = Assert.assertThrows(OnmsUpgradeException.class,
                () -> performUpgrade("org.opennms.upgrade.criticaltests", false, null));

        Assert.assertTrue(e.getMessage(), e.getMessage().contains(TestCriticalUpgradeFailsInExecute.class.getName()));
        Assert.assertTrue(output.toString(), output.toString().contains("Error: can't tell whether task "
                + TestCriticalUpgradeFailsInExecute.class.getName() + " was executed before, because: Can't load upgrade status data."));
        Assert.assertFalse(output.toString(), output.toString().contains("pre-execution phase because"));
        Assert.assertTrue(UpgradeHelper.getExecutedList().isEmpty());
        Assert.assertFalse(configured.exists());
        Assert.assertFalse(output.toString(), output.toString().contains("OpenNMS is still running"));
    }

    @Test
    public void testSkippingACriticalTaskWithNoWorkLeftOrATaskThatIsNotCriticalDoesNotFail() throws Exception {
        // e.g. install -dis while OpenNMS runs, after a good migration: the migrator and the seed have nothing to do
        performUpgrade("org.opennms.upgrade.skippedtests.done", true);

        Assert.assertTrue(UpgradeHelper.getExecutedList().isEmpty());
        Assert.assertTrue(configured.exists());
    }

    /**
     * Perform upgrade, as if OpenNMS were stopped: whether an OpenNMS instance runs on this computer does not matter.
     *
     * @param scope the scope
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void performUpgrade(String scope) throws OnmsUpgradeException {
        performUpgrade(scope, false);
    }

    private void performUpgrade(final String scope, final boolean opennmsRunning) throws OnmsUpgradeException {
        performUpgrade(scope, opennmsRunning, upgradeStatus);
    }

    /**
     * @param status the status of the executed tasks; null for the default, the status file in the OpenNMS home
     */
    private void performUpgrade(final String scope, final boolean opennmsRunning, final UpgradeStatus status) throws OnmsUpgradeException {
        Upgrade upgrade = new Upgrade() {
            @Override
            protected boolean isOpennmsRunning() {
                return opennmsRunning;
            }

            @Override
            protected void log(final String msgFormat, final Object... args) {
                super.log(msgFormat, args);
                output.append(String.format(msgFormat, args));
            }
        };
        upgrade.setClassScope(scope);
        upgrade.setUpgradeStatus(status);
        upgrade.execute();
    }
}
