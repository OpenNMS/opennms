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
import java.util.TreeMap;

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
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.opennms.core.utils.LogUtils;

public class JRobinConverter {
    private File m_directory;
    private File m_groupFile;

    public File getDirectory() {
        return m_directory;
    }

    public void setDirectory(final File dir) {
        m_directory = dir;
    }

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
     */
    public static void main(final String... args) throws ParseException, ConverterException {
        setupLogging();
        if (args.length == 0) {
            System.err.println(new Date() + ": no directory specified!");
            System.exit(1);
        }

        final List<File> scanFiles = new ArrayList<File>();
        
        final Options options = new Options();
        options.addOption("h", "help", false, "This help.");
        options.addOption("s", "scan", true, "Scan a directory for storeByGroup RRDs.");
        
        final CommandLineParser parser = new GnuParser();
        final CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h")) {
            new HelpFormatter().printHelp("jrobin-converter", options);
            System.exit(1);
        }
        if (cmd.hasOption("s")) {
            scanFiles.addAll(findGroupRrds(new File(cmd.getOptionValue("s"))));
        } else {
            for (final Object arg : cmd.getArgList()) {
                final File f = new File((String)arg);
                if (f.exists()) {
                    scanFiles.add(f);
                }
            }
        }

        if (scanFiles.size() == 0) {
            System.err.println(new Date() + ": error, no storeByGroup RRDs found!");
            System.exit(1);
        }
        
