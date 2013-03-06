/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFilePut;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.keepRuntimeFolder;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Hashtable;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.karaf.itests.KarafTestSupport;
import org.apache.karaf.tooling.exam.options.LogLevelOption;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class TopologyMapIntegrationTest extends KarafTestSupport {

    //@Inject
    //private TopologyProvider<?,?,?,?> m_topologyProvider;
    
    @Before
    public void setUp() {
        //MockLogAppender.setupLogging();
    }

    /*
    @ProbeBuilder
    @Override
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
        return probe;
    }
    */

    @Configuration
    @Override
    public Option[] config() {

        return combine(
               ///super.config(),
               /*
               new Option[] {
                   karafDistributionConfiguration().frameworkUrl(
                           maven().groupId("org.apache.karaf").artifactId("apache-karaf").versionAsInProject().type("tar.gz")
                       ).karafVersion(
                           MavenUtils.getArtifactVersion("org.apache.karaf", "apache-karaf")
                       ).name("Apache Karaf").unpackDirectory(new File("target/exam")),
                   keepRuntimeFolder()//,
                   //logLevel(LogLevelOption.LogLevel.ERROR)
               },
               */
               new Option[] {
                   karafDistributionConfiguration().frameworkUrl(
                           // Use mvn:org.opennms.container:karaf:tar.gz as the Karaf distribution so that it has the same
                           // settings as a running OpenNMS system
                           maven().groupId("org.opennms.container").artifactId("karaf").versionAsInProject().type("tar.gz")
                       ).karafVersion(
                           MavenUtils.getArtifactVersion("org.apache.karaf", "apache-karaf")
                       ).name("Apache Karaf").unpackDirectory(new File("target/exam"))
                   //keepRuntimeFolder()
               },
               options(
                   logLevel(LogLevelOption.LogLevel.DEBUG),

                   // Change the RMI/JMX ports that Karaf management runs on so that it doesn't conflict
                   // with a running OpenNMS instance.
                   //
                   // Note: The next time we upgrade Karaf, this should be unnecessary because the configs in
                   // KarafTestSupport have been changed in an identical manner.
                   editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", "1101"),
                   editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", "44445"),

                   // Change the SSH port so that it doesn't conflict with a running OpenNMS instance
                   editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", "8201"),

                   systemPackage("org.apache.commons.logging"),

                   //mavenBundle("org.apache.felix", "org.apache.felix.configadmin").versionAsInProject(),
                   //mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject(), 
                   //mavenBundle("org.ops4j.pax.logging", "pax-logging-service").versionAsInProject(), 
                   ///mavenBundle("org.apache.aries", "org.apache.aries.util").versionAsInProject(),
                   ///mavenBundle("org.apache.aries.blueprint", "org.apache.aries.blueprint").versionAsInProject(),
                   //mavenBundle("org.apache.aries.blueprint", "org.apache.aries.blueprint.sample").versionAsInProject(),
                   //mavenBundle("org.apache.aries.jmx", "org.apache.aries.jmx.blueprint").versionAsInProject(),
                   ///mavenBundle("org.apache.aries.proxy", "org.apache.aries.proxy").versionAsInProject(),
                   //mavenBundle("org.osgi", "org.osgi.compendium").versionAsInProject(),
                   
                   //systemPackage("org.ops4j.pax.exam.options"),
                   /****
                   mavenBundle("org.ops4j.base", "ops4j-base-util-property").versionAsInProject(),
                   mavenBundle("org.ops4j.base", "ops4j-base-monitors").versionAsInProject(),
                   mavenBundle("org.ops4j.base", "ops4j-base-io").versionAsInProject(),
                   mavenBundle("org.ops4j.base", "ops4j-base-lang").versionAsInProject(),
                   mavenBundle("org.ops4j.base", "ops4j-base-store").versionAsInProject(),
                   mavenBundle("org.ops4j.pax.exam", "pax-exam").versionAsInProject(),
                   mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime").versionAsInProject(),
                   mavenBundle("org.apache.karaf.shell", "org.apache.karaf.shell.console").versionAsInProject(),
                   wrappedBundle(mavenBundle("org.apache.karaf.itests", "itests").versionAsInProject().classifier("tests")),
                   ****/
                   //mavenBundle("org.opennms.features.topology", "api").versionAsInProject(),
                   //mavenBundle("org.opennms.features.topology.plugins.topo", "linkd").versionAsInProject(),
                   //mavenBundle("com.vaadin", "vaadin").versionAsInProject(),
                   junitBundles()
            )
        );
    }

    /**
     * Override this function to specify a different port for the RMI service.
     * 
     * Note: The next time we upgrade Karaf, this should be unnecessary because the configs in
     * KarafTestSupport have been changed in an identical manner.
     */
    @Override
    public JMXConnector getJMXConnector() throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1101/karaf-root");
        Hashtable<String,String[]> env = new Hashtable<String,String[]>();
        String[] credentials = new String[]{"admin", "admin"};
        env.put("jmx.remote.credentials", credentials);
        JMXConnector connector = JMXConnectorFactory.connect(url, env);
        return connector;
    }

    @Test
    @Ignore
    public void listCommands() throws Exception {
        System.out.println(executeCommand("list -t 0"));
    }

    @Test
    @Ignore
    public void getHelloService() {
        System.out.println(executeCommand("bundle:start org.opennms.features.topology:api"));
        assertNotNull(getOsgiService(GraphProvider.class));
    }
}
