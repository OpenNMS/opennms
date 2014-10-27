/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.ncs;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.keepRuntimeFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.bootClasspathLibrary;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.File;
import java.io.InputStream;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
@Ignore
public class HelloWorldTest {
    @Inject
    private NCSComponentRepository m_repository;

    @Inject
    private BundleContext m_context;

    /*
    @Inject
    private Action m_action;
    */

    public HelloWorldTest() {
    }

    @Configuration
    public Option[] config() {
        InputStream inp = bundle()
            .add(MockNCSComponentRepository.class)
            .add(MockNCSCompontentRepositoryActivator.class)
            .set(Constants.BUNDLE_SYMBOLICNAME, "Mock NCS Component Repository")
            .set(Constants.IMPORT_PACKAGE, "org.osgi.framework,org.opennms.netmgt.model.ncs")
            .set(Constants.BUNDLE_ACTIVATOR, MockNCSCompontentRepositoryActivator.class.getName())
            .build();

        return options(
            karafDistributionConfiguration().frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("tar.gz").version("2.2.7")).karafVersion("2.2.7").name("Apache Karaf").unpackDirectory(new File("target/exam")),
            bootClasspathLibrary(maven().groupId("org.opennms.features.ncs").artifactId("ncs-model").versionAsInProject()),
            systemPackages(
                "org.opennms.netmgt.model.ncs;version=1.11.5"
            ),
            bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes/"),
            provision(inp),
            junitBundles(),
            keepRuntimeFolder()
        );
    }

    @Before
    public void setUp() throws Exception {
        assertNotNull(m_context);
    }

    @Test
    public void testSomething() {
        assertNotNull(m_repository);
        assertEquals(0, m_repository.countAll());
        // assertNotNull(m_action);
    }
}
