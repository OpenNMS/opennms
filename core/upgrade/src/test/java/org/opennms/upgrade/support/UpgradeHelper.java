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

import java.util.ArrayList;
import java.util.List;

/**
 * The Class UpgradeHelper.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class UpgradeHelper {

    /** The executed list. */
    public static List<String> executed = new ArrayList<>();

    /** The rolled-back list. */
    public static List<String> rolledback = new ArrayList<>();

    /**
     * Gets the executed List.
     *
     * @return the upgrade order
     */
    public static List<String> getExecutedList() {
        return executed;
    }

    /**
     * Adds an executed class.
     *
     * @param upgradeId the upgrade id
     */
    public static void addExecuted(String upgradeId) {
        executed.add(upgradeId);
    }

    /**
     * Gets the rolled-back List.
     *
     * @return the upgrade order
     */
    public static List<String> getRolledBackList() {
        return rolledback;
    }

    /**
     * Adds an rolled-back class.
     *
     * @param upgradeId the upgrade id
     */
    public static void addRolledBack(String upgradeId) {
        rolledback.add(upgradeId);
    }

}
