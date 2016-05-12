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
		addFeaturesUrl(maven().groupId("org.opennms.container").artifactId("karaf").version("19.0.0-SNAPSHOT").type("xml").classifier("features").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version("19.0.0-SNAPSHOT").type("xml").classifier("features").getURL());
	}

	@Test
	public void testInstallFeatureAtomikos() {
		installFeature("atomikos");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureBatik() {
		installFeature("batik");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureC3P0() {
		installFeature("c3p0");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCastor() {
		installFeature("castor");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsBeanutils() {
		installFeature("commons-beanutils");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsCli() {
		installFeature("commons-cli");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsCodec() {
		installFeature("commons-codec");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsCollections() {
		installFeature("commons-collections");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	@Ignore("OSGi dependency problems: javax.servlet")
	public void testInstallFeatureCommonsConfiguration() {
		installFeature("commons-configuration");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsDigester() {
		installFeature("commons-digester");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsExec() {
		installFeature("commons-exec");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsIo() {
		installFeature("commons-io");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsJexl() {
		installFeature("commons-jexl");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsLang() {
		installFeature("commons-lang");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureCommonsNet() {
		installFeature("commons-net");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureDnsjava() {
		installFeature("dnsjava");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureDropwizardMetrics() {
		installFeature("dropwizard-metrics");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureFop() {
		installFeature("fop");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureGuava() {
		installFeature("guava");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureHibernate36() {
		installFeature("hibernate36");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureHibernateValidator41() {
		installFeature("hibernate-validator41");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureJaxb() {
		installFeature("jaxb");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureJfreechart() {
		installFeature("jfreechart");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureJicmp() {
		installFeature("jicmp");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureJicmp6() {
		installFeature("jicmp6");
		System.out.println(executeCommand("features:list -i"));
	}
	/*
	@Test
	public void testInstallFeatureJolokia() {
		installFeature("jolokia");
		System.out.println(executeCommand("features:list -i"));
	}
	*/
	@Test
	public void testInstallFeatureJrobin() {
		installFeature("jrobin");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureJsonLib() {
		installFeature("json-lib");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureLmaxDisruptor() {
		installFeature("lmax-disruptor");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureJna() {
		installFeature("net.java.dev.jna");
		System.out.println(executeCommand("features:list -i"));
	}


	/*
	@Test
	public void testInstallFeature250() {
		installFeature("opennms-activemq-config");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeature260() {
		installFeature("opennms-activemq");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeature270() {
		installFeature("opennms-activemq-dispatcher-config");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeature280() {
		installFeature("opennms-activemq-dispatcher");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeature290() {
		installFeature("opennms-activemq-event-forwarder");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeature300() {
		installFeature("opennms-activemq-event-receiver");
		System.out.println(executeCommand("features:list -i"));
	}
	*/
	@Test
	@Ignore("OSGi dependency problems: org.opennms.netmgt.alarmd.api")
	public void testInstallFeatureAmqpAlarmNorthbounder() {
		installFeature("opennms-amqp-alarm-northbounder");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureAmqpEventForwarder() {
		installFeature("opennms-amqp-event-forwarder");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureAmqpEventReceiver() {
		installFeature("opennms-amqp-event-receiver");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCollectionApi() {
		installFeature("opennms-collection-api");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCollectionPersistenceRrd() {
		installFeature("opennms-collection-persistence-rrd");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsConfigApi() {
		installFeature("opennms-config-api");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsConfig() {
		installFeature("opennms-config");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsConfigJaxb() {
		installFeature("opennms-config-jaxb");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreCamel() {
		installFeature("opennms-core-camel");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreDaemon() {
		installFeature("opennms-core-daemon");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreDb() {
		installFeature("opennms-core-db");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCore() {
		installFeature("opennms-core");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsCoreWeb() {
		installFeature("opennms-core-web");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsDaoApi() {
		installFeature("opennms-dao-api");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsDao() {
		installFeature("opennms-dao");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsDiscovery() {
		installFeature("opennms-discovery");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsDiscoveryDaemon() {
		installFeature("opennms-discovery-daemon");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsEventsApi() {
		installFeature("opennms-events-api");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsEventsDaemon() {
		installFeature("opennms-events-daemon");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsEventsTraps() {
		installFeature("opennms-events-traps");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpApi() {
		installFeature("opennms-icmp-api");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpJna() {
		installFeature("opennms-icmp-jna");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpJni() {
		installFeature("opennms-icmp-jni");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsIcmpJni6() {
		installFeature("opennms-icmp-jni6");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsJavamail() {
		installFeature("opennms-javamail");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsModel() {
		installFeature("opennms-model");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsPollerApi() {
		installFeature("opennms-poller-api");
		System.out.println(executeCommand("features:list -i"));
	}
	/*
	@Test
	public void testInstallFeatureOpennmsProvisioning() {
		installFeature("opennms-provisioning");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsReporting() {
		installFeature("opennms-reporting");
		System.out.println(executeCommand("features:list -i"));
	}
	*/
	@Test
	public void testInstallFeatureOpennmsRrdApi() {
		installFeature("opennms-rrd-api");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsRrdJrobin() {
		installFeature("opennms-rrd-jrobin");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsSnmp() {
		installFeature("opennms-snmp");
		System.out.println(executeCommand("features:list -i"));
	}
	/*
	@Test
	public void testInstallFeatureOpennmsWebapp() {
		installFeature("opennms-webapp");
		System.out.println(executeCommand("features:list -i"));
	}
	*/
	@Test
	public void testInstallFeatureOrgJson() {
		installFeature("org.json");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeaturePostgresql() {
		installFeature("postgresql");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureSpringSecurity32() {
		installFeature("pax-http"); // Provides javax.servlet version 2.6
		installFeature("spring-security32");
		System.out.println(executeCommand("features:list -i"));
	}
	@Test
	public void testInstallFeatureSpringWebflow() {
		installFeature("spring-webflow");
		System.out.println(executeCommand("features:list -i"));
	}

    @Test
    public void testInstallFeatureTsrmTroubleticketer() {
        installFeature("pax-http"); // Provides javax.servlet version 2.6
        installFeature("tsrm-troubleticketer");
        System.out.println(executeCommand("features:list -i"));
    }
}
