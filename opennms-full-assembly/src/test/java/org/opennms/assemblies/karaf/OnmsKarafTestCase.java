/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
				.version("33.1.4-SNAPSHOT");
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
