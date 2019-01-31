/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.daemon;

import java.util.List;

/**
 * A service to receive all existing Daemons as well as reload them.
 *
 * @author sgrund
 */
public interface DaemonService {
    List<DaemonInfo> getDaemons();

    /**
     * Reload the daemon with the given name.
     *
     * In order to reload the daemon it must listen
     * to {@link org.opennms.netmgt.events.api.EventConstants#RELOAD_DAEMON_CONFIG_UEI} events.
     *
     * @param daemonName Case insensitive name of the daemon
     * @throws {@link java.util.NoSuchElementException} if a daemon for <code>daemonName</code> does not exist
     */
    void triggerReload(String daemonName) throws DaemonReloadException;

    /**
     * Returns the {@link DaemonReloadInfo} of the provided daemon.
     *
     * @param daemonName Case insensitive name of the daemon
     * @throws {@link java.util.NoSuchElementException} if a daemon for <code>daemonName</code> does not exist
     */
    DaemonReloadInfo getCurrentReloadState(String daemonName);
}