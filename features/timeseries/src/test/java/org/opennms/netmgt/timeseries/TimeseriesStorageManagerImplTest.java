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
package org.opennms.netmgt.timeseries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.opennms.integration.api.v1.timeseries.InMemoryStorage;

public class TimeseriesStorageManagerImplTest {

    @Test
    public void shouldBindAndUnbindProperly() throws StorageException {
        ServiceLookup<Class<?>, String> lookup = Mockito.mock(ServiceLookup.class);
        TimeSeriesStorage storage1 = new InMemoryStorage();
        TimeSeriesStorage storage2 = new InMemoryStorage();

        // test for null values
        TimeseriesStorageManagerImpl manager = new TimeseriesStorageManagerImpl(lookup);
        manager.onUnbind(null, null); // should be ignored
        assertThrows(StorageException.class, manager::get);
        manager.onUnbind(storage1, null); // should be ignored
        assertThrows(StorageException.class, manager::get);

        // add multiple storages, the last added should be given back
        manager.onBind(storage1, null);
        assertEquals(storage1, manager.get());
        manager.onBind(storage2, null);
        assertEquals(storage2, manager.get());
        manager.onBind(storage1, null);
        assertEquals(storage2, manager.get()); // we expect storage 2 since the storage1 was added already and is thus ignored
        manager.onUnbind(storage1, null);
        manager.onUnbind(storage1, null); // should be ignored
        assertEquals(storage2, manager.get());
        manager.onBind(storage1, null);
        assertEquals(storage1, manager.get()); // we expect storage 1 since storage1 was newly added

        // remove all storage
        manager.onUnbind(storage1, null);
        manager.onUnbind(storage2, null);
        assertThrows(StorageException.class, manager::get);
    }
}
