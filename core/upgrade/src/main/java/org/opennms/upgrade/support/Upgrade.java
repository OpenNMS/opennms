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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.logging.Logging;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.FuzzyDateFormatter;
import org.opennms.netmgt.vmmgr.ControllerUtils;
import org.opennms.upgrade.api.Ignore;
import org.opennms.upgrade.api.OnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeComparator;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * The Class Upgrade.
 * <p>This is the helper class that is going to be instantiated from outside OpenNMS to perform the upgrade operations.</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class Upgrade {

    /** The class scope. */
    private String classScope = "org.opennms.upgrade"; // To avoid issues with OSGi and other classes.

    /** The upgrade status object. */
    private UpgradeStatus upgradeStatus;

    /**
     * Gets the upgrade status.
     *
     * @return the upgrade status
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public UpgradeStatus getUpgradeStatus() throws OnmsUpgradeException {
        if (upgradeStatus == null) {
            upgradeStatus = new UpgradeStatus();
        }
        return upgradeStatus;
    }

    /**
     * Sets the upgrade status.
     *
     * @param upgradeStatus the new upgrade status
     */
    public void setUpgradeStatus(UpgradeStatus upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
    }

    /**
     * Gets the class scope.
     *
     * @return the class scope
     */
    public String getClassScope() {
        return classScope;
    }

    /**
     * Sets the class scope.
     *
     * @param classScope the new class scope
     */
    public void setClassScope(String classScope) {
        this.classScope = classScope;
    }

    /**
     * Checks if is OpenNMS running.
     *
     * @return true, if is OpenNMS running
     */
    protected boolean isOpennmsRunning() {
        try {
            return ControllerUtils.getController().status() == 0;
        } catch (Exception e) {
            log("Warning: can't retrieve OpeNNMS status (assuming it is not running).\n");
            return false;
        }
    }

    /**
     * Was executed.
     *
     * @param upg the upgrade implementation class
     * @return true, if successful
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected boolean wasExecuted(OnmsUpgrade upg) throws OnmsUpgradeException {
        return getUpgradeStatus().wasExecuted(upg);
    }

    /**
     * Execute upgrade.
     *
     * @param upg the upgrade implementation class
     * @return true, if the pre-execution or the execution phase failed
     */
    protected boolean executeUpgrade(OnmsUpgrade upg) {
        Date start = new Date();
        try {
            if (wasExecuted(upg)) {
                log("  Task %s has been executed at %s\n", upg.getId(), getUpgradeStatus().getLastExecutionTime(upg));
                return false;
            }
        } catch (OnmsUpgradeException e) {
            if (upg.isCritical()) {
                log("  Error: can't tell whether task %s was executed before, because: %s\n", upg.getId(), e.getMessage());
            } else {
                log("  Ignoring: %s\n", e.getMessage());
            }
            return true;
        }
        try {
            log("- Running pre-execution phase\n");
            upg.preExecute();
        } catch (OnmsUpgradeException e) {
            if (upg.isCritical()) {
                log("  Error: can't run the pre-execution phase because: %s\n", e.getMessage());
            } else {
                log("  Ignoring: %s\n", e.getMessage());
            }
            return true;
        }
        boolean failed = false;
        try {
            log("- Running execution phase\n");
            upg.execute();
            if (upg.runOnlyOnce()) {
                log("- Saving the execution state\n");
                markAsExecuted(upg);
            } else {
                log("- Ignore the execution status, as this task is executed with every call\n");
            }
        } catch (OnmsUpgradeException executeException) {
            failed = true;
            log("  %s: can't perform the upgrade operation because: %s\n", upg.isCritical() ? "Error" : "Warning", executeException.getMessage());
            try {
                log("- Executing rollback phase\n");
                upg.rollback();
            } catch (OnmsUpgradeException rollbackException) {
                log("  Warning: can't rollback the upgrade because: %s\n", rollbackException.getMessage());
                rollbackException.printStackTrace();
            }
        }
        try {
            log("- Running post-execution phase\n");
            upg.postExecute();
        } catch (OnmsUpgradeException e) {
            log("  Warning: can't run the post-execute phase because: %s\n", e.getMessage());
        }
        log("\nFinished in %s\n\n", FuzzyDateFormatter.calculateDifference(start, new Date()));
        return failed;
    }

    /**
     * Mark as executed.
     *
     * @param upg the upgrade implementation class
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected void markAsExecuted(OnmsUpgrade upg) throws OnmsUpgradeException {
        getUpgradeStatus().markAsExecuted(upg);
    }

    /**
     * Log.
     *
     * @param msgFormat the message format
     * @param args the message's arguments
     */
    protected void log(String msgFormat, Object... args) {
        System.out.printf(msgFormat, args);
    }

    /**
     * Gets the upgrade objects.
     *
     * @return the upgrade objects
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected List<OnmsUpgrade> getUpgradeObjects() throws OnmsUpgradeException {
        List<OnmsUpgrade> upgrades = new ArrayList<>();
        try {
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
            provider.addIncludeFilter(new AssignableTypeFilter(OnmsUpgrade.class));
            Set<BeanDefinition> components = provider.findCandidateComponents(getClassScope());
            for (BeanDefinition component : components) {
                if (component.isAbstract()) {
                    continue;
                }
                Class<?> cls = Class.forName(component.getBeanClassName());
                if (cls.getAnnotation(Ignore.class) != null) {
                    continue;
                }
                OnmsUpgrade upgrade = (OnmsUpgrade) cls.newInstance();
                upgrades.add(upgrade);
                log("Found upgrade task %s\n", upgrade.getId());
            }
            Collections.sort(upgrades, new OnmsUpgradeComparator());
        } catch (Exception e) {
            throw new OnmsUpgradeException("  Can't find the upgrade classes because: " + e.getMessage(), e);
        }
        return upgrades;
    }

    /**
     * Execute.
     * <p>Perform the upgrade operations.</p>
     * 
     * @throws OnmsUpgradeException the OpenNMS upgrade exception, also when a critical task failed (see
     * {@link OnmsUpgrade#isCritical()}): then the other tasks ran, and etc/configured is removed
     */
    public void execute() throws OnmsUpgradeException {
        final Map<String,String> mdc = Logging.getCopyOfContextMap();
        Logging.putPrefix("upgrade");
        try {
            log("\n==============================================================================\n");
            log("OpenNMS Upgrader");
            log("\n==============================================================================\n\n");
            log("OpenNMS is currently %s\n", (isOpennmsRunning() ? "running" : "stopped"));
            final List<String> criticalFailures = new ArrayList<>();
            List<OnmsUpgrade> upgradeObjects = getUpgradeObjects();
            for (OnmsUpgrade upg : upgradeObjects) {
                log("Processing %s: %s\n", upg.getId(), upg.getDescription());
                if (isOpennmsRunning()) {
                    if (upg.requiresOnmsRunning()) {
                        if (executeUpgrade(upg) && upg.isCritical()) {
                            criticalFailures.add(upg.getId());
                        }
                    } else if (skippedWithWorkLeft(upg)) {
                        criticalFailures.add(upg.getId());
                    } else {
                        log("  Task %s requires that OpenNMS is stopped but it is running (ignoring)\n", upg.getId());
                    }
                } else {
                    if (upg.requiresOnmsRunning()) {
                        log("  Task %s requires OpenNMS is running but it is stopped (ignoring)\n", upg.getId());
                    } else if (executeUpgrade(upg) && upg.isCritical()) {
                        criticalFailures.add(upg.getId());
                    }
                }
            }
            if (!criticalFailures.isEmpty()) {
                fail(criticalFailures);
            }
            thank();
            log("\nUpgrade completed successfully!\n");
        } finally {
            Logging.setContextMap(mdc);
        }
    }

    /**
     * Checks a task that is skipped because it requires that OpenNMS is stopped, but OpenNMS is running: a critical task
     * that still has work to do fails the upgrade, so that OpenNMS does not start again without that work (see
     * {@link OnmsUpgrade#isPending()}). A task that runs only once and has run has no work left.
     * <p>A task that is skipped because it requires that OpenNMS is running is not checked: OpenNMS runs without it,
     * and after the upgrade removed etc/configured, the admin could not start OpenNMS to run it.</p>
     *
     * @return true, if the task is critical and has work left, or it can't tell
     */
    private boolean skippedWithWorkLeft(final OnmsUpgrade upg) {
        if (!upg.isCritical()) {
            return false;
        }
        try {
            if ((upg.runOnlyOnce() && wasExecuted(upg)) || !upg.isPending()) {
                return false;
            }
        } catch (final OnmsUpgradeException e) {
            log("  Error: task %s requires that OpenNMS is stopped, but it is running, and the upgrade can't tell whether the task has work left: %s\n", upg.getId(), e.getMessage());
            log("  Fix the cause, stop OpenNMS, then run the installer again.\n");
            return true;
        }
        log("  Error: task %s requires that OpenNMS is stopped, but it is running, and OpenNMS cannot run without its work. Stop OpenNMS, then run the installer again.\n", upg.getId());
        return true;
    }

    /**
     * Ends an upgrade in which critical tasks failed: removes etc/configured, so that OpenNMS does not start until the
     * installer runs again, and throws.
     */
    private void fail(final List<String> criticalFailures) throws OnmsUpgradeException {
        final String tasks = String.join(", ", criticalFailures);
        final File configured = new File(ConfigFileConstants.getHome(), "etc" + File.separator + "configured");
        log("\n==============================================================================\n");
        log("UPGRADE FAILED");
        log("\n==============================================================================\n\n");
        log("OpenNMS cannot run without these tasks, which failed or were skipped: %s.\n", tasks);
        log("Their messages above say why. Fix the cause, then run the installer again:\n\n");
        log("  %s/bin/install -dis\n\n", ConfigFileConstants.getHome());
        if (!configured.exists() || configured.delete()) {
            log("%s is removed, so OpenNMS does not start until then.\n", configured);
            if (isOpennmsRunning()) {
                log("OpenNMS is still running, and the upgrade does not stop it. But once it stops, it does not start again until then,"
                        + " also not when a service manager such as systemd restarts it.\n");
            }
        } else {
            log("Warning: %s could not be removed. Do not start OpenNMS until the installer has run without this failure.\n", configured);
        }
        throw new OnmsUpgradeException("The upgrade failed: these tasks failed or were skipped, and OpenNMS cannot run without them: " + tasks);
    }

    private void thank() {
        log("\n*** Thanks for using OpenNMS!\n");
        log("***\n");
        log("*** Consider joining our active and supportive online community through\n");
        log("***\n");
        log("*** https://www.opennms.com/participate/\n");
        log("***\n");
        log("*** To connect with users, testers, experts, and contributors.\n");
        log("***\n");
        log("*** Or email us directly at contactus@opennms.com to learn more.\n");
    }

    /**
     * The main method.
     * <p>This is the class that must be called externally to perform the upgrade.</p>
     * 
     * TODO: be able to pass the class scope (package filter)
     * TODO: be able to pass the execution status file
     * 
     * <p>A failed upgrade exits with status 1, which fails the installer too.</p>
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
            new Upgrade().execute();
        } catch (final OnmsUpgradeException e) {
            System.err.println("ERROR: " + e.getMessage());
            if (e.getCause() != null) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}
