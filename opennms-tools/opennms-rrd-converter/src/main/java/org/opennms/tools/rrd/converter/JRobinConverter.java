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
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;
import org.jrobin.core.ArcDef;
import org.jrobin.core.Archive;
import org.jrobin.core.FetchData;
import org.jrobin.core.RrdBackendFactory;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.opennms.core.utils.LogUtils;

public class JRobinConverter {
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
                System.out.println(new Date() + ": processing " + m_rrdFile + " (" + m_count + "/" + m_total + ")");
                final List<String> dsNames = m_converter.getDsNames(m_rrdFile);
                if (dsNames.size() == 1) {
                    System.err.println(new Date() + ": - " + m_rrdFile + " only has one dsName, skipping");
                    return;
                }
                final File temporaryRrd = m_converter.createTempRrd(m_rrdFile);
                
                m_converter.consolidateRrdFile(m_rrdFile, temporaryRrd);
                if (!m_converter.moveFileSafely(temporaryRrd, m_rrdFile)) {
                    System.err.println(new Date() + ": - unable to move " + temporaryRrd + " to " + m_rrdFile);
                    System.exit(1);
                }
                System.out.println(new Date() + ": finished processing " + m_rrdFile);
            } catch (final Exception e) {
                System.err.println(new Date() + ": error while converting " + m_rrdFile);
                e.printStackTrace();
            }
        }
    }

    private File m_groupFile;

    public File getGroupFile() {
        return m_groupFile;
    }
    
    public void setGroupFile(final File rrd) {
        m_groupFile = rrd;
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
        setupLogging();
        RrdBackendFactory.setDefaultFactory("MNIO");
        if (args.length == 0) {
            System.err.println(new Date() + ": no directory specified!");
            System.exit(1);
        }

        final Options options = new Options();
        options.addOption("h", "help", false, "This help.");
        options.addOption("s", "scan", true, "Scan a directory for storeByGroup RRDs.");
        options.addOption("t", "threads", true, "Number of threads to start.");
        
        final CommandLineParser parser = new GnuParser();
        final CommandLine cmd = parser.parse(options, args);

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
        final RrdDb groupRrd = new RrdDb(groupFile, true);
        final RrdDb outputRrd = new RrdDb(outputFile);

        final RrdDatabase data = new RrdDatabase(outputRrd);
        final List<File> individualRrds = getMatchingGroupRrds(groupFile);
        LogUtils.debugf(JRobinConverter.class, "individual RRDs = %s", individualRrds);


        final long endTime = groupRrd.getLastArchiveUpdateTime();
        // 1 year
        final long startTime = endTime - (60L * 60L * 24L * 365L);
        for (final File individualRrdFile : individualRrds) {
            final RrdDb inputRrd = new RrdDb(individualRrdFile, true);
            final List<String> dsNames = getDsNames(individualRrdFile);
            LogUtils.debugf(JRobinConverter.class, "input DSNames = %s", dsNames);
            if (dsNames.size() > 1) {
                LogUtils.warnf(JRobinConverter.class, "%s: more than one dsname found!", individualRrdFile);
            }
            final String dsName = dsNames.get(0);
            final String dsType = groupRrd.getDatasource(dsName).getDsType();

            collectSampleData(inputRrd, 300, dsName, dsType, startTime, endTime, data);
            inputRrd.close();
        }

        final List<String> dsNames = getDsNames(groupFile);
        for (int i = 0; i < dsNames.size(); i++) {
            final String dsName = dsNames.get(i);
            final String dsType = groupRrd.getDatasource(dsName).getDsType();
            collectSampleData(groupRrd, 300, dsName, dsType, startTime, endTime, data);
        }
        
        data.createSamples(outputRrd);
        
        outputRrd.close();
    }

    @SuppressWarnings("unused")
    private void printSample(final long sampleTime, final Map<String, Double> entry) {
        final StringBuffer sb = new StringBuffer();
        sb.append(new Date(sampleTime * 1000L)).append(": ");
        final Set<String> keys = entry.keySet();
        for (final Iterator<String> it = keys.iterator(); it.hasNext(); ) {
            final String key = it.next();
            sb.append(key).append("=").append(entry.get(key));
            if (it.hasNext()) sb.append(", ");
        }
        LogUtils.debugf(JRobinConverter.class, sb.toString());
    }

    public void collectSampleData(final RrdDb inputRrd, final int groupDsStep, final String dsName, final String dsType, final long startTime, final long endTime, RrdDatabase rrdDatabase) throws IOException, RrdException {
        long lastSampleTime = 0;
        for (int j = inputRrd.getArcCount() - 1; j >= 0; j--) {
            RrdArchive rrdArchive = new RrdArchive(inputRrd.getArchive(j), Arrays.asList(inputRrd.getDsNames()));
            if (!rrdArchive.isAverage()) {
                continue;
            }
            LogUtils.debugf(JRobinConverter.class, "dsName=%s, archive=%d, start=%s, end=%s, step=%d (%s)", dsName, j, new Date(startTime * 1000L), new Date(endTime * 1000L), groupDsStep, inputRrd);

            Long current = startTime;
            final FetchData fd = inputRrd.createFetchRequest(rrdArchive.getArchive().getConsolFun(), startTime, endTime).fetchData();
            final Long step = fd.getStep();
            LogUtils.debugf(JRobinConverter.class, "step = %d", step);

            double lastValue = 0;
            Long stepCount;

            if (step > groupDsStep) {
                LogUtils.debugf(JRobinConverter.class, "step %d > %d", step, groupDsStep);
                if (step % groupDsStep == 0) {
                    stepCount = (step / groupDsStep);
                    LogUtils.debugf(JRobinConverter.class, "step is evenly divisible (%d / %d = %d)", step, groupDsStep, stepCount);
                } else {
                    LogUtils.errorf(JRobinConverter.class, "step is NOT evenly divisible");
                    throw new UnsupportedOperationException("unable to convert data, step sizes are not even");
                }
            } else {
                stepCount = 1L;
            }

            for (final double originalSample : fd.getValues(dsName)) {
                double sample = originalSample;
                if (current > lastSampleTime) {
                    if (Double.isNaN(sample)) {
                        current += step;
                    } else {
                        for (int i = 0; i < stepCount; i++) {
                            if (dsType.equals("COUNTER")) {
                                sample = lastValue + (originalSample * groupDsStep);
                                lastValue = sample;
                            }
                            rrdDatabase.addSample(current, dsName, sample);
                            
                            lastSampleTime = current;
                            current += groupDsStep;
                        }
                    }
                }
            }
        }
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
//        final File outputFile = File.createTempFile("jrobin-", ".jrb");
        final File directory = new File("target/rrd");
        directory.mkdirs();
        final File outputFile = new File(directory, "temp-" + rrdFile.getName());
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
        if (out.delete()) {
            tempOut.renameTo(out);
            return in.delete();
        }
        return false;
    }
    
    public void setupLogging() {
        final Properties logConfig = new Properties();

        final String consoleAppender = ("CONSOLE");
        
        logConfig.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        logConfig.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        logConfig.put("log4j.appender.CONSOLE.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");

        logConfig.put("log4j.rootCategory", ("INFO, CONSOLE"));
        logConfig.put("log4j.logger.org.apache.commons.httpclient.HttpMethodBase", "ERROR");
        logConfig.put("log4j.logger.org.exolab.castor", "INFO");
        logConfig.put("log4j.logger.org.snmp4j", "ERROR");
        logConfig.put("log4j.logger.org.snmp4j.agent", "ERROR");
        logConfig.put("log4j.logger.org.hibernate.cfg.AnnotationBinder", ("ERROR," + consoleAppender));
        
        PropertyConfigurator.configure(logConfig);

    }
}
