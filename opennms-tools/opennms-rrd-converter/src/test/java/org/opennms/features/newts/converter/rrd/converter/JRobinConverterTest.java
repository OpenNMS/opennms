/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.newts.converter.rrd.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jrobin.core.ArcDef;
import org.jrobin.core.Archive;
import org.jrobin.core.DsDef;
import org.jrobin.core.FetchData;
import org.jrobin.core.Robin;
import org.jrobin.core.RrdBackendFactory;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.RrdFileBackendFactory;
import org.jrobin.core.RrdJRobin14FileBackend.LockMode;
import org.jrobin.core.RrdJRobin14FileBackendFactory;
import org.jrobin.core.RrdNioBackendFactory;
import org.jrobin.core.RrdNioByteBufferBackendFactory;
import org.jrobin.core.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.features.newts.converter.rrd.converter.LogUtils.Level;

public class JRobinConverterTest {
    private static final double ACCEPTABLE_DOUBLE_DELTA = 0.00000000001;
    JRobinConverter m_converter = null;
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final long SECONDS_PER_DAY = 24L * SECONDS_PER_HOUR;
    private static final long SECONDS_PER_YEAR = 366L * SECONDS_PER_DAY;
    private long m_baseTime = 1298046000L;
    
    File m_workDir = new File("target/rrd");
    private final File m_sineFull    = new File(m_workDir, "sine.rrd");
    private final File m_sineSourceA  = new File(m_workDir, "a.rrd");
    private final File m_sineSourceB  = new File(m_workDir, "b.rrd");
    private final File m_variation   = new File(m_workDir, "variation.rrd");
    private final File m_overlapping = new File(m_workDir, "overlapping.rrd");
    
    @BeforeClass
    public static void setFactory() throws RrdException {
        RrdBackendFactory.setDefaultFactory("MNIO");
        LogUtils.setLevel(Level.DEBUG);
    }

    @Before
    public void setUp() throws Exception {
        m_converter = new JRobinConverter();
        m_workDir.mkdirs();
    }

    @After
    public void tearDown() throws Exception {
        m_sineFull.delete();
        m_sineSourceA.delete();
        m_sineSourceB.delete();
        m_variation.delete();
        m_overlapping.delete();
    }

    private void createMockVariationRrds(final RrdBackendFactory factory) throws RrdException, IOException {
        final long end = getMidnightInSeconds(m_baseTime);
        initializeRrd(m_variation, new String[] { "a", "b" }, new String[] { "1:4032", "12:1488", "288:366" });
        initializeRrd(m_overlapping, new String[] { "c", "d", "a" }, new String[] { "1:4032", "12:1488", "288:366" });

        final RrdDb variation;
        final RrdDb overlapping;
        if (factory == null) {
            variation   = new RrdDb(m_variation);
            overlapping = new RrdDb(m_overlapping);
        } else {
            variation   = new RrdDb(m_variation.getAbsolutePath(), factory);
            overlapping = new RrdDb(m_overlapping.getAbsolutePath(), factory);
        }

        final long start = (end - SECONDS_PER_YEAR);
        final Function sequence = new AverageSequence(300, 10);
        final Function sequenceCounter = new Counter(0, sequence);
        final Function overlappingSequence = new AverageSequence(300, 20);
        final Function overlappingCounter = new Counter(0, overlappingSequence);
        long timestamp = start - 300L;
        for (; timestamp <= end; timestamp += 300L) {
            final Sample variationSample = variation.createSample(timestamp);
            final double variationValue = sequenceCounter.evaluate(timestamp);
            variationSample.setValue("a", variationValue);
            variationSample.update();
            
            final Sample overlappingSample = overlapping.createSample(timestamp);
            final double overlappingValue = overlappingCounter.evaluate(timestamp);
            overlappingSample.setValue("d", Double.NaN);
            if (((timestamp % 1200) / 300) < 2) {
                overlappingSample.setValue("a", overlappingValue);
            }
            overlappingSample.setValue("c", overlappingValue);
            overlappingSample.update();
        }
        
        variation.close();
        overlapping.close();
    }

