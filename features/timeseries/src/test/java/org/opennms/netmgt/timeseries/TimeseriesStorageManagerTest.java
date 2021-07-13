/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.memory.InMemoryStorage;

public class TimeseriesStorageManagerTest {

    @Test
    public void shouldBindAndUnbindProperly() {
        ServiceLookup<Class<?>, String> lookup = Mockito.mock(ServiceLookup.class);
        TimeSeriesStorage storage1 = new InMemoryStorage();
        TimeSeriesStorage storage2 = new InMemoryStorage();

        // test for null values
        TimeseriesStorageManager manager = new TimeseriesStorageManager(lookup);
        manager.onUnbind(null, null); // should be ignored
        assertNull(manager.get());
        manager.onUnbind(storage1, null); // should be ignored
        assertNull(manager.get());

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
        assertNull(manager.get());
    }
}
