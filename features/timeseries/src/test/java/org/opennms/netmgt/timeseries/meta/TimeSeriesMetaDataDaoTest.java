package org.opennms.netmgt.timeseries.meta;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils.toResourceId;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.netmgt.model.ResourcePath;

public class TimeSeriesMetaDataDaoTest {

    @Test
    public void cachingShouldWork() throws SQLException, StorageException, ExecutionException {
        TimeSeriesMetaDataDao dao = Mockito.spy(new TimeSeriesMetaDataDao(null, 100, 60));
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
