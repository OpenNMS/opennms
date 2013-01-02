/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.tools.rrd.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jrobin.core.ArcDef;
import org.jrobin.core.RrdBackendFactory;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.opennms.tools.rrd.converter.LogUtils.Level;

public class JRobinConverter {
    private static final int DEFAULT_NUMBER_OF_THREADS = 5;
    private static final String DEFAULT_JROBIN_FACTORY = "MNIO";
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final long ONE_YEAR_IN_SECONDS = 60L * 60L * 24L * 366L;
    private static final AtomicInteger m_count = new AtomicInteger(0);
    private static final AtomicInteger m_finished = new AtomicInteger(0);
    private static final AtomicInteger m_total = new AtomicInteger(0);

    private static final class JRobinConsolidationRunnable implements Runnable {
        private final File m_rrdFile;
        private final JRobinConverter m_converter;

        private JRobinConsolidationRunnable(final File rrdFile, final JRobinConverter converter) {
            m_rrdFile = rrdFile;
            m_converter = converter;
        }

        public void run() {
            try {
                synchronized(m_total) {
                    LogUtils.infof(this, "Starting processing %s (%d/%d Started)", m_rrdFile, m_count.incrementAndGet(), m_total.get());
                }
                final List<String> dsNames = m_converter.getDsNames(m_rrdFile);
                if (dsNames.size() == 1) {
                    LogUtils.warnf(this, "%s only has one dsName, skipping", m_rrdFile);
                    return;
                }
                final File outputRrdFile = m_converter.createTempRrd(m_rrdFile);
                final File backupRrdFile = new File(m_rrdFile.getAbsolutePath() + ".orig");
                final File finishedRrdFile = new File(m_rrdFile.getAbsolutePath() + ".finished");
                if (finishedRrdFile.exists()) {
                    LogUtils.warnf(this, "File %s has already been converted, because %s exists!  Skipping.", m_rrdFile, finishedRrdFile);
                    return;
                }

                LogUtils.debugf(this, "Using %s as temporary file", outputRrdFile);

                m_converter.consolidateRrdFile(m_rrdFile, outputRrdFile);
                renameFile(m_rrdFile, backupRrdFile);
                renameFile(outputRrdFile, m_rrdFile);
                renameFile(backupRrdFile, finishedRrdFile);
                LogUtils.infof(this, "Completed processing %s (%d/%d Complete)", m_rrdFile, m_finished.incrementAndGet(), m_total.get());
            } catch (final Exception e) {
                LogUtils.infof(this, e, "Error while converting %s", m_rrdFile);
            }
        }

        private void renameFile(final File source, final File target) {
            LogUtils.debugf(this, "Renaming %s to %s", source, target);
            if (target.exists()) {
                LogUtils.errorf(this, "%s already exists!", target);
                System.exit(1);
            }
            if (!source.renameTo(target)) {
                LogUtils.errorf(this, "Unable to rename %s to %s", source, target);
                System.exit(1);
            }
            source.delete();
        }
    }

    /**
     * @param args
     * @throws ParseException 
     * @throws ConverterException 
     * @throws RrdException 
     */
    public static void main(final String[] args) throws ParseException, ConverterException, RrdException {
        new JRobinConverter().execute(args);
    }

