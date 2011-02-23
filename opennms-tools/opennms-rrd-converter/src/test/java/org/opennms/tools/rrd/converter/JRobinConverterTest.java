package org.opennms.tools.rrd.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jrobin.core.Archive;
import org.jrobin.core.FetchData;
import org.jrobin.core.Robin;
import org.jrobin.core.RrdBackendFactory;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.test.mock.MockLogAppender;

public class JRobinConverterTest {
    JRobinConverter m_converter = null;
    File m_workDir = new File("target/rrd");
    private static final long MILLIS_PER_HOUR = 3600L * 1000L;
    private static final long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;
    private static final long MILLIS_PER_YEAR = 366L * MILLIS_PER_DAY;
    private long m_baseTime = 1298046000L;
    private final File m_mib2Interfaces = new File(m_workDir, "mib2-interfaces.rrd");
    private final File m_ifInOctets     = new File(m_workDir, "ifInOctets.rrd");
    private final File m_ifOutOctets    = new File(m_workDir, "ifOutOctets.rrd");
    private final File m_sineFull       = new File(m_workDir, "sine.rrd");
    private final File m_sineSource     = new File(m_workDir, "a.rrd");
    private final File m_variation      = new File(m_workDir, "variation.rrd");
    
    @BeforeClass
    public static void setFactory() throws RrdException {
        RrdBackendFactory.setDefaultFactory("MNIO");
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_converter = new JRobinConverter();
//        m_workDir = createTempDir();
        m_workDir.mkdirs();
        createMockRrds();
    }

    @After
    public void tearDown() throws Exception {
        m_mib2Interfaces.delete();
        m_ifInOctets.delete();
        m_ifOutOctets.delete();
//        m_sineFull.delete();
//        m_sineSource.delete();
//        m_variation.delete();
        System.err.println("sineFull = " + m_sineFull.getAbsolutePath());
        System.err.println("sineSource = " + m_sineSource.getAbsolutePath());
        System.err.println("variation = " + m_variation.getAbsolutePath());
    }
    
