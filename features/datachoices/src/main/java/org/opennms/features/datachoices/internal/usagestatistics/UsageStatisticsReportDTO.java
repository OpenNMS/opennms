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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import com.google.common.base.Throwables;

@XmlRootElement
public class UsageStatisticsReportDTO {

    private String m_systemId;

    private String m_osName;
    private String m_osVersion;
    private String m_osArch;

    private String m_version;
    private String m_packageName;

    private int m_nodes;
    private int m_ipInterfaces;
    private int m_snmpInterfaces;
    private long m_snmpInterfacesWithFlows;
    private int m_monitoredServices;
    private int m_events;
    private int m_eventsPastHours;
    private int m_alarms;
    private int m_alarmsPastHours;
    private String m_loginsPast60Days;
    private long m_situations;
    private int m_destinationPathCount;
    private Boolean m_notificationEnablementStatus;
    private int m_onCallRoleCount;
    private Map<String, Long> m_nodesBySysOid = Collections.emptyMap();
    private long m_monitoringLocations;
    private long m_minions;
    private String m_installedFeatures;
    private String m_installedOIAPlugins;
    private String m_cpuUtilization;
    private String m_memoryUtilization;
    private Integer m_availableProcessors;
    private Long m_freePhysicalMemorySize;
    private Long m_totalPhysicalMemorySize;
    private long m_provisiondImportThreadPoolSize;
    private long m_provisiondScanThreadPoolSize;
    private long m_provisiondRescanThreadPoolSize;
    private long m_provisiondWriteThreadPoolSize;
    private long m_requisitionCount;
    private long m_requisitionWithChangedFSCount;
    private Map<String, Long> m_provisiondRequisitionSchemeCount;
    private Map<String, Boolean> m_services;
    private String m_databaseProductName;
    private String m_databaseProductVersion;
    private String m_sinkStrategy;
    private String m_rpcStrategy;
    private String m_tssStrategies;
    private int m_businessEdgeCount;
    private long m_pollsCompleted;
    private long m_eventLogsProcessed;
    private long m_coreFlowsPersisted;
    private long m_flowsPerSecond;
    private long m_coreNewtsSamplesInserted;
    private long m_coreQueuedUpdatesCompleted;
    private int m_users;
    private int m_groups;
    private long m_dcbSucceed;
    private long m_dcbFailed;
    private long m_dcbWebUiEntries;
    private Map<String, Long> m_nodesWithDeviceConfigBySysOid = Collections.emptyMap();
    private int outages;
    private int notifications;
    private long m_onmsStartupTimeSeconds;

    private Map<String, Long> m_applianceCounts = Collections.emptyMap();

    private boolean m_inContainer;
    public int getNotifications() {return notifications;}

    public void setNotifications(int notifications) {this.notifications = notifications;}

    public int getOutages() {return outages;}

    public void setOutages(int outages) {this.outages = outages;}

    private int applications;

    public int getApplications(){return applications;}

    public void setApplications(int applications){this.applications = applications;}

    public void setSystemId(String systemId) {
        m_systemId = systemId;
    }

    public String getSystemId() {
        return m_systemId;
    }

    public void setOsName(String osName) {
        m_osName = osName;
    }

    public String getOsName() {
        return m_osName;
    }

    public void setOsVersion(String osVersion){
        m_osVersion = osVersion;
    }

    public String getOsVersion() {
        return m_osVersion;
    }

    public void setOsArch(String osArch) {
        m_osArch = osArch;
    }

    public String getOsArch() {
        return m_osArch;
    }

    public void setVersion(String version) {
        m_version = version;
    }

    public String getVersion() {
        return m_version;
    }

    public void setPackageName(String packageName) {
        m_packageName = packageName;
    }

    public String getPackageName() {
        return m_packageName;
    }

    public void setNodes(int nodes) {
        m_nodes = nodes;
    }

    public int getNodes() {
        return m_nodes;
    }

