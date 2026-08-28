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
package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.EventConfEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EventConfEventDao extends OnmsDao<EventConfEvent, Long> {

    EventConfEvent get(Long id);

    List<EventConfEvent> findBySourceId(Long sourceId);

    EventConfEvent findByUei(String uei);

    List<EventConfEvent> findByUeiAndSourceId(String uei, Long sourceId);

    int countBySourceId(Long sourceId);

    List<EventConfEvent> findEnabledEvents();

    void deleteBySourceId(Long sourceId);

    void deleteAll(final Collection<EventConfEvent> list);

    List<EventConfEvent> filterEventConf(String uei, String vendor, String sourceName, int offset, int limit);

    void updateEventEnabledFlag(Long sourceId, List<Long> eventIds, boolean enabled);

    Map<String, Object> findBySourceId(Long sourceId, String eventFilter, String eventSortBy, String eventOrder, Integer totalRecords, Integer offset, Integer limit);

    void saveAll(Collection<EventConfEvent> events);

    EventConfEvent findBySourceIdAndEventId(Long sourceId,Long eventId);

    void deleteByEventIds(Long sourceId,List<Long> eventIds);

    List<EventConfEvent> findEventsByVendor(final String vendor);

    /**
     * @return the highest {@code eventOrder} within the given source, or 0 when the source has no events.
     */
    Integer findMaxEventOrder(Long sourceId);

    /**
     * Allocates the {@code eventOrder} for an event appended to the given source
     * ({@code findMaxEventOrder + 1}). The source row is locked for the rest of the current
     * transaction so that concurrent appenders to the same source are serialized and cannot
     * receive the same value. Must be called inside a transaction.
     */
    Integer nextEventOrder(Long sourceId);

    /**
     * Renumbers the events of the given source so that {@code eventOrder} is dense 1..N,
     * preserving the current relative order (by eventOrder, then id).
     */
    void compactEventOrder(Long sourceId);
}
