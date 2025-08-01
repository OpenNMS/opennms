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

import org.opennms.osgi.VaadinApplicationContext;

import java.util.Collections;
import java.util.Map;

class UserScopedHistoryEntry<T> {
    private final Class<? extends Operation> clazz;

    public UserScopedHistoryEntry(Class<? extends Operation> clazz) {
        this.clazz = clazz;
    }

    public Map<String, String> createEntry(VaadinApplicationContext applicationContext, T value) {
        return Collections.singletonMap(createKey(applicationContext), createValue(value));
    }

    public T loadEntry(VaadinApplicationContext applicationContext, Map<String, String> settings) {
        return (T)settings.get(createKey(applicationContext));
    }

    private String createValue(T input) {
        if (input == null) return "";
        if (input instanceof String) return (String)input;
        return input.toString();
    }

    private String createKey(VaadinApplicationContext context) {
        return clazz.getName() + ";" + context.getUiId() + ";" + context.getUsername();
    }
}
