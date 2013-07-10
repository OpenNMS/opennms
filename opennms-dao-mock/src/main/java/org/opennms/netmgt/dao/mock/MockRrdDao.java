package org.opennms.netmgt.dao.mock;

import java.io.File;
import java.io.InputStream;

import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.springframework.dao.DataAccessException;

public class MockRrdDao implements RrdDao {

    @Override
    public double getPrintValue(OnmsAttribute attribute, String cf, long start, long end) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public double[] getPrintValues(OnmsAttribute attribute, String rraConsolidationFunction, long startTimeInMillis, long endTimeInMillis, String... printFunctions) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public InputStream createGraph(String command, File workDir) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getGraphTopOffsetWithText() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getGraphLeftOffset() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getGraphRightOffset() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Double getLastFetchValue(OnmsAttribute attribute, int interval) throws DataAccessException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Double getLastFetchValue(OnmsAttribute attribute, int interval, int range) throws DataAccessException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