    private void createMockSineRrds(final RrdBackendFactory factory) throws RrdException, IOException {
        final long start = (m_baseTime - (SECONDS_PER_DAY * 56L));
        initializeRrd(m_sineFull, new String[] { "a", "b" }, new String[] { "1:4032", "12:1488", "288:366" });
        initializeRrd(m_sineSourceA, new String[] { "a" }, new String[] { "1:8928", "12:8784" });
        initializeRrd(m_sineSourceB, new String[] { "b" }, new String[] { "1:8928", "12:8784" });
        final RrdDb sineFull;
        final RrdDb sineSourceA;
        final RrdDb sineSourceB;
        if (factory == null) {
            sineFull   = new RrdDb(m_sineFull);
            sineSourceA = new RrdDb(m_sineSourceA);
            sineSourceB = new RrdDb(m_sineSourceB);
        } else {
            sineFull   = new RrdDb(m_sineFull.getAbsolutePath(), factory);
            sineSourceA = new RrdDb(m_sineSourceA.getAbsolutePath(), factory);
            sineSourceB = new RrdDb(m_sineSourceB.getAbsolutePath(), factory);
        }
        Function bigSine = new Sin(start, 15, -10, SECONDS_PER_DAY * 7L);
        Function smallSine = new Sin(start, 7, 5, SECONDS_PER_DAY * 2L);
        Function bigSineCounter = new Counter(0, bigSine);
        Function smallSineCounter = new Counter(0, smallSine);

        long timestamp = start - 300L;
        for(; timestamp <= (m_baseTime - (SECONDS_PER_DAY * 28L)); timestamp += 300L) {
            Sample sampleA = sineSourceA.createSample(timestamp);
            sampleA.setValue("a", bigSineCounter.evaluate(timestamp));
            sampleA.update();
            Sample sampleB = sineSourceB.createSample(timestamp);
            sampleB.setValue("b", Double.NaN);
            sampleB.update();
        }
        for(; timestamp <= m_baseTime; timestamp += 300L) {
            Sample sampleA = sineFull.createSample(timestamp);
            sampleA.setValue("a", smallSineCounter.evaluate(timestamp));
            sampleA.update();
            Sample sampleB = sineSourceB.createSample(timestamp);
            sampleB.setValue("b", Double.NaN);
            sampleB.update();
        }
        sineFull.close();
        sineSourceA.close();
        sineSourceB.close();
    }

    private long getMidnightInSeconds(final long seconds) {
        return (((seconds) / SECONDS_PER_DAY) * SECONDS_PER_DAY);
    }

    private void initializeRrd(final File fileName, final String[] dsNames, final String[] archives) throws RrdException, IOException {
        final RrdDef rrdDef = new RrdDef(fileName.getAbsolutePath());
        rrdDef.setStartTime(0);
        final DsDef[] dsDefs = new DsDef[dsNames.length];
        for (int i = 0; i < dsNames.length; i++) {
            dsDefs[i] = new DsDef(dsNames[i], "COUNTER", 600, 0, Double.NaN);
        }
        rrdDef.addDatasource(dsDefs);
        final ArcDef[] arcDefs = new ArcDef[archives.length];
        for (int i = 0; i < archives.length; i++) {
            String[] entry = archives[i].split(":");
            Integer steps = Integer.valueOf(entry[0]);
            Integer rows = Integer.valueOf(entry[1]);
            arcDefs[i] = new ArcDef("AVERAGE", 0.5D, steps, rows);
        }
        rrdDef.addArchive(arcDefs);
        final RrdDb db = new RrdDb(rrdDef);
        db.close();
    }

    @Test
    public void testGetDsNames() throws Exception {
        createMockSineRrds(null);
        final List<String> dsNames = m_converter.getDsNames(m_sineFull);
        assertTrue(dsNames.contains("a"));
        assertEquals(2, dsNames.size());
    }

    @Test
    public void testGetRras() throws Exception {
        createMockSineRrds(null);
        final List<String> rras = m_converter.getRras(m_sineFull);
        assertEquals(3, rras.size());
    }

