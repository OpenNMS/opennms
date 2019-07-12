/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.ipc.sink.common.SinkStrategy;
import org.opennms.core.rpc.common.RpcStrategy;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.core.utils.TimeSeries;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.features.datachoices.internal.StateChangeHandler;
import org.opennms.karaf.extender.Feature;
import org.opennms.karaf.extender.KarafExtender;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UsageStatisticsReporter implements StateChangeHandler {
    
    @Reference
    private KarafExtender m_extender;
    
    private static final Logger LOG = LoggerFactory.getLogger(UsageStatisticsReporter.class);

    public static final String USAGE_REPORT = "usage-report";
    
    private Pattern XMX_PAT = Pattern.compile("^(?:-XX:MaxHeapSize=|-Xmx)(\\d+)([KkMmGg]?)$");

    private String m_url;

    private long m_interval;

    private Timer m_timer;

    private StateManager m_stateManager;

    private NodeDao m_nodeDao;

    private IpInterfaceDao m_ipInterfaceDao;

    private SnmpInterfaceDao m_snmpInterfaceDao;

    private MonitoredServiceDao m_monitoredServiceDao;

    private EventDao m_eventDao;

    private AlarmDao m_alarmDao;
    
    private MonitoringLocationDao m_monitoringLocationDao;
    
    private MinionDao m_minionDao;
    
    private DataSource m_dataSource;
    
    private boolean m_useSystemProxy = true; // true == legacy behaviour

    private boolean m_includeSensitiveDetails = false;
    
    public synchronized void init() {
        if (m_timer != null) {
            LOG.warn("Usage statistic reporter was already initialized.");
        }
        try {
            if (Boolean.FALSE.equals(m_stateManager.isEnabled())) {
                LOG.info("Usage statistic reporting is disabled.");
                return;
            } else if (Boolean.TRUE.equals(m_stateManager.isEnabled())) {
                sendAndSchedule();
            }
            LOG.info("Waiting for user confirmation.");
            // Listen for state changes
            m_stateManager.onIsEnabledChanged(this);
        } catch (IOException e) {
            LOG.warn("Failed check opt-in status. Assuming user opted out.", e);
        }
    }

    public synchronized void sendAndSchedule() {
        LOG.info("Scheduling usage statistics report every {} ms", m_interval);
        m_timer = new Timer();
        m_timer.schedule(new Task(), 0, m_interval);

        // Fire of the first report in a background thread
        sendAsync();
    }

    @Override
    public synchronized void onIsEnabledChanged(boolean isEnabled) {
        if (isEnabled && m_timer == null) {
            sendAndSchedule();
        } else if (!isEnabled && m_timer != null) {
            destroy();
        }
    }

    public synchronized void destroy() {
        if (m_timer != null) {
            LOG.info("Disabling scheduled report.");
            m_timer.cancel();
            m_timer = null;
        }
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            final UsageStatisticsReportDTO usageStatsReport = generateReport(m_includeSensitiveDetails);
            final String usageStatsReportJson = usageStatsReport.toJson();

            final HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                    .setConnectionTimeout(30 * 1000)
                    .setSocketTimeout(30 * 1000);
            if(m_useSystemProxy) {
                clientWrapper.useSystemProxySettings();
            }
            try (CloseableHttpClient client = clientWrapper.getClient()) {
                HttpPost httpRequest = new HttpPost(m_url + USAGE_REPORT);
                httpRequest.setEntity(new StringEntity(usageStatsReportJson, ContentType.create("application/json", StandardCharsets.UTF_8)));
                LOG.info("Sending usage statistics report to {}: {}", httpRequest.getURI(), usageStatsReportJson);
                client.execute(httpRequest);
                LOG.info("Succesfully sent usage statistics report.");
            } catch (IOException e) {
                LOG.info("The usage statistics report was not succesfully delivered: {}", e.getMessage());
            }
        }
    }

    public void sendSync() {
        new Task().run();
    }

    public void sendAsync() {
        Thread thread = new Thread(new Task());
        thread.start();
    }

    public UsageStatisticsReportDTO generateReport(boolean detailed) {
        m_includeSensitiveDetails = detailed;
        final SystemInfoUtils sysInfoUtils = new SystemInfoUtils();
        final UsageStatisticsReportDTO usageStatisticsReport;
        if (m_includeSensitiveDetails) {
            usageStatisticsReport = new DetailedUsageStatisticsReportDTO();
        } else {
            usageStatisticsReport = new UsageStatisticsReportDTO();
        }
        // Unique system identifier
        try {
            usageStatisticsReport.setSystemId(m_stateManager.getOrGenerateSystemId());
        } catch (IOException e) {
            LOG.warn("An error occurred while retrieving the system id. " +
                        "The usage report will be submitted with a null system id.", e);
        }
        // Operating System
        usageStatisticsReport.setOsName(System.getProperty("os.name"));
        usageStatisticsReport.setOsArch(System.getProperty("os.arch"));
        usageStatisticsReport.setOsVersion(System.getProperty("os.version"));
        usageStatisticsReport.setOsMemSize(determineOsMemSize());
        usageStatisticsReport.setOsCpus(determineOsCpus());
        usageStatisticsReport.setKeyPathFsUsedPct(determineKeyPathFsUsedPct());
        // OpenNMS version and flavor
        usageStatisticsReport.setVersion(sysInfoUtils.getVersion());
        usageStatisticsReport.setPackageName(sysInfoUtils.getPackageName());
        // Object counts
        usageStatisticsReport.setNodes(m_nodeDao.countAll());
        usageStatisticsReport.setIpInterfaces(m_ipInterfaceDao.countAll());
        usageStatisticsReport.setSnmpInterfaces(m_snmpInterfaceDao.countAll());
        usageStatisticsReport.setMonitoredServices(m_monitoredServiceDao.countAll());
        usageStatisticsReport.setEvents(m_eventDao.countAll());
        usageStatisticsReport.setAlarms(m_alarmDao.countAll());
        // Node statistics
        usageStatisticsReport.setNodesBySysOid(m_nodeDao.getNumberOfNodesBySysOid());
        // Location and Minion statistics
        usageStatisticsReport.setMonitoredServices(m_monitoringLocationDao.countAll());
        usageStatisticsReport.setNumMinions(m_minionDao.countAll());
        // Time-series, sink, and RPC strategies
        usageStatisticsReport.setTimeSeriesStrategy(TimeSeries.getTimeseriesStrategy().toString());
        usageStatisticsReport.setSinkStrategy(SinkStrategy.getSinkStrategy().toString());
        usageStatisticsReport.setRpcStrategy(RpcStrategy.getRpcStrategy().toString());
        // User and group counts
        try {
            UserFactory.init();
            usageStatisticsReport.setNumOnmsUsers(UserFactory.getInstance().getUserNames().size());
            GroupFactory.init();
            usageStatisticsReport.setNumOnmsGroups(GroupFactory.getInstance().getGroupNames().size());
        } catch (IOException e) {
            LOG.error("Encountered IOException while computing user and/or group counts. Setting both to -1.");
            usageStatisticsReport.setNumOnmsUsers(-1);
            usageStatisticsReport.setNumOnmsGroups(-1);
        }
        // Karaf features
        usageStatisticsReport.setKarafFeatureList(computeKarafFeatureList());
        
        // JVM details
        usageStatisticsReport.setJvmMaxHeapSize(determineXmxValue());
        usageStatisticsReport.setJvmVendor(System.getProperty("java.vm.vendor"));
        usageStatisticsReport.setJvmSpecVersion(System.getProperty("java.specification.version"));
        usageStatisticsReport.setJvmRuntimeVersion(System.getProperty("java.runtime.version"));
        usageStatisticsReport.setJvmUptime(ManagementFactory.getRuntimeMXBean().getUptime());

        if (m_includeSensitiveDetails) {
            populateSensitiveDetails((DetailedUsageStatisticsReportDTO) usageStatisticsReport);
        }
        return usageStatisticsReport;
    }
    
    private List<String> computeKarafFeatureList() {
        Set<String> featureSet = new HashSet<>();
        File etcDir = new File(new File(System.getProperty("opennms.home")), "etc");
        File featuresBootDotDDir = new File(etcDir, "featuresBoot.d");
        File featuresCfgFile = new File(etcDir, "org.apache.karaf.features.cfg");
        Properties featuresProp = new Properties();
        if (!featuresCfgFile.canRead()) {
            LOG.error("Features config file {} does not exist or is not readable. Returning empty feature list.", featuresCfgFile.getAbsolutePath());
            return Collections.emptyList();
        }
        try (FileInputStream fis = new FileInputStream(featuresCfgFile)) {
            featuresProp.load(fis);
            String bootProp = featuresProp.getProperty("featuresBoot");
            if (bootProp != null) {
                for (String propName : bootProp.split(",")) {
                    if (propName.startsWith("(") && propName.length() > 1) {
                        propName = propName.substring(1);
                    }
                    if (propName.endsWith(")") && propName.length() > 1) {
                        propName = propName.substring(0, propName.length() - 2);
                    }
                    featureSet.add(propName.trim());
                }
            }
        } catch (IOException e) {
            LOG.error("Encountered IOException while loading Karaf features. Features list will be empty.");
        }
        
        try {
            for (Feature feature: m_extender.getFeaturesBoot()) {
                featureSet.add(feature.getName());
            }
        } catch (IOException e1) {
            LOG.error("Encountered IOException while loading Karaf featuresBoot.d directory contents. Feature list may be inaccurate.");
        }

        List<String> featureList = new ArrayList<>(featureSet);
        Collections.sort(featureList);
        return featureList;
    }
    
    private void populateSensitiveDetails(DetailedUsageStatisticsReportDTO usageStatisticsReport) {
        usageStatisticsReport.setHostname(determineHostname());
        usageStatisticsReport.setJvmUserName(System.getProperty("user.name"));
        usageStatisticsReport.setSeLinuxEnforce(determineSELinuxEnforce());
        usageStatisticsReport.setFsUtilInfo(determineFilesystemUsage());
        // RDBMS details
        try (Connection conn = m_dataSource.getConnection()) {
            usageStatisticsReport.setRdbmsType(conn.getMetaData().getDatabaseProductName());
            usageStatisticsReport.setRdbmsVersion(conn.getMetaData().getDatabaseProductVersion());
            final String dbUrl = conn.getMetaData().getURL().toLowerCase();
            if (dbUrl.contains("://localhost") || dbUrl.contains("://127.0.0.1")) {
                usageStatisticsReport.setRdbmsOnLocalhost(true);
            }
        } catch (SQLException e) {
            LOG.error("Encountered SQLException while fetching RDBMS details");
        }
    }
    
    private String determineHostname() {
        String hostname = "";
        File hostnameFile = new File("/proc/sys/kernel/hostname");
        if (!hostnameFile.canRead()) {
            return hostname;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(hostnameFile))) {
            hostname = br.readLine();
        } catch (IOException e) {
            LOG.error("Failed to determine hostname via /proc. Hostname will be empty.");
        }
        return hostname;
    }
    
    private long determineXmxValue() {
        final List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        final String xmxString;
        long xmxVal = -1L;
        long xmxMult = 1L;
        for (String arg : inputArgs) {
            Matcher xmxMat = XMX_PAT.matcher(arg);
            if (xmxMat.matches()) {
                xmxString = xmxMat.group(1);
                try {
                    xmxVal = Long.parseLong(xmxString);
                } catch (NumberFormatException nfe) {
                    LOG.error("Failed to parse Xmx / MaxHeapSize numeric value '{}'. Using -1 as a placeholder.", xmxString);
                }
                final String xmxSuffix = xmxMat.group(2).toLowerCase();
                if ("k".contentEquals(xmxSuffix)) {
                    xmxMult = 1024L;
                } else if ("m".contentEquals(xmxSuffix)) {
                    xmxMult = 1024L*1024L;
                } else if ("g".contentEquals(xmxSuffix)) {
                    xmxMult = 1024L*1024L*1024L;
                }
                break;
            }
        }
        if (xmxVal == -1L) {
            LOG.error("Failed to extract Xmx / MaxHeapSize value from JVM input args '{}'. Using -1 as a placeholder.", ManagementFactory.getRuntimeMXBean().getInputArguments());
            return xmxVal;
        } else {
            return xmxVal * xmxMult;
        }
    }
    
    private long determineOsMemSize() {
        String memtotalStr = "";
        Long memtotal = -1L;
        File meminfoFile = new File("/proc/meminfo");
        if (!meminfoFile.canRead()) {
            return memtotal;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(meminfoFile))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("MemTotal:")) {
                    final String memtotalWithUnits = line.split(":")[1].trim();  // e.g. "1882220 kB"
                    memtotalStr = memtotalWithUnits.split(" ")[0];
                    break;
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            LOG.error("Failed to determine MemTotal via /proc. osMemSize will be -1.");
        }
        if (! "".equals(memtotalStr)) {
            try {
                memtotal = Long.parseLong(memtotalStr) * 1024L;
            } catch (NumberFormatException nfe) {
                LOG.error("Failed to parse MemTotal value '{}' for osMemSize. Using -1 as a placeholder.", memtotalStr);
            }
        }
        return memtotal;
    }
    
    private long determineOsCpus() {
        long cpus = -1L;
        File cpuinfoFile = new File("/proc/cpuinfo");
        if (!cpuinfoFile.canRead()) {
            return cpus;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(cpuinfoFile))) {
            cpus = 0;
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("processor")) {
                    cpus++;
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            LOG.error("Encountered IOException while counting CPUs via /proc. osCpus will be -1.");
            cpus = -1L;
        }
        if (cpus == 0) {
            LOG.error("Failed to count any CPUs via /proc. Using -1 as a placeholder.");
            cpus = -1L;
        }
        return cpus;
    }
    
    private int determineSELinuxEnforce() {
        String enforceStr = "";
        int enforce = -1;
        File enforceFile = new File("/sys/fs/selinux/enforce");
        if (!enforceFile.canRead()) {
            return enforce;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(enforceFile))) {
            enforceStr = br.readLine();
        } catch (IOException e) {
            LOG.error("Failed to determine SELinux enforcement status via /sys. seLinuxEnforce will be -1.");
            return -1;
        }
        try {
            enforce = Integer.parseInt(enforceStr, 10);
        } catch (NumberFormatException nfe) {
            LOG.error("Failed to parse value '{}' for SELinux enforcement status via /sys. seLinuxEnforce will be -1.", enforceStr);
            return -1;
        }
        return enforce;
    }
    
    private Map<String,Map<String,Number>> determineFilesystemUsage() {
        final long oneGig = 1024L*1024L*1024L;
        Map<String,Map<String,Number>> fsUsage = new LinkedHashMap<>(); 
        for (FileStore fs : FileSystems.getDefault().getFileStores()) {
            Map<String,Number> fsEntry = new LinkedHashMap<>();
            final long totalB, totalGB, usedB, usedGB, availB, availGB, usedPct;
            try {
                totalB = fs.getTotalSpace();
                totalGB = totalB / oneGig;
                usedB = (totalB - fs.getUsableSpace());
                usedGB = usedB / oneGig;
                availB = fs.getUsableSpace();
                availGB = availB / oneGig;
                if (totalB >= oneGig) {
                    usedPct = new Double(new Double(usedB) / new Double(totalB) * 100).longValue();
                    fsEntry.put("sizeGB", totalGB);
                    fsEntry.put("usedGB", usedGB);
                    fsEntry.put("availGB", availGB);
                    fsEntry.put("usedPct", usedPct);
                    fsUsage.put(fs.name(), fsEntry);
                } else {
                    LOG.info("Omitting stats for filesystem {} because total size is smaller than 1GB", fs.name());
                }
            } catch (IOException e) {
                LOG.error("Encountered IOException while retrieving filesystem utilization data for FS '{}'", fs.name());
                return fsUsage;
            }
        }
        return fsUsage;
    }
    
    private Map<String,Number> determineKeyPathFsUsedPct() {
        Map<String,Number> stats = new LinkedHashMap<>();
        Map<String,File> paths = new LinkedHashMap<>();
        paths.put("onmsHome", new File(System.getProperty("opennms.home")));
        paths.put("onmsShare", new File(paths.get("onmsHome"), "share"));
        paths.put("onmsLogs", new File(paths.get("onmsHome"), "logs"));
        for (String pathToken : paths.keySet()) {
            Double total = new Double(paths.get(pathToken).getTotalSpace());
            Double usable = new Double(paths.get(pathToken).getUsableSpace());
            if (usable > 0) {
                Double usedPct = (total - usable) / total * 100;
                stats.put(pathToken, usedPct.longValue());
            } else {
                LOG.error("totalSpace on path {} is zero. Returning -1 for utilization.", pathToken);
                stats.put(pathToken, -1);
            }
        }
        return stats;
    }

    public void setUrl(String url) {
        m_url = url;
    }

    public void setInterval(long interval) {
        m_interval = interval;
    }

    public void setStateManager(StateManager stateManager) {
        m_stateManager = stateManager;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }

    public MonitoringLocationDao getMonitoringLocationDao() {
        return m_monitoringLocationDao;
    }

    public void setMonitoringLocationDao(
            MonitoringLocationDao monitoringLocationDao) {
        m_monitoringLocationDao = monitoringLocationDao;
    }

    public MinionDao getMinionDao() {
        return m_minionDao;
    }

    public void setMinionDao(MinionDao minionDao) {
        m_minionDao = minionDao;
    }

    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public void setUseSystemProxy(boolean useSystemProxy){
        m_useSystemProxy = useSystemProxy;
    }
    
    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public void setIncludeSensitiveDetails(boolean includeSensitiveDetails) {
        m_includeSensitiveDetails = includeSensitiveDetails;
    }
}
