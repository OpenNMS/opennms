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
import java.util.Objects;
import java.util.Optional;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The highest record number seen per node and log, kept in the JSON key-value store
 * so a restart neither replays nor skips records.
 */
public class EventLogCursorStore {

    private static final Logger LOG = LoggerFactory.getLogger(EventLogCursorStore.class);

    public static final String CONTEXT = "wsman-eventlog";

    private final JsonStore jsonStore;
    private final ObjectMapper mapper = new ObjectMapper();

    public EventLogCursorStore(JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    /** One key per node and log, so concurrent polls of one node never overwrite each other. */
    static String key(int nodeId, String logfile) {
        return nodeId + "/" + logfile.toLowerCase();
    }

    public Long get(int nodeId, String logfile) {
        final Optional<String> json = jsonStore.get(key(nodeId, logfile), CONTEXT);
        if (!json.isPresent()) {
            return null;
        }
        try {
            final Cursor cursor = mapper.readValue(json.get(), Cursor.class);
            return cursor.cursor;
        } catch (IOException e) {
            LOG.warn("Ignoring an unreadable cursor for node {} log {}: {}", nodeId, logfile, e.getMessage());
            return null;
        }
    }

    public void put(int nodeId, String logfile, long recordNumber) {
        final Cursor cursor = new Cursor();
        cursor.cursor = recordNumber;
        try {
            jsonStore.put(key(nodeId, logfile), mapper.writeValueAsString(cursor), CONTEXT);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void clear(int nodeId, String logfile) {
        jsonStore.delete(key(nodeId, logfile), CONTEXT);
    }

    public void clear(int nodeId) {
        final String prefix = nodeId + "/";
        for (String key : jsonStore.enumerateContext(CONTEXT).keySet()) {
            if (key.startsWith(prefix)) {
                jsonStore.delete(key, CONTEXT);
            }
        }
    }

    public static class Cursor {
        public Long cursor;
    }
}
