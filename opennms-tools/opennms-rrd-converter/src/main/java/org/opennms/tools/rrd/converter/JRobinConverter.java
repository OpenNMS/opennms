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
import java.util.TreeMap;

import org.apache.log4j.PropertyConfigurator;
import org.jrobin.core.ArcDef;
import org.jrobin.core.Archive;
import org.jrobin.core.Datasource;
import org.jrobin.core.FetchData;
import org.jrobin.core.FetchRequest;
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
     */
    public static void main(final String... args) {
        setupLogging();
        if (args.length == 0) {
            System.err.println("no directory specified!");
            System.exit(1);
        }
        final File topDirectory = new File(args[0]);
        
        try {
            final List<File> groupRrds = findGroupRrds(topDirectory);

            for (final File groupRrd : groupRrds) {
                System.out.println("- processing " + groupRrd);
                final File temporaryRrd = createTempRrd(groupRrd);
                
                JRobinConverter.consolidateRrdFile(groupRrd, temporaryRrd);
                if (!moveFileSafely(temporaryRrd, groupRrd)) {
                    System.err.println("  - unable to move " + temporaryRrd + " to " + groupRrd);
                    System.exit(1);
                }
            }
        } catch (final Exception e) {
            System.err.println("error while converting RRDs");
            e.printStackTrace();
        }
    }

    public static List<File> getMatchingGroupRrds(final File rrdDir, final File rrdGroupFile) throws ConverterException {
        if (rrdDir == null || rrdGroupFile == null) return Collections.emptyList();

        final List<String> dsNames = getDsNames(rrdGroupFile);
        final List<File> files = new ArrayList<File>();
        
        for (final File f : rrdDir.listFiles()) {
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
            final RrdDb db = new RrdDb(rrdFile.getPath());
            return Arrays.asList(db.getDsNames());
        } catch (final Exception e) {
            LogUtils.debugf(JRobinConverter.class, e, "error reading file %s", rrdFile);
            throw new ConverterException(e);
        }
    }

    public static List<String> getRras(final File rrdFile) throws ConverterException {
        try {
            final List<String> rras = new ArrayList<String>();
            final RrdDb db = new RrdDb(rrdFile.getPath());
            for (final ArcDef def : db.getRrdDef().getArcDefs()) {
                rras.add(def.dump());
            }
            return rras;
        } catch (final Exception e) {
            LogUtils.debugf(JRobinConverter.class, e, "error reading file %s", rrdFile);
            throw new ConverterException(e);
        }
    }

    public static void consolidateRrdFile(final File groupFile, final File outputFile) throws IOException, RrdException, ConverterException, Exception {
        final File rrdDir = groupFile.getParentFile();
        final RrdDb groupRrd = new RrdDb(groupFile.getAbsolutePath());

        final Map<Long,Map<String,Double>> sampleMap = new TreeMap<Long,Map<String,Double>>();
        final Map<Long,Double[]> sampleData = new TreeMap<Long,Double[]>();
        final List<File> individualRrds = JRobinConverter.getMatchingGroupRrds(rrdDir, groupFile);

        final RrdDb outputRrd = new RrdDb(outputFile.getAbsolutePath());
        final List<String> dsn = JRobinConverter.getDsNames(outputFile);
        LogUtils.debugf(JRobinConverter.class, "output DSNames = %s", dsn);
        final Map<String,Integer> dsIndexes = new HashMap<String,Integer>();
        for (final String dsName : dsn) {
            dsIndexes.put(dsName, outputRrd.getDsIndex(dsName));
        }

        for (final File f : individualRrds) {
            RrdDb inputRrd = new RrdDb(f.getAbsolutePath());
            final List<String> dsNames = JRobinConverter.getDsNames(f);
            LogUtils.debugf(JRobinConverter.class, "input DSNames = %s", dsNames);
            final String dsName = dsNames.get(0);

            final int groupDsCount = groupRrd.getDsCount();
            final int groupDsIndex = groupRrd.getDsIndex(dsName);
            final Datasource ds = groupRrd.getDatasource(dsName);
            final String dsType = ds.getDsType();

            collectSampleData(inputRrd, sampleData, groupDsCount, groupDsIndex, 300, dsName, sampleMap, dsType);
        }

        final List<String> dsNames = JRobinConverter.getDsNames(groupFile);
        for (int i = 0; i < dsNames.size(); i++) {
            final String dsName = dsNames.get(i);
            final Datasource ds = groupRrd.getDatasource(dsName);
            final String dsType = ds.getDsType();
            collectSampleData(groupRrd, sampleData, dsNames.size(), i, 300, dsName, sampleMap, dsType);
        }
        
        for (final long sampleTime : sampleMap.keySet()) {
            final Map<String, Double> entry = sampleMap.get(sampleTime);
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
            LogUtils.debugf(JRobinConverter.class, "sample = %s", s);
            s.update();

//            LogUtils.debugf(JRobinConverter.class, "sample = %s", sb.toString());
        }
        
        outputRrd.close();
    }

    public static void collectSampleData(RrdDb inputRrd, Map<Long,Double[]> sampleData, final int groupDsCount, final int groupDsIndex, int groupDsStep, String dsName, Map<Long, Map<String, Double>> sampleMap, String dsType) throws Exception {
        LogUtils.debugf(JRobinConverter.class, "inputRrd=%s, groupDsCount=%d, groupDsIndex=%d", inputRrd, groupDsCount, groupDsIndex);
        long lastSampleTime = 0;
        for (int j = inputRrd.getArcCount() - 1; j >= 0; j--) {
            final Archive inputArchive = inputRrd.getArchive(j);
            if (!inputArchive.getConsolFun().equals("AVERAGE")) {
                continue;
            }
            LogUtils.debugf(JRobinConverter.class, "=== index %d, archive %d (%s) ===", groupDsIndex, j, inputRrd.getPath());

            final Long start = inputArchive.getStartTime();
            Long current = start;
            final FetchRequest fr = inputRrd.createFetchRequest(inputArchive.getConsolFun(), inputArchive.getStartTime(), inputRrd.getLastArchiveUpdateTime());
            final FetchData fd = fr.fetchData();
            Long step = fd.getStep();
            LogUtils.debugf(JRobinConverter.class, "start time = %s, step = %d", new Date(start * 1000L), step);
            double lastValue = Double.NaN;

            for (double sample : fd.getValues(dsName)) {
                if (current > lastSampleTime) {
                    if (!Double.isNaN(sample)) {

                        if (dsType.equals("COUNTER")) {
                            if (!Double.isNaN(lastValue)) {
                                sample = lastValue + (sample * inputRrd.getHeader().getStep());
                            }
                            lastValue = sample;
                        }
                        Map<String,Double> sl = sampleMap.get(current);
                        if (sl == null) {
                            sl = new TreeMap<String,Double>();
                        }
                        Double[] sampleList = sampleData.get(current);
                        if (sampleList == null) {
                            sampleList = new Double[groupDsCount];
                            Arrays.fill(sampleList, Double.NaN);
                        }
                        sl.put(dsName, sample);
                        sampleMap.put(current, sl);
                        sampleList[groupDsIndex] = sample;
                        sampleData.put(current, sampleList);
                        lastSampleTime = current;
                    }
                }
                current += step;
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

    public static File createTempRrd(final File rrdFile) throws Exception {
        final File outputFile = File.createTempFile("jrobin-", ".jrb");

        final RrdDb oldRrd = new RrdDb(rrdFile.getAbsolutePath());

        final RrdDef rrdDef = oldRrd.getRrdDef();
        rrdDef.setPath(outputFile.getPath());
        rrdDef.setStartTime(0);
        RrdDb newRrd = new RrdDb(rrdDef);
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
        Properties logConfig = new Properties();

        String consoleAppender = ("CONSOLE");
        
        logConfig.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        logConfig.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        logConfig.put("log4j.appender.CONSOLE.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");

        logConfig.put("log4j.rootCategory", ("DEBUG, CONSOLE"));
        logConfig.put("log4j.logger.org.apache.commons.httpclient.HttpMethodBase", "ERROR");
        logConfig.put("log4j.logger.org.exolab.castor", "INFO");
        logConfig.put("log4j.logger.org.snmp4j", "ERROR");
        logConfig.put("log4j.logger.org.snmp4j.agent", "ERROR");
        logConfig.put("log4j.logger.org.hibernate.cfg.AnnotationBinder", ("ERROR," + consoleAppender));
        
        PropertyConfigurator.configure(logConfig);

    }
}
