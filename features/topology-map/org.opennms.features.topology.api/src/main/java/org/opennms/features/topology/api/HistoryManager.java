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
