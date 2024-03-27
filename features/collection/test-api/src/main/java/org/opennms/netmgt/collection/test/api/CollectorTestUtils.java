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
package org.opennms.netmgt.collection.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executors;

import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.rpc.utils.RpcTargetHelper;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.client.rpc.CollectorClientRpcModule;
import org.opennms.netmgt.collection.client.rpc.LocationAwareCollectorClientImpl;
import org.opennms.netmgt.collection.core.CollectionSpecification;
import org.opennms.netmgt.collection.core.DefaultCollectdInstrumentation;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.collection.support.DefaultServiceCollectorRegistry;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.test.FileAnticipator;

public abstract class CollectorTestUtils {

    public static LocationAwareCollectorClient createLocationAwareCollectorClient() {
        final DefaultServiceCollectorRegistry serviceCollectorRegistry = new DefaultServiceCollectorRegistry();
        final CollectorClientRpcModule collectorClientRpcModule = new CollectorClientRpcModule();
        collectorClientRpcModule.setServiceCollectorRegistry(serviceCollectorRegistry);
        collectorClientRpcModule.setExecutor(Executors.newSingleThreadExecutor());
        final MockRpcClientFactory rpcClientFactory = new MockRpcClientFactory();
        final LocationAwareCollectorClientImpl locationAwareCollectorClient = new LocationAwareCollectorClientImpl(rpcClientFactory);
        locationAwareCollectorClient.setRpcModule(collectorClientRpcModule);
        locationAwareCollectorClient.setRpcTargetHelper(new RpcTargetHelper());
        locationAwareCollectorClient.setEntityScopeProvider(new MockEntityScopeProvider());
        locationAwareCollectorClient.afterPropertiesSet();
        return locationAwareCollectorClient;
    }

    public static CollectionSpecification createCollectionSpec(String svcName, ServiceCollector svcCollector,
                                                               String collectionName,
                                                               ReadablePollOutagesDao pollOutagesDao, String className) {
        Objects.requireNonNull(pollOutagesDao);
        
        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        Parameter collectionParm = new Parameter();
        collectionParm.setKey("collection");
        collectionParm.setValue(collectionName);
        service.addParameter(collectionParm);
        pkg.addService(service);

        CollectionSpecification spec = new CollectionSpecification(pkg, svcName, svcCollector, new DefaultCollectdInstrumentation(),
                createLocationAwareCollectorClient(), pollOutagesDao, className);
        return spec;
    }

    public static void persistCollectionSet(RrdStrategy<?, ?> rrdStrategy, ResourceStorageDao resourceStorageDao,
            CollectionSpecification spec, CollectionSet collectionSet) {
        RrdRepository repository=spec.getRrdRepository("default");
        System.err.println("repository = " + repository);
        ServiceParameters params = spec.getServiceParameters();
        System.err.println("service parameters = " + params);

        RrdPersisterFactory persisterFactory = new RrdPersisterFactory();
        persisterFactory.setRrdStrategy(rrdStrategy);
        persisterFactory.setResourceStorageDao(resourceStorageDao);
        CollectionSetVisitor persister = persisterFactory.createPersister(params, repository);

        System.err.println("persister = " + persister);
        collectionSet.visit(persister);
    }

    public static void collectNTimes(RrdStrategy<?, ?> rrdStrategy, ResourceStorageDao resourceStorageDao,
            CollectionSpecification spec, CollectionAgent agent, int numUpdates) throws InterruptedException, CollectionException {

        for(int i = 0; i < numUpdates; i++) {
            // now do the actual collection
            CollectionSet collectionSet = spec.collect(agent);
            assertEquals("collection status", CollectionStatus.SUCCEEDED, collectionSet.getStatus());

            persistCollectionSet(rrdStrategy, resourceStorageDao, spec, collectionSet);

            System.err.println("COLLECTION "+i+" FINISHED");

            //need a one second time elapse to update the RRD
            Thread.sleep(1010);
        }
    }

    public static void failToCollectNTimes(RrdStrategy<?, ?> rrdStrategy, ResourceStorageDao resourceStorageDao,
            CollectionSpecification spec, CollectionAgent agent, int numUpdates) throws InterruptedException, CollectionException {

        for(int i = 0; i < numUpdates; i++) {
            // now do the actual collection
            CollectionSet collectionSet = spec.collect(agent);
            assertEquals("collection status", CollectionStatus.FAILED, collectionSet.getStatus());

            persistCollectionSet(rrdStrategy, resourceStorageDao, spec, collectionSet);

            System.err.println("COLLECTION "+i+" FINISHED");

            //need a one second time elapse to update the RRD
            Thread.sleep(1010);
        }
    }

    public static File anticipatePath(FileAnticipator fa, File rootDir, String... pathElements) {
        File parent = rootDir;
        assertTrue(pathElements.length > 0);
        for (String pathElement : pathElements) {
            parent = fa.expecting(parent, pathElement);
        }
        return parent;
    }

    public static String rrd(RrdStrategy<Object, Object> rrdStrategy, String file) {
        return file + rrdStrategy.getDefaultFileExtension();
    }
}
