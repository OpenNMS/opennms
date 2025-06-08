/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.datachoices.internal.usagestatistics;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.sql.DataSource;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.karaf.features.Dependency;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.opennms.core.db.DataSourceFactoryBean;
import org.opennms.core.ipc.sink.common.SinkStrategy;
import org.opennms.core.rpc.common.RpcStrategy;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.core.utils.TimeSeries;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.features.datachoices.internal.StateManager;
import org.opennms.features.datachoices.internal.StateManager.StateChangeHandler;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.usageanalytics.api.UsageAnalyticDao;
import org.opennms.features.usageanalytics.api.UsageAnalyticMetricName;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class UsageStatisticsReporter implements StateChangeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UsageStatisticsReporter.class);

    public static final String USAGE_REPORT = "usage-report";
    private static final String JMX_OBJ_OS = "java.lang:type=OperatingSystem";
    private static final String JMX_OBJ_OPENNMS_POLLERD = "OpenNMS:Name=Pollerd";
    private static final String JMX_OBJ_OPENNMS_EVENTLOGS_PROCESS = "org.opennms.netmgt.eventd:name=eventlogs.process,type=timers";
    private static final String JMX_OBJ_OPENNMS_FLOWS_PERSISTED = "org.opennms.netmgt.flows:name=flowsPersisted,type=meters";
    private static final String JMX_OBJ_OPENNMS_REPO_SAMPLE_INSERTED = "org.opennms.newts:name=repository.samples-inserted,type=meters";
    private static final String JMX_OBJ_OPENNMS_QUEUED = "OpenNMS:Name=Queued";
    private static final String JMX_ATTR_FREE_PHYSICAL_MEMORY_SIZE = "FreePhysicalMemorySize";
    private static final String JMX_ATTR_TOTAL_PHYSICAL_MEMORY_SIZE = "TotalPhysicalMemorySize";
    private static final String JMX_ATTR_AVAILABLE_PROCESSORS = "AvailableProcessors";
    private static final String JMX_ATTR_TASKS_COMPLETED = "TasksCompleted";
    private static final String JMX_ATTR_COUNT = "Count";
    private static final String JMX_ATTR_UPDATES_COMPLETED = "UpdatesCompleted";
    private static final MBeanServer M_BEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
    private static final int MAX_DEP_RECURSION_DEPTH = 2;
    private static final String OIA_FEATURE_NAME = "opennms-integration-api";
    private static final String API_LAYER_FEATURE_NAME = "opennms-api-layer";

    public static final String APPLIANCE_VIRTUAL_OID = ".1.3.6.1.4.1.5813.42.5.1";
    public static final String APPLIANCE_MINI_OID = ".1.3.6.1.4.1.5813.42.5.2";
    public static final String APPLIANCE_1U_OID = ".1.3.6.1.4.1.5813.42.5.3";

    //could be made configurable in future
    public static final int DEFAULT_ALERTS_LAST_HOURS = 24;
    public static final int DEFAULT_EVENTS_LAST_HOURS = 24;

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

    private MonitoringSystemDao m_monitoringSystemDao;

    private BusinessServiceEdgeDao m_businessServiceEdgeDao;

    private DeviceConfigDao m_deviceConfigDao;

    private FeaturesService m_featuresService;

    private ProvisiondConfigurationDao m_provisiondConfigurationDao;

    private ServiceConfigFactory m_serviceConfigurationFactory;

    private DestinationPathFactory m_destinationPathFactory;

    private NotifdConfigFactory m_notifdConfigFactory;

    private UsageAnalyticDao m_usageAnalyticDao;

    private GroupFactory m_groupFactory;
    private ForeignSourceRepository m_deployedForeignSourceRepository;
    private DataSourceFactoryBean m_dataSourceFactoryBean;

    private ApplicationDao m_applicationDao;
    
    private boolean m_useSystemProxy = true; // true == legacy behaviour

    private OutageDao m_outageDao;

    private NotificationDao m_notificationDao;


    private FlowQueryService flowQueryService;

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

        usageStatisticsReport.setFlowCountPerSecond(getFlowCount());
        // Operating System
        usageStatisticsReport.setOsName(System.getProperty("os.name"));
        usageStatisticsReport.setOsArch(System.getProperty("os.arch"));
        usageStatisticsReport.setOsVersion(System.getProperty("os.version"));
        // OpenNMS version and flavor
        usageStatisticsReport.setVersion(sysInfoUtils.getVersion());
        usageStatisticsReport.setPackageName(sysInfoUtils.getPackageName());
        // Object counts
        usageStatisticsReport.setNodes(m_nodeDao.countAll());
        usageStatisticsReport.setIpInterfaces(m_ipInterfaceDao.countAll());
        usageStatisticsReport.setSnmpInterfaces(m_snmpInterfaceDao.countAll());
        usageStatisticsReport.setSnmpInterfacesWithFlows(m_snmpInterfaceDao.getNumInterfacesWithFlows());
        usageStatisticsReport.setMonitoredServices(m_monitoredServiceDao.countAll());
        usageStatisticsReport.setEvents(m_eventDao.countAll());
        usageStatisticsReport.setEventsLastHours(m_eventDao.getNumEventsLastHours(DEFAULT_EVENTS_LAST_HOURS));
        usageStatisticsReport.setAlarms(m_alarmDao.countAll());
        usageStatisticsReport.setAlarmsLastHours(m_alarmDao.getNumAlarmsLastHours(DEFAULT_ALERTS_LAST_HOURS));
        usageStatisticsReport.setSituations(m_alarmDao.getNumSituations());
        usageStatisticsReport.setMonitoringLocations(m_monitoringLocationDao.countAll());
        usageStatisticsReport.setMinions(m_monitoringSystemDao.getNumMonitoringSystems(OnmsMonitoringSystem.TYPE_MINION));
        usageStatisticsReport.setApplications(m_applicationDao.countAll());
        usageStatisticsReport.setOutages(m_outageDao.currentOutageCount());
        usageStatisticsReport.setNotifications(m_notificationDao.countAll());
        
        // Node statistics
        usageStatisticsReport.setNodesBySysOid(m_nodeDao.getNumberOfNodesBySysOid());
        // Karaf features
        usageStatisticsReport.setInstalledFeatures(getInstalledFeatures());
        usageStatisticsReport.setInstalledOIAPlugins(getInstalledOIAPluginsByDependencyTree());
        setJmxAttributes(usageStatisticsReport);
        gatherProvisiondData(usageStatisticsReport);
        usageStatisticsReport.setServices(m_serviceConfigurationFactory.getServiceNameMap());
        usageStatisticsReport.setGroups(this.getGroupCount());
        usageStatisticsReport.setUsers(this.getUserCount());
        usageStatisticsReport.setDestinationPathCount(getDestinationPathCount());
        usageStatisticsReport.setNotificationEnablementStatus(getNotificationEnablementStatus());
        usageStatisticsReport.setOnCallRoleCount(m_groupFactory.getRoles().size());
        usageStatisticsReport.setRequisitionCount(getDeployedRequisitionCount());
        usageStatisticsReport.setRequisitionWithChangedFSCount(getDeployedRequisitionWithModifiedFSCount());
        usageStatisticsReport.setBusinessEdgeCount(m_businessServiceEdgeDao.countAll());
        usageStatisticsReport.setSinkStrategy(SinkStrategy.getSinkStrategy().getName());
        usageStatisticsReport.setRpcStrategy(RpcStrategy.getRpcStrategy().getName());
        usageStatisticsReport.setTssStrategies(TimeSeries.getTimeseriesStrategy().getName());
        // DCB statistics
        usageStatisticsReport.setDcbSucceed(m_usageAnalyticDao.getValueByMetricName(UsageAnalyticMetricName.DCB_SUCCEED.toString()));
        usageStatisticsReport.setDcbFailed(m_usageAnalyticDao.getValueByMetricName(UsageAnalyticMetricName.DCB_FAILED.toString()));
        usageStatisticsReport.setDcbWebUiEntries(m_usageAnalyticDao.getValueByMetricName(UsageAnalyticMetricName.DCB_WEBUI_ENTRY.toString()));
        usageStatisticsReport.setNodesWithDeviceConfigBySysOid(m_deviceConfigDao.getNumberOfNodesWithDeviceConfigBySysOid());
        usageStatisticsReport.setApplianceCounts(this.getApplianceCountByModel());
        // Container
        usageStatisticsReport.setInContainer(this.isContainerized());

        setDatasourceInfo(usageStatisticsReport);

        return usageStatisticsReport;
    }

    private long getFlowCount() {

        long flowCount = 0;

        if(flowQueryService != null) {
            try {
                final long currentTime = System.currentTimeMillis();
                final long twentyFourHoursAgo = currentTime - Duration.ofHours(24).toMillis();
                final List<Filter> filters = Collections.singletonList(new TimeRangeFilter(twentyFourHoursAgo, currentTime));
                flowCount = flowQueryService.getFlowCount(filters).get();

            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("An error occurred while retrieving the flow count. ", e);
            }
        }

        return flowCount;
    }

    private boolean isContainerized() {
        // this will detect podman even in custom container builds
        final boolean inPodman = new File("/run/.containerenv").exists();
        // this will detect docker even in custom container builds
        final boolean inDocker = new File("/.dockerenv").exists();
        // this will detect in any case if our unmodified container is run, since this file was created in our Dockerfile
        final boolean containerRunning = "container".equals(System.getenv("OPENNMS_EXECUTION_ENVIRONMENT"));

        return inPodman || inDocker || containerRunning;
    }

    private void setJmxAttributes(UsageStatisticsReportDTO usageStatisticsReport) {
        setSystemJmxAttributes(usageStatisticsReport);
        setOpenNmsJmxAttributes(usageStatisticsReport);
    }

    private void setSystemJmxAttributes(UsageStatisticsReportDTO usageStatisticsReport) {
        Object freePhysicalMemSizeObj = getJmxAttribute(JMX_OBJ_OS, JMX_ATTR_FREE_PHYSICAL_MEMORY_SIZE);
        if (freePhysicalMemSizeObj != null) {
            usageStatisticsReport.setFreePhysicalMemorySize((long) freePhysicalMemSizeObj);
        }
        Object totalPhysicalMemSizeObj = getJmxAttribute(JMX_OBJ_OS, JMX_ATTR_TOTAL_PHYSICAL_MEMORY_SIZE);
        if (totalPhysicalMemSizeObj != null) {
            usageStatisticsReport.setTotalPhysicalMemorySize((long) totalPhysicalMemSizeObj);
        }
        Object availableProcessorsObj = getJmxAttribute(JMX_OBJ_OS, JMX_ATTR_AVAILABLE_PROCESSORS);
        if (availableProcessorsObj != null) {
            usageStatisticsReport.setAvailableProcessors((int) availableProcessorsObj);
        }

        //populating current system cpu utilization
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        double cpuLoad =  processor.getSystemCpuLoad(1000) * 100;
        usageStatisticsReport.setCpuUtilization(String.format("%.2f%%", cpuLoad));

        Object totalPhysicalMemorySizeObj = getJmxAttribute(JMX_OBJ_OS, JMX_ATTR_TOTAL_PHYSICAL_MEMORY_SIZE);
        if (totalPhysicalMemorySizeObj != null) {
            long totalMemory = (long)totalPhysicalMemorySizeObj;
            if (totalMemory == 0) {
                usageStatisticsReport.setMemoryUtilization("0%");
            } else {
                Object freePhysicalMemorySizeObj = getJmxAttribute(JMX_OBJ_OS, JMX_ATTR_FREE_PHYSICAL_MEMORY_SIZE);
                if (freePhysicalMemorySizeObj != null) {
                    long freeMemory = (long) freePhysicalMemorySizeObj;
                    double utilizedMemory = ((double) (totalMemory - freeMemory) / totalMemory) * 100;
                    usageStatisticsReport.setMemoryUtilization(String.format("%.2f%%", utilizedMemory));
                }
            }
        }
    }

    private void setOpenNmsJmxAttributes(UsageStatisticsReportDTO usageStatisticsReport) {
        Object pollerTasksCompletedObj = getJmxAttribute(JMX_OBJ_OPENNMS_POLLERD, JMX_ATTR_TASKS_COMPLETED);
        if (pollerTasksCompletedObj != null) {
            usageStatisticsReport.setPollsCompleted((long) pollerTasksCompletedObj);
        }
        Object eventLogsProcessedObj = getJmxAttribute(JMX_OBJ_OPENNMS_EVENTLOGS_PROCESS, JMX_ATTR_COUNT);
        if (eventLogsProcessedObj != null) {
            usageStatisticsReport.setEventLogsProcessed((long) eventLogsProcessedObj);
        }
        Object coreFlowsPersistedObj = getJmxAttribute(JMX_OBJ_OPENNMS_FLOWS_PERSISTED, JMX_ATTR_COUNT);
        if (coreFlowsPersistedObj != null) {
            usageStatisticsReport.setCoreFlowsPersisted((long) coreFlowsPersistedObj);
        }
        Object coreNewtsSamplesInsertedObj = getJmxAttribute(JMX_OBJ_OPENNMS_REPO_SAMPLE_INSERTED, JMX_ATTR_COUNT);
        if (coreNewtsSamplesInsertedObj != null) {
            usageStatisticsReport.setCoreNewtsSamplesInserted((long) coreNewtsSamplesInsertedObj);
        }
        Object coreQueuedUpdatesCompletedObj = getJmxAttribute(JMX_OBJ_OPENNMS_QUEUED, JMX_ATTR_UPDATES_COMPLETED);
        if (coreQueuedUpdatesCompletedObj != null) {
            usageStatisticsReport.setCoreQueuedUpdatesCompleted((long) coreQueuedUpdatesCompletedObj);
        }
    }

    private Object getJmxAttribute(String objectName, String attributeName) {
        ObjectName objNameActual;
        try {
            objNameActual = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            LOG.warn("Failed to query from object name " + objectName, e);
            return null;
        }
        try {
            return M_BEAN_SERVER.getAttribute(objNameActual, attributeName);
        } catch (InstanceNotFoundException | AttributeNotFoundException
                 | ReflectionException | MBeanException e) {
            LOG.warn("Failed to query from attribute name " + attributeName + " on object " + objectName, e);
            return null;
        }
    }

    private long getDeployedRequisitionCount() {
        return m_deployedForeignSourceRepository.getRequisitions().size();
    }

    private long getDeployedRequisitionWithModifiedFSCount() {
        ForeignSource defaultFS = m_deployedForeignSourceRepository.getDefaultForeignSource();
        return m_deployedForeignSourceRepository.getRequisitions().stream()
                .filter(req -> !req.getForeignSource().equals(defaultFS.getName())).count();
    }

    void setDatasourceInfo(final UsageStatisticsReportDTO usageStatisticsReport) {
        try {
            final DataSource datasource = m_dataSourceFactoryBean.getObject();
            try (final Connection connection = datasource.getConnection()) {
                final DatabaseMetaData metaData = connection.getMetaData();

                usageStatisticsReport.setDatabaseProductName(metaData.getDatabaseProductName());
                usageStatisticsReport.setDatabaseProductVersion(metaData.getDatabaseProductVersion());
            }
        } catch (Exception e) {
            LOG.error("Error retrieving datasource information", e);
        }
    }
    private int getUserCount() {
        try {
            UserFactory.init();
            UserManager userFactory = UserFactory.getInstance();
            return userFactory.getUsers().size();
        }catch (Exception e) {
            return 0;
        }
    }

    private int getGroupCount() {
        try{
            GroupFactory.init();
            GroupManager groupFactory = GroupFactory.getInstance();
            return groupFactory.getGroups().size();
        } catch (Exception e) {
            return 0;
        }
    }
    private String getInstalledFeatures() {
        String installedFeatures;
        try {
            installedFeatures = Arrays.stream(m_featuresService.listInstalledFeatures())
                    .map(f -> f.getName() + "/" + f.getVersion())
                    .sorted()
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            installedFeatures = "ERROR: Failed to enumerate the installed features: " + e.getMessage();
        }
        return installedFeatures;
    }

    /**
     *  This descends the dependency tree breadth-first to find all installed features that have a direct
     *  or transitive dependence on the opennms-integration-api feature.
     *
     *  'opennms-api-layer' is the only core feature that is dependent on the opennms-integration-api.
     *  Don't include that in the list of installed plugins.
     */
    private String getInstalledOIAPluginsByDependencyTree() {
        List<Feature> featuresDependentOnOIA = new ArrayList<>();
        List<String> featuresToIgnore = new ArrayList<>();
        featuresToIgnore.add(API_LAYER_FEATURE_NAME);
        try {
            for (Feature feature : m_featuresService.listInstalledFeatures()) {
                featuresDependentOnOIA = recurse(feature, featuresDependentOnOIA, featuresToIgnore, 0);
            }
        }
        catch (Exception e) {
            return "ERROR: Failed to enumerate the installed features: " + e.getMessage();
        }
        return featuresDependentOnOIA.stream()
                    .map(f -> f.getName() + "/" + f.getVersion())
                    .sorted()
                    .collect(Collectors.joining(","));
    }

    private List<Feature> recurse(Feature feature, List<Feature> oiaDependentFeatures, List<String> featuresToIgnore, int depth) {
        // should this feature be ignored?
        if (featuresToIgnore.contains(feature.getName())) {
            return oiaDependentFeatures;
        }
        // Is this feature already known to be dependent on OIA?
        if (oiaDependentFeatures.contains(feature)) {
            return oiaDependentFeatures;
        }
        // Is this feature directly dependent on OIA?
        for (Dependency dep : feature.getDependencies()) {
            if (dep.getName().equals(OIA_FEATURE_NAME)) {
                oiaDependentFeatures.add(feature);
                return oiaDependentFeatures;
            }
        }
        // If this as deep as we should go, don't inspect children
        if (depth >= MAX_DEP_RECURSION_DEPTH) {
            return oiaDependentFeatures;
        }
        // Are any of this feature's dependencies already known to be dependent on OIA?
        for (Dependency dep : feature.getDependencies()) {
            if (oiaDependentFeatures.stream().map(d -> d.getName()).anyMatch(str -> str.equals(dep.getName()))) {
                oiaDependentFeatures.add(feature);
                return oiaDependentFeatures;
            }
        }
        // Don't recheck this feature in the case of circular dependencies
        featuresToIgnore.add(feature.getName());
        // Does this feature have a transitive dependency on OIA that we haven't mapped yet?
        for (Dependency dep : feature.getDependencies()) {
            try {
                int numDependencies = oiaDependentFeatures.size();
                Feature depFeature = m_featuresService.getFeature(dep.getName());
                oiaDependentFeatures = recurse(depFeature, oiaDependentFeatures, featuresToIgnore, depth++);
                // If we found any new dependencies within depFeature, then this feature
                // is also dependent on OIA
                if (numDependencies < oiaDependentFeatures.size()) {
                    oiaDependentFeatures.add(feature);
                    return oiaDependentFeatures;
                }

            }
            catch (Exception e) {}
        }
        // This feature is not dependent on OIA
        return oiaDependentFeatures;
    }

    private Map<String, Long> getApplianceCountByModel() {
        Map<String, Long> appliances = new HashMap();
        var oidMap = m_nodeDao.getNumberOfNodesBySysOid();
        appliances.put("virtualAppliance",  oidMap.containsKey(APPLIANCE_VIRTUAL_OID) ? oidMap.get(APPLIANCE_VIRTUAL_OID) : 0L);
        appliances.put("applianceMini",     oidMap.containsKey(APPLIANCE_MINI_OID)    ? oidMap.get(APPLIANCE_MINI_OID)    : 0L);
        appliances.put("appliance1U",       oidMap.containsKey(APPLIANCE_1U_OID)      ? oidMap.get(APPLIANCE_1U_OID)      : 0L);
        return appliances;
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

    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public void setMonitoringLocationDao(MonitoringLocationDao monitoringLocationDao) {
        m_monitoringLocationDao = monitoringLocationDao;
    }

    public void setMonitoringSystemDao(MonitoringSystemDao monitoringSystemDao) {
        m_monitoringSystemDao = monitoringSystemDao;
    }

    public void setBusinessServiceEdgeDao(BusinessServiceEdgeDao businessServiceDao) {
        m_businessServiceEdgeDao = businessServiceDao;
    }

    public void setOutageDao(OutageDao outageDao){
        m_outageDao = outageDao;
    }

    public void setNotificationDao(NotificationDao notificationDao){
        m_notificationDao = notificationDao;
    }
    public void setFeaturesService(FeaturesService featuresService) {
        m_featuresService = featuresService;
    }
    
    public void setApplicationDao(ApplicationDao applicationDao){
        m_applicationDao = applicationDao;
    }
    public void setUseSystemProxy(boolean useSystemProxy){
        m_useSystemProxy = useSystemProxy;
    }

    public ProvisiondConfigurationDao getProvisiondConfigurationDao() {
        return m_provisiondConfigurationDao;
    }

    public void setProvisiondConfigurationDao(ProvisiondConfigurationDao provisiondConfigurationDao) {
        m_provisiondConfigurationDao = provisiondConfigurationDao;
    }

    public void setDataSourceFactoryBean(DataSourceFactoryBean dataSourceFactoryBean) {
        m_dataSourceFactoryBean = dataSourceFactoryBean;
    }

    public void setUsageAnalyticDao(UsageAnalyticDao usageAnalyticDao) {
        m_usageAnalyticDao = usageAnalyticDao;
    }

    private void gatherProvisiondData(final UsageStatisticsReportDTO usageStatisticsReport) {
        try {
            usageStatisticsReport.setProvisiondImportThreadPoolSize(m_provisiondConfigurationDao.getImportThreads());
            usageStatisticsReport.setProvisiondRescanThreadPoolSize(m_provisiondConfigurationDao.getRescanThreads());
            usageStatisticsReport.setProvisiondScanThreadPoolSize(m_provisiondConfigurationDao.getScanThreads());
            usageStatisticsReport.setProvisiondWriteThreadPoolSize(m_provisiondConfigurationDao.getWriteThreads());
            usageStatisticsReport.setProvisiondRequisitionSchemeCount(m_provisiondConfigurationDao.getRequisitionSchemeCount());
        } catch (IOException e) {
            LOG.error("Error retrieving provisiond configuration", e);
        }
    }

    public ServiceConfigFactory getServiceConfigurationFactory() {
        return m_serviceConfigurationFactory;
    }

    public void setServiceConfigurationFactory(ServiceConfigFactory serviceConfigurationFactory) {
        m_serviceConfigurationFactory = serviceConfigurationFactory;
    }

    public void setDestinationPathFactory(DestinationPathFactory destinationPathFactory){
        m_destinationPathFactory = destinationPathFactory;
    }

    public void setNotifdConfigFactory(NotifdConfigFactory notifdConfigFactory) {
        this.m_notifdConfigFactory = notifdConfigFactory;
    }

    public void setGroupFactory(GroupFactory groupFactory) {
        this.m_groupFactory = groupFactory;
    }

    public void setDeployedForeignSourceRepository(ForeignSourceRepository fsRepo) {
        this.m_deployedForeignSourceRepository = fsRepo;
    }

    public ForeignSourceRepository getDeployedForeignSourceRepository() {
        return m_deployedForeignSourceRepository;
    }

    public void setDeviceConfigDao(DeviceConfigDao deviceConfigDao) {
        this.m_deviceConfigDao = deviceConfigDao;
    }

    public void setFlowQueryService(FlowQueryService flowQueryService) {
        this.flowQueryService = flowQueryService;
    }


    private int getDestinationPathCount(){
        try {
            return m_destinationPathFactory.getPaths().size();

        } catch (IOException e) {
            return -1;
        }
    }

    private Boolean getNotificationEnablementStatus(){
        try {
            final String bool = m_notifdConfigFactory.getNotificationStatus();
            switch (bool){
                case "on":
                    return true;
                case "off":
                    return false;
                default:
                    return null;
            }
        } catch (IOException e) {
            return null;
        }
    }
}
