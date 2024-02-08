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
package org.opennms.netmgt.collectd.tca;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.collectd.tca.TcaCollectionHandler.RESOURCE_TYPE_NAME;

import java.io.File;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.distributed.kvstore.blob.inmemory.InMemoryMapBlobStore;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public class TcaCollectionHandlerTest {
    @Test
    public void shouldSaveLastTimestamp() {
        // set up dependencies...
        SnmpCollectionAgent agent = Mockito.mock(SnmpCollectionAgent.class);
        RrdRepository repository = Mockito.mock(RrdRepository.class);
        when(repository.getRrdBaseDir()).thenReturn(new File("/"));
        ResourceTypesDao resourceTypesDao = Mockito.mock(ResourceTypesDao.class);
        when(resourceTypesDao.getResourceTypeByName(RESOURCE_TYPE_NAME)).thenReturn(Mockito.mock(ResourceType.class));
        LocationAwareSnmpClient locationAwareSnmpClient = Mockito.mock(LocationAwareSnmpClient.class);
        BlobStore blobStore = InMemoryMapBlobStore.withDefaultTicks();
        CollectionResource resource = Mockito.mock(CollectionResource.class);
        when(resource.getPath()).thenReturn(ResourcePath.fromString("aa/bb"));

        TcaCollectionHandler handler = new TcaCollectionHandler(agent, repository,
                resourceTypesDao, locationAwareSnmpClient, blobStore);

        // now test it
        long timestamp = 42L;
        handler.setLastTimestamp(resource, timestamp);
        assertEquals(timestamp, handler.getLastTimestamp(resource));
    }
}
