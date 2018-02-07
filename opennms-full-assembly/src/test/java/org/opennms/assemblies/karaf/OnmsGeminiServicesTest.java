/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.assemblies.karaf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opennms.core.soa.ServiceRegistry;


/**
 * This is a sort of meta-test that show us which services are exposed
 * in an OpenNMS instance's {@link ServiceRegistry} that are not yet
 * examined inside {@link OnmsGeminiFeatureKarafIT}.
 * 
 * @author Seth
 */
public class OnmsGeminiServicesTest {

	/**
	 * You can generate this list by logging into an OpenNMS system's Karaf
	 * console and running:
	 * 
	 * {@code bundle:services org.opennms.core.soa | sort > services.txt}
	 * 
	 * Then you just need to run it through {@code uniq} and tweak a couple lines
	 * that have multiple values.
	 * 
	 * @return An array of all {@link ServiceRegistry} services that run on
	 * a full OpenNMS installation.
	 */
	public static final Class<?>[] allOpennmsServices() {
		return OnmsGeminiFeatureKarafIT.toArray(
			javax.sql.DataSource.class,
			org.opennms.core.rpc.api.RpcClientFactory.class,
			org.opennms.features.status.api.node.NodeStatusCalculator.class,
			org.opennms.features.telemetry.adapters.registry.api.TelemetryAdapterRegistry.class,
			org.opennms.netmgt.alarmd.api.Northbounder.class,
			org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao.class,
			org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao.class,
			org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionDao.class,
			org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao.class,
			org.opennms.netmgt.bsm.service.BusinessServiceManager.class,
			org.opennms.netmgt.bsm.service.BusinessServiceStateMachine.class,
			org.opennms.netmgt.collection.api.CollectionAgentFactory.class,
			org.opennms.netmgt.collection.api.LocationAwareCollectorClient.class,
			org.opennms.netmgt.collection.api.PersisterFactory.class,
			org.opennms.netmgt.collection.api.ServiceCollectorRegistry.class,
			org.opennms.netmgt.config.PollOutagesConfig.class,
			org.opennms.netmgt.config.PollerConfig.class,
			org.opennms.netmgt.config.SyslogdConfig.class,
			org.opennms.netmgt.config.TrapdConfig.class,
			org.opennms.netmgt.config.api.CollectdConfigFactory.class,
			org.opennms.netmgt.config.api.DataCollectionConfigDao.class,
			org.opennms.netmgt.config.api.DatabaseSchemaConfig.class,
			org.opennms.netmgt.config.api.DiscoveryConfigurationFactory.class,
			org.opennms.netmgt.config.api.EventConfDao.class,
			org.opennms.netmgt.config.api.EventdConfig.class,
			org.opennms.netmgt.config.api.GroupConfig.class,
			org.opennms.netmgt.config.api.OpennmsServerConfig.class,
			org.opennms.netmgt.config.api.ResourceTypesDao.class,
			org.opennms.netmgt.config.api.SnmpAgentConfigFactory.class,
			org.opennms.netmgt.config.api.UserConfig.class,
			org.opennms.netmgt.dao.api.AcknowledgmentDao.class,
			org.opennms.netmgt.dao.api.AlarmDao.class,
			org.opennms.netmgt.dao.api.AlarmRepository.class,
			org.opennms.netmgt.dao.api.ApplicationDao.class,
			org.opennms.netmgt.dao.api.AssetRecordDao.class,
			org.opennms.netmgt.dao.api.BridgeBridgeLinkDao.class,
			org.opennms.netmgt.dao.api.BridgeElementDao.class,
			org.opennms.netmgt.dao.api.BridgeMacLinkDao.class,
			org.opennms.netmgt.dao.api.BridgeStpLinkDao.class,
			org.opennms.netmgt.dao.api.BridgeTopologyDao.class,
			org.opennms.netmgt.dao.api.CategoryDao.class,
			org.opennms.netmgt.dao.api.CdpElementDao.class,
			org.opennms.netmgt.dao.api.CdpLinkDao.class,
			org.opennms.netmgt.dao.api.DatabaseReportConfigDao.class,
			org.opennms.netmgt.dao.api.DemandPollDao.class,
			org.opennms.netmgt.dao.api.DistPollerDao.class,
			org.opennms.netmgt.dao.api.EventDao.class,
			org.opennms.netmgt.dao.api.EventdServiceManager.class,
			org.opennms.netmgt.dao.api.FilterFavoriteDao.class,
			org.opennms.netmgt.dao.api.GenericPersistenceAccessor.class,
			org.opennms.netmgt.dao.api.GraphDao.class,
			org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao.class,
			org.opennms.netmgt.dao.api.HwEntityDao.class,
			org.opennms.netmgt.dao.api.IfLabel.class,
			org.opennms.netmgt.dao.api.InterfaceToNodeCache.class,
			org.opennms.netmgt.dao.api.IpInterfaceDao.class,
			org.opennms.netmgt.dao.api.IpNetToMediaDao.class,
			org.opennms.netmgt.dao.api.IsIsElementDao.class,
			org.opennms.netmgt.dao.api.IsIsLinkDao.class,
			org.opennms.netmgt.dao.api.JavaMailConfigurationDao.class,
			org.opennms.netmgt.dao.api.LldpElementDao.class,
			org.opennms.netmgt.dao.api.LldpLinkDao.class,
			org.opennms.netmgt.dao.api.LocationMonitorDao.class,
			org.opennms.netmgt.dao.api.MemoDao.class,
			org.opennms.netmgt.dao.api.MinionDao.class,
			org.opennms.netmgt.dao.api.MonitoredServiceDao.class,
			org.opennms.netmgt.dao.api.MonitoringLocationDao.class,
			org.opennms.netmgt.dao.api.MonitoringSystemDao.class,
			org.opennms.netmgt.dao.api.NodeDao.class,
			org.opennms.netmgt.dao.api.NodeLabel.class,
			org.opennms.netmgt.dao.api.NotificationDao.class,
			org.opennms.netmgt.dao.api.OnmsReportConfigDao.class,
			org.opennms.netmgt.dao.api.OspfElementDao.class,
			org.opennms.netmgt.dao.api.OspfLinkDao.class,
			org.opennms.netmgt.dao.api.OutageDao.class,
			org.opennms.netmgt.dao.api.PathOutageDao.class,
			org.opennms.netmgt.dao.api.PathOutageManager.class,
			org.opennms.netmgt.dao.api.ReportCatalogDao.class,
			org.opennms.netmgt.dao.api.RequisitionedCategoryAssociationDao.class,
			org.opennms.netmgt.dao.api.ResourceDao.class,
			org.opennms.netmgt.dao.api.ResourceReferenceDao.class,
			org.opennms.netmgt.dao.api.ResourceStorageDao.class,
			org.opennms.netmgt.dao.api.RrdDao.class,
			org.opennms.netmgt.dao.api.ScanReportDao.class,
			org.opennms.netmgt.dao.api.ServiceTypeDao.class,
			org.opennms.netmgt.dao.api.SessionFactoryWrapper.class,
			org.opennms.netmgt.dao.api.SnmpInterfaceDao.class,
			org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao.class,
			org.opennms.netmgt.dao.api.StatisticsReportDao.class,
			org.opennms.netmgt.dao.api.StatisticsReportDataDao.class,
			org.opennms.netmgt.dao.api.SurveillanceViewConfigDao.class,
			org.opennms.netmgt.dao.api.TopologyDao.class,
			org.opennms.netmgt.dao.api.UserNotificationDao.class,
			org.opennms.netmgt.dao.stats.AlarmStatisticsService.class,
			org.opennms.netmgt.discovery.DiscoveryTaskExecutor.class,
			org.opennms.netmgt.events.api.EventForwarder.class,
			org.opennms.netmgt.events.api.EventIpcBroadcaster.class,
			org.opennms.netmgt.events.api.EventIpcManager.class,
			org.opennms.netmgt.events.api.EventProxy.class,
			org.opennms.netmgt.events.api.EventSubscriptionService.class,
			org.opennms.netmgt.filter.api.FilterDao.class,
			org.opennms.netmgt.icmp.PingerFactory.class,
			org.opennms.netmgt.icmp.Pinger.class,
			org.opennms.netmgt.icmp.proxy.LocationAwarePingClient.class,
			org.opennms.netmgt.measurements.api.MeasurementFetchStrategy.class,
			org.opennms.netmgt.measurements.api.MeasurementsService.class,
			org.opennms.netmgt.model.FilterManager.class,
			org.opennms.netmgt.model.ServiceDaemon.class,
			org.opennms.netmgt.poller.LocationAwarePollerClient.class,
			org.opennms.netmgt.poller.ServiceMonitorRegistry.class,
			org.opennms.netmgt.provision.IpInterfacePolicy.class,
			org.opennms.netmgt.provision.OnmsPolicy.class,
			org.opennms.netmgt.provision.LocationAwareDetectorClient.class,
			org.opennms.netmgt.provision.LocationAwareDnsLookupClient.class,
			org.opennms.netmgt.provision.NodePolicy.class,
			org.opennms.netmgt.provision.ServiceDetector.class,
			org.opennms.netmgt.provision.SnmpInterfacePolicy.class,
			org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry.class,
			org.opennms.netmgt.provision.persist.LocationAwareRequisitionClient.class,
			org.opennms.netmgt.provision.persist.RequisitionProviderRegistry.class,
			org.opennms.netmgt.provision.persist.RequisitionProvider.class,
			org.opennms.netmgt.rrd.RrdStrategy.class,
			org.opennms.netmgt.snmp.SnmpStrategy.class,
			org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient.class,
			org.opennms.netmgt.ticketd.TicketerServiceLayer.class,
			org.opennms.netmgt.topology.persistence.api.LayoutDao.class,
			org.opennms.systemreport.SystemReportFormatter.class,
			org.opennms.systemreport.SystemReportPlugin.class,
			org.opennms.web.api.OnmsHeaderProvider.class,
			org.opennms.web.springframework.security.SpringSecurityUserDao.class,
			org.springframework.transaction.support.TransactionOperations.class
		);
	}

