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
     * This test attempts to install all features from the OpenNMS
     * features.xml.
     */
    @Test
    public void testInstallAllOpenNMSFeatures() {
        final String version = getOpenNMSVersion();
        addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("standard").getURL());
        addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("spring-legacy").getURL());
        addFeaturesUrl(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("features").getURL());

        installFeature("atomikos");
        installFeature("batik");
        installFeature("c3p0");
        installFeature("commons-beanutils");
        installFeature("commons-cli");
        installFeature("commons-codec");
        installFeature("commons-collections");
        // OSGi dependency problems: javax.servlet
        //installFeature("commons-configuration");
        installFeature("commons-digester");
        installFeature("commons-exec");
        installFeature("commons-io");
        installFeature("commons-jexl");
        installFeature("commons-lang");
        installFeature("commons-net");
        installFeature("dnsjava");
        installFeature("dropwizard-metrics");
        installFeature("fop");
        installFeature("guava");
        installFeature("hibernate36");
        installFeature("hibernate-validator41");
        installFeature("java-native-access");
        installFeature("jaxb");
        installFeature("jfreechart");
        installFeature("jicmp");
        installFeature("jicmp6");
        // Test fails because of dependence on 'http' feature
        //installFeature("jolokia");
        installFeature("jrobin");
        installFeature("json-lib");
        installFeature("lmax-disruptor");
        // OSGi dependency problems: org.opennms.netmgt.alarmd.api
        //installFeature("opennms-amqp-alarm-northbounder");
        installFeature("opennms-amqp-event-forwarder");
        installFeature("opennms-amqp-event-receiver");
        installFeature("opennms-collection-api");
        installFeature("opennms-collection-persistence-rrd");
        installFeature("opennms-config-api");
        installFeature("opennms-config");
        installFeature("opennms-config-jaxb");
        installFeature("opennms-core-camel");
        // OSGi dependency problems: org.apache.activemq.broker
        //installFeature("opennms-core-daemon");
        installFeature("opennms-core-db");
        installFeature("opennms-core");
        installFeature("opennms-core-web");
        installFeature("opennms-dao-api");
        // OSGi dependency problems: org.apache.activemq.broker
        //installFeature("opennms-dao");
        installFeature("opennms-events-api");
        installFeature("opennms-icmp-api");
        installFeature("opennms-icmp-jna");
        installFeature("opennms-icmp-jni");
        installFeature("opennms-icmp-jni6");
        installFeature("opennms-icmp-best");
        installFeature("opennms-javamail");
        installFeature("opennms-model");
        installFeature("opennms-poller-api");
        // OSGi dependency problems
        //installFeature("opennms-provisioning");
        installFeature("opennms-rrd-api");
        installFeature("opennms-rrd-jrobin");
        installFeature("opennms-snmp");

        installFeature("opennms-syslogd");
        // Syslog listeners can only be installed one at a time
        //installFeature("opennms-syslogd-listener-javanet");
        //installFeature("opennms-syslogd-listener-camel-netty");

        installFeature("opennms-trapd");

        installFeature("org.json");
        installFeature("postgresql");
        installFeature("spring-security32");
        installFeature("spring-webflow");

//        System.out.println(executeCommand("feature:list -i"));
    }
}