    @Test
    public void testBackends() throws Exception {
        final RrdBackendFactory[] factories = new RrdBackendFactory[] {
                new RrdFileBackendFactory(),
                new RrdNioBackendFactory(),
                new RrdNioByteBufferBackendFactory(),
                new RrdJRobin14FileBackendFactory(LockMode.EXCEPTION_IF_LOCKED),
                new RrdJRobin14FileBackendFactory(LockMode.WAIT_IF_LOCKED),
                new RrdJRobin14FileBackendFactory(LockMode.NO_LOCKS)
        };

        for (final RrdBackendFactory factory : factories) {
//            LogUtils.infof(this, "starting with backend factory %s", factory);
            m_sineFull.delete();
            m_sineSourceA.delete();
            long factoryStart = System.nanoTime();
            createMockSineRrds(factory);
            for (int i = 0; i < 10; i++) {
                final File newFile = m_converter.createTempRrd(m_sineFull);
                try {
                    m_converter.consolidateRrdFile(m_sineSourceA, newFile);
                } finally {
                    newFile.delete();
                }
            }
            long nanos = System.nanoTime() - factoryStart;
            LogUtils.infof(this, "factory %s took %f seconds", factory, (nanos / 1000000000D));
        }
    }

    @Test
    public void testSine() throws Exception {
        createMockSineRrds(null);
        final File newFile = m_converter.createTempRrd(m_sineFull);
        try {
            m_converter.consolidateRrdFile(m_sineSourceA, newFile);
        } finally {
            newFile.delete();
        }
    }

    @Test
    public void testScanRrds() throws Exception {
        final File topDirectory = new File("src/test/rrds");

        List<File> rrds = m_converter.findRrds(topDirectory);
        assertEquals(14, rrds.size());
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd")));
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd")));
        
        rrds = m_converter.findGroupRrds(topDirectory);
        assertEquals(1, rrds.size());
        assertTrue(rrds.contains(new File("src/test/rrds/90020/Se0/mib2-interfaces.rrd")));
        assertFalse(rrds.contains(new File("src/test/rrds/90020/Se0/ifOutOctets.rrd")));
    }

    @Test
    public void testGetMatchingRrds() throws Exception {
        createMockSineRrds(null);
        final List<File> matches = m_converter.getMatchingGroupRrds(m_sineFull);
        assertEquals(2, matches.size());
    }
    
    @Test
    public void testFetch() throws Exception {
        createMockSineRrds(null);
        RrdDb rrd = new RrdDb(m_sineFull);
        final long endTime = rrd.getLastArchiveUpdateTime();
        final long startTime = endTime - (60L * 60L * 24L * 365L);
        final FetchData fd = rrd.createFetchRequest("AVERAGE", startTime, endTime, 300).fetchData();
        double[] values = fd.getValues("a");
        assertEquals(367, values.length);
    }
    
