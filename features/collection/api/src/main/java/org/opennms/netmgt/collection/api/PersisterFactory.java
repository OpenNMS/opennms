/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
     * Creates a visitor that is used to persist attributes in a {@ CollectionSet}.
     *
     * @param params  used to determine if individual groups or resources in the collection set should be persisted
     * @param repository  used to the RRD persisters to build the appropriate RRD structures
     * @return a {@link Persister} that persists the attributes
     */
    public Persister createPersister(ServiceParameters params, RrdRepository repository);

    /**
     * Creates a visitor that is used to persist attributes in a {@ CollectionSet}.
     *
     * @param params  used to determine if individual groups or resources in the collection set should be persisted
     * @param repository  used to the RRD persisters to build the appropriate RRD structures
     * @param dontPersistCounters used to disable persistence for counters in order to try and avoid spikes
     * @param forceStoreByGroup forces the given {@ CollectionSet} to be persisted as a group
     * @param dontReorderAttributes store attributes in the order they are visited
     * @return a {@link Persister} that persists the attributes
     */
    public Persister createPersister(ServiceParameters params, RrdRepository repository,
            boolean dontPersistCounters, boolean forceStoreByGroup, boolean dontReorderAttributes);

}
