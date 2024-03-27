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
package org.opennms.features.apilayer.collectors;

import java.io.File;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import org.opennms.features.apilayer.common.collectors.CollectionSetMapper;
import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.CollectionSetPersistenceService;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;

public class CollectionSetPersistenceServiceImpl implements CollectionSetPersistenceService {
    private static final ServiceParameters EMPTY_SERVICE_PARAMETERS = new ServiceParameters(Collections.emptyMap());
    private static final RrdRepository DEFAULT_RRD_REPOSITORY;

    static {
        // Use some default RRD repository settings
        final RrdRepository repository = new RrdRepository();
        repository.setStep(300);
        repository.setHeartBeat(repository.getStep() * 2);
        repository.setRraList(Arrays.asList(
                "RRA:AVERAGE:0.5:1:2016",
                "RRA:AVERAGE:0.5:12:1488",
                "RRA:AVERAGE:0.5:288:366",
                "RRA:MAX:0.5:288:366",
                "RRA:MIN:0.5:288:366"
        ));
        DEFAULT_RRD_REPOSITORY = repository;
    }

    private final CollectionAgentFactory collectionAgentFactory;
    private final PersisterFactory persisterFactory;

    public CollectionSetPersistenceServiceImpl(CollectionAgentFactory collectionAgentFactory, PersisterFactory persisterFactory) {
        this.collectionAgentFactory = Objects.requireNonNull(collectionAgentFactory);
        this.persisterFactory = Objects.requireNonNull(persisterFactory);
    }

    @Override
    public void persist(int nodeId, InetAddress iface, CollectionSet collectionSet) {
        persist(nodeId, iface, collectionSet, DEFAULT_RRD_REPOSITORY);
    }

    @Override
    public void persist(int nodeId, InetAddress iface, CollectionSet collectionSet, org.opennms.integration.api.v1.collectors.RrdRepository repository) {
        persist(nodeId, iface, collectionSet, toRepository(repository));
    }

    private void persist(int nodeId, InetAddress iface, CollectionSet collectionSet, RrdRepository repository) {
        final CollectionAgent agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId), iface);
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        final org.opennms.netmgt.collection.api.CollectionSet internalCollectionSet = CollectionSetMapper.buildCollectionSet(builder, collectionSet);

        // Assume we're dealing with node level resources and not response time
        repository.setRrdBaseDir(new File(ResourceTypeUtils.DEFAULT_RRD_ROOT, ResourceTypeUtils.SNMP_DIRECTORY));

        // Create the persister
        final Persister persister = persisterFactory.createPersister(EMPTY_SERVICE_PARAMETERS, repository);

        // Persist
        internalCollectionSet.visit(persister);
    }

    private static RrdRepository toRepository(org.opennms.integration.api.v1.collectors.RrdRepository repository) {
        final RrdRepository rrdRepository = new RrdRepository();
        rrdRepository.setStep(repository.getStep());
        rrdRepository.setHeartBeat(repository.getHeartbeat());
        rrdRepository.setRraList(repository.getRRAs());
        return rrdRepository;
    }

}