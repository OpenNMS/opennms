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
package org.opennms.netmgt.collection.support;

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
