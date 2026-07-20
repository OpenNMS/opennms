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

import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.model.SnmpCollectionSource;

/**
 * Builds the in-memory SNMP data collection config from the database and
 * publishes it into {@link DataCollectionConfigDao}.
 *
 * <p>Initial load runs synchronously at startup so that downstream
 * components (collectd, pollerd) see a populated config before they start
 * scheduling work.
 *
 * <p>Post-startup reloads (triggered by CRUD on the REST layer and by plugin
 * extension sync) are submitted asynchronously via a single-threaded
 * executor, which serializes concurrent reload requests and keeps reloads
 * off the caller's transaction thread.
 *
 * <p>Implementation is {@link SnmpDataCollectionConfigLoaderImpl}. The
 * interface exists so OSGi consumers in other bundles get a JDK-proxyable
 * type (concrete classes need {@code ext:proxy-method="classes"} which is
 * best avoided when the boundary can be expressed as an interface).
 */
public interface SnmpDataCollectionConfigLoader {

    /**
     * Submit a reload on the dedicated single-threaded executor so it runs
     * outside any active transaction on the caller thread and serializes
     * concurrent requests.
     */
    void scheduleDataCollectionConfigReload();

    /**
     * Synchronous reload: materialize from DB and publish to the DAO. Most
     * production callers should use {@link #scheduleDataCollectionConfigReload()}
     * instead. Used directly by lifecycle hooks and tests.
     */
    void reloadDataCollectionConfigFromDb();

    /**
     * Materialize a {@link DatacollectionGroup} (the on-wire/import format)
     * from a persisted {@link SnmpCollectionSource} plus its child rows.
     * Used by the REST download endpoint to assemble the legacy XML shape
     * for a single source on demand.
     */
    DatacollectionGroup buildDataCollectionGroupFromDb(SnmpCollectionSource source);
}
