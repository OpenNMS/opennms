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

import java.util.Collection;

import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.model.SnmpCollectionSource;

/**
 * Persists plugin-contributed SNMP data collection groups into the DB tables
 * that back the runtime config. One {@code DatacollectionGroup} from a plugin
 * extension maps to one {@link SnmpCollectionSource} row whose {@code uploadedBy}
 * is {@link #PLUGIN_UPLOADED_BY}; the source's child entities (MIB groups,
 * resource types, system defs) are replaced wholesale on each sync.
 *
 * <p>Mirrors the eventconf {@code EventConfExtensionManager} pattern: the
 * marker column is {@code uploaded_by}, sources outside that marker are never
 * touched, and the source's {@code enabled} flag is preserved across syncs so
 * that an admin's disable intent survives plugin reloads.
 *
 * <p>Profile attachment is not handled here — matches XML migration semantics:
 * sources are inserted independently, and admins attach them to profiles
 * through the admin page when they want them active.
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
     * Plugin sources not present in {@code aggregated} are removed; new ones
     * are inserted; existing ones have their child entities (MIB groups,
     * resource types, system defs) replaced. The source's {@code enabled}
     * flag is preserved when the row already exists.
     *
     * <p>Runs under a single transaction.
     *
     * @return {@code true} if any row was inserted, updated, or deleted.
     */
    boolean syncPluginGroupsToDb(Collection<DatacollectionGroup> aggregated);

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
