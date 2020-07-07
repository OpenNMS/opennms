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
		addFeaturesUrl(maven().groupId("org.opennms.container").artifactId("karaf").version("2016.1.25-SNAPSHOT").type("xml").classifier("features").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version("2016.1.25-SNAPSHOT").type("xml").classifier("features").getURL());
	}

	@Test
	public void testInstallFeature99() {
		installFeature("atomikos");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature00() {
		installFeature("batik");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature01() {
		installFeature("c3p0");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature02() {
		installFeature("castor");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature03() {
		installFeature("commons-beanutils");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature04() {
		installFeature("commons-cli");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature05() {
		installFeature("commons-codec");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature06() {
		installFeature("commons-collections");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature07() {
		installFeature("commons-configuration");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature08() {
		installFeature("commons-digester");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature09() {
		installFeature("commons-exec");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature10() {
		installFeature("commons-io");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature11() {
		installFeature("commons-jexl");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature12() {
		installFeature("commons-lang");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature13() {
		installFeature("commons-net");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature14() {
		installFeature("dnsjava");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature15() {
		installFeature("fop");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature16() {
		installFeature("guava");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature17() {
		installFeature("hibernate36");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature18() {
		installFeature("hibernate-validator41");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature19() {
		installFeature("jaxb");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature20() {
		installFeature("jersey-client");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature21() {
		installFeature("jfreechart");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature22() {
		installFeature("jolokia");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature23() {
		installFeature("jrobin");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature24() {
		installFeature("json-lib");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature25() {
		installFeature("opennms-activemq-config");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature26() {
		installFeature("opennms-activemq");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature27() {
		installFeature("opennms-activemq-dispatcher-config");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature28() {
		installFeature("opennms-activemq-dispatcher");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature29() {
		installFeature("opennms-activemq-event-forwarder");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature30() {
		installFeature("opennms-activemq-event-receiver");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature31() {
		installFeature("opennms-collection-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature32() {
		installFeature("opennms-collection-persistence-rrd");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature33() {
		installFeature("opennms-config-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature34() {
		installFeature("opennms-config");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature35() {
		installFeature("opennms-config-jaxb");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature36() {
		installFeature("opennms-core-daemon");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature37() {
		installFeature("opennms-core-db");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature38() {
		installFeature("opennms-core");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature39() {
		installFeature("opennms-core-web");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature40() {
		installFeature("opennms-dao-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature41() {
		installFeature("opennms-dao");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature42() {
		installFeature("opennms-events-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature43() {
		installFeature("opennms-events-daemon");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature44() {
		installFeature("opennms-events-traps");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature45() {
		installFeature("opennms-icmp-api");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature46() {
		installFeature("opennms-javamail");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature47() {
		installFeature("opennms-model");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature48() {
		installFeature("opennms-poller-api");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature49() {
		installFeature("opennms-provisioning");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature50() {
		installFeature("opennms-reporting");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature51() {
		installFeature("opennms-rrd-api");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature52() {
		installFeature("opennms-rrd-jrobin");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature53() {
		installFeature("opennms-snmp");
		System.out.println(executeCommand("features:list"));
	}
	/*
	@Test
	public void testInstallFeature54() {
		installFeature("opennms-webapp");
		System.out.println(executeCommand("features:list"));
	}
	*/
	@Test
	public void testInstallFeature55() {
		installFeature("org.json");
		System.out.println(executeCommand("features:list"));
	}
	@Test
	public void testInstallFeature56() {
		installFeature("postgresql");
		System.out.println(executeCommand("features:list"));
	}
}
