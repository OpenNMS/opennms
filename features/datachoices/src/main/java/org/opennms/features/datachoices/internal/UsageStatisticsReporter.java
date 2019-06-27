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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.opennms.core.ipc.sink.common.SinkStrategy;
import org.opennms.core.rpc.common.RpcStrategy;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.core.utils.TimeSeries;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.features.datachoices.internal.StateManager.StateChangeHandler;
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

public class UsageStatisticsReporter implements StateChangeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UsageStatisticsReporter.class);

    public static final String USAGE_REPORT = "usage-report";

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
    
    private boolean m_useSystemProxy = true; // true == legacy behaviour

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
            final UsageStatisticsReportDTO usageStatsReport = generateReport();
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

    public UsageStatisticsReportDTO generateReport() {
        final SystemInfoUtils sysInfoUtils = new SystemInfoUtils();
        final UsageStatisticsReportDTO usageStatisticsReport = new UsageStatisticsReportDTO();
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
        // JVM uptime
        usageStatisticsReport.setJvmUptime(ManagementFactory.getRuntimeMXBean().getUptime());
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
        usageStatisticsReport.setMinions(m_minionDao.countAll());
        // Time-series, sink, and RPC strategies
        usageStatisticsReport.setTimeSeriesStrategy(TimeSeries.getTimeseriesStrategy().toString());
        usageStatisticsReport.setSinkStrategy(SinkStrategy.getSinkStrategy().toString());
        usageStatisticsReport.setRpcStrategy(RpcStrategy.getRpcStrategy().toString());
        // User and group counts
        try {
            UserFactory.init();
            usageStatisticsReport.setNumUsers(UserFactory.getInstance().getUserNames().size());
            GroupFactory.init();
            usageStatisticsReport.setNumGroups(GroupFactory.getInstance().getGroupNames().size());
        } catch (IOException e) {
            LOG.error("Encountered IOException while computing user and/or group counts. Setting both to -1.");
            usageStatisticsReport.setNumUsers(-1);
            usageStatisticsReport.setNumGroups(-1);
        }
        // Karaf features
        usageStatisticsReport.setKarafFeatureList(computeKarafFeatureList());
        return usageStatisticsReport;
    }
    
    private List<String> computeKarafFeatureList() {
        Set<String> featureSet = new HashSet<>();
        File etcDir = new File(new File(System.getProperty("opennms.home")), "etc");
        File featuresBootDotDDir = new File(etcDir, "featuresBoot.d");
        File featuresCfgFile = new File(etcDir, "org.apache.karaf.features.cfg");
        Properties featuresProp = new Properties();
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
            LOG.error("Encountered IOException while loading Karaf features config. Features list will be empty.");
        }
        for (File featureDotDFile : featuresBootDotDDir.listFiles()) {
            if (featureDotDFile.isDirectory() || featureDotDFile.getName().startsWith(".")) {
                // Ignore directories and dot-files
                continue;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(featureDotDFile))) {
                String line = br.readLine();
                while (line != null) {
                    if (line.matches("^\\s*#.*")) {
                        // Ignore comments
                        line = br.readLine();
                        continue;
                    }
                    String lineBody = line.trim();
                    if (lineBody.startsWith("!") && lineBody.length() > 1) {
                        String removeFeature = lineBody.substring(1).trim();
                        featureSet.remove(removeFeature);
                    } else {
                        featureSet.add(lineBody);
                    }
                    line = br.readLine();
                }
            } catch (IOException e) {
                LOG.error("Encountered IOException while loading Karaf featuresBoot.d directory contents. Feature list may be inaccurate.");
            }
        }

        List<String> featureList = new ArrayList<>(featureSet);
        Collections.sort(featureList);
        return featureList;
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
}
