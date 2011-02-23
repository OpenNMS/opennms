package org.opennms.tools.rrd.converter;

import java.io.IOException;
import java.util.List;

public interface RrdDataSource {

    long getStartTime() throws IOException;
    long getEndTime() throws IOException;
    long getStep() throws IOException;
    List<RrdEntry> getData(int step) throws IOException;

}
