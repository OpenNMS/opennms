/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;

public class DelegatingPersisterFactory implements PersisterFactory {

    private final List<PersisterFactory> delegates;

    public DelegatingPersisterFactory(List<PersisterFactory> delegates) {
        this.delegates = Objects.requireNonNull(delegates);
    }

    public DelegatingPersisterFactory(PersisterFactory delegate1, PersisterFactory delegate2) {
        this.delegates = Arrays.asList(delegate1, delegate2);
    }

    public DelegatingPersisterFactory(PersisterFactory... delegates) {
        this.delegates = Arrays.asList(delegates);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return new DelegatingPersister(delegates.stream()
            .map(pf -> pf.createPersister(params, repository))
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters, boolean forceStoreByGroup, boolean dontReorderAttributes) {
        return new DelegatingPersister(delegates.stream()
                .map(pf -> pf.createPersister(params, repository, dontPersistCounters, forceStoreByGroup, dontReorderAttributes))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }
}
