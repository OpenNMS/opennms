/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api;

/**
 * North bound Interface API.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public interface Northbounder {

    /**
     * Start.
     *
     * @throws NorthbounderException the northbounder exception
     */
    public void start() throws NorthbounderException;

    /**
     * Used to determine if the northbounder is ready to accept alarms.
     *
     * If no northbounders are ready, the caller can save resources by not creating and
     * initializing the {@link NorthboundAlarm}s.
     *
     * This method is called once after northbounder is registered and started.
     * If the status were to change sometime after, the northbounder must re-register itself.
     *
     * @return <code>true</code> if the northbounder is ready to accept alarms, <code>false</code> otherwise.
     */
    boolean isReady();

    /**
     * On alarm.
     *
     * @param alarm the alarm
     * @throws NorthbounderException the northbounder exception
     */
    public void onAlarm(NorthboundAlarm alarm) throws NorthbounderException;

    /**
     * Stop.
     *
     * @throws NorthbounderException the northbounder exception
     */
    public void stop() throws NorthbounderException;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName();

    /**
     * Reloads configuration.
     */
    public void reloadConfig() throws NorthbounderException;

}
