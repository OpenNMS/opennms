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
package org.opennms.netmgt.availability;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The daemon that keeps category availability snapshots current. It runs
 * the {@link AvailabilitySnapshotScheduler}; the web tier and the REST API
 * read the snapshots from the database.
 */
public final class AvailabilityDaemon extends AbstractServiceDaemon {

    public static final String LOG4J_CATEGORY = "availability";

    @Autowired
    private AvailabilitySnapshotScheduler m_scheduler;

    public AvailabilityDaemon() {
        super(LOG4J_CATEGORY);
        Logging.putPrefix(LOG4J_CATEGORY);
    }

    @Override
    protected synchronized void onInit() {
    }

    @Override
    protected synchronized void onStart() {
        m_scheduler.start();
    }

    @Override
    protected synchronized void onStop() {
        m_scheduler.stop();
    }

    @Override
    protected synchronized void onPause() {
        m_scheduler.stop();
    }

    @Override
    protected synchronized void onResume() {
        m_scheduler.start();
    }
}
