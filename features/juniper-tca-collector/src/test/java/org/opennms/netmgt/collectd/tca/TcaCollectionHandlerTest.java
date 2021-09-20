/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
