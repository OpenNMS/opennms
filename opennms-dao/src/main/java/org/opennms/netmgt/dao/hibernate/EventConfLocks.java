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
package org.opennms.netmgt.dao.hibernate;

import org.hibernate.Session;
import org.opennms.core.sysprops.SystemProperties;

/**
 * Lock hygiene shared by the event-conf DAOs.
 * <p>
 * Hibernate's {@code LockOptions#setTimeOut} is ignored by the PostgreSQL dialect (only NOWAIT and
 * SKIP LOCKED are supported), so a bounded wait has to be requested from the server itself:
 * {@code SET LOCAL lock_timeout} applies to every lock the current transaction then tries to take -
 * the {@code SELECT ... FOR UPDATE} on a source row, the named allocation lock and the row locks a
 * renumbering's UPDATEs take - and is discarded when the transaction ends. Without it a waiter blocks
 * forever behind a long-running holder (not a deadlock, so the server never intervenes) and pins its
 * HTTP worker thread.
 */
final class EventConfLocks {

    /** System property overriding the wait, in milliseconds; {@code 0} disables the timeout. */
    static final String LOCK_TIMEOUT_PROPERTY = "org.opennms.eventconf.lockTimeoutMs";

    static final long DEFAULT_LOCK_TIMEOUT_MS = 30_000L;

    private EventConfLocks() {
    }

    /** Bounds every lock wait of the current transaction; call before the first lock is requested. */
    static void applyLockTimeout(final Session session) {
        final long timeoutMs = SystemProperties.getLong(LOCK_TIMEOUT_PROPERTY, DEFAULT_LOCK_TIMEOUT_MS);
        if (timeoutMs > 0) {
            session.createNativeQuery("SET LOCAL lock_timeout = " + timeoutMs).executeUpdate();
        }
    }
}
