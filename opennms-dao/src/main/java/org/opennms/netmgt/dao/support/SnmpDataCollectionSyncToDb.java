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
package org.opennms.netmgt.dao.support;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.model.SnmpCollectionSource;

/**
 * Persists plugin-contributed SNMP data collection groups into the DB tables
 * that back the runtime config. One {@code DatacollectionGroup} from a plugin
 * extension maps to one {@link SnmpCollectionSource} row whose {@code uploadedBy}
 * is {@link #PLUGIN_UPLOADED_BY}; the source's child entities (MIB groups,
 * resource types, system defs) are replaced wholesale on each sync.
 *
 * <p>Plugin extensions declare a target snmp-collection name via
 * {@code SnmpCollectionExtension.getSnmpCollectionName()}. The sync uses that
 * key to auto-attach each source row to the matching profile (by adding the
 * source name to that profile's {@code source_names} JSON column) — mirrors
 * the pre-DB behavior in {@code DefaultDataCollectionConfigDao#translateConfig}
 * that auto-added plugin groups to the targeted snmp-collection via
 * {@code <include-collection>} entries. Sources whose target profile doesn't
 * exist remain detached; the operator can attach them later via the UI.
 *
 * <p>The {@code uploaded_by} marker column means user/migration-uploaded
 * sources are never touched by sync; the source's {@code enabled} flag is
 * preserved across syncs so an admin's disable intent survives plugin reloads.
 *
 * <p>Implementation is {@link SnmpDataCollectionSyncToDbImpl}. The interface
 * exists so that OSGi consumers in other bundles get a JDK-proxyable type
 * (concrete classes need {@code ext:proxy-method="classes"} which is best
 * avoided when the boundary can be expressed cleanly as an interface).
 */
public interface SnmpDataCollectionSyncToDb {

    /**
     * Marker value written to {@link SnmpCollectionSource#getUploadedBy()} for
     * rows owned by the plugin sync. Sources outside this marker are user
     * uploads or migration artifacts and are never touched by sync.
     */
    String PLUGIN_UPLOADED_BY = "opennms-plugins";

    /**
     * Reconcile the DB's plugin-marker rows with the supplied aggregated set.
     * The map key is the target snmp-collection name (matches the profile name
     * in the new world); the value is the list of {@code DatacollectionGroup}s
     * the plugin contributes to that target.
     *
     * <p>For each {@code (target, group)}:
     * <ul>
     *   <li>Upsert the source row (replacing children).</li>
     *   <li>Add the source name to the target profile's {@code source_names}
     *       (idempotent). If no profile of that name exists, log a WARN and
     *       leave the source detached.</li>
     * </ul>
     *
     * <p>Plugin-marker sources not present in any incoming entry are removed
     * (orphan cleanup). Non-plugin rows are untouched.
     *
     * <p>Runs under a single transaction.
     *
     * @return {@code true} if any row was inserted, updated, or deleted.
     */
    boolean syncPluginGroupsToDb(Map<String, List<DatacollectionGroup>> groupsByCollection);

    /**
     * Whether a source row is managed by the plugin sync. Callers in the REST
     * and UI layers use this to enforce read-only semantics on plugin rows.
     */
    static boolean isPluginSourced(final SnmpCollectionSource source) {
        return source != null && PLUGIN_UPLOADED_BY.equals(source.getUploadedBy());
    }

    /**
     * Convenience for guarding write paths: throws if the source is plugin-managed.
     * {@code op} appears in the error message for context.
     */
    static void requireNotPluginSourced(final SnmpCollectionSource source, final String op) {
        if (isPluginSourced(source)) {
            throw new IllegalArgumentException(op + " not allowed on plugin-sourced source '"
                    + source.getName() + "'; sources whose uploadedBy is '"
                    + PLUGIN_UPLOADED_BY + "' are managed by plugin sync and are read-only "
                    + "(enable/disable is still permitted).");
        }
    }
}
