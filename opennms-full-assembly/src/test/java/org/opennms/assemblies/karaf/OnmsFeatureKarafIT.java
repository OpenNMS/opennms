/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import static org.ops4j.pax.exam.CoreOptions.maven;

import java.util.EnumSet;

import org.apache.karaf.features.FeaturesService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.karaf.KarafTestCase;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * <p>This test checks that the features from:</p>
 * <code>
 * mvn:org.opennms.karaf/opennms/${currentVersion}/xml/features
 * <code>
 * <p>load correctly in Karaf.</p>
 * 
 * @author Seth
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OnmsFeatureKarafIT extends KarafTestCase {

	@Before
	public void setUp() {
		final String version = getOpenNMSVersion();
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("standard").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("spring-legacy").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("features").getURL());
	}

	@Test
	public void testInstallFeatureAtomikos() {
		installFeature("atomikos");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureBatik() {
		installFeature("batik");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureBsf() {
		installFeature("bsf");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureC3P0() {
		installFeature("c3p0");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsBeanutils() {
		installFeature("commons-beanutils");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsCli() {
		installFeature("commons-cli");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsCodec() {
		installFeature("commons-codec");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsCollections() {
		installFeature("commons-collections");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsConfiguration() {
		installFeature("commons-configuration");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsDigester() {
		installFeature("commons-digester");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsExec() {
		installFeature("commons-exec");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsIo() {
		installFeature("commons-io");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsJexl() {
		installFeature("commons-jexl");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsJxpath() {
		installFeature("commons-jxpath");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsLang() {
		installFeature("commons-lang");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsLang3() {
		installFeature("commons-lang3");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsNet() {
		installFeature("commons-net");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureDnsjava() {
		installFeature("dnsjava");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureDropwizardMetrics() {
		installFeature("dropwizard-metrics");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureEifAdapter() {
		installFeature("eif-adapter");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureFop() {
		installFeature("fop");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureGeminiBlueprint() {
		installFeature("gemini-blueprint", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureGuava17() {
		installFeature("guava17");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureGuava() {
		installFeature("guava");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureHibernate36() {
		installFeature("hibernate36");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureHibernateValidator41() {
		installFeature("hibernate-validator41");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureHikariCp() {
		installFeature("hikari-cp");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJavaNativeAccess() {
		installFeature("java-native-access");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJavaxMail() {
		installFeature("javax.mail");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJavaxServlet() {
		installFeature("javax.servlet");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJavaxValidation() {
		installFeature("javax.validation");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJaxb() {
		installFeature("jaxb");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJcifs() {
		installFeature("jcifs");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJfreechart() {
		installFeature("jfreechart");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJicmp() {
		installFeature("jicmp");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJicmp6() {
		installFeature("jicmp6");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJldap() {
		installFeature("jldap");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJodaTime() {
		installFeature("joda-time");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJolokiaClient() {
		installFeature("jolokia-client");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJrobin() {
		installFeature("jrobin");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJsonLib() {
		installFeature("json-lib");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureJsonSimple() {
		installFeature("json-simple");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureLmaxDisruptor() {
		installFeature("lmax-disruptor");
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	@Ignore("OSGi dependency problems: org.opennms.netmgt.alarmd.api")
	public void testInstallFeatureAmqpAlarmNorthbounder() {
		installFeature("opennms-amqp-alarm-northbounder");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureAmqpEventForwarder() {
		installFeature("opennms-amqp-event-forwarder");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureAmqpEventReceiver() {
		installFeature("opennms-amqp-event-receiver");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsActivemqPool() {
		installFeature("opennms-activemq-pool");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsAwsSqs() {
		installFeature("opennms-aws-sqs");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsBootstrap() {
		installFeature("opennms-bootstrap");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsBundleRefresher() {
		installFeature("opennms-bundle-refresher");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCollectionApi() {
		installFeature("opennms-collection-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCollectionCommands() {
		installFeature("opennms-collection-api"); // System classpath
		installFeature("opennms-collection-commands");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCollectionPersistenceRrd() {
		installFeature("opennms-collection-persistence-rrd");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsConfigApi() {
		installFeature("opennms-config-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsConfig() {
		installFeature("opennms-config");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsConfigJaxb() {
		installFeature("opennms-config-jaxb");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreCamel() {
		installFeature("opennms-core-camel");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreDaemon() {
		installFeature("opennms-core-daemon");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreDb() {
		installFeature("opennms-core-db");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCore() {
		installFeature("opennms-core");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreIpcSinkApi() {
		installFeature("opennms-core-ipc-sink-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreIpcSinkCamel() {
		installFeature("opennms-core-ipc-sink-camel");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreIpcSinkKafka() {
		installFeature("opennms-core-ipc-sink-kafka");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreIpcSinkAwsSqs() {
		installFeature("opennms-core-ipc-sink-aws-sqs");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreIpcRpcApi() {
		installFeature("opennms-core-ipc-rpc-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreIpcRpcJms() {
		installFeature("opennms-core-ipc-rpc-jms");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreIpcRpcAwsSqs() {
		installFeature("opennms-core-ipc-rpc-aws-sqs");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreIpcRpcCommands() {
		installFeature("opennms-core-ipc-rpc-commands");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreWeb() {
		installFeature("opennms-core-web");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsDaoApi() {
		installFeature("opennms-dao-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsDao() {
		installFeature("opennms-dao");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsEventsApi() {
		installFeature("opennms-events-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsEventsCommands() {
		installFeature("opennms-core"); // System classpath
		installFeature("opennms-events-api"); // System classpath
		installFeature("opennms-model"); // System classpath
		installFeature("opennms-events-commands");
		System.out.println(executeCommand("feature:list -i"));
	}
	
	@Test
	public void testInstallFeatureOpennmsIcmpApi() {
		installFeature("opennms-icmp-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpBest() {
		installFeature("opennms-icmp-best");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpJna() {
		installFeature("opennms-icmp-jna");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpJni() {
		installFeature("opennms-icmp-jni");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpJni6() {
		installFeature("opennms-icmp-jni6");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpCommands() {
		installFeature("opennms-icmp-commands");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsJavamail() {
		installFeature("opennms-javamail");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsKafka() {
		installFeature("opennms-kafka");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsModel() {
		installFeature("opennms-model");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsOsgiJsr223() {
		installFeature("opennms-osgi-jsr223");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsPollerApi() {
		installFeature("opennms-poller-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsPollerMonitorsCore() {
		installFeature("opennms-poller-monitors-core");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsPollerShell() {
		installFeature("opennms-config"); // System classpath
		installFeature("opennms-dao-api"); // System classpath
		installFeature("opennms-poller-api"); // System classpath
		installFeature("opennms-poller-shell");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsProvisioning() {
		installFeature("opennms-core"); // System classpath
		installFeature("opennms-model"); // System classpath
		installFeature("opennms-provisioning-api", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles)); // System classpath
		installFeature("opennms-provisioning", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsProvisioningDetectors() {
		installFeature("pax-http"); // Provides javax.servlet version 2.6
		installFeature("opennms-config"); // System classpath
		installFeature("opennms-provisioning-detectors", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsProvisioningShell() {
		installFeature("opennms-core"); // System classpath
		installFeature("opennms-model"); // System classpath
		installFeature("opennms-config"); // System classpath
		installFeature("opennms-provisioning", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles)); // System classpath
		installFeature("opennms-provisioning-detectors", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles)); // System classpath
		installFeature("opennms-provisioning-shell", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsRrdApi() {
		installFeature("opennms-rrd-api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsRrdJrobin() {
		installFeature("opennms-rrd-jrobin");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsSnmp() {
		installFeature("opennms-snmp");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsSnmpCommands() {
		installFeature("opennms-config-api"); // System classpath
		installFeature("opennms-snmp-commands");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsSyslogd() {
		installFeature("opennms-syslogd");
		System.out.println(executeCommand("feature:list -i"));
	}
	
	@Test
	public void testInstallFeatureOpennmsSyslogdListenerJavanet() {
		installFeature("opennms-syslogd-listener-javanet");
		System.out.println(executeCommand("feature:list -i"));
	}
	
	@Test
	public void testInstallFeatureOpennmsSyslogdListenerCamelNetty() {
		installFeature("opennms-syslogd-listener-camel-netty");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsTelemetryCollection() {
		installFeature("opennms-dao-api"); // System classpath
		installFeature("opennms-telemetry-collection");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	@Ignore
	public void testInstallFeatureOpennmsTelemetryDaemon() {
		//installFeature("gemini-blueprint", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		installFeature("opennms-telemetry-daemon", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
		System.out.println(executeCommand("bundle:services"));
	}
	@Test
	public void testInstallFeatureOpennmsTelemetryJti() {
		installFeature("opennms-dao-api"); // System classpath
		installFeature("opennms-telemetry-jti");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsTelemetryNxos() {
		installFeature("opennms-dao-api"); // System classpath
		installFeature("opennms-telemetry-nxos");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsTrapd() {
		installFeature("opennms-trapd");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	@Ignore("OSGi dependency problems: org.opennms.features.reporting.model")
	public void testInstallFeatureOpennmsVmware() {
		installFeature("opennms-core"); // System classpath
		installFeature("opennms-vmware");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsXmlCollector() {
		installFeature("opennms-config-api"); // System classpath
		installFeature("opennms-dao-api"); // System classpath
		installFeature("opennms-xml-collector");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOrgJson() {
		installFeature("org.json");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOwaspEncoder() {
		installFeature("owasp-encoder");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOwaspHtmlSanitizer() {
		installFeature("owasp-html-sanitizer");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeaturePostgresql() {
		installFeature("postgresql");
		System.out.println(executeCommand("feature:list -i"));
	}
	public void testInstallFeatureQuartz() {
		installFeature("quartz");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureRateLimitedLogger() {
		installFeature("rate-limited-logger");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureSmack() {
		installFeature("smack");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureSpringSecurity32() {
		installFeature("spring-security32");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureSpringWebflow() {
		installFeature("spring-webflow");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	@Ignore("OSGi dependency problems: javax.ws.rs")
	public void testInstallFeatureJiraTroubleticketer() {
		installFeature("opennms-core"); // System classpath
		installFeature("jira-troubleticketer");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureTsrmTroubleticketer() {
		installFeature("pax-http"); // Provides javax.servlet version 2.6
		installFeature("opennms-core"); // System classpath
		installFeature("tsrm-troubleticketer");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureTwitter4J() {
		installFeature("twitter4j");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureWmiIntegration() {
		installFeature("opennms-config"); // System classpath
		installFeature("wmi-integration", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureWsmanIntegration() {
		installFeature("wsman-integration", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	@Ignore("OSGi dependency problems: javax.ws.rs.core")
	public void testInstallFeatureDatachoices() {
		installFeature("datachoices");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	@Ignore("OSGi dependency problems: javax.persistence")
	public void testInstallFeatureOpennmsBsmServiceApi() {
		installFeature("org.opennms.features.bsm.service.api");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	@Ignore("OSGi dependency problems: javax.persistence")
	public void testInstallFeatureOpennmsBsmShell() {
		installFeature("org.opennms.features.bsm.service.api");
		installFeature("org.opennms.features.bsm.shell-commands");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureAlarmChangeNotifier() {
		installFeature("pax-http", "4.3.0");
		installFeature("opennms-http-whiteboard");
		installFeature("org.opennms.plugin.licencemanager"); // Plugin manager
		installFeature("org.opennms.plugin.featuremanager"); // Plugin manager
		installFeature("opennms-core"); // System classpath
		installFeature("opennms-core-db"); // System classpath
		installFeature("opennms-events-api"); // System classpath
		installFeature("opennms-model"); // System classpath
		installFeature("alarm-change-notifier");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsEsRest() {
		installFeature("pax-http", "4.3.0");
		installFeature("opennms-http-whiteboard");
		installFeature("org.opennms.plugin.licencemanager"); // Plugin manager
		installFeature("org.opennms.plugin.featuremanager"); // Plugin manager
		installFeature("opennms-core"); // System classpath
		installFeature("opennms-dao-api"); // System classpath
		installFeature("opennms-es-rest");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureInternalPluginsDescriptor() {
		installFeature("pax-http", "4.3.0");
		installFeature("opennms-http-whiteboard");
		installFeature("internal-plugins-descriptor");
		System.out.println(executeCommand("feature:list -i"));
	}
    @Test
    public void testInstallFeatureOpennmsSituationFeedback() {
        installFeature("opennms-dao-api"); // System classpath
        installFeature("opennms-situation-feedback");
    }
}
