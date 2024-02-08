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
