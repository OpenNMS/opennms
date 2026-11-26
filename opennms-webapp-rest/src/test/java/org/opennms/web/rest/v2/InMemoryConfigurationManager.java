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

package org.opennms.web.rest.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.opennms.features.config.exception.ConfigAlreadyExistsException;
import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;

/**
 * A {@link ConfigurationManagerService} that keeps configurations in memory. Only the calls the XML collector
 * REST service makes are implemented; there is no schema validation.
 */
final class InMemoryConfigurationManager {

    private final Map<String, Map<String, JSONObject>> store = new HashMap<>();
    private final AtomicInteger writes = new AtomicInteger();
    private final ConfigurationManagerService cms = mock(ConfigurationManagerService.class);

    InMemoryConfigurationManager() {
        when(cms.getJSONConfiguration(anyString(), anyString())).thenAnswer(inv -> Optional
                .ofNullable(configs(inv.getArgument(0)).get(inv.<String>getArgument(1)))
                .map(json -> new JSONObject(json.toString())));
        when(cms.getConfigIds(anyString())).thenAnswer(inv -> new TreeSet<>(configs(inv.getArgument(0)).keySet()));
        doAnswer(inv -> {
            final Map<String, JSONObject> configs = configs(inv.getArgument(0));
            if (configs.containsKey(inv.<String>getArgument(1))) {
                throw new ConfigAlreadyExistsException("exists: " + inv.getArgument(1));
            }
            configs.put(inv.getArgument(1), new JSONObject(inv.<JsonAsString>getArgument(2).toString()));
            writes.incrementAndGet();
            return null;
        }).when(cms).registerConfiguration(anyString(), anyString(), any(JsonAsString.class));
        doAnswer(inv -> {
            final Map<String, JSONObject> configs = configs(inv.getArgument(0));
            if (!configs.containsKey(inv.<String>getArgument(1))) {
                throw new ConfigNotFoundException("not found: " + inv.getArgument(1));
            }
            configs.put(inv.getArgument(1), new JSONObject(inv.<JsonAsString>getArgument(2).toString()));
            writes.incrementAndGet();
            return null;
        }).when(cms).updateConfiguration(anyString(), anyString(), any(JsonAsString.class), anyBoolean());
        doAnswer(inv -> {
            configs(inv.getArgument(0)).remove(inv.<String>getArgument(1));
            writes.incrementAndGet();
            return null;
        }).when(cms).unregisterConfiguration(anyString(), anyString());
    }

    ConfigurationManagerService service() {
        return cms;
    }

    void put(final String configName, final String configId, final String json) {
        configs(configName).put(configId, new JSONObject(json));
    }

    JSONObject get(final String configName, final String configId) {
        return configs(configName).get(configId);
    }

    Map<String, JSONObject> configs(final String configName) {
        return store.computeIfAbsent(configName, k -> new TreeMap<>());
    }

    int writes() {
        return writes.get();
    }
}
