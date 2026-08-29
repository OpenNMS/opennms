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

import org.opennms.netmgt.model.EventConfSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EventConfSourceDao extends OnmsDao<EventConfSource, Long> {

    EventConfSource get(Long id);

    EventConfSource findByName(String name);

    List<EventConfSource> findAllEnabled();

    List<EventConfSource> findByVendor(String vendor);

    List<EventConfSource> findAllByFileOrder();

    Map<Long, String> getIdToNameMap();

    void saveOrUpdate(EventConfSource source);

    void delete(EventConfSource source);

    void deleteAll(final Collection<EventConfSource> list);

    void updateEnabledFlag(final Collection<Long> sourceIds, boolean enabled, boolean cascadeToEvents);

    Map<String, Object> filterEventConfSource(String filter, String sortBy, String order, Integer totalRecords,
                                              Integer offset, Integer limit);

    void deleteBySourceIds(List<Long> sourceIds);

    List<String> findAllNames();

    Integer findMaxFileOrder();

    /**
     * Allocates the {@code fileOrder} for a new source ({@code findMaxFileOrder + 1}, i.e. evaluated
     * before every existing source). Concurrent allocations are serialized by {@link #lockFileOrders()},
     * so two creators can never be handed the same value. Must be called inside a transaction.
     */
    Integer nextFileOrder();

    /**
     * Takes the transaction-scoped named lock that serializes every change to {@code fileOrder} values
     * (allocation and renumbering) - the DAO's {@code accessLocks} row, see {@code AbstractDaoHibernate#lock()}.
     * Released when the transaction ends.
     */
    void lockFileOrders();

    /**
     * Locks the source row ({@code SELECT ... FOR UPDATE}) for the rest of the current transaction.
     * Hold it before replacing, appending or compacting the source's events so concurrent writers
     * cannot interleave and produce duplicate event positions.
     */
    void lockForUpdate(Long sourceId);

    List<String> findAllVendors();
}
