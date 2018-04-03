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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
@Ignore("This doesn't work because of problems with the system classpath")
public class FeaturesBootKarafIT extends OnmsKarafTestCase {

	/**
	 * This test attempts to install all features from the OpenNMS
	 * featuresBoot list in:
	 * 
	 * src/main/filtered-resources/etc/org.apache.karaf.features.cfg
	 */
	@Test
	public void testInstallAllOpenNMSFeatures() {
		final String version = getOpenNMSVersion();
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("standard").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("spring-legacy").getURL());
		addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("features").getURL());

		for (String feature : new String[] {
			"karaf-framework",
			"ssh",
			"config",
			"features",
			"management",
			"http",
			"http-whiteboard",
			"kar",
			"deployer",
			"opennms-jaas-login-module",
			"datachoices",
			"opennms-topology-runtime-browsers",
			"opennms-topology-runtime-linkd",
			"opennms-topology-runtime-simple",
			"opennms-topology-runtime-vmware",
			"opennms-topology-runtime-application",
			"opennms-topology-runtime-bsm",
			"osgi-nrtg-local",
			"vaadin-node-maps",
			"vaadin-snmp-events-and-metrics",
			"vaadin-dashboard",
			"dashlet-summary",
			"dashlet-alarms",
			"dashlet-bsm",
			"dashlet-map",
			"dashlet-image",
			"dashlet-charts",
			"dashlet-grafana",
			"dashlet-rtc",
			"dashlet-rrd",
			"dashlet-ksc",
			"dashlet-topology",
			"dashlet-url",
			"dashlet-surveillance",
			"vaadin-surveillance-views",
			"vaadin-jmxconfiggenerator",
			"vaadin-opennms-pluginmanager",
			"vaadin-adminpage",
			"org.opennms.features.bsm.shell-commands"
		}) {
			System.out.println("Installing feature: " + feature);
			installFeature(feature);
			System.out.println("Installed feature: " + feature);
		}

		System.out.println(executeCommand("feature:list -i"));
	}
}