    public void execute(final String[] args) throws ParseException, ConverterException, RrdException {
        if (args.length == 0) {
            LogUtils.errorf(this, "no files or directories specified!");
            System.exit(1);
        }

        final Options options = new Options();
        options.addOption("h", "help", false, "This help.");
        options.addOption("f", "factory", true, "The JRobin factory to use. (Default: " + DEFAULT_JROBIN_FACTORY + ")");
        options.addOption("l", "log", true, "The log level to use. (Default: " + DEFAULT_LOG_LEVEL + ")");
        options.addOption("t", "threads", true, "Number of threads to start. (Default: " + DEFAULT_NUMBER_OF_THREADS + ")");

        final CommandLineParser parser = new GnuParser();
        final CommandLine cmd = parser.parse(options, args);

        LogUtils.setLevel(Level.valueOf(cmd.getOptionValue("l", DEFAULT_LOG_LEVEL)));
        RrdBackendFactory.setDefaultFactory(cmd.getOptionValue("f", DEFAULT_JROBIN_FACTORY));

        final Set<File> rrds = new ConcurrentSkipListSet<File>();

        if (cmd.hasOption("h")) {
            new HelpFormatter().printHelp("jrobin-converter [options] [file-or-directory1] [...file-or-directoryN]", options);
            System.exit(1);
        }
        if (cmd.getArgList().size() == 0) {
            LogUtils.infof(this, "No files or directories specified!  Exiting.");
            System.exit(0);
        }

        int threads = DEFAULT_NUMBER_OF_THREADS;
        if (cmd.hasOption("t")) {
            try {
                threads = Integer.valueOf(cmd.getOptionValue("t"));
            } catch (final NumberFormatException e) {
                LogUtils.warnf(JRobinConverter.class, e, "failed to format -t %s to a number", cmd.getOptionValue("t"));
            }
        }
        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (final Object arg : cmd.getArgList()) {
            LogUtils.infof(this, "Scanning %s for storeByGroup data.", arg);
            final File f = new File((String)arg);
            if (f.exists()) {
                if (f.isDirectory()) {
                    rrds.addAll(findGroupRrds(f));
                    for (final File rrdFile : findGroupRrds(f)) {
                        consolidateRrd(executor, rrdFile);
                    }
                } else {
                    consolidateRrd(executor, f);
                }
            }
        }
        LogUtils.infof(this, "Finished scanning for storeByGroup RRDs. (Total RRD count: %d)", m_total.get());

        executor.shutdown();
    }

    private void consolidateRrd(final ExecutorService executor, final File rrdFile) {
        m_total.incrementAndGet();
        executor.execute(new JRobinConsolidationRunnable(rrdFile, this));
    }

    public List<File> getMatchingGroupRrds(final File rrdGroupFile) throws ConverterException {
        if (rrdGroupFile == null) return Collections.emptyList();
        final List<String> dsNames;

        try {
            dsNames = getDsNames(rrdGroupFile);
        } catch (final ConverterException e) {
            LogUtils.debugf(this, "Unable to get dsNames for %s", rrdGroupFile);
            return Collections.emptyList();
        }

        final List<File> files = new ArrayList<File>();
        
        for (final File f : rrdGroupFile.getAbsoluteFile().getParentFile().listFiles()) {
            for (final String dsName : dsNames) {
                if (f.getName().equals(dsName + ".rrd") || f.getName().equals(dsName + ".jrb")) {
                    files.add(f);
                }
            }
        }
        
        return files;
    }

    public List<String> getDsNames(final File rrdFile) throws ConverterException {
        try {
            final RrdDb db = new RrdDb(rrdFile.getAbsolutePath(), true);
            return Arrays.asList(db.getDsNames());
        } catch (final Exception e) {
            LogUtils.debugf(JRobinConverter.class, e, "error reading file %s", rrdFile);
            throw new ConverterException(e);
        }
    }

    public List<String> getRras(final File rrdFile) throws ConverterException {
        try {
            final List<String> rras = new ArrayList<String>();
            final RrdDb db = new RrdDb(rrdFile.getAbsolutePath(), true);
            for (final ArcDef def : db.getRrdDef().getArcDefs()) {
                rras.add(def.dump());
            }
            return rras;
        } catch (final Exception e) {
            LogUtils.debugf(JRobinConverter.class, e, "error reading file %s", rrdFile);
            throw new ConverterException(e);
        }
    }


    public Map<String, Integer> getDsIndexes(final RrdDb rrd) throws RrdException, IOException {
        final Map<String,Integer> indexes = new HashMap<String,Integer>();
        for (final String dsName : rrd.getDsNames()) {
            indexes.put(dsName, rrd.getDsIndex(dsName));
        }
        return indexes;
    }

