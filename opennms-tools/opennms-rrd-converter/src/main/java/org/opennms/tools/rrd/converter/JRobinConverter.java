package org.opennms.tools.rrd.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static final long ONE_YEAR_IN_SECONDS = 60L * 60L * 24L * 366L;

    private static final class JRobinConsolidationRunnable implements Runnable {
        private final File m_rrdFile;
        private static volatile Integer m_total = 0;
        private volatile Integer m_count = 0;
        private final JRobinConverter m_converter;

        private JRobinConsolidationRunnable(final File rrdFile, final int count, final JRobinConverter converter) {
            m_rrdFile = rrdFile;
            m_count = count;
            m_converter = converter;
        }

        public static void setTotal(final int total) {
            m_total = total;
        }

        public void run() {
            try {
                LogUtils.infof(this, "starting processing %s (%d/%d)", m_rrdFile, m_count, m_total);
                final List<String> dsNames = m_converter.getDsNames(m_rrdFile);
                if (dsNames.size() == 1) {
                    LogUtils.warnf(this, "%s only has one dsName, skipping", m_rrdFile);
                    return;
                }
                final File outputRrdFile = m_converter.createTempRrd(m_rrdFile);
                final File backupRrdFile = new File(m_rrdFile.getAbsolutePath() + ".orig");
                final File finishedRrdFile = new File(m_rrdFile.getAbsolutePath() + ".finished");
                if (backupRrdFile.exists()) {
                    LogUtils.errorf(this, "backup file %s already exists!", backupRrdFile);
                    System.exit(1);
                }

                LogUtils.debugf(this, "using %s as temporary file", outputRrdFile);

                m_converter.consolidateRrdFile(m_rrdFile, outputRrdFile);
                renameFile(m_rrdFile, backupRrdFile);
                renameFile(outputRrdFile, m_rrdFile);
                renameFile(backupRrdFile, finishedRrdFile);
                LogUtils.infof(this, "finished processing %s", m_rrdFile);
            } catch (final Exception e) {
                LogUtils.debugf(this, e, "error while converting %s", m_rrdFile);
            }
        }

        private void renameFile(final File source, final File target) {
            LogUtils.debugf(this, "renaming %s to %s", source, target);
            if (!source.renameTo(target)) {
                LogUtils.errorf(this, "unable to rename %s to %s", source, target);
                System.exit(1);
            }
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
            System.err.println(new Date() + ": no directory specified!");
            System.exit(1);
        }

        final Options options = new Options();
        options.addOption("h", "help", false, "This help.");
        options.addOption("f", "factory", true, "The JRobin factory to use.");
        options.addOption("l", "log", true, "The log level to use. (Default: INFO)");
        options.addOption("s", "scan", true, "Scan a directory for storeByGroup RRDs.");
        options.addOption("t", "threads", true, "Number of threads to start.");

        final CommandLineParser parser = new GnuParser();
        final CommandLine cmd = parser.parse(options, args);

        LogUtils.setLevel(Level.valueOf(cmd.getOptionValue("l", "INFO")));
        RrdBackendFactory.setDefaultFactory(cmd.getOptionValue("f", "MNIO"));

        final List<File> rrds = Collections.synchronizedList(new ArrayList<File>());

        if (cmd.hasOption("h")) {
            new HelpFormatter().printHelp("jrobin-converter", options);
            System.exit(1);
        }
        if (cmd.hasOption("s")) {
            rrds.addAll(findGroupRrds(new File(cmd.getOptionValue("s"))));
        } else {
            for (final Object arg : cmd.getArgList()) {
                final File f = new File((String)arg);
                if (f.exists()) {
                    rrds.add(f);
                }
            }
        }
        int threads = 5;
        if (cmd.hasOption("t")) {
            try {
                Integer t = Integer.valueOf(cmd.getOptionValue("t"));
                threads = t;
            } catch (final NumberFormatException e) {
                LogUtils.warnf(JRobinConverter.class, e, "failed to format -t %s to a number", cmd.getOptionValue("t"));
            }
        }

        if (rrds.size() == 0) {
            System.err.println(new Date() + ": error, no storeByGroup RRDs found!");
            System.exit(1);
        }
        
        try {
            final ExecutorService executor = Executors.newFixedThreadPool(threads);
            JRobinConsolidationRunnable.setTotal(rrds.size());
            int count = 1;
            for (final File rrdFile : rrds) {
                executor.execute(new JRobinConsolidationRunnable(rrdFile, count++, this));
                try {
                    Thread.sleep(10);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            executor.shutdown();
        } catch (final Exception e) {
            System.err.println(new Date() + ": error while converting RRDs");
            e.printStackTrace();
        }
    }

    public List<File> getMatchingGroupRrds(final File rrdGroupFile) throws ConverterException {
        if (rrdGroupFile == null) return Collections.emptyList();

        final List<String> dsNames = getDsNames(rrdGroupFile);
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
        for (long time = startTime; time <= endTime; time += 300) {
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
            final List<String> dsNames = getDsNames(file);
            if (dsNames.size() < 2) {
                it.remove();
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
        parentFile.mkdirs();
        LogUtils.debugf(this, "created temporary RRD: %s", outputFile);
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
