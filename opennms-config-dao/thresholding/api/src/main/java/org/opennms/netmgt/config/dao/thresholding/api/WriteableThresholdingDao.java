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
package org.opennms.netmgt.config.dao.thresholding.api;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.opennms.netmgt.config.dao.common.api.WriteableDao;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;

public interface WriteableThresholdingDao extends WriteableDao<ThresholdingConfig>, ReadableThresholdingDao {
    /**
     * A lock guarding the configuration held by this DAO. Readers that need a stable view across several
     * calls should hold this while reading.
     */
    Lock getReadLock();

    /**
     * A lock guarding the configuration held by this DAO. Every read-modify-write cycle against
     * {@link #getWriteableConfig()} must hold this for its whole duration, otherwise a concurrent writer
     * (or a reload triggered by one) silently discards the mutation.
     */
    Lock getWriteLock();

    /**
     * Run a mutation against the writeable configuration while holding the write lock. Note that the
     * configuration is passed in fresh, so callers must not retain the reference beyond the consumer.
     */
    default void withWriteLock(Consumer<ThresholdingConfig> consumerWithLock) {
        getWriteLock().lock();

        try {
            consumerWithLock.accept(getWriteableConfig());
        } finally {
            getWriteLock().unlock();
        }
    }
}
