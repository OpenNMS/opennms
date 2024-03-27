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
package org.opennms.features.config.osgi.del;

import java.util.Objects;
import java.util.Set;

/**
 * We hard code here the migrated services.
 * This is necessary since we don't have access to CM when we need to make a decision about to whom to delegate to.
 * We can't use the start of the pid name ("org.opennms") since features might be started from fileinstall before
 * cm is available.
 * This class can be deleted once we have moved all opennms plugins to cm.
 * Migrated services come in two flavors: simple and multi. Simple services have only one instance, multi have several instances.
 */
public class MigratedServices {

    final public static Set<String> PIDS_SINGLE_INSTANCE = Set.of(
            "org.opennms.features.datachoices"
            // add you migrated service here...
    );

    final public static Set<String> PIDS_MULTI_INSTANCE = Set.of(
            // "org.opennms.netmgt.graph.provider.graphml"
            // ... or here
    );

    public static boolean isMigrated(final String pid) {
        Objects.requireNonNull(pid);
        return PIDS_SINGLE_INSTANCE.contains(pid) || isMultiInstanceService(pid);
    }

    public static boolean isMultiInstanceService(final String pid) {
        Objects.requireNonNull(pid);
        return PIDS_MULTI_INSTANCE.stream()
                .anyMatch(pid::startsWith);
    }
}
