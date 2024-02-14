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
package org.opennms.netmgt.collection.api;

import org.opennms.netmgt.rrd.RrdRepository;

/**
 * Used to instantiate an appropriate {@link CollectionSetVisitor} whose role is to
 * persist the attributes in a {@link CollectionSet}.
 *
 * @author jwhite
 */
public interface PersisterFactory {

    /**
     * Creates a visitor that is used to persist attributes in a {@link CollectionSet}.
     *
     * @param params  used to determine if individual groups or resources in the collection set should be persisted
     * @param repository  used to the RRD persisters to build the appropriate RRD structures
     * @return a {@link Persister} that persists the attributes
     */
    public Persister createPersister(ServiceParameters params, RrdRepository repository);

    /**
     * Creates a visitor that is used to persist attributes in a {@link CollectionSet}.
     *
     * @param params  used to determine if individual groups or resources in the collection set should be persisted
     * @param repository  used to the RRD persisters to build the appropriate RRD structures
     * @param dontPersistCounters used to disable persistence for counters in order to try and avoid spikes
     * @param forceStoreByGroup forces the given {@link CollectionSet} to be persisted as a group
     * @param dontReorderAttributes store attributes in the order they are visited
     * @return a {@link Persister} that persists the attributes
     */
    public Persister createPersister(ServiceParameters params, RrdRepository repository,
            boolean dontPersistCounters, boolean forceStoreByGroup, boolean dontReorderAttributes);

}