	@Test
	public void showUntestedServices() {
		List<Class<?>> allServices = new ArrayList<>(Arrays.asList(allOpennmsServices()));
		Arrays.stream(OnmsGeminiFeatureKarafIT.merge(
			OnmsGeminiFeatureKarafIT.opennmsCollectionApiServices(),
			OnmsGeminiFeatureKarafIT.opennmsConfigServices(),
			OnmsGeminiFeatureKarafIT.opennmsCoreDaemonServices(),
			OnmsGeminiFeatureKarafIT.opennmsCoreIpcRpcApiServices(),
			OnmsGeminiFeatureKarafIT.opennmsCoreIpcRpcServices(),
			OnmsGeminiFeatureKarafIT.opennmsCoreIpcSinkServices(),
			OnmsGeminiFeatureKarafIT.opennmsCoreServices(),
			OnmsGeminiFeatureKarafIT.opennmsDaoServices(),
			OnmsGeminiFeatureKarafIT.opennmsPollerApiServices(),
			OnmsGeminiFeatureKarafIT.opennmsSnmpServices(),
			OnmsGeminiFeatureKarafIT.opennmsTelemetryAdapterServices(),
			OnmsGeminiFeatureKarafIT.opennmsTelemetryDaemonServices()
		)).forEach(allServices::remove);
		System.out.printf("Services that are not exported from tests in %s:\n", getClass().getName());
		allServices.stream().map(Class::getName).forEach(System.out::println);
	}
}
