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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.netmgt.config.destinationPaths.Escalate;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;

/** Destination paths that the user-management services read in place of destinationPaths.xml. */
final class TestDestinationPaths {
    private final List<Path> m_paths = new CopyOnWriteArrayList<>();

    /** Makes the services read these paths until {@link #restore()}. */
    TestDestinationPaths() {
        UserMgmtRestSupport.setDestinationPaths(() -> m_paths);
    }

    /** Adds a path with {@code target} as its first target and, if not null, {@code escalationTarget} in an escalation. */
    void add(final String name, final String target, final String escalationTarget) {
        final Path path = new Path(name);
        path.addTarget(new Target(target, "javaEmail"));
        if (escalationTarget != null) {
            final Escalate escalate = new Escalate();
            escalate.setDelay("15m");
            escalate.addTarget(new Target(escalationTarget, "javaEmail"));
            path.addEscalate(escalate);
        }
        m_paths.add(path);
    }

    void clear() {
        m_paths.clear();
    }

    void restore() {
        UserMgmtRestSupport.setDestinationPaths(UserMgmtRestSupport.NOTIFD_DESTINATION_PATHS);
    }
}
