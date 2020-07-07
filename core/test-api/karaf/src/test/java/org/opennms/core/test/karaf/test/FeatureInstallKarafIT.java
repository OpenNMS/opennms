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
public class FeatureInstallKarafIT extends KarafTestCase {

    /**
     * This test attempts to install all features from the standard Karaf
     * features.xml.
     */
    @Test
    public void testInstallAllStandardFeatures() {
        installFeature("admin");
        installFeature("aries-blueprint");
        installFeature("aries-proxy");
        installFeature("blueprint-web");
        installFeature("config");
        installFeature("deployer");
        installFeature("diagnostic");
        installFeature("eventadmin");
        installFeature("features");
        installFeature("http");
        installFeature("http-whiteboard");
        installFeature("jaas");
        installFeature("jasypt-encryption");
        installFeature("jetty");
        installFeature("karaf-framework");
        installFeature("kar");
        installFeature("management");
        installFeature("obr");
        //installFeature("scr");
        installFeature("service-security");
        installFeature("service-wrapper");
        installFeature("shell");
        installFeature("ssh");
        installFeature("war");
        installFeature("webconsole");
        installFeature("wrap");
        installFeature("wrapper");

        System.out.println(executeCommand("features:list"));
    }

    /**
     * This test attempts to install all features from the Spring Karaf
     * features.xml.
     */
    @Test
    public void testInstallAllSpringFeatures() {
        installFeature("spring");
        installFeature("spring-aspects");
        installFeature("spring-instrument");
        installFeature("spring-jdbc");
        installFeature("spring-jms");
        //installFeature("spring-test");
        installFeature("spring-orm");
        installFeature("spring-oxm");
        installFeature("spring-tx");
        //installFeature("spring-web");
        //installFeature("spring-web-portlet");
        //installFeature("spring-websocket");
        //installFeature("spring-security");

        System.out.println(executeCommand("features:list"));
    }

    /**
     * This test attempts to install all features from the OpenNMS
     * features.xml.
     */
    @Test
    public void testInstallAllOpenNMSFeatures() {
        addFeaturesUrl(maven().groupId("org.opennms.container").artifactId("karaf").version("2016.1.24").type("xml").classifier("features").getURL());
        addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version("2016.1.24").type("xml").classifier("features").getURL());

        installFeature("atomikos");
        installFeature("batik");
        installFeature("c3p0");
        installFeature("castor");
        installFeature("commons-beanutils");
        installFeature("commons-cli");
        installFeature("commons-codec");
        installFeature("commons-collections");
        //installFeature("commons-configuration");
        installFeature("commons-digester");
        installFeature("commons-exec");
        installFeature("commons-io");
        installFeature("commons-jexl");
        installFeature("commons-lang");
        installFeature("commons-net");
        installFeature("dnsjava");
        installFeature("fop");
        installFeature("guava");
        installFeature("hibernate36");
        installFeature("hibernate-validator41");
        installFeature("jaxb");
        installFeature("jersey-client");
        installFeature("jfreechart");
        //installFeature("jolokia");
        installFeature("jrobin");
        installFeature("json-lib");
        installFeature("opennms-activemq-config");
        //installFeature("opennms-activemq");
        installFeature("opennms-activemq-dispatcher-config");
        //installFeature("opennms-activemq-dispatcher");
        //installFeature("opennms-activemq-event-forwarder");
        //installFeature("opennms-activemq-event-receiver");
        installFeature("opennms-collection-api");
        installFeature("opennms-collection-persistence-rrd");
        installFeature("opennms-config-api");
        installFeature("opennms-config");
        installFeature("opennms-config-jaxb");
        installFeature("opennms-core-daemon");
        installFeature("opennms-core-db");
        installFeature("opennms-core");
        installFeature("opennms-core-web");
        installFeature("opennms-dao-api");
        installFeature("opennms-dao");
        installFeature("opennms-events-api");
        installFeature("opennms-events-daemon");
        installFeature("opennms-events-traps");
        installFeature("opennms-icmp-api");
        //installFeature("opennms-javamail");
        installFeature("opennms-model");
        installFeature("opennms-poller-api");
        //installFeature("opennms-provisioning");
        //installFeature("opennms-reporting");
        installFeature("opennms-rrd-api");
        installFeature("opennms-rrd-jrobin");
        installFeature("opennms-snmp");
        //installFeature("opennms-webapp");
        installFeature("org.json");
        installFeature("postgresql");

        System.out.println(executeCommand("features:list"));
    }
}
