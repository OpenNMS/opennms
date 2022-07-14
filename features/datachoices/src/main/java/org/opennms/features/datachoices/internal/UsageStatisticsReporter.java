/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.karaf.features.FeaturesService;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.features.datachoices.internal.StateManager.StateChangeHandler;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class UsageStatisticsReporter implements StateChangeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UsageStatisticsReporter.class);

    public static final String USAGE_REPORT = "usage-report";
    private static final String JMX_OBJ_OS = "java.lang:type=OperatingSystem";
    private static final String JMX_ATTR_FREE_PHYSICAL_MEMORY_SIZE = "FreePhysicalMemorySize";
    private static final String JMX_ATTR_TOTAL_PHYSICAL_MEMORY_SIZE = "TotalPhysicalMemorySize";
    private static final String JMX_ATTR_AVAILABLE_PROCESSORS = "AvailableProcessors";
    private static final MBeanServer M_BEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

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

    private FeaturesService m_featuresService;

    private ProvisiondConfigurationDao m_provisiondConfigurationDao;

    private ServiceConfigFactory m_serviceConfigurationFactory;

    private DestinationPathFactory m_destinationPathFactory;

    private NotifdConfigFactory m_notifdConfigFactory;

    private GroupFactory m_groupFactory;

    private ForeignSourceRepository m_deployedForeignSourceRepository;

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
        usageStatisticsReport.setAlarms(m_alarmDao.countAll());
        usageStatisticsReport.setSituations(m_alarmDao.getNumSituations());
        usageStatisticsReport.setMonitoringLocations(m_monitoringLocationDao.countAll());
        usageStatisticsReport.setMinions(m_monitoringSystemDao.getNumMonitoringSystems(OnmsMonitoringSystem.TYPE_MINION));
        // Node statistics
        usageStatisticsReport.setNodesBySysOid(m_nodeDao.getNumberOfNodesBySysOid());
        // Karaf features
        String installedFeatures;
        try {
            installedFeatures = Arrays.stream(m_featuresService.listInstalledFeatures())
                    .map(f -> f.getName() + "/" + f.getVersion())
                    .sorted()
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            installedFeatures = "ERROR: Failed to enumerate the installed features: " + e.getMessage();
        }
        usageStatisticsReport.setInstalledFeatures(installedFeatures);
        setJmxAttributes(usageStatisticsReport);
        gatherProvisiondData(usageStatisticsReport);
        usageStatisticsReport.setServices(m_serviceConfigurationFactory.getServiceNameMap());

        usageStatisticsReport.setDestinationPathCount(getDestinationPathCount());
        usageStatisticsReport.setNotificationEnablementStatus(getNotificationEnablementStatus());
        usageStatisticsReport.setOnCallRoleCount(m_groupFactory.getRoles().size());
        usageStatisticsReport.setRequisitionCount(getDeployedRequisitionCount());
        usageStatisticsReport.setRequisitionWithChangedFSCount(getDeployedRequisitionWithModifiedFSCount());
        usageStatisticsReport.setBusinessEdges(m_businessServiceEdgeDao.countAll());

        return usageStatisticsReport;
    }
    private void setJmxAttributes(UsageStatisticsReportDTO usageStatisticsReport) {
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

    public void setFeaturesService(FeaturesService featuresService) {
        m_featuresService = featuresService;
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
