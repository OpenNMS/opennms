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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import org.opennms.core.test.karaf.KarafTestCase;
import org.ops4j.pax.exam.options.MavenUrlReference;

/**
 * @deprecated This test base class doesn't work because our Karaf 
 * container artifact:
 * 
 * mvn:org.opennms.container/org.opennms.container.karaf/${version}/tar.gz
 * 
 * isn't packaged with a top-level product directory like the Apache Karaf
 * tar.gz artifacts are.
 */
public class OnmsKarafTestCase extends KarafTestCase {

	/**
	 * Use the OpenNMS-modified Karaf container.
	 */
	@Override
	protected MavenUrlReference getFrameworkUrl() {
		return maven()
				.groupId("org.opennms.container")
				.artifactId("org.opennms.container.karaf")
				.type("tar.gz")
				.version("23.0.0-SNAPSHOT");
	}

	/**
	 * Fetch the OpenNMS system classpath from our modified custom.properties file.
	 */
	@Override
	protected String[] getSystemPackages() {
		Properties customProperties = new Properties();
		try {
			customProperties.load(new FileInputStream("../container/karaf/src/main/filtered-resources/etc/custom.properties"));
		} catch (IOException e) {
			System.err.println("Unexpected error while trying to load system properties");
			e.printStackTrace();
			return new String[0];
		}

		String classpath =  customProperties.getProperty("org.osgi.framework.system.packages.extra");
		System.out.println("System classpath: " + classpath);
		return Arrays.stream(classpath.split(","))
			// Remove all of the version constraints
			.map(s -> {
				return s.replaceAll(";.*$", "");
			})
			.collect(Collectors.toList()).toArray(new String[0]);
	}
}