    public void setIpInterfaces(int ipInterfaces) {
        m_ipInterfaces = ipInterfaces;
    }

    public int getIpInterfaces() {
        return m_ipInterfaces;
    }

    public void setSnmpInterfaces(int snmpInterfaces) {
        m_snmpInterfaces = snmpInterfaces;
    }

    public int getSnmpInterfaces() {
        return m_snmpInterfaces;
    }

    public void setSnmpInterfacesWithFlows(long snmpInterfacesWithFlows) {
        m_snmpInterfacesWithFlows = snmpInterfacesWithFlows;
    }

    public long getSnmpInterfacesWithFlows() {
        return m_snmpInterfacesWithFlows;
    }

    public void setMonitoredServices(int monitoredServices) {
        m_monitoredServices = monitoredServices;
    }

    public int getMonitoredServices() {
        return m_monitoredServices;
    }

    public void setEvents(int events) {
        m_events = events;
    }

    public int getEvents() {
        return m_events;
    }

    public void setEventsPastHours(int eventsPastHours) {
        m_eventsPastHours = eventsPastHours;
    }

    public int getEventsPastHours() { return m_eventsPastHours; }

    public void setAlarms(int alarms) {
        m_alarms = alarms;
    }

    public int getAlarms() {
        return m_alarms;
    }

    public void setAlarmsPastHours(int alarmsPastHours) {
        m_alarmsPastHours = alarmsPastHours;
    }

    public int getAlarmsPastHours() { return m_alarmsPastHours; }

    public void setLoginsPast60Days(String loginsPast60Days) {
        m_loginsPast60Days = loginsPast60Days;
    }

    public String getLoginsPast60Days() {return m_loginsPast60Days;}

    public long getSituations() {
        return m_situations;
    }

    public void setSituations(long situations) {
        m_situations = situations;
    }

    public void setNodesBySysOid(Map<String, Long> nodesBySysOid) {
        m_nodesBySysOid = nodesBySysOid;
    }

    public Map<String, Long> getNodesBySysOid() {
        return m_nodesBySysOid;
    }

    public long getMonitoringLocations() {
        return m_monitoringLocations;
    }

    public void setMonitoringLocations(long monitoringLocations) {
        m_monitoringLocations = monitoringLocations;
    }

    public long getMinions() {
        return m_minions;
    }

    public void setMinions(long minions) {
        this.m_minions = minions;
    }

    public String getInstalledFeatures() {
        return m_installedFeatures;
    }

    public void setInstalledFeatures(String installedFeatures) {
        m_installedFeatures = installedFeatures;
    }

    public String getInstalledOIAPlugins() {
        return m_installedOIAPlugins;
    }

    public void setCpuUtilization(String cpuUtilization) {
        this.m_cpuUtilization = cpuUtilization;
    }

    public String getCpuUtilization() {
        return m_cpuUtilization;
    }

    public void setMemoryUtilization(String memoryUtilization) {
        this.m_memoryUtilization = memoryUtilization;
    }

    public String getMemoryUtilization() {
        return m_memoryUtilization;
    }

    public void setInstalledOIAPlugins(String plugins) {
        this.m_installedOIAPlugins = plugins;
    }

    public void setAvailableProcessors(Integer availableProcessors) {
        this.m_availableProcessors = availableProcessors;
    }

    public Integer getAvailableProcessors() {
        return this.m_availableProcessors;
    }

    public Long getFreePhysicalMemorySize() {
        return this.m_freePhysicalMemorySize;
    }

    public void setFreePhysicalMemorySize(Long freePhysicalMemorySize) {
        this.m_freePhysicalMemorySize = freePhysicalMemorySize;
    }

    public Long getTotalPhysicalMemorySize() {
        return this.m_totalPhysicalMemorySize;
    }

