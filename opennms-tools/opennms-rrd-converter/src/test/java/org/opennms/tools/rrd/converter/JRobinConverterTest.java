package org.opennms.tools.rrd.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private static final double ACCEPTABLE_DOUBLE_DELTA = 0.00000000001;
    JRobinConverter m_converter = null;
    File m_workDir = new File("target/rrd");
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final long SECONDS_PER_DAY = 24L * SECONDS_PER_HOUR;
    private static final long SECONDS_PER_YEAR = 366L * SECONDS_PER_DAY;
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
//        m_mib2Interfaces.delete();
//        m_ifInOctets.delete();
//        m_ifOutOctets.delete();
//        m_sineFull.delete();
//        m_sineSource.delete();
//        m_variation.delete();
    }
    
    protected void createMockRrds() throws Exception {
        LogUtils.infof(this, "creating empty RRDs");
        
        m_sineFull.delete();
        m_sineSource.delete();
        m_variation.delete();

        initializeGroupRrd(m_sineFull);
        initializeSingleRrd(m_sineSource);
        initializeGroupRrd(m_variation);

        RrdDb sineFull    = new RrdDb(m_sineFull);
        RrdDb sineSource  = new RrdDb(m_sineSource);
        RrdDb variation   = new RrdDb(m_variation);

        long start = (m_baseTime - (SECONDS_PER_DAY * 56L));
        Function bigSine = new Sin(start, 15, -10, SECONDS_PER_DAY * 7L);
        Function smallSine = new Sin(start, 7, 5, SECONDS_PER_DAY * 2L);

        Function bigSineCounter = new Counter(0, bigSine);
        Function smallSineCounter = new Counter(0, smallSine);

        long timestamp = start - 300L;
        for(; timestamp <= (m_baseTime - (SECONDS_PER_DAY * 28L)); timestamp += 300L) {
            Sample sample = sineSource.createSample(timestamp);
            double value = bigSineCounter.evaluate(timestamp);
            sample.setValue("a", value);
            sample.update();
        }
        for(; timestamp <= m_baseTime; timestamp += 300L) {
            Sample sample = sineFull.createSample(timestamp);
            double value = smallSineCounter.evaluate(timestamp);
            sample.setValue("a", value);
            sample.update();
        }

        final long end = getMidnightInSeconds(m_baseTime);
        start = (end - SECONDS_PER_YEAR);
        Function sequence = new AverageSequence(300, 10);
        Function sequenceCounter = new Counter(0, sequence);
        timestamp = start - 300L;
        for (; timestamp <= end; timestamp += 300L) {
            Sample sample = variation.createSample(timestamp);
            double value = sequenceCounter.evaluate(timestamp);
            sample.setValue("a", value);
            sample.update();
        }

        sineFull.close();
        sineSource.close();
        variation.close();
    }

    protected long getMidnightInSeconds(final long seconds) {
        return (((seconds) / SECONDS_PER_DAY) * SECONDS_PER_DAY);
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
    public void testGetDsNames() throws Exception {
        final List<String> dsNames = m_converter.getDsNames(m_sineFull);
        LogUtils.debugf(this, "dsNames = %s", dsNames);
        assertTrue(dsNames.contains("a"));
        assertEquals(2, dsNames.size());
    }

    @Test
    public void testGetRras() throws Exception {
        final List<String> rras = m_converter.getRras(m_sineFull);
        assertEquals(3, rras.size());
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

    @Test
    public void testGetMatchingRrds() throws Exception {
        final List<File> matches = m_converter.getMatchingGroupRrds(m_sineFull);
        assertEquals(1, matches.size());
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
    public void testRrdArchiveZero() throws Exception {
        RrdDb rrd = new RrdDb(m_variation);
        BaseRrdDataSource archive = new RrdArchive(rrd.getArchive(0), Arrays.asList(rrd.getDsNames()));
        final int expectedArchiveSize = 4032;
        final int expectedArchiveStep = 300;

        final long end = getMidnightInSeconds(m_baseTime);
        final long start = (end - ((expectedArchiveSize - 1) * expectedArchiveStep));

        assertEquals(end, archive.getEndTime());
        assertEquals(start, archive.getStartTime());
        assertEquals(expectedArchiveStep, archive.getNativeStep());

        assertEquals(0.5833333333333334D, archive.getDataAt(start).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.5866666666666667D, archive.getDataAt(start + 300).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.58D, archive.getDataAt(end).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);

        List<RrdEntry> entries = archive.getData(expectedArchiveStep);
        assertEquals(expectedArchiveSize, entries.size());
        assertEquals(start, entries.get(0).getTimestamp());
        assertEquals(end, entries.get(expectedArchiveSize - 1).getTimestamp());
        assertEquals(0.5833333333333334D, entries.get(0).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.5866666666666667D, entries.get(1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.3833333333333334D, entries.get(expectedArchiveSize - 2).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.58D, entries.get(expectedArchiveSize - 1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertTrue(Double.isNaN(entries.get(0).getValue("b")));

        assertEquals(0.5833333333333334D, archive.getDataAt(start + 150).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.5866666666666667D, archive.getDataAt(start + 450).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.58D, archive.getDataAt(end + 150).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);

        List<RrdEntry> halfEntries = archive.getData(expectedArchiveStep / 2);
        assertEquals(expectedArchiveSize * 2, halfEntries.size());
        assertEquals(start, halfEntries.get(0).getTimestamp());
        assertEquals(end + (expectedArchiveStep / 2), halfEntries.get((expectedArchiveSize * 2) - 1).getTimestamp());
        assertEquals(0.5833333333333334D, halfEntries.get(0).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.5833333333333334D, halfEntries.get(1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.58D, halfEntries.get((expectedArchiveSize * 2) - 2).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.58D, halfEntries.get((expectedArchiveSize * 2) - 1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertTrue(Double.isNaN(halfEntries.get(0).getValue("b")));
        
        for (int i = 0; i < halfEntries.size(); i++) {
            final RrdEntry halfEntry = halfEntries.get(i);
            final RrdEntry entry = entries.get(i/2);
            assertEquals(halfEntry.getTimestamp(), entry.getTimestamp() + ((i % 2) * (expectedArchiveStep / 2)));
            assertEquals(halfEntry.getValue("a"), entry.getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
            assertEquals(halfEntry.getValue("b"), entry.getValue("b"), ACCEPTABLE_DOUBLE_DELTA);
        }
    }

    @Test
    public void testRrdArchiveOne() throws Exception {
        RrdDb rrd = new RrdDb(m_variation);
        TimeSeriesDataSource archive = new RrdArchive(rrd.getArchive(1), Arrays.asList(rrd.getDsNames()));
        final int expectedArchiveSize = 1488;
        final int expectedArchiveStep = 3600;

        final long end = getMidnightInSeconds(m_baseTime);
        final long start = (end - ((expectedArchiveSize - 1) * expectedArchiveStep));

        assertEquals(end, archive.getEndTime());
        assertEquals(start, archive.getStartTime());
        assertEquals(expectedArchiveStep, archive.getNativeStep());

        List<RrdEntry> entries = archive.getData(expectedArchiveStep);
        assertEquals(expectedArchiveSize, entries.size());
        assertEquals(start, entries.get(0).getTimestamp());
        assertEquals(end, entries.get(expectedArchiveSize - 1).getTimestamp());
        assertEquals(0.601111111111111D, entries.get(0).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.634444444444445D, entries.get(1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.334444444444445D, entries.get(expectedArchiveSize - 2).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.301111111111111D, entries.get(expectedArchiveSize - 1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertTrue(Double.isNaN(entries.get(0).getValue("b")));

        List<RrdEntry> halfEntries = archive.getData(expectedArchiveStep / 2);
        assertEquals(expectedArchiveSize * 2, halfEntries.size());
        assertEquals(start, halfEntries.get(0).getTimestamp());
        assertEquals(end + (expectedArchiveStep / 2), halfEntries.get((expectedArchiveSize * 2) - 1).getTimestamp());
        assertEquals(0.601111111111111D, halfEntries.get(0).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.601111111111111D, halfEntries.get(1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.301111111111111D, halfEntries.get((expectedArchiveSize * 2) - 2).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.301111111111111D, halfEntries.get((expectedArchiveSize * 2) - 1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertTrue(Double.isNaN(halfEntries.get(0).getValue("b")));
        
        for (int i = 0; i < halfEntries.size(); i++) {
            final RrdEntry halfEntry = halfEntries.get(i);
            final RrdEntry entry = entries.get(i/2);
            assertEquals(halfEntry.getTimestamp(), entry.getTimestamp() + ((i % 2) * (expectedArchiveStep / 2)));
            assertEquals(halfEntry.getValue("a"), entry.getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
            assertEquals(halfEntry.getValue("b"), entry.getValue("b"), ACCEPTABLE_DOUBLE_DELTA);
        }
    }

    @Test
    public void testRrdArchiveTwo() throws Exception {
        RrdDb rrd = new RrdDb(m_variation);
        TimeSeriesDataSource archive = new RrdArchive(rrd.getArchive(2), Arrays.asList(rrd.getDsNames()));
        final int expectedArchiveSize = 366;
        final int expectedArchiveStep = 86400;

        final long end = getMidnightInSeconds(m_baseTime);
        final long start = (end - ((expectedArchiveSize - 1) * expectedArchiveStep));

        assertEquals(end, archive.getEndTime());
        assertEquals(start, archive.getStartTime());
        assertEquals(expectedArchiveStep, archive.getNativeStep());

        List<RrdEntry> entries = archive.getData(expectedArchiveStep);
        assertEquals(expectedArchiveSize, entries.size());
        assertEquals(start, entries.get(0).getTimestamp());
        assertEquals(end, entries.get(expectedArchiveSize - 1).getTimestamp());
        assertEquals(0.981666666666667D, entries.get(0).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.981666666666667D, entries.get(1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.981666666666667D, entries.get(expectedArchiveSize - 2).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.981666666666667D, entries.get(expectedArchiveSize - 1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertTrue(Double.isNaN(entries.get(0).getValue("b")));

        List<RrdEntry> halfEntries = archive.getData(expectedArchiveStep / 2);
        assertEquals(expectedArchiveSize * 2, halfEntries.size());
        assertEquals(start, halfEntries.get(0).getTimestamp());
        assertEquals(end + (expectedArchiveStep / 2), halfEntries.get((expectedArchiveSize * 2) - 1).getTimestamp());
        assertEquals(0.981666666666667D, halfEntries.get(0).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.981666666666667D, halfEntries.get(1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.981666666666667D, halfEntries.get((expectedArchiveSize * 2) - 2).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.981666666666667D, halfEntries.get((expectedArchiveSize * 2) - 1).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertTrue(Double.isNaN(halfEntries.get(0).getValue("b")));
        
        for (int i = 0; i < halfEntries.size(); i++) {
            final RrdEntry halfEntry = halfEntries.get(i);
            final RrdEntry entry = entries.get(i/2);
            assertEquals(halfEntry.getTimestamp(), entry.getTimestamp() + ((i % 2) * (expectedArchiveStep / 2)));
            assertEquals(halfEntry.getValue("a"), entry.getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
            assertEquals(halfEntry.getValue("b"), entry.getValue("b"), ACCEPTABLE_DOUBLE_DELTA);
        }
    }

    @Test
    public void testRrdDatabase() throws Exception {
        RrdDb rrd = new RrdDb(m_variation);
        BaseRrdDataSource rrdDatabase = new RrdDatabase(rrd);
        final int largestArchiveStep = 86400;

        final long end = getMidnightInSeconds(m_baseTime);
        final long start = (end - SECONDS_PER_YEAR + largestArchiveStep);
                                                  // ^^^^^^^^^^^^^^^^^^ accounts for the extra step added to each RRA in setUp()

        LogUtils.debugf(this, "start = %d, end = %d", start, end);

        assertEquals(end, rrdDatabase.getEndTime());
        assertEquals(start, rrdDatabase.getStartTime());
        assertEquals(105120, rrdDatabase.getRows());

        assertEquals(0.981666666666667D, rrdDatabase.getDataAt(start).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.981666666666667D, rrdDatabase.getDataAt(start + 300).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.601111111111111D, rrdDatabase.getDataAt(start + (300 * 87276)).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.301111111111111D, rrdDatabase.getDataAt(start + (300 * 101088)).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.58D, rrdDatabase.getDataAt(end).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);

        List<RrdEntry> entries = rrdDatabase.getData(300);

        RrdEntry lastEntry = null;
        for (int i = 0; i < entries.size(); i++) {
            final RrdEntry entry = entries.get(i);
            if (lastEntry != null) {
                assertEquals(entry.getTimestamp(), lastEntry.getTimestamp() + 300);
            }
            lastEntry = entry;
        }

        assertEquals(0.981666666666667D, entries.get(0).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.981666666666667D, entries.get(87275).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.601111111111111D, entries.get(87276).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.301111111111111D, entries.get(101088).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.583333333333333D, entries.get(101089).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
    }

    @Test
    public void testAggregateRrdDatabase() throws Exception {
        RrdDb source = new RrdDb(m_sineSource);
        RrdDb full = new RrdDb(m_sineFull);
  
        RrdDatabase sourceDatabase = new RrdDatabase(source);
        RrdDatabase fullDatabase = new RrdDatabase(full);
        List<RrdDatabase> datasources = new ArrayList<RrdDatabase>();
        datasources.add(sourceDatabase);
        datasources.add(fullDatabase);

        BaseRrdDataSource aggregate = new AggregateTimeSeriesDataSource(datasources);
        assertEquals(300, aggregate.getNativeStep());
        assertEquals(1264006800, aggregate.getStartTime());
        assertEquals(1298046000, aggregate.getEndTime());
        assertEquals(113464, aggregate.getRows());

        assertEquals(Double.NaN, aggregate.getDataAt(1264006800).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.049168976942D, aggregate.getDataAt(1293210000).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.071266671798D, aggregate.getDataAt(1294181400).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.050000000000D, aggregate.getDataAt(1295626800).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.024241692151D, aggregate.getDataAt(1295629200).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.023515134858D, aggregate.getDataAt(1296836700).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(0.023333333333D, aggregate.getDataAt(1298046000).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);

        List<RrdEntry> entries = aggregate.getData(150);
        for (final RrdEntry entry : entries) {
            if (!Double.isNaN(entry.getValue("a"))) {
                LogUtils.debugf(this, "entry = %s", entry);
            }
        }
        assertEquals(entries.get(entries.size() - 1).getValue("a"), aggregate.getDataAt(aggregate.getEndTime() + 299).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
    }
    
    @Test
    public void testRrdDatabaseAndAggregateRrdDatabase() throws Exception {
        long dbTime = 0;
        long aggTime = 0;
        for (int i = 0; i < 20; i++) {
            RrdDb source = new RrdDb(m_sineSource);
            RrdDatabase sourceDatabase = new RrdDatabase(source);
    
            BaseRrdDataSource aggregate = new AggregateTimeSeriesDataSource(Collections.singletonList(sourceDatabase));

            long dbStart = System.nanoTime();
            List<RrdEntry> rawEntries = sourceDatabase.getData(150);
            dbTime += System.nanoTime() - dbStart;
            
            long aggStart = System.nanoTime();
            List<RrdEntry> aggregateEntries = aggregate.getData(150);
            aggTime += System.nanoTime() - aggStart;

            assertEquals(aggregate.getEndTime() + 150, aggregateEntries.get(aggregateEntries.size() - 1).getTimestamp());
            assertEquals(rawEntries.size(), aggregateEntries.size());
        }
        LogUtils.debugf(this, "dbTime = %d (%f)", dbTime, dbTime / 1000000.0 / 20.0);
        LogUtils.debugf(this, "aggTime = %d (%f)", aggTime, aggTime / 1000000.0 / 20.0);
    }

    @Test
    public void testRrdDatabaseAndAggregateRrdDatabaseGetDataAt() throws Exception {
        long dbTime = 0;
        long aggTime = 0;
        for (int i = 0; i < 20; i++) {
            RrdDb source = new RrdDb(m_sineSource);
            RrdDatabase sourceDatabase = new RrdDatabase(source);
    
            BaseRrdDataSource aggregate = new AggregateTimeSeriesDataSource(Collections.singletonList(sourceDatabase));

            long dbStart = System.nanoTime();
            List<RrdEntry> rawEntries = getAllData(sourceDatabase);
            dbTime += System.nanoTime() - dbStart;
            
            long aggStart = System.nanoTime();
            List<RrdEntry> aggregateEntries = getAllData(aggregate);
            aggTime += System.nanoTime() - aggStart;

            assertEquals(aggregate.getEndTime() + 150, aggregateEntries.get(aggregateEntries.size() - 1).getTimestamp());
            assertEquals(rawEntries.size(), aggregateEntries.size());
        }
        LogUtils.debugf(this, "dbTime = %d (%f)", dbTime, dbTime / 1000000.0 / 20.0);
        LogUtils.debugf(this, "aggTime = %d (%f)", aggTime, aggTime / 1000000.0 / 20.0);
    }

    private List<RrdEntry> getAllData(TimeSeriesDataSource sourceDatabase) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>(sourceDatabase.getRows());
        for (long time = sourceDatabase.getStartTime(); time < sourceDatabase.getEndTime() + sourceDatabase.getNativeStep(); time += 150) {
            entries.add(sourceDatabase.getDataAt(time));
        }
        return entries;
    }

    @Test
    public void testCombine() throws Exception {
        final File newFile = m_converter.createTempRrd(m_sineFull);
        try {
            m_converter.consolidateRrdFile(m_sineFull, newFile);
            RrdDb newRrd = new RrdDb(newFile.getPath(), true);
            BaseRrdDataSource rrdDatabase = new RrdDatabase(newRrd);

            assertEquals(m_baseTime, newRrd.getLastArchiveUpdateTime());
            Archive archive = newRrd.getArchive(0);
            Robin robin = archive.getRobin(newRrd.getDsIndex("a"));
            assertEquals(4032, robin.getSize());
            assertEquals(Double.NaN, rrdDatabase.getDataAt(1293195600).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
            assertEquals(0.049532528339D, rrdDatabase.getDataAt(1293210000).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
            assertEquals(0.023333333333D, rrdDatabase.getDataAt(1298046000).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        } finally {
//            newFile.delete();
        }
    }

    /**
     * Create a new temporary directory. Use something like
     * {@link #recursiveDelete(File)} to clean this directory up since it isn't
     * deleted automatically
     * @return  the new directory
     * @throws IOException if there is an error creating the temporary directory
     */
    @SuppressWarnings("unused")
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
            LogUtils.debugf(this, "%s: %s = %f", rrd, new Date(timestamps[i]), values[i]);
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
            long i = (timestamp % SECONDS_PER_DAY) / 300;
            long h = i / 12;
            final long j = i % 12;
            final double result = m_baseline + (m_variation * (h-12)) + (j - 6);
//            final double result = m_baseline + (m_variation * (h-12)) + (j - 6);
//            LogUtils.debugf(this, "i = %d, h = %d, j = %d, result = %f", i, h, j, result);
            return result;
        }
        
    }
}
