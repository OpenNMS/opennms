/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
