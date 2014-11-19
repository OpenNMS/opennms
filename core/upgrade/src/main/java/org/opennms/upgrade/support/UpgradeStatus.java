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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.Properties;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.upgrade.api.OnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Class UpgradeStatus.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class UpgradeStatus {

    /** The Constant STATUS_FILE. */
    public static final String STATUS_FILE = "opennms-upgrade-status.properties";

    /** The status properties. */
    private Properties status;

    /** The status file. */
    private File statusFile;

    /**
     * Instantiates a new upgrade status.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public UpgradeStatus() throws OnmsUpgradeException {
        this(new File(ConfigFileConstants.getFilePathString() + STATUS_FILE));
    }

    /**
     * Instantiates a new upgrade status.
     *
     * @param statusFile the status file
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public UpgradeStatus(File statusFile) throws OnmsUpgradeException {
        this.statusFile = statusFile;
        status = new Properties();
        try {
            if (statusFile.exists()) {
                status.load(new FileReader(statusFile));
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't load upgrade status data.", e);
        }
    }

    /**
     * Was executed.
     *
     * @param upg the upgrade implementation class
     * @return true, if successful
     */
    public boolean wasExecuted(OnmsUpgrade upg) {
        for (Object obj : status.keySet()) {
            String cls = (String) obj;
            if (cls.equals(upg.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the last execution time.
     *
     * @param upg the upgrade implementation class
     * @return the last execution time
     */
    public String getLastExecutionTime(OnmsUpgrade upg) {
        for (Object obj : status.keySet()) {
            String cls = (String) obj;
            if (cls.equals(upg.getId())) {
                return status.getProperty(cls);
            }
        }
        return "Never";
    }

    /**
     * Mark as executed.
     *
     * @param upg the upgrade implementation class
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public void markAsExecuted(OnmsUpgrade upg) throws OnmsUpgradeException {
        status.put(upg.getId(), new Date().toString());
        try {
            status.store(new FileWriter(statusFile), null);
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't save upgrade status.", e);
        }
    }

}
