/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd.shell;

import java.util.Map;

import org.apache.karaf.shell.api.action.Argument;

public abstract class AbstractKeyOrIndexCommand extends AbstractThresholdStateCommand {
    @Argument(index = 0, description = "The key itself or its index as listed in the enumerate command",
            required = true)
    protected String keyOrIndex;

    protected String getKey() {
        Integer index = null;
        String key = null;

        // Try to derive an index from the passed argument and map it to a key in the session
        try {
            index = Integer.parseInt(keyOrIndex);

            Object stateKeyIndexes = session.get(STATE_INDEXES_SESSION_KEY);

            if (stateKeyIndexes == null) {
                throw new IllegalStateException("The state key index has not been populated by the enumerate command");
            }

            key = ((Map<Integer, String>) stateKeyIndexes).get(index);

            if (key == null) {
                throw new IllegalArgumentException("Could not find a state mapped to index " + index);
            }
        } catch (NumberFormatException ignore) {
        }

        // If there wasn't a valid index treat the argument as the key for hte state
        if (index == null) {
            key = keyOrIndex;
        }

        return key;
    }
}
