package org.opennms.tools.rrd.converter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;

public class RrdDatabase implements RrdDataSource {
    private SortedMap<Long,RrdEntry> m_entryMap = new TreeMap<Long,RrdEntry>();
    private RrdDb m_rrd;

    public RrdDatabase(final RrdDb rrd) throws IOException {
        m_rrd = rrd;
    }

    protected void createSamples(final RrdDb outputRrd) throws IOException, RrdException {
        for (final long sampleTime : m_entryMap.keySet()) {
            final RrdEntry rrdEntry = m_entryMap.get(sampleTime);
            rrdEntry.createSample(outputRrd);
        }
    }

    protected void addSample(final Long current, final String dsName, final double sample) throws IOException {
        RrdEntry rrdEntry = m_entryMap.get(current);
        if (rrdEntry == null) {
            rrdEntry = new RrdEntry(current, Arrays.asList(m_rrd.getDsNames()));
            m_entryMap.put(current, rrdEntry);
        }

        rrdEntry.setValue(dsName, sample);
    }

    public long getStartTime() throws IOException {
        long startTime = Long.MAX_VALUE;
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            startTime = Math.min(m_rrd.getArchive(i).getStartTime(), startTime);
        }
        return startTime;
    }

    public long getEndTime() throws IOException {
        long endTime = 0;
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            endTime = Math.max(m_rrd.getArchive(i).getEndTime(), endTime);
        }
        return endTime;
    }

    public long getStep() throws IOException {
        return m_rrd.getHeader().getStep();
    }

    public List<RrdEntry> getData(int step) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
