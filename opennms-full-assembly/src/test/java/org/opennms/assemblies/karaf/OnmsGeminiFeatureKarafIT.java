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

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.maven;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.kafka.KafkaSinkConstants;
import org.opennms.core.soa.ServiceRegistry;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;


/**
 * This test uses gemini-blueprint to bootstrap our bundles and
 * verifies that they expose their services via the {@link ServiceRegistry}.
 * 
 * @author Seth
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OnmsGeminiFeatureKarafIT extends KarafGeminiTestCase {

	public static Class<?>[] opennmsCoreServices() {
		return toArray(
			org.opennms.core.soa.ServiceRegistry.class
		);
	}

	public static Class<?>[] opennmsSnmpServices() {
		return toArray(
			org.opennms.netmgt.snmp.SnmpStrategy.class
		);
	}

	public static Class<?>[] opennmsCollectionApiServices() {
		return toArray(
			org.opennms.netmgt.collection.api.ServiceCollectorRegistry.class
		);
	}

	public static Class<?>[] opennmsPollerApiServices() {
		return toArray(
			org.opennms.netmgt.poller.ServiceMonitorRegistry.class
		);
	}

	public static Class<?>[] opennmsConfigServices() {
		return toArray(
			org.opennms.netmgt.config.api.OpennmsServerConfig.class,
			org.opennms.netmgt.config.api.EventConfDao.class,
			org.opennms.netmgt.config.api.GroupConfig.class,
			org.opennms.netmgt.config.api.UserConfig.class,
			org.opennms.netmgt.config.api.DiscoveryConfigurationFactory.class,
			org.opennms.netmgt.config.api.DatabaseSchemaConfig.class,
			org.opennms.netmgt.config.api.CollectdConfigFactory.class,
			org.opennms.netmgt.config.api.DataCollectionConfigDao.class,
			org.opennms.netmgt.config.api.SnmpAgentConfigFactory.class,
			org.opennms.netmgt.config.api.EventdConfig.class,
			org.opennms.netmgt.config.api.ResourceTypesDao.class,
			org.opennms.netmgt.config.PollOutagesConfig.class,
			org.opennms.netmgt.config.PollerConfig.class,
			org.opennms.netmgt.config.SyslogdConfig.class,
			org.opennms.netmgt.config.TrapdConfig.class
		);
	}

	public static Class<?>[] opennmsDaoServices() {
		return toArray(
			org.opennms.netmgt.dao.api.SessionFactoryWrapper.class,
			// Doesn't seem to work, possibly because of classloader issues?
			//org.springframework.transaction.support.TransactionOperations.class,
			org.opennms.netmgt.model.FilterManager.class,
			org.opennms.netmgt.dao.api.HwEntityDao.class,
			org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao.class,
			org.opennms.netmgt.dao.api.LldpLinkDao.class,
			org.opennms.netmgt.dao.api.LldpElementDao.class,
			org.opennms.netmgt.dao.api.OspfLinkDao.class,
			org.opennms.netmgt.dao.api.OspfElementDao.class,
			org.opennms.netmgt.dao.api.IsIsLinkDao.class,
			org.opennms.netmgt.dao.api.IsIsElementDao.class,
			org.opennms.netmgt.dao.api.IpNetToMediaDao.class,
			org.opennms.netmgt.dao.api.BridgeMacLinkDao.class,
			org.opennms.netmgt.dao.api.BridgeBridgeLinkDao.class,
			org.opennms.netmgt.dao.api.BridgeTopologyDao.class,
			org.opennms.netmgt.dao.api.BridgeStpLinkDao.class,
			org.opennms.netmgt.dao.api.BridgeElementDao.class,
			org.opennms.netmgt.dao.api.CdpLinkDao.class,
			org.opennms.netmgt.dao.api.CdpElementDao.class,
			org.opennms.netmgt.dao.api.AcknowledgmentDao.class,
			org.opennms.netmgt.dao.api.AlarmDao.class,
			org.opennms.netmgt.dao.api.AlarmRepository.class,
			org.opennms.netmgt.dao.api.MemoDao.class,
			org.opennms.netmgt.dao.api.AssetRecordDao.class,
			org.opennms.netmgt.dao.api.CategoryDao.class,
			org.opennms.netmgt.dao.api.DistPollerDao.class,
			org.opennms.netmgt.dao.api.DemandPollDao.class,
			org.opennms.netmgt.dao.api.EventDao.class,
			org.opennms.netmgt.dao.api.EventdServiceManager.class,
			org.opennms.netmgt.dao.api.IfLabel.class,
			org.opennms.netmgt.dao.api.InterfaceToNodeCache.class,
			org.opennms.netmgt.dao.api.IpInterfaceDao.class,
			org.opennms.netmgt.dao.api.MonitoredServiceDao.class,
			org.opennms.netmgt.dao.api.MinionDao.class,
			org.opennms.netmgt.dao.api.MonitoringSystemDao.class,
			org.opennms.netmgt.dao.api.NodeDao.class,
			org.opennms.netmgt.dao.api.NodeLabel.class,
			org.opennms.netmgt.dao.api.RequisitionedCategoryAssociationDao.class,
			org.opennms.netmgt.dao.api.ReportCatalogDao.class,
			org.opennms.netmgt.dao.api.NotificationDao.class,
			org.opennms.netmgt.dao.api.OutageDao.class,
			org.opennms.netmgt.dao.api.PathOutageDao.class,
			org.opennms.netmgt.dao.api.PathOutageManager.class,
			org.opennms.netmgt.dao.api.ServiceTypeDao.class,
			org.opennms.netmgt.dao.api.SnmpInterfaceDao.class,
			org.opennms.netmgt.dao.api.UserNotificationDao.class,
			org.opennms.netmgt.dao.api.ApplicationDao.class,
			org.opennms.netmgt.dao.api.StatisticsReportDao.class,
			org.opennms.netmgt.dao.api.StatisticsReportDataDao.class,
			org.opennms.netmgt.dao.api.ResourceReferenceDao.class,
			org.opennms.netmgt.dao.api.MonitoringLocationDao.class,
			org.opennms.netmgt.dao.api.LocationMonitorDao.class,
			org.opennms.netmgt.dao.api.ScanReportDao.class,
			org.opennms.netmgt.dao.api.SurveillanceViewConfigDao.class,
			org.opennms.netmgt.dao.api.DatabaseReportConfigDao.class,
			org.opennms.netmgt.dao.api.OnmsReportConfigDao.class,
			org.opennms.netmgt.filter.api.FilterDao.class,
			org.opennms.netmgt.dao.api.FilterFavoriteDao.class,
			org.opennms.netmgt.dao.api.GenericPersistenceAccessor.class,
			org.opennms.netmgt.dao.api.TopologyDao.class,
			org.opennms.netmgt.rrd.RrdStrategy.class,
			org.opennms.netmgt.dao.api.ResourceStorageDao.class,
			org.opennms.netmgt.collection.api.PersisterFactory.class,
			org.opennms.netmgt.dao.api.ResourceDao.class,
			org.opennms.netmgt.dao.api.RrdDao.class,
			org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao.class,
			org.opennms.netmgt.dao.api.JavaMailConfigurationDao.class
		);
	}

	// TODO: Check that the component is actually the 'opennms.broker' Component
	public static Class<?>[] opennmsCoreDaemonServices() {
		return toArray(
			org.apache.camel.Component.class
		);
	}

	public static Class<?>[] opennmsCoreIpcRpcApiServices() {
		return toArray(
			org.opennms.core.rpc.api.RpcModule.class
		);
	}

	public static Class<?>[] opennmsCoreIpcSinkServices() {
		return toArray(
			org.opennms.core.ipc.sink.api.MessageConsumerManager.class,
			org.opennms.core.ipc.sink.api.MessageDispatcherFactory.class
		);
	}

	public static Class<?>[] opennmsCoreIpcRpcServices() {
		return toArray(
			org.opennms.core.rpc.api.RpcClientFactory.class
		);
	}

	public static Class<?>[] opennmsTelemetryAdapterServices() {
		return toArray(
			org.opennms.features.telemetry.adapters.factory.api.AdapterFactory.class
		);
	}

	public static Class<?>[] opennmsTelemetryDaemonServices() {
		return toArray(
			org.opennms.features.telemetry.adapters.registry.api.TelemetryAdapterRegistry.class
		);
	}

	@Before
	public void setUp() {
		final String version = getOpenNMSVersion();
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("standard").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("spring-legacy").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("features").getURL());
	}

	@Test
	public void testInstallFeatureOpennmsConfig() {
		installFeature("opennms-config");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsDaemon() {
		installFeature("opennms-core-daemon");

		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreDaemonServices(),
			opennmsCoreServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsCoreIpcSinkCamelGemini() {
		installFeature("opennms-dao"); // Provides DistPollerDao service
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
		installFeature("opennms-core-ipc-sink-camel-gemini");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreIpcSinkServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsCoreIpcSinkKafkaGemini() {
		// Provide a dummy value for the Kafka service
		System.setProperty(KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX + "bootstrap.servers", "127.0.0.1:9092");
		installFeature("opennms-dao"); // Provides DistPollerDao service
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
		installFeature("opennms-core-ipc-sink-kafka-gemini");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreIpcSinkServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsCoreIpcSinkAwsSqsGemini() {
		installFeature("opennms-dao"); // Provides DistPollerDao service
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
		installFeature("opennms-core-ipc-sink-aws-sqs-gemini");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreIpcSinkServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsCoreIpcRpcApi() {
		installFeature("opennms-core-ipc-rpc-api");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCoreIpcRpcApiServices(),
			opennmsCoreServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsCoreIpcRpcJmsGemini() {
		installFeature("opennms-dao"); // Provides DistPollerDao service
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
		installFeature("opennms-core-ipc-rpc-jms-gemini");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreIpcRpcApiServices(),
			opennmsCoreIpcRpcServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsCoreIpcRpcAwsSqsGemini() {
		installFeature("opennms-dao"); // Provides DistPollerDao service
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
		installFeature("opennms-core-ipc-rpc-aws-sqs-gemini");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreIpcRpcApiServices(),
			opennmsCoreIpcRpcServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsDao() {
		installFeature("opennms-dao");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsTelemetryDaemon() {
		installFeature("opennms-telemetry-daemon");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices(),
			opennmsTelemetryDaemonServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsTelemetryJti() {
		installFeature("opennms-telemetry-jti");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices(),
			opennmsTelemetryAdapterServices()
		));
	}

	@Test
	public void testInstallFeatureOpennmsTelemetryNxos() {
		installFeature("opennms-telemetry-nxos");
		System.out.println(executeCommand("feature:list -i"));
		assertOsgiServices(merge(
			opennmsCollectionApiServices(),
			opennmsConfigServices(),
			opennmsCoreServices(),
			opennmsDaoServices(),
			opennmsPollerApiServices(),
			opennmsSnmpServices(),
			opennmsTelemetryAdapterServices()
		));
	}

	private void assertOsgiServices(Class<?>[] services) {
		try {
			for (Class<?> service : services) {
				assertNotNull("Could not find service: " + service.getName(), getOsgiService(service, 30000));
			}
		} finally {
			System.out.println(executeCommand("bundle:services"));
		}
	}

	public static Class<?>[] merge(Class<?>[]... arrays) {
		return Stream.of(arrays).flatMap(Stream::of).toArray(Class<?>[]::new);
	}

	public static Class<?>[] toArray(Class<?>... array) {
		return array;
	}
}
