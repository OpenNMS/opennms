/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.logging.Logging;
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
     */
    protected void executeUpgrade(OnmsUpgrade upg) {
        Date start = new Date();
        try {
            if (wasExecuted(upg)) {
                log("  Task %s has been executed at %s\n", upg.getId(), getUpgradeStatus().getLastExecutionTime(upg));
                return;
            }
            log("- Running pre-execution phase\n");
            upg.preExecute();
        } catch (OnmsUpgradeException e) {
            log("  Ignoring: %s\n", e.getMessage());
            return;
        }
        try {
            log("- Running execution phase\n");
            upg.execute();
            log("- Saving the execution state\n");
            markAsExecuted(upg);
        } catch (OnmsUpgradeException executeException) {
            log("  Warning: can't perform the upgrade operation because: %s\n", executeException.getMessage());
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
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public void execute() throws OnmsUpgradeException {
        final Map<String,String> mdc = Logging.getCopyOfContextMap();
        Logging.putPrefix("upgrade");
        log("\n==============================================================================\n");
        log("OpenNMS Upgrader");
        log("\n==============================================================================\n\n");
        log("OpenNMS is currently %s\n", (isOpennmsRunning() ? "running" : "stopped"));
        List<OnmsUpgrade> upgradeObjects = getUpgradeObjects();
        for (OnmsUpgrade upg : upgradeObjects) {
            log("Processing %s: %s\n", upg.getId(), upg.getDescription());
            if (isOpennmsRunning()) {
                if (upg.requiresOnmsRunning()) {
                    executeUpgrade(upg);
                } else {
                    log("  Task %s requires that OpenNMS is stopped but it is running (ignoring)\n", upg.getId());
                }
            } else {
                if (upg.requiresOnmsRunning()) {
                    log("  Task %s requires OpenNMS is running but it is stopped (ignoring)\n", upg.getId());
                } else {
                    executeUpgrade(upg);
                }
            }
        }
        log("\nUpgrade completed successfully!\n");
        Logging.setContextMap(mdc);
    }

    /**
     * The main method.
     * <p>This is the class that must be called externally to perform the upgrade.</p>
     * 
     * TODO: be able to pass the class scope (package filter)
     * TODO: be able to pass the execution status file
     * 
     * @param args the arguments
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public static void main(String[] args) throws OnmsUpgradeException {
        new Upgrade().execute();
    }
}
