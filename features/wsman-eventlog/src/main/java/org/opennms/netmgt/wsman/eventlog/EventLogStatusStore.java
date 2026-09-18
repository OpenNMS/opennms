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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Per-node read status in the JSON key-value store, so the Manage WS-Man page can show
 * it from the webapp without reaching into the daemon.
 */
public class EventLogStatusStore {

    private static final Logger LOG = LoggerFactory.getLogger(EventLogStatusStore.class);

    public static final String CONTEXT = "wsman-eventlog-status";

    private final JsonStore jsonStore;
    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public EventLogStatusStore(JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    public Optional<EventLogReadStatus> get(int nodeId) {
        final Optional<String> json = jsonStore.get(Integer.toString(nodeId), CONTEXT);
        if (!json.isPresent()) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(json.get(), EventLogReadStatus.class));
        } catch (IOException e) {
            LOG.warn("Ignoring unreadable status for node {}: {}", nodeId, e.getMessage());
            return Optional.empty();
        }
    }

    public List<EventLogReadStatus> getAll() {
        final List<EventLogReadStatus> all = new ArrayList<>();
        for (Map.Entry<String, String> entry : jsonStore.enumerateContext(CONTEXT).entrySet()) {
            try {
                all.add(mapper.readValue(entry.getValue(), EventLogReadStatus.class));
            } catch (IOException e) {
                LOG.warn("Ignoring unreadable status for node {}: {}", entry.getKey(), e.getMessage());
            }
        }
        all.sort((a, b) -> Integer.compare(a.nodeId, b.nodeId));
        return all;
    }

    /** Reads, lets the caller change one log's entry, and writes back; per-node writes are serialised. */
    public synchronized void update(EventLogTarget target, String packageName, String log, Consumer<EventLogReadStatus.LogStatus> change) {
        final EventLogReadStatus status = get(target.getNodeId()).orElseGet(EventLogReadStatus::new);
        status.nodeId = target.getNodeId();
        status.nodeLabel = target.getNodeLabel();
        status.address = target.getAddress().getHostAddress();
        status.location = target.getLocation();
        change.accept(status.forLog(packageName, log));
        try {
            jsonStore.put(Integer.toString(target.getNodeId()), mapper.writeValueAsString(status), CONTEXT);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void clear() {
        jsonStore.truncateContext(CONTEXT);
    }

    public void clear(int nodeId) {
        jsonStore.delete(Integer.toString(nodeId), CONTEXT);
    }

    /** Drops the entries of package/log pairs that are no longer configured; keys are "package/log" lower-cased. */
    public synchronized void retainOnly(Set<String> packageLogKeys) {
        for (EventLogReadStatus status : getAll()) {
            final int before = status.logs.size();
            status.logs.removeIf(l -> l.packageName == null || l.log == null
                    || !packageLogKeys.contains((l.packageName + "/" + l.log).toLowerCase()));
            if (status.logs.isEmpty()) {
                clear(status.nodeId);
            } else if (status.logs.size() != before) {
                try {
                    jsonStore.put(Integer.toString(status.nodeId), mapper.writeValueAsString(status), CONTEXT);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
