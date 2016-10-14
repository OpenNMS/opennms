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

package org.opennms.features.topology.api;

import org.opennms.features.topology.api.support.SavedHistory;

/**
 * Common interface to handle the user's history.
 */
public interface HistoryManager {

    /**
     * Saves the current state of the {@link GraphContainer} for the current <code>userId</code>.
     * The returned {@link String} is the history fragment (hash).
     *
     * @param userId The user to save the history for
     * @param container The current {@link GraphContainer}
     * @return the history fragment (hash) of the saved history
     * @see #getHistoryFragment(String)
     */
    String saveOrUpdateHistory(String userId, GraphContainer container);

    /**
     * Restores the given history represented by <code>fragementId</code>.
     * The {@link GraphContainer} represents the user's state, which does not require the userId.
     *
     * @param fragment The history fragment (history hash)
     * @param container The {@link GraphContainer} needed to actually apply the history.
     */
    void applyHistory(String fragment, GraphContainer container);

    String getHistoryFragment(String userId);

    /**
     * Returns the history object for the provided <code>fragment</code>.
     */
    SavedHistory getHistoryByFragment(String fragment);

    /**
     * Returns the history object for the provided <code>userId</code>.
     */
    SavedHistory getHistoryByUserId(String userId);

    /**
     * Deletes the history for all users.
     */
    void deleteHistory();
}
