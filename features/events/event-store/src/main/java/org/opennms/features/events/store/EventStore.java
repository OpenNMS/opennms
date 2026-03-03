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
package org.opennms.features.events.store;

import java.util.List;
import java.util.Optional;

/**
 * Read-only store for querying archived events.
 *
 * <p>The archive is a materialized view of the Kafka fault-event topic,
 * populated by {@link EventArchiveWriter}. It provides query capabilities
 * for REST API, UI, and reporting use cases that previously queried the
 * now-removed {@code events} table directly.</p>
 */
public interface EventStore {

    /**
     * Retrieve a single event by its TSID.
     */
    Optional<StoredEvent> getByTsid(long tsid);

    /**
     * Find events matching the given criteria.
     *
     * @param criteria filter, sort, and pagination parameters
     * @return matching events, ordered by {@code event_time} per the criteria sort order
     */
    List<StoredEvent> findByCriteria(EventCriteria criteria);

    /**
     * Count events matching the given criteria (ignoring limit/offset).
     */
    long count(EventCriteria criteria);
}
