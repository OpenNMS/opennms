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
