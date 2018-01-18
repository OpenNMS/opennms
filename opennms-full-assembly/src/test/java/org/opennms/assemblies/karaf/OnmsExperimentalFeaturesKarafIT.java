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
 * mvn:org.opennms.karaf/opennms/${currentVersion}/xml/experimental
 * <code>
 * <p>load correctly in Karaf.</p>
 * 
 * @author Seth
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OnmsExperimentalFeaturesKarafIT extends KarafTestCase {

	@Before
	public void setUp() {
		final String version = getOpenNMSVersion();
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("standard").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("spring-legacy").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("experimental").getURL());
	}

	@Test
	public void testInstallFeatureOpennmsActivemqComponent() {
		installFeature("opennms-activemq-component");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	public void testInstallFeatureOpennmsDiscovery() {
		installFeature("opennms-discovery");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	@Ignore("OSGi dependency problems: org.apache.activemq.broker")
	public void testInstallFeatureOpennmsEventsDaemon() {
		installFeature("opennms-events-daemon");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	@Ignore("OSGi dependency problems: org.opennms.rancid")
	public void testInstallFeatureOpennmsReporting() {
		installFeature("opennms-reporting");
		System.out.println(executeCommand("feature:list -i"));
	}
	@Test
	@Ignore("Experimental feature")
	public void testInstallFeatureOpennmsWebapp() {
		installFeature("opennms-webapp");
		System.out.println(executeCommand("feature:list -i"));
	}
}