    protected void createMockRrds() throws Exception {
        LogUtils.infof(this, "creating empty RRDs");
        
        m_mib2Interfaces.delete();
        m_ifInOctets.delete();
        m_ifOutOctets.delete();
        m_sineFull.delete();
        m_sineSource.delete();
        m_variation.delete();

        initializeRrdLike(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd"), m_mib2Interfaces);
        initializeRrdLike(new File("src/test/rrds/90020/Se0/ifInOctets.rrd"), m_ifInOctets);
        initializeRrdLike(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd"), m_ifOutOctets);
        
        initializeGroupRrd(m_sineFull);
        initializeSingleRrd(m_sineSource);
        initializeGroupRrd(m_variation);

        RrdDb ifInOctets  = new RrdDb(m_ifInOctets);
        RrdDb ifOutOctets = new RrdDb(m_ifOutOctets);
        RrdDb mib2        = new RrdDb(m_mib2Interfaces);
        RrdDb sineFull    = new RrdDb(m_sineFull);
        RrdDb sineSource  = new RrdDb(m_sineSource);
        RrdDb variation   = new RrdDb(m_variation);

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
        
        long start = ((m_baseTime * 1000L) - (MILLIS_PER_DAY * 56L));
        Function bigSine = new Sin(start, 15, -10, MILLIS_PER_DAY * 7L);
        Function smallSine = new Sin(start, 7, 5, MILLIS_PER_DAY * 2L);
        Function moSuccessRate = new Cos(start, .5, .3, MILLIS_PER_DAY);
        Function mtSuccessRate = new Cos(start, .5, -.2, 2*MILLIS_PER_DAY);

        Function moAttempts = new Counter(0, bigSine);
        Function moCompletes = new Counter(0, new Times(moSuccessRate, bigSine));
        
        Function mtAttempts = new Counter(0, smallSine);
        Function mtCompletes = new Counter(0, new Times(mtSuccessRate, smallSine));

        long timestamp = start - 300000;
        for(; timestamp <= ((m_baseTime * 1000L) - (MILLIS_PER_DAY * 28L)); timestamp += 300000) {
            Sample sample = sineSource.createSample(timestamp/1000L);
            double value = moAttempts.evaluate(timestamp);
            sample.setValue("a", value);
            sample.update();
        }
        for(; timestamp <= (m_baseTime * 1000L); timestamp += 300000) {
            Sample sample = sineFull.createSample(timestamp/1000L);
            double value = mtAttempts.evaluate(timestamp);
            sample.setValue("a", value);
            sample.update();
        }

        final long end = (((m_baseTime * 1000L) / MILLIS_PER_DAY) * MILLIS_PER_DAY);
        start = (end - MILLIS_PER_YEAR);
        Function sequence = new AverageSequence(300, 10);
        Function sequenceCounter = new Counter(0, sequence);
        timestamp = start - 300000;
        for (; timestamp <= end; timestamp += 300000) {
            Sample sample = variation.createSample(timestamp/1000L);
            double value = sequenceCounter.evaluate(timestamp);
            sample.setValue("a", value);
//            LogUtils.debugf(this, "sample = %s", sample);
            sample.update();
        }
        
        ifInOctets.close();
        ifOutOctets.close();
        mib2.close();
        
        sineFull.close();
        sineSource.close();
        variation.close();
    }

    private void initializeGroupRrd(final File rrd) throws RrdException, IOException {
        RrdDef rrdDef = new RrdDef(rrd.getAbsolutePath());
        rrdDef.setStartTime(0);
        rrdDef.addDatasource("DS:a:COUNTER:600:0:U");
        rrdDef.addDatasource("DS:b:COUNTER:600:0:U");
        rrdDef.addArchive("RRA:AVERAGE:0.5:1:4032");
        rrdDef.addArchive("RRA:AVERAGE:0.5:12:1488");
        rrdDef.addArchive("RRA:AVERAGE:0.5:288:366");
        RrdDb  db = new RrdDb(rrdDef);
        db.close();
    }

    private void initializeSingleRrd(final File rrd) throws RrdException, IOException {
        RrdDef rrdDef = new RrdDef(rrd.getAbsolutePath());
        rrdDef.setStartTime(0);
        rrdDef.addDatasource("DS:a:COUNTER:600:0:U");
        rrdDef.addArchive("RRA:AVERAGE:0.5:1:8928");
        rrdDef.addArchive("RRA:AVERAGE:0.5:12:8784");
        RrdDb  db = new RrdDb(rrdDef);
        db.close();
    }
    
    @Test
    public void testGetDsNames() throws Exception {
        final List<String> dsNames = m_converter.getDsNames(m_mib2Interfaces);
        LogUtils.debugf(this, "dsNames = %s", dsNames);
        assertTrue(dsNames.contains("ifInDiscards"));
        assertEquals(9, dsNames.size());
    }

    @Test
    public void testGetRras() throws Exception {
        final List<String> rras = m_converter.getRras(m_mib2Interfaces);
        assertEquals(5, rras.size());
    }

    @Test
    public void testSine() throws Exception {
        final File newFile = m_converter.createTempRrd(m_sineFull);
        try {
            m_converter.consolidateRrdFile(m_sineSource, newFile);
        } finally {
//            newFile.delete();
        }
        System.err.println("newFile = " + newFile.getAbsolutePath());
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
        final List<File> matches = m_converter.getMatchingGroupRrds(m_mib2Interfaces);
        assertEquals(2, matches.size());
    }
    
    @Test
    public void testCombine() throws Exception {
        final File newFile = m_converter.createTempRrd(m_mib2Interfaces);
        try {
            m_converter.consolidateRrdFile(m_mib2Interfaces, newFile);
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
        } finally {
            newFile.delete();
        }
    }

    @Test
    public void testFetch() throws Exception {
        RrdDb rrd = new RrdDb(m_sineFull);
        final long endTime = rrd.getLastArchiveUpdateTime();
        final long startTime = endTime - (60L * 60L * 24L * 365L);
        final FetchData fd = rrd.createFetchRequest("AVERAGE", startTime, endTime, 300).fetchData();
        double[] values = fd.getValues("a");
        long[] timestamps = fd.getTimestamps();
        
        for (int i = 0; i < timestamps.length; i++) {
            LogUtils.debugf(this, "%s = %f", new Date(timestamps[i] * 1000L), values[i]);
        }
    }
    
    @Test
    public void testRrdArchive() throws Exception {
        RrdDb rrd = new RrdDb(m_variation);
        RrdArchive archive = new RrdArchive(rrd.getArchive(0), Arrays.asList(rrd.getDsNames()));

        final List<RrdEntry> entries = archive.getData(300);
        assertEquals(4032, entries.size());
        for (final RrdEntry entry : entries) {
            LogUtils.debugf(this, "entry = %s", entry);
        }
    }

    @Test
    public void testScanRrds() throws Exception {
        final File topDirectory = new File("src/test/rrds");

        List<File> rrds = m_converter.findRrds(topDirectory);
        assertTrue(rrds.size() > 0);
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd")));
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd")));
        
        rrds = m_converter.findGroupRrds(topDirectory);
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
        Map<String,Integer> indexes = m_converter.getDsIndexes(rrd);
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

    interface Function {
        double evaluate(long timestamp);
    }
    
    class Sin implements Function {
        
        long m_startTime;
        double m_offset;
        double m_amplitude;
        double m_period;
        double m_factor;
        
        Sin(long startTime, double offset, double amplitude, double period) {
            m_startTime = startTime;
            m_offset = offset;
            m_amplitude = amplitude;
            m_period = period;
            m_factor = 2 * Math.PI / period;
        }
        
        public double evaluate(long timestamp) {
            long x = timestamp - m_startTime;
            double ret = (m_amplitude * Math.sin(m_factor * x)) + m_offset;
//            System.out.println("Sin("+ x + ") = " + ret);
            return ret;
        }
    }
    
    class Cos implements Function {
        
        long m_startTime;
        double m_offset;
        double m_amplitude;
        double m_period;
        
        double m_factor;
        
        Cos(long startTime, double offset, double amplitude, double period) {
            m_startTime = startTime;
            m_offset = offset;
            m_amplitude = amplitude;
            m_period = period;
            
            m_factor = 2 * Math.PI / period;
        }
        
        public double evaluate(long timestamp) {
            long x = timestamp - m_startTime;
            double ret = (m_amplitude * Math.cos(m_factor * x)) + m_offset;
            System.out.println("Cos("+ x + ") = " + ret);
            return ret;
        }
    }
    
    class Times implements Function {
        Function m_a;
        Function m_b;
        
        Times(Function a, Function b) {
            m_a = a;
            m_b = b;
        }

        public double evaluate(long timestamp) {
            return m_a.evaluate(timestamp)*m_b.evaluate(timestamp);
        }
    }
    
    class Counter implements Function {
        double m_prevValue;
        Function m_function;
        
        Counter(double initialValue, Function function) {
            m_prevValue = initialValue;
            m_function = function;
        }

        public double evaluate(long timestamp) {
            double m_diff = m_function.evaluate(timestamp);
            m_prevValue += m_diff;
            return m_prevValue;
        }
        
    }

    class AverageSequence implements Function {
        private long m_baseline;
        private long m_variation;

        public AverageSequence(long baseline, long variation) {
            m_baseline = baseline;
            m_variation = variation;
        }

        public double evaluate(long timestamp) {
            long i = (timestamp % MILLIS_PER_DAY) / 300000;
            long h = i / 12;
            final long j = i % 12;
            final double result = m_baseline + (m_variation * (h-12)) + (j - 6);
//            final double result = m_baseline + (m_variation * (h-12)) + (j - 6);
//            LogUtils.debugf(this, "i = %d, h = %d, j = %d, result = %f", i, h, j, result);
            return result;
        }
        
    }
}