    public void setTotalPhysicalMemorySize(long totalPhysicalMemorySize) {
        this.m_totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    public long getProvisiondImportThreadPoolSize() {
        return m_provisiondImportThreadPoolSize;
    }

    public void setProvisiondImportThreadPoolSize(long provisiondImportThreadPoolSize) {
        m_provisiondImportThreadPoolSize = provisiondImportThreadPoolSize;
    }

    public long getProvisiondScanThreadPoolSize() {
        return m_provisiondScanThreadPoolSize;
    }

    public void setProvisiondScanThreadPoolSize(long provisiondScanThreadPoolSize) {
        m_provisiondScanThreadPoolSize = provisiondScanThreadPoolSize;
    }

    public long getProvisiondRescanThreadPoolSize() {
        return m_provisiondRescanThreadPoolSize;
    }

    public void setProvisiondRescanThreadPoolSize(long provisiondRescanThreadPoolSize) {
        m_provisiondRescanThreadPoolSize = provisiondRescanThreadPoolSize;
    }

    public long getProvisiondWriteThreadPoolSize() {
        return m_provisiondWriteThreadPoolSize;
    }

    public void setProvisiondWriteThreadPoolSize(long provisiondWriteThreadPoolSize) {
        m_provisiondWriteThreadPoolSize = provisiondWriteThreadPoolSize;
    }

    public Map<String, Long> getProvisiondRequisitionSchemeCount() {
        return m_provisiondRequisitionSchemeCount;
    }

    public void setProvisiondRequisitionSchemeCount(Map<String, Long> provisiondRequisitionSchemeCount) {
        m_provisiondRequisitionSchemeCount = provisiondRequisitionSchemeCount;
    }

    public Map<String, Boolean> getServices() {
        return m_services;
    }

    public void setServices(Map<String, Boolean> services) {
        m_services = services;
    }

    public int getDestinationPathCount() {
        return m_destinationPathCount;
    }

    public void setDestinationPathCount(int m_destinationPathCount) {
        this.m_destinationPathCount = m_destinationPathCount;
    }

    public Boolean isNotificationEnablementStatus() {
        return m_notificationEnablementStatus;
    }

    public void setNotificationEnablementStatus(Boolean m_notificationEnablementStatus) {
        this.m_notificationEnablementStatus = m_notificationEnablementStatus;
    }

    public int getOnCallRoleCount() {
        return m_onCallRoleCount;
    }

    public void setOnCallRoleCount(int m_onCallRoleCount) {
        this.m_onCallRoleCount = m_onCallRoleCount;
    }

    public void setRequisitionCount(long count) {
        this.m_requisitionCount = count;
    }

    public long getRequisitionCount() {
        return m_requisitionCount;
    }

    public void setRequisitionWithChangedFSCount(long count) {
        this.m_requisitionWithChangedFSCount = count;
    }

    public long getRequisitionWithChangedFSCount() {
        return m_requisitionWithChangedFSCount;
    }
    
    public void setBusinessEdgeCount(int edges) {
        this.m_businessEdgeCount = edges;
    }

    public int getBusinessEdgeCount() {
        return m_businessEdgeCount;
    }

    public String getDatabaseProductName() {
        return m_databaseProductName;
    }

    public void setDatabaseProductName(String databaseProductName) {
        this.m_databaseProductName = databaseProductName;
    }

    public String getDatabaseProductVersion() {
        return m_databaseProductVersion;
    }

    public void setDatabaseProductVersion(String databaseProductVersion) {
        this.m_databaseProductVersion = databaseProductVersion;
    }

    public long getPollsCompleted() {
        return m_pollsCompleted;
    }

    public void setPollsCompleted(long pollsCompleted) {
        this.m_pollsCompleted = pollsCompleted;
    }
    public long getEventLogsProcessed() {
        return m_eventLogsProcessed;
    }

    public void setEventLogsProcessed(long eventLogsProcessed) {
        this.m_eventLogsProcessed = eventLogsProcessed;
    }

    public long getCoreFlowsPersisted() {
        return m_coreFlowsPersisted;
    }

    public void setCoreFlowsPersisted(long coreFlowsPersisted) {
        this.m_coreFlowsPersisted = coreFlowsPersisted;
    }

    public long getFlowsPerSecond() {
        return m_flowsPerSecond;
    }

    public void setFlowsPerSecond(long flowsPerSecond) {
        this.m_flowsPerSecond = flowsPerSecond;
    }

    public long getCoreNewtsSamplesInserted() {
        return m_coreNewtsSamplesInserted;
    }

    public void setCoreNewtsSamplesInserted(long coreNewtsSamplesInserted) {
        this.m_coreNewtsSamplesInserted = coreNewtsSamplesInserted;
    }

    public long getCoreQueuedUpdatesCompleted() {
        return m_coreQueuedUpdatesCompleted;
    }

    public void setCoreQueuedUpdatesCompleted(long coreQueuedUpdatesCompleted) {
        this.m_coreQueuedUpdatesCompleted = coreQueuedUpdatesCompleted;
    }

    public void setSinkStrategy(String sinkStrategy) {
        this.m_sinkStrategy = sinkStrategy;
    }

    public String getSinkStrategy() {
        return m_sinkStrategy;
    }

    public void setRpcStrategy(String rpcStrategy) {
        this.m_rpcStrategy = rpcStrategy;
    }

    public String getRpcStrategy() {
        return m_rpcStrategy;
    }

    public void setTssStrategies(String tssStrategies) {
        this.m_tssStrategies = tssStrategies;
    }

    public String getTssStrategies() {
        return m_tssStrategies;
    }

    public int getGroups() {
        return this.m_groups;
    }

    public void setGroups(int m_groups) {
        this.m_groups = m_groups;
    }

    public int getUsers() {
        return this.m_users;
    }

    public void setUsers(int m_users) {
        this.m_users = m_users;
    }

    public long getDcbSucceed() {
        return m_dcbSucceed;
    }

    public void setDcbSucceed(long m_dcbSucceed) {
        this.m_dcbSucceed = m_dcbSucceed;
    }

    public long getDcbFailed() {
        return m_dcbFailed;
    }

    public void setDcbFailed(long m_dcbFailed) {
        this.m_dcbFailed = m_dcbFailed;
    }

    public long getDcbWebUiEntries() {
        return m_dcbWebUiEntries;
    }

    public void setDcbWebUiEntries(long m_dcbWebUiEntries) {
        this.m_dcbWebUiEntries = m_dcbWebUiEntries;
    }

    public Map<String, Long> getNodesWithDeviceConfigBySysOid() {
        return m_nodesWithDeviceConfigBySysOid;
    }

    public void setNodesWithDeviceConfigBySysOid(Map<String, Long> nodesWithConfigBySysOid) {
        this.m_nodesWithDeviceConfigBySysOid = nodesWithConfigBySysOid;
    }

    public long getOnmsStartupTimeSeconds() { return m_onmsStartupTimeSeconds; }

    public void setOnmsStartupTimeSeconds(long onmsStartupTimeSeconds) { this.m_onmsStartupTimeSeconds = onmsStartupTimeSeconds; }

    public Map<String, Long> getApplianceCounts() {
        return m_applianceCounts;
    }

    public void setInContainer(final boolean inContainer) {
        m_inContainer = inContainer;
    }

    public boolean isInContainer() {
        return m_inContainer;
    }

    public void setApplianceCounts(Map<String, Long> applianceCounts) {
        m_applianceCounts = applianceCounts;
    }

    public String toJson() {
        return toJson(false);
    }

    public String toJson(boolean prettyPrint) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(Feature.SORT_PROPERTIES_ALPHABETICALLY);
        if (prettyPrint) {
            mapper.enable(Feature.INDENT_OUTPUT);
        }
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