        try {
            int count = 1;
            for (final File groupRrd : scanFiles) {
                System.out.println(new Date() + ": processing " + groupRrd + " (" + count + "/" + scanFiles.size() + ")");
                final File temporaryRrd = createTempRrd(groupRrd);
                
                JRobinConverter.consolidateRrdFile(groupRrd, temporaryRrd);
                if (!moveFileSafely(temporaryRrd, groupRrd)) {
                    System.err.println(new Date() + ": - unable to move " + temporaryRrd + " to " + groupRrd);
                    System.exit(1);
                }
            }
        } catch (final Exception e) {
            System.err.println(new Date() + ": error while converting RRDs");
            e.printStackTrace();
        }
    }

    public static List<File> getMatchingGroupRrds(final File rrdGroupFile) throws ConverterException {
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

    public static List<String> getDsNames(final File rrdFile) throws ConverterException {
        try {
            final RrdDb db = new RrdDb(rrdFile.getAbsolutePath(), true);
            return Arrays.asList(db.getDsNames());
        } catch (final Exception e) {
            LogUtils.debugf(JRobinConverter.class, e, "error reading file %s", rrdFile);
            throw new ConverterException(e);
        }
    }

    public static List<String> getRras(final File rrdFile) throws ConverterException {
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


    public static Map<String, Integer> getDsIndexes(final RrdDb rrd) throws RrdException, IOException {
        final Map<String,Integer> indexes = new HashMap<String,Integer>();
        for (final String dsName : rrd.getDsNames()) {
            indexes.put(dsName, rrd.getDsIndex(dsName));
        }
        return indexes;
    }

    public static void consolidateRrdFile(final File groupFile, final File outputFile) throws IOException, RrdException, ConverterException {
        final RrdDb groupRrd = new RrdDb(groupFile, true);

        final Map<Long,Map<String,Double>> sampleMap = new TreeMap<Long,Map<String,Double>>();
        final List<File> individualRrds = JRobinConverter.getMatchingGroupRrds(groupFile);
        LogUtils.debugf(JRobinConverter.class, "individual RRDs = %s", individualRrds);

        final RrdDb outputRrd = new RrdDb(outputFile);
        final List<String> dsn = JRobinConverter.getDsNames(outputFile);
        LogUtils.debugf(JRobinConverter.class, "output DSNames = %s", dsn);

        final long endTime = groupRrd.getLastArchiveUpdateTime();
        // 1 year
        final long startTime = endTime - (60L * 60L * 24L * 365L);
        for (final File individualRrdFile : individualRrds) {
            final RrdDb inputRrd = new RrdDb(individualRrdFile, true);
            final List<String> dsNames = JRobinConverter.getDsNames(individualRrdFile);
            LogUtils.debugf(JRobinConverter.class, "input DSNames = %s", dsNames);
            if (dsNames.size() > 1) {
                LogUtils.warnf(JRobinConverter.class, "%s: more than one dsname found!", individualRrdFile);
            }
            final String dsName = dsNames.get(0);
            final String dsType = groupRrd.getDatasource(dsName).getDsType();

            collectSampleData(inputRrd, 300, dsName, sampleMap, dsType, startTime, endTime);
            inputRrd.close();
        }

        final List<String> dsNames = JRobinConverter.getDsNames(groupFile);
        for (int i = 0; i < dsNames.size(); i++) {
            final String dsName = dsNames.get(i);
            final String dsType = groupRrd.getDatasource(dsName).getDsType();
            collectSampleData(groupRrd, 300, dsName, sampleMap, dsType, startTime, endTime);
        }
        
        for (final long sampleTime : sampleMap.keySet()) {
            final Map<String, Double> entry = sampleMap.get(sampleTime);
//            printSample(sampleTime, entry);
            final Sample s = outputRrd.createSample(sampleTime);
            final double[] values = new double[dsNames.size()];
            for (int i = 0; i < dsNames.size(); i++) {
                final String string = dsNames.get(i);
                if (string != null) {
                    final Double value = entry.get(string);
                    if (value != null) values[i] = value;
                }
            }
            s.setValues(values);
            s.update();
        }
        
        outputRrd.close();
    }

    @SuppressWarnings("unused")
    private static void printSample(final long sampleTime, final Map<String, Double> entry) {
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

    public static void collectSampleData(final RrdDb inputRrd, final int groupDsStep, final String dsName, final Map<Long, Map<String, Double>> sampleMap, final String dsType, final long startTime, final long endTime) throws IOException, RrdException {
        long lastSampleTime = 0;
        for (int j = inputRrd.getArcCount() - 1; j >= 0; j--) {
            final Archive inputArchive = inputRrd.getArchive(j);
            if (!inputArchive.getConsolFun().equals("AVERAGE")) {
                continue;
            }
            LogUtils.debugf(JRobinConverter.class, "dsName=%s, archive=%d, start=%s, end=%s, step=%d (%s)", dsName, j, new Date(startTime * 1000L), new Date(endTime * 1000L), groupDsStep, inputRrd);

            Long current = startTime;
            final FetchData fd = inputRrd.createFetchRequest(inputArchive.getConsolFun(), startTime, endTime).fetchData();
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
                            Map<String,Double> dsEntries = sampleMap.get(current);
                            if (dsEntries == null) {
                                dsEntries = new TreeMap<String,Double>();
                            }
                            dsEntries.put(dsName, sample);
                            sampleMap.put(current, dsEntries);
                            lastSampleTime = current;
                            current += groupDsStep;
                        }
                    }
                }
            }
        }
    }

    public static List<File> findRrds(final File topDirectory) {
        final List<File> files = new ArrayList<File>();
        findRrds(topDirectory, files);
        return files;
    }
    
    public static List<File> findGroupRrds(final File topDirectory) throws ConverterException {
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

    public static void findRrds(final File directory, final List<File> files) {
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

    public static File createTempRrd(final File rrdFile) throws IOException, RrdException {
        final File outputFile = File.createTempFile("jrobin-", ".jrb");
        final RrdDb oldRrd = new RrdDb(rrdFile.getAbsolutePath(), true);
        final RrdDef rrdDef = oldRrd.getRrdDef();
        rrdDef.setPath(outputFile.getAbsolutePath());
        rrdDef.setStartTime(0);
        final RrdDb newRrd = new RrdDb(rrdDef);
        newRrd.close();

        return outputFile;
    }

    public static boolean moveFileSafely(final File in, final File out) throws IOException {
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
    
    public static void setupLogging() {
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
