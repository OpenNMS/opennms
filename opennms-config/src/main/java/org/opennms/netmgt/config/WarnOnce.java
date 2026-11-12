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
package org.opennms.netmgt.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Remembers the problems that were logged at WARN, so that a problem seen again, for example on every reload or read,
 * can be logged at DEBUG. It holds at most a given number of keys: the least recently seen one is dropped to make
 * room, and warns again when it comes back. Thread-safe.
 */
public final class WarnOnce {

    private final Set<List<String>> m_warned;

    public WarnOnce(final int max) {
        m_warned = Collections.newSetFromMap(Collections.synchronizedMap(
                new LinkedHashMap<List<String>, Boolean>(16, 0.75f, true) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected boolean removeEldestEntry(final Map.Entry<List<String>, Boolean> eldest) {
                        return size() > max;
                    }
                }));
    }

    /** True the first time the key is seen, or the first time since it was dropped: log at WARN then, at DEBUG otherwise. */
    public boolean isNew(final String... key) {
        return m_warned.add(Arrays.asList(key));
    }
}
