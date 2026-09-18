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
package org.opennms.netmgt.wsman.eventlog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The highest record number seen per node and log, kept in the JSON key-value store
 * so a restart neither replays nor skips records.
 */
public class EventLogCursorStore {

    private static final Logger LOG = LoggerFactory.getLogger(EventLogCursorStore.class);

    public static final String CONTEXT = "wsman-eventlog";

    private static final TypeReference<Map<String, Long>> CURSORS = new TypeReference<Map<String, Long>>() { };

    private final JsonStore jsonStore;
    private final ObjectMapper mapper = new ObjectMapper();

    public EventLogCursorStore(JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    public Map<String, Long> get(int nodeId) {
        final Optional<String> json = jsonStore.get(Integer.toString(nodeId), CONTEXT);
        if (!json.isPresent()) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(json.get(), CURSORS);
        } catch (IOException e) {
            LOG.warn("Ignoring unreadable cursors for node {}: {}", nodeId, e.getMessage());
            return new HashMap<>();
        }
    }

    public Long get(int nodeId, String logfile) {
        return get(nodeId).get(logfile);
    }

    public void put(int nodeId, String logfile, long recordNumber) {
        final Map<String, Long> cursors = get(nodeId);
        cursors.put(logfile, recordNumber);
        try {
            jsonStore.put(Integer.toString(nodeId), mapper.writeValueAsString(cursors), CONTEXT);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void clear(int nodeId) {
        jsonStore.delete(Integer.toString(nodeId), CONTEXT);
    }
}
