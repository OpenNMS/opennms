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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.karaf.KarafTestCase;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * <p>This test checks that the features from:</p>
 * <code>
 * mvn:org.opennms.karaf/opennms/${currentVersion}/xml/minion
 * <code>
 * <p>load correctly in Karaf.</p>
 * 
 * @author Seth
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class MinionFeatureKarafIT extends KarafTestCase {

	@Before
	public void setUp() {
		addFeaturesUrl(maven().groupId("org.apache.karaf.features").artifactId("standard").version(getKarafVersion()).type("xml").classifier("features").getURL());
		addFeaturesUrl(maven().groupId("org.apache.karaf.features").artifactId("spring-legacy").version(getKarafVersion()).type("xml").classifier("features").getURL());

		// This artifact contains Minion-only Karaf features
		final String version = getOpenNMSVersion();
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("minion").getURL());
	}

	@Test
	public void testInstallFeatureMinionCoreApi() {
		installFeature("minion-core-api");
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureOpennmsDaoMinion() {
		installFeature("opennms-dao-minion");
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureOpennmsTrapdListener() {
		installFeature("opennms-trapd-listener");
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionHeartbeatProducer() {
		installFeature("minion-heartbeat-producer");
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionProvisiondDetectors() {
		installFeature("minion-provisiond-detectors", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionProvisiondRequisitions() {
		installFeature("minion-provisiond-requisitions", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionCollection() {
		installFeature("minion-collection", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionPoller() {
		installFeature("minion-poller");
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionShellCollection() {
		installFeature("minion-shell-collection", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionShellPoller() {
		installFeature("minion-shell-poller");
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionShellProvision() {
		installFeature("minion-shell-provision", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionShell() {
		installFeature("minion-shell", EnumSet.of(FeaturesService.Option.NoAutoRefreshBundles));
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionIcmpProxy() {
		installFeature("minion-icmp-proxy");
		System.out.println(executeCommand("feature:list -i"));
	}

	@Test
	public void testInstallFeatureMinionSnmpProxy() {
		installFeature("minion-snmp-proxy");
		System.out.println(executeCommand("feature:list -i"));
	}
}