    @Test
    public void testRrdArchiveZero() throws Exception {
        createMockVariationRrds(null);
        RrdDb rrd = new RrdDb(m_variation);
        TimeSeriesDataSource archive = new RrdArchive(rrd.getArchive(0), Arrays.asList(rrd.getDsNames()));
        final int expectedArchiveSize = 4032;
        final int expectedArchiveStep = 300;

        final long end = getMidnightInSeconds(m_baseTime);
        final long start = (end - ((expectedArchiveSize - 1) * expectedArchiveStep));

        assertEquals(end, archive.getEndTime());
        assertEquals(start, archive.getStartTime());
        assertEquals(expectedArchiveStep, archive.getNativeStep());
        assertEquals(expectedArchiveSize, archive.getRows());

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
        createMockVariationRrds(null);
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
        createMockVariationRrds(null);
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
        createMockVariationRrds(null);
        RrdDb rrd = new RrdDb(m_variation);
        TimeSeriesDataSource rrdDatabase = new RrdDatabase(rrd);
        final int largestArchiveStep = 86400;

        final long end = getMidnightInSeconds(m_baseTime);
        final long start = (end - SECONDS_PER_YEAR + largestArchiveStep);
                                                  // ^^^^^^^^^^^^^^^^^^ accounts for the extra step added to each RRA in setUp()

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
        createMockSineRrds(null);
        RrdDb source = new RrdDb(m_sineSourceA);
        RrdDb full = new RrdDb(m_sineFull);
  
        RrdDatabase sourceDatabase = new RrdDatabase(source);
        RrdDatabase fullDatabase = new RrdDatabase(full);
        List<RrdDatabase> datasources = new ArrayList<>();
        datasources.add(sourceDatabase);
        datasources.add(fullDatabase);

        TimeSeriesDataSource aggregate = new AggregateTimeSeriesDataSource(datasources);
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
        assertEquals(entries.get(entries.size() - 1).getValue("a"), aggregate.getDataAt(aggregate.getEndTime() + 299).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
    }
    
    @Test
    public void testRrdDatabaseAndAggregateRrdDatabase() throws Exception {
        createMockSineRrds(null);
        long dbTime = 0;
        long aggTime = 0;
        for (int i = 0; i < 20; i++) {
            RrdDb source = new RrdDb(m_sineSourceA);
            RrdDatabase sourceDatabase = new RrdDatabase(source);
    
            TimeSeriesDataSource aggregate = new AggregateTimeSeriesDataSource(Collections.singletonList(sourceDatabase));

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
        createMockSineRrds(null);
        long dbTime = 0;
        long aggTime = 0;
        for (int i = 0; i < 20; i++) {
            RrdDb source = new RrdDb(m_sineSourceA);
            RrdDatabase sourceDatabase = new RrdDatabase(source);
    
            TimeSeriesDataSource aggregate = new AggregateTimeSeriesDataSource(Collections.singletonList(sourceDatabase));

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

    @Test
    public void testAggregateWithOverlappingData() throws Exception {
        createMockVariationRrds(null);
        RrdDatabase overlappingDatabase = new RrdDatabase(new RrdDb(m_overlapping, true));
        RrdDatabase variationDatabase = new RrdDatabase(new RrdDb(m_variation, true));
        List<RrdDatabase> datasources = new ArrayList<>();
        datasources.add(overlappingDatabase);
        datasources.add(variationDatabase);

        TimeSeriesDataSource aggregate = new AggregateTimeSeriesDataSource(datasources);
        final List<String> aggregateDsNames = Arrays.asList(new String[] { "c", "d", "a", "b" });

        assertEquals(variationDatabase.getStartTime(), overlappingDatabase.getStartTime());
        assertEquals(variationDatabase.getStartTime(), aggregate.getStartTime());
        assertEquals(variationDatabase.getEndTime(), overlappingDatabase.getEndTime());
        assertEquals(variationDatabase.getEndTime(), aggregate.getEndTime());
        assertEquals(aggregateDsNames, aggregate.getDsNames());

        assertEquals(1.0933333333333D, aggregate.getDataAt(1297956000).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.1966666666666D, aggregate.getDataAt(1297956300).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.1000000000000D, aggregate.getDataAt(1297956600).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
        assertEquals(1.1033333333333D, aggregate.getDataAt(1297956900).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
    }
    
    @Test
    public void testCombine() throws Exception {
        createMockSineRrds(null);
        final File newFile = m_converter.createTempRrd(m_sineFull);
        try {
            m_converter.consolidateRrdFile(m_sineFull, newFile);
            RrdDb newRrd = new RrdDb(newFile.getPath(), true);
            TimeSeriesDataSource rrdDatabase = new RrdDatabase(newRrd);

            assertEquals(m_baseTime, newRrd.getLastArchiveUpdateTime());
            Archive archive = newRrd.getArchive(0);
            Robin robin = archive.getRobin(newRrd.getDsIndex("a"));
            assertEquals(4032, robin.getSize());
            assertEquals(Double.NaN, rrdDatabase.getDataAt(1293195600).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
            assertEquals(0.049584469635D, rrdDatabase.getDataAt(1293210000).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
            assertEquals(0.023333333333D, rrdDatabase.getDataAt(1298046000).getValue("a"), ACCEPTABLE_DOUBLE_DELTA);
            
        } finally {
            newFile.delete();
        }
    }

    @Test
    public void testCleaner() throws Exception {
        createMockSineRrds(null);
        RrdCleaner cleaner = new RrdCleaner();
        List<File> rrds = cleaner.findRrds(new File("src/test/rrds"));
        assertEquals(14, rrds.size()); // 13 single-metric JRBs plus 1 temporal file
    }

    protected void checkArchive(final RrdDb rrd, final Integer nanSample, final Integer numberSample, final Double numberValue, final Integer archiveIndex, final Integer numDses, final Integer dsIndex, String dsName) throws RrdException, IOException {
        LogUtils.debugf(this, "checking archive %s for consistency", rrd);
        final Archive archive = rrd.getArchive(archiveIndex);
        final Map<String,Integer> indexes = m_converter.getDsIndexes(rrd);
        assertEquals(numDses, Integer.valueOf(indexes.size()));
        final Robin robin = archive.getRobin(dsIndex);
        if (nanSample == null) {
            for (final double value : robin.getValues()) {
                assertTrue(!Double.isNaN(value));
            }
        } else {
            assertTrue(Double.isNaN(robin.getValue(nanSample)));
        }
        assertEquals(numberValue, Double.valueOf(robin.getValue(numberSample)));

        // Make sure FetchData matches
        final FetchData data = rrd.createFetchRequest("AVERAGE", archive.getStartTime(), archive.getEndTime()).fetchData();
        final double[] values = data.getValues(dsName);
        if (nanSample == null) {
            for (final double value : values) {
                assertTrue(!Double.isNaN(value));
            }
        } else {
            assertTrue(Double.isNaN(values[nanSample]));
        }
        
        assertEquals(numberValue, Double.valueOf(values[numberSample]));
    }

    private List<RrdEntry> getAllData(final TimeSeriesDataSource sourceDatabase) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>(sourceDatabase.getRows());
        for (long time = sourceDatabase.getStartTime(); time < sourceDatabase.getEndTime() + sourceDatabase.getNativeStep(); time += 150) {
            entries.add(sourceDatabase.getDataAt(time));
        }
        return entries;
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
        
        Sin(final long startTime, final double offset, final double amplitude, final double period) {
            m_startTime = startTime;
            m_offset = offset;
            m_amplitude = amplitude;
            m_period = period;
            m_factor = 2 * Math.PI / period;
        }
        
        public double evaluate(final long timestamp) {
            final long x = timestamp - m_startTime;
            return (m_amplitude * Math.sin(m_factor * x)) + m_offset;
        }
    }
    
    class Cos implements Function {
        
        long m_startTime;
        double m_offset;
        double m_amplitude;
        double m_period;
        
        double m_factor;
        
        Cos(final long startTime, final double offset, final double amplitude, final double period) {
            m_startTime = startTime;
            m_offset = offset;
            m_amplitude = amplitude;
            m_period = period;
            
            m_factor = 2 * Math.PI / period;
        }
        
        public double evaluate(final long timestamp) {
            final long x = timestamp - m_startTime;
            return (m_amplitude * Math.cos(m_factor * x)) + m_offset;
        }
    }
    
    class Times implements Function {
        Function m_a;
        Function m_b;
        
        Times(final Function a, final Function b) {
            m_a = a;
            m_b = b;
        }

        public double evaluate(final long timestamp) {
            return m_a.evaluate(timestamp)*m_b.evaluate(timestamp);
        }
    }
    
    class Counter implements Function {
        double m_prevValue;
        Function m_function;
        
        Counter(final double initialValue, final Function function) {
            m_prevValue = initialValue;
            m_function = function;
        }

        public double evaluate(final long timestamp) {
            final double m_diff = m_function.evaluate(timestamp);
            m_prevValue += m_diff;
            return m_prevValue;
        }
        
    }

    class AverageSequence implements Function {
        private long m_baseline;
        private long m_variation;

        public AverageSequence(final long baseline, final long variation) {
            m_baseline = baseline;
            m_variation = variation;
        }

        public double evaluate(final long timestamp) {
            final long i = (timestamp % SECONDS_PER_DAY) / 300;
            final long h = i / 12;
            final long j = i % 12;
            final double result = m_baseline + (m_variation * (h-12)) + (j - 6);
            return result;
        }
        
    }
}
