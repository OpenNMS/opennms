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

package org.opennms.core.test.karaf.test;

import static org.ops4j.pax.exam.CoreOptions.maven;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.karaf.KarafTestCase;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
@Ignore("Ignore for now since creating the features repo is causing dependency problems")
public class OnmsFeatureKarafIT extends KarafTestCase {

	@Before
	public void setUp() {
		addFeaturesUrl(maven().groupId("org.opennms.container").artifactId("karaf").version("17.0.0-SNAPSHOT").type("xml").classifier("features").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version("17.0.0-SNAPSHOT").type("xml").classifier("features").getURL());
	}

	@Test
	public void testInstallFeature000() {
		installFeature("atomikos");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature010() {
		installFeature("batik");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature015() {
		installFeature("c3p0");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature020() {
		installFeature("castor");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature030() {
		installFeature("commons-beanutils");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature040() {
		installFeature("commons-cli");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature050() {
		installFeature("commons-codec");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature060() {
		installFeature("commons-collections");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature070() {
		installFeature("commons-configuration");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature080() {
		installFeature("commons-digester");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature090() {
		installFeature("commons-exec");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature100() {
		installFeature("commons-io");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature110() {
		installFeature("commons-jexl");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature120() {
		installFeature("commons-lang");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature130() {
		installFeature("commons-net");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature140() {
		installFeature("dnsjava");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature150() {
		installFeature("fop");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature160() {
		installFeature("guava");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature170() {
		installFeature("hibernate36");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature180() {
		installFeature("hibernate-validator41");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature190() {
		installFeature("jaxb");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature200() {
		installFeature("jersey-client");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature210() {
		installFeature("jfreechart");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature220() {
		installFeature("jolokia");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature230() {
		installFeature("jrobin");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature240() {
		installFeature("json-lib");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature250() {
		installFeature("opennms-activemq-config");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature260() {
		installFeature("opennms-activemq");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature270() {
		installFeature("opennms-activemq-dispatcher-config");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature280() {
		installFeature("opennms-activemq-dispatcher");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature290() {
		installFeature("opennms-activemq-event-forwarder");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature300() {
		installFeature("opennms-activemq-event-receiver");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature310() {
		installFeature("opennms-collection-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature320() {
		installFeature("opennms-collection-persistence-rrd");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature330() {
		installFeature("opennms-config-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature340() {
		installFeature("opennms-config");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature350() {
		installFeature("opennms-config-jaxb");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature360() {
		installFeature("opennms-core-daemon");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature370() {
		installFeature("opennms-core-db");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature380() {
		installFeature("opennms-core");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature390() {
		installFeature("opennms-core-web");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature400() {
		installFeature("opennms-dao-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature410() {
		installFeature("opennms-dao");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature415() {
		installFeature("opennms-discovery");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature420() {
		installFeature("opennms-events-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature430() {
		installFeature("opennms-events-daemon");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature440() {
		installFeature("opennms-events-traps");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallIcmpApi() {
		installFeature("opennms-icmp-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallIcmpJna() {
		installFeature("opennms-icmp-jna");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallIcmpJni() {
		installFeature("opennms-icmp-jni");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallIcmpJni6() {
		installFeature("opennms-icmp-jni6");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature460() {
		installFeature("opennms-javamail");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature470() {
		installFeature("opennms-model");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature480() {
		installFeature("opennms-poller-api");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature490() {
		installFeature("opennms-provisioning");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature500() {
		installFeature("opennms-reporting");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature510() {
		installFeature("opennms-rrd-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature520() {
		installFeature("opennms-rrd-jrobin");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature530() {
		installFeature("opennms-snmp");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature540() {
		installFeature("opennms-webapp");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature550() {
		installFeature("org.json");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature560() {
		installFeature("postgresql");
		System.out.println(executeCommand("features:list"));
	}
}
