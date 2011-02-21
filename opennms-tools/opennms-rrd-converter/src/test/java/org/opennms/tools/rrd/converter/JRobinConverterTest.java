package org.opennms.tools.rrd.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jrobin.core.Archive;
import org.jrobin.core.FetchData;
import org.jrobin.core.Robin;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.test.mock.MockLogAppender;

public class JRobinConverterTest {
    JRobinConverter m_converter = null;
    File m_workDir = null;
    private long m_baseTime = 1298046000L;
    private final File m_mib2Interfaces = new File(m_workDir, "mib2-interfaces.rrd");
    private final File m_ifInOctets     = new File(m_workDir, "ifInOctets.rrd");
    private final File m_ifOutOctets    = new File(m_workDir, "ifOutOctets.rrd");
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_converter = new JRobinConverter();
        m_workDir = createTempDir();
        createMockRrds();
    }

    @After
    public void tearDown() throws Exception {
        m_mib2Interfaces.delete();
        m_ifInOctets.delete();
        m_ifOutOctets.delete();
    }
    
    protected void createMockRrds() throws Exception {
        LogUtils.infof(this, "creating empty RRDs");
        initializeRrdLike(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd"), m_mib2Interfaces);
        initializeRrdLike(new File("src/test/rrds/90020/Se0/ifInOctets.rrd"), m_ifInOctets);
        initializeRrdLike(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd"), m_ifOutOctets);

        RrdDb ifInOctets  = new RrdDb(m_ifInOctets);
        RrdDb ifOutOctets = new RrdDb(m_ifOutOctets);
        RrdDb mib2        = new RrdDb(m_mib2Interfaces);

        Archive archive = ifInOctets.getArchive(0);
        Robin robin = archive.getRobin(0);
        robin.setValues(2.0D);
        archive = ifInOctets.getArchive(1);
        robin = archive.getRobin(0);
        robin.setValues(2.0D);
        ifInOctets.getHeader().setLastUpdateTime(m_baseTime);
        
        archive = ifOutOctets.getArchive(0);
        robin = archive.getRobin(0);
        robin.setValues(4.0D);
        archive = ifOutOctets.getArchive(1);
        robin = archive.getRobin(0);
        robin.setValues(4.0D);
        ifOutOctets.getHeader().setLastUpdateTime(m_baseTime);

        long currentValue = 3000;
        mib2.getHeader().setLastUpdateTime(m_baseTime);
        for (long i = (ifInOctets.getLastArchiveUpdateTime() + 300); i <= (ifInOctets.getLastArchiveUpdateTime() + 5100); i = i + 300) {
            Sample sample = mib2.createSample(i);
            sample.setValue("ifInOctets", currentValue);
            sample.setValue("ifOutOctets", (currentValue * 2));
            sample.update();
            
            currentValue += 300;
        }

        ifInOctets.close();
        ifOutOctets.close();
        mib2.close();
    }

    @Test
    public void testGetDsNames() throws Exception {
        final List<String> dsNames = JRobinConverter.getDsNames(m_mib2Interfaces);
        LogUtils.debugf(this, "dsNames = %s", dsNames);
        assertTrue(dsNames.contains("ifInDiscards"));
        assertEquals(9, dsNames.size());
    }

    @Test
    public void testGetRras() throws Exception {
        final List<String> rras = JRobinConverter.getRras(m_mib2Interfaces);
        assertEquals(5, rras.size());
    }

    @Test
    public void testCheckRawData() throws Exception {
        RrdDb ifInOctets = new RrdDb(m_ifInOctets, true);
        RrdDb ifOutOctets = new RrdDb(m_ifOutOctets, true);
        RrdDb mib2 = new RrdDb(m_mib2Interfaces, true);
        checkArchive(mib2, 4015, 4016, 2.0D, 0, 9, 8, "ifOutOctets");
        checkArchive(ifInOctets, null, 2322, 2.0D, 0, 1, 0, "ifInOctets");
        checkArchive(ifOutOctets, null, 4000, 4.0D, 0, 1, 0, "ifOutOctets");
        mib2.close();
        ifOutOctets.close();
        ifInOctets.close();
    }

    @Test
    public void testGetMatchingRrds() throws Exception {
        final List<File> matches = JRobinConverter.getMatchingGroupRrds(m_mib2Interfaces);
        assertEquals(2, matches.size());
    }
    
    @Test
    public void testCombine() throws Exception {
        final File newFile = JRobinConverter.createTempRrd(m_mib2Interfaces);
        JRobinConverter.consolidateRrdFile(m_mib2Interfaces, newFile);
        RrdDb newRrd = new RrdDb(newFile.getPath(), true);
        assertEquals(m_baseTime + 4800L, newRrd.getLastArchiveUpdateTime());
        Archive archive = newRrd.getArchive(0);
        Robin robin = archive.getRobin(newRrd.getDsIndex("ifInOctets"));
        assertEquals(4032, robin.getSize());
        assertEquals(Double.valueOf(2.0D), Double.valueOf(robin.getValue(robin.getSize() - 1)));

        archive = newRrd.getArchive(1);
        robin = archive.getRobin(newRrd.getDsIndex("ifOutOctets"));

        final FetchData fd = newRrd.createFetchRequest("AVERAGE", archive.getStartTime(), archive.getEndTime()).fetchData();
        final long[] timestamps = fd.getTimestamps();
        double[] values = fd.getValues("ifOutOctets");
        
        LogUtils.warnf(this, "start time = %s", new Date(archive.getStartTime() * 1000L));
        LogUtils.warnf(this, "end time = %s", new Date(archive.getEndTime() * 1000L));
        assertEquals(1488, robin.getSize());
        values = robin.getValues();
        for (int i = values.length - 1; i >= 0; i--) {
            LogUtils.debugf(this, "(1) %s = %d/%f", new Date(timestamps[i] * 1000L), i, values[i]);
        }
        assertFalse(Double.isNaN(robin.getValue(robin.getSize() - 20)));
        assertEquals(Double.valueOf(4.0D), Double.valueOf(robin.getValue(robin.getSize() - 1)));
    }

    @Test
    public void testScanRrds() throws Exception {
        final File topDirectory = new File("src/test/rrds");

        List<File> rrds = JRobinConverter.findRrds(topDirectory);
        assertTrue(rrds.size() > 0);
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd")));
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd")));
        
        rrds = JRobinConverter.findGroupRrds(topDirectory);
        assertTrue(rrds.size() > 0);
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd")));
        assertFalse(rrds.contains(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd")));
    }


    /**
     * Create a new temporary directory. Use something like
     * {@link #recursiveDelete(File)} to clean this directory up since it isn't
     * deleted automatically
     * @return  the new directory
     * @throws IOException if there is an error creating the temporary directory
     */
    private static File createTempDir() throws IOException
    {
        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        File newTempDir;
        final int maxAttempts = 9;
        int attemptCount = 0;
        do
        {
            attemptCount++;
            if(attemptCount > maxAttempts) {
                throw new IOException(
                        "The highly improbable has occurred! Failed to " +
                        "create a unique temporary directory after " +
                        maxAttempts + " attempts.");
            }
            final String dirName = UUID.randomUUID().toString();
            newTempDir = new File(sysTempDir, dirName);
        } while(newTempDir.exists());

        if (newTempDir.mkdirs()) {
            return newTempDir;
        } else {
            throw new IOException("Failed to create temp dir named " + newTempDir.getAbsolutePath());
        }
    }

    /**
     * Recursively delete file or directory
     * @param fileOrDir
     *          the file or dir to delete
     * @return
     *          true iff all files are successfully deleted
     */
    @SuppressWarnings("unused")
    private static boolean recursiveDelete(final File fileOrDir) {
        if(fileOrDir.isDirectory()) {
            for(final File innerFile : fileOrDir.listFiles()) {
                if(!recursiveDelete(innerFile)) {
                    return false;
                }
            }
        }

        return fileOrDir.delete();
    }

    private static void initializeRrdLike(final File fromRrd, final File toRrd) throws Exception {
        final RrdDb oldRrd = new RrdDb(fromRrd.getAbsolutePath(), true);

        final RrdDef rrdDef = oldRrd.getRrdDef();
        rrdDef.setPath(toRrd.getPath());
        rrdDef.setStartTime(0);
        final RrdDb newRrd = new RrdDb(rrdDef);
        newRrd.close();
    }

    protected void checkArchive(RrdDb rrd, final Integer nanSample, final Integer numberSample, final Double numberValue, final Integer archiveIndex, final Integer numDses, final Integer dsIndex, String dsName) throws RrdException, IOException {
        LogUtils.debugf(this, "checking archive %s for consistency", rrd);
        Archive archive = rrd.getArchive(archiveIndex);
        Map<String,Integer> indexes = JRobinConverter.getDsIndexes(rrd);
        assertEquals(numDses, Integer.valueOf(indexes.size()));
        Robin robin = archive.getRobin(dsIndex);
        if (nanSample == null) {
            for (final double value : robin.getValues()) {
                assertTrue(!Double.isNaN(value));
            }
        } else {
            assertTrue(Double.isNaN(robin.getValue(nanSample)));
        }
        assertEquals(numberValue, Double.valueOf(robin.getValue(numberSample)));

        // Make sure FetchData matches
        FetchData data = rrd.createFetchRequest("AVERAGE", archive.getStartTime(), archive.getEndTime()).fetchData();
        double[] values = data.getValues(dsName);
        if (nanSample == null) {
            for (final double value : values) {
                assertTrue(!Double.isNaN(value));
            }
        } else {
            assertTrue(Double.isNaN(values[nanSample]));
        }
        
        assertEquals(numberValue, Double.valueOf(values[numberSample]));
        /*
        long[] timestamps = data.getTimestamps();
        for (int i = 0; i < timestamps.length; i++) {
            LogUtils.debugf(this, "%s: %s = %f", rrd, new Date(timestamps[i] * 1000L), values[i]);
        }
        */
    }
    
}
