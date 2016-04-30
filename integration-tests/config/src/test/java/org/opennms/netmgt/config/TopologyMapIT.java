/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.karaf.KarafTestCase;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;

@RunWith(PaxExam.class)
public class TopologyMapIT extends KarafTestCase {

    /*
    @Configuration
    public Option[] config() {
        return new Option[] {
            karafDistributionConfiguration().frameworkUrl(
                    // Use mvn:org.opennms.container:karaf:tar.gz as the Karaf distribution so that it has the same
                    // settings as a running OpenNMS system
                    maven().groupId("org.opennms.container").artifactId("karaf").versionAsInProject().type("tar.gz")
                ).karafVersion(
                    //MavenUtils.getArtifactVersion("org.apache.karaf", "apache-karaf")
                    "2.4.3"
                ).name("OpenNMS Apache Karaf").unpackDirectory(new File("target/exam")
            ),

            keepRuntimeFolder(),

            // Crank the logging
            logLevel(LogLevelOption.LogLevel.DEBUG),

            // Change the RMI/JMX ports that Karaf management runs on so that it doesn't conflict
            // with a running OpenNMS instance.
            //
            // Note: The next time we upgrade Karaf, this should be unnecessary because the configs in
            // KarafTestSupport have been changed in an identical manner.
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", "1101"),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", "44445"),

            editConfigurationFilePut("etc/org.apache.karaf.features.cfg", "featuresBoot", "config,ssh,http,http-whiteboard,exam"),

            // Change the SSH port so that it doesn't conflict with a running OpenNMS instance
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", "8201"),

            / **
             * I think that we need to install org.apache.karaf.itests:itests:tests and all of its dependencies
             * into the container so that the unit test will execute properly. This doesn't seem to work with
             * Karaf 2.3.1... I get inconsistent behavior, almost like there is a race condition when registering
             * services or something. *sigh*
             * /
            //wrappedBundle(mavenBundle("org.apache.karaf.itests", "itests").versionAsInProject().classifier("tests")),
            / *
            mavenBundle("org.ops4j.base", "ops4j-base-util-property").versionAsInProject(),
            mavenBundle("org.ops4j.base", "ops4j-base-monitors").versionAsInProject(),
            mavenBundle("org.ops4j.base", "ops4j-base-io").versionAsInProject(),
            mavenBundle("org.ops4j.base", "ops4j-base-lang").versionAsInProject(),
            mavenBundle("org.ops4j.base", "ops4j-base-store").versionAsInProject(),
            mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime").versionAsInProject(),
            * /
            mavenBundle("org.apache.karaf.shell", "org.apache.karaf.shell.console").versionAsInProject().noStart(),
            mavenBundle("org.ops4j.pax.exam", "pax-exam").versionAsInProject().noStart(),
            mavenBundle("org.apache.karaf.itests", "itests").versionAsInProject().classifier("tests").noStart(),

            junitBundles()
        };
    }
    */

    @Test
    public void listCommands() throws Exception {
        addFeaturesUrl(maven().groupId("org.apache.karaf.assemblies.features").artifactId("standard").version("2.4.3").classifier("features").type("xml").getURL());
        System.out.println(executeCommand("osgi:list -t 0"));
    }

    @Test
    public void getTopologyProvider() {
        addFeaturesUrl(maven().groupId("org.apache.karaf.assemblies.features").artifactId("standard").version("2.4.3").classifier("features").type("xml").getURL());

        /*
        TODO: Install the topology features and fetch them from the OSGi registry

        System.out.println(executeCommand("osgi:install mvn:org.opennms.features.topology/api"));
        System.out.println(executeCommand("osgi:start 74"));

        assertNotNull(getOsgiService(GraphProvider.class));
         */
    }
}