    public void consolidateRrdFile(final File groupFile, final File outputFile) throws IOException, RrdException, ConverterException {
        final List<RrdDatabase> rrds = new ArrayList<RrdDatabase>();
        rrds.add(new RrdDatabase(new RrdDb(groupFile, true)));
        for (final File individualFile : getMatchingGroupRrds(groupFile)) {
            final RrdDb individualRrd = new RrdDb(individualFile, true);
            rrds.add(new RrdDatabase(individualRrd));
        }
        final TimeSeriesDataSource dataSource = new AggregateTimeSeriesDataSource(rrds);

        final RrdDb outputRrd = new RrdDb(outputFile);
        final RrdDatabaseWriter writer = new RrdDatabaseWriter(outputRrd);

        final long endTime = dataSource.getEndTime();
        // 1 year
        final long startTime = endTime - ONE_YEAR_IN_SECONDS;
        for (long time = startTime; time <= endTime; time += dataSource.getNativeStep()) {
            final RrdEntry entry = dataSource.getDataAt(time);
            writer.write(entry);
        }
        dataSource.close();
        outputRrd.close();
    }

    public List<File> findRrds(final File topDirectory) {
        final List<File> files = new ArrayList<File>();
        findRrds(topDirectory, files);
        return files;
    }
    
    public List<File> findGroupRrds(final File topDirectory) throws ConverterException {
        final List<File> files = new ArrayList<File>();
        findRrds(topDirectory, files);
        for (final Iterator<File> it = files.iterator(); it.hasNext(); ) {
            final File file = it.next();
            try {
                final List<String> dsNames = getDsNames(file);
                if (dsNames.size() < 2) {
                    it.remove();
                }
            } catch (final ConverterException e) {
                LogUtils.debugf(this, e, "Unable to get dsNames from %s", file);
            }
        }
        return files;
    }

    public void findRrds(final File directory, final List<File> files) {
        for (final File f : directory.listFiles()) {
            if (f.isDirectory()) {
                findRrds(f, files);
            } else {
                if (f.getName().endsWith(".rrd") || f.getName().endsWith(".jrb")) {
                    files.add(f);
                }
            }
        }
    }

    public File createTempRrd(final File rrdFile) throws IOException, RrdException {
        final File parentFile = rrdFile.getParentFile();
        final File outputFile = new File(parentFile, rrdFile.getName() + ".temp");
        outputFile.delete(); // just in case there's an old one lying around
        parentFile.mkdirs();
//        LogUtils.debugf(this, "created temporary RRD: %s", outputFile);
        final RrdDb oldRrd = new RrdDb(rrdFile.getAbsolutePath(), true);
        final RrdDef rrdDef = oldRrd.getRrdDef();
        rrdDef.setPath(outputFile.getAbsolutePath());
        rrdDef.setStartTime(0);
        final RrdDb newRrd = new RrdDb(rrdDef);
        newRrd.close();

        return outputFile;
    }

    public boolean moveFileSafely(final File in, final File out) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        final File tempOut = File.createTempFile("move", ".tmp");
        try {
            fis = new FileInputStream(in);
            fos = new FileOutputStream(tempOut);
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            try {
                if (inChannel != null) inChannel.close();
            } catch (IOException e) {
                LogUtils.debugf(JRobinConverter.class, "failed to close channel %s", inChannel);
            }
            try {
                if (outChannel != null) outChannel.close();
            } catch (IOException e) {
                LogUtils.debugf(JRobinConverter.class, "failed to close channel %s", outChannel);
            }
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                LogUtils.debugf(JRobinConverter.class, "failed to close stream %s", fis);
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                LogUtils.debugf(JRobinConverter.class, "failed to close stream %s", fos);
            }
        }
        out.delete();
        if (!out.exists()) {
            tempOut.renameTo(out);
            return in.delete();
        }
        return false;
    }
    
}
