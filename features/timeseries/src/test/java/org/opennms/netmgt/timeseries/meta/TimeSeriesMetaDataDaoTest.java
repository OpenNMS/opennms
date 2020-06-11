/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2020 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.timeseries.meta;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils.toResourceId;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.sql.DataSource;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.netmgt.model.ResourcePath;

import com.codahale.metrics.MetricRegistry;

public class TimeSeriesMetaDataDaoTest {

    @Test
    public void cachingShouldWork() throws SQLException, StorageException, ExecutionException {
        TimeSeriesMetaDataDao dao = Mockito.spy(new TimeSeriesMetaDataDao(mock(DataSource.class), 100, 60, new MetricRegistry()));
        Mockito.doNothing().when(dao).storeUncached(anyCollection());
        ResourcePath resourcePathA = new ResourcePath("a", "b", "c");
        MetaData meta = new MetaData(toResourceId(resourcePathA).toString(), "key", "value");
        dao.store(Arrays.asList(meta, meta));
        dao.store(Collections.singletonList(meta));
        dao.store(Collections.singletonList(meta));
        dao.getForResourcePath(resourcePathA);
        // if the cache works correctly we should have only one database call:
        Mockito.verify(dao, times(1)).storeUncached(anyCollection());
    }
}
