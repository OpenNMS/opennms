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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.karaf.KarafTestCase;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class FeatureInstallKarafIT extends KarafTestCase {

    /**
     * This test attempts to install all features from the standard Karaf
     * features.xml.
     */
    @Test
    public void testInstallAllStandardFeatures() {
        //installFeature("aries-annotation");
        installFeature("aries-blueprint");
        installFeature("aries-proxy");
        installFeature("blueprint-web");
        installFeature("bundle");
        installFeature("config");
        installFeature("deployer");
        installFeature("diagnostic");
        installFeature("eventadmin");
        installFeature("feature");
        // The 'framework-security' feature installation causes a refresh of 
        // basically the entire container so avoid it during this test
        //installFeature("framework-security");
        installFeature("http");
        installFeature("http-whiteboard");
        installFeature("instance");
        installFeature("jaas-boot");
        installFeature("jaas");
        installFeature("jasypt-encryption");
        // TODO: Test both versions of Jetty?
        installFeature("jetty");
        installFeature("jolokia");
        installFeature("kar");
        installFeature("log");
        installFeature("management");
        installFeature("minimal");
        // The 'obr' feature installation causes a refresh of 
        // the 'org.apache.karaf.deployer.features' bundle so
        // avoid it during this test
        //installFeature("obr");
        installFeature("package");
        installFeature("profile");
        installFeature("scheduler");
        installFeature("scr");
        installFeature("service");
        installFeature("service-security");
        installFeature("service-wrapper");
        installFeature("shell-compat");
        installFeature("shell");
        installFeature("ssh");
        installFeature("standard");
        installFeature("system");
        installFeature("war");
        installFeature("webconsole");
        installFeature("wrap");
        installFeature("wrapper");

        System.out.println(executeCommand("feature:list -i"));
    }

    /**
     * This test attempts to install all features from the Spring Karaf
     * features.xml.
     */
    @Test
    public void testInstallAllSpringFeatures() {
        addFeaturesUrl(maven().groupId("org.apache.karaf.features").artifactId("spring-legacy").version("4.1.5").type("xml").classifier("features").getURL());

        installFeature("spring", "4.2.9.RELEASE_1");
        installFeature("spring-aspects", "4.2.9.RELEASE_1");
        installFeature("spring-instrument", "4.2.9.RELEASE_1");
        installFeature("spring-jdbc", "4.2.9.RELEASE_1");
        installFeature("spring-jms", "4.2.9.RELEASE_1");
        //installFeature("spring-test", "4.2.9.RELEASE_1");
        installFeature("spring-orm", "4.2.9.RELEASE_1");
        installFeature("spring-oxm", "4.2.9.RELEASE_1");
        installFeature("spring-tx", "4.2.9.RELEASE_1");
        installFeature("spring-web", "4.2.9.RELEASE_1");
        //installFeature("spring-web-portlet");
        //installFeature("spring-websocket");
        //installFeature("spring-security");

        System.out.println(executeCommand("feature:list -i"));
    }
}
