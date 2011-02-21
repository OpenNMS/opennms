package org.opennms.tools.rrd.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.jrobin.core.Archive;
import org.jrobin.core.FetchData;
import org.jrobin.core.Robin;
import org.jrobin.core.RrdDb;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.test.mock.MockLogAppender;

public class JRobinConverterTest {
    JRobinConverter m_converter = null;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_converter = new JRobinConverter();
        assertTrue(new File("src/test/rrds/90020/Se0").isDirectory());
        assertTrue(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd").isFile());
    }

    @Test
    public void testGetDsNames() throws Exception {
        final List<String> dsNames = JRobinConverter.getDsNames(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd"));
        LogUtils.debugf(this, "dsNames = %s", dsNames);
        assertTrue(dsNames.contains("ifInDiscards"));
        assertEquals(9, dsNames.size());
    }

    @Test
    public void testGetRras() throws Exception {
        final List<String> rras = JRobinConverter.getRras(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd"));
        assertEquals(5, rras.size());
    }

    @Test
    public void testCombine() throws Exception {
        final File groupFile = new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd");
        assertTrue(groupFile.isFile());

        final File newFile = JRobinConverter.createTempRrd(groupFile);
        JRobinConverter.consolidateRrdFile(groupFile, newFile);
        RrdDb newRrd = new RrdDb(newFile.getAbsolutePath());
        assertEquals(1298046000L, newRrd.getLastArchiveUpdateTime());
        Archive archive = newRrd.getArchive(0);
        Robin robin = archive.getRobin(8);
        assertEquals(4032, robin.getSize());
        assertEquals(Double.valueOf(28.386210058206633D), Double.valueOf(robin.getValue(robin.getSize() - 1)));
        double[] values = robin.getValues();
        for (int i = values.length - 1; i >= 0; i--) {
            LogUtils.debugf(this, "(0) value = %d/%f", i, values[i]);
        }

        archive = newRrd.getArchive(1);
        robin = archive.getRobin(3);
        
        final FetchData fd = newRrd.createFetchRequest("AVERAGE", archive.getStartTime(), archive.getEndTime()).fetchData();
        final long[] timestamps = fd.getTimestamps();
        values = fd.getValues("ifOutOctets");
        
        for (int i = 0; i < timestamps.length; i++) {
            LogUtils.debugf(this, "%s: %f", new Date(timestamps[i] * 1000L), values[i]);
        }
        
        /*
        LogUtils.warnf(this, "start time = %s", new Date(archive.getStartTime() * 1000L));
        LogUtils.warnf(this, "end time = %s", new Date(archive.getEndTime() * 1000L));
        assertEquals(1488, robin.getSize());
        values = robin.getValues();
        for (int i = values.length - 1; i >= 0; i--) {
            LogUtils.debugf(this, "(1) value = %d/%f", i, values[i]);
        }
        assertFalse(Double.isNaN(robin.getValue(robin.getSize() - 20)));
        assertEquals(Double.valueOf(27D), Double.valueOf(robin.getValue(robin.getSize() - 1)));
        */
    }

    @Test
    public void testScanRrds() throws Exception {
        final File topDirectory = new File("src/test/rrds");

        List<File> rrds = JRobinConverter.findRrds(topDirectory);
//        LogUtils.debugf(this, "RRDs = %s", rrds);
        assertTrue(rrds.size() > 0);
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd")));
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd")));
        
        rrds = JRobinConverter.findGroupRrds(topDirectory);
//        LogUtils.debugf(this, "group RRDs = %s", rrds);
        assertTrue(rrds.size() > 0);
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd")));
        assertFalse(rrds.contains(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd")));
    }

}
