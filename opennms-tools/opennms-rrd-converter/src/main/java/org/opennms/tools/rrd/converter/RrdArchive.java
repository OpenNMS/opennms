package org.opennms.tools.rrd.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jrobin.core.Archive;
import org.jrobin.core.Robin;
import org.springframework.util.Assert;

public class RrdArchive implements RrdDataSource {

    private Archive m_archive;
    private List<String> m_dsNames;

    public RrdArchive(final Archive archive, final List<String> dsNames) {
        m_archive = archive;
        m_dsNames = dsNames;
    }

    public Archive getArchive() {
        return m_archive;
    }

    protected boolean isAverage() throws IOException {
        return getArchive().getConsolFun().equals("AVERAGE");
    }

    public List<RrdEntry> getData(final int step) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>();
        final long arcStep = m_archive.getArcStep();
        Assert.isTrue(arcStep % step == 0, "archive step (" + arcStep + ") must be evenly divisible by step");
        final long repeat = arcStep / step;
        for (int row = 0; row < m_archive.getRows(); row++) {
            for (int repeatedRow = 0; repeatedRow < repeat; repeatedRow++) {
                
                final RrdEntry entry = new RrdEntry(getStartTime() + ((row * arcStep) + (repeatedRow * step)), m_dsNames);
                for (int i = 0; i < m_dsNames.size(); i++) {
                    final Robin r = m_archive.getRobin(i);
                    entry.setValue(m_dsNames.get(i), r.getValue(row));
                }
                entries.add(entry);
            }
        }
        return entries;
    }

    public long getStep() throws IOException {
        return m_archive.getArcStep();
    }
    
    public long getStartTime() throws IOException {
        return m_archive.getStartTime();
    }

    public long getEndTime() throws IOException {
        return m_archive.getEndTime();
    }
}
