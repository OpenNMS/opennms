/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.springframework.core.io.FileSystemResource;

public class CollectDConfigTest {
    List<String> fullyConfiguredcollectorNames = Arrays.asList(
            "Elasticsearch",
            "PostgreSQL",
            "SNMP",
            "WMI",
            "WS-Man",
            "OpenNMS-JVM",
            "JMX-Minion",
            "JMX-Kafka");
    @Test
    public void testCollectDConfiguration() {
        final CollectdConfiguration config = JaxbUtils.unmarshal(CollectdConfiguration.class,
                new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile("collectd-configuration.xml")));
        assertTrue("Preconfigured packages should be configured", config.getPackages().size()>0);
        List<Collector> collectors = config.getCollectors();
        assertTrue("Preconfigured collectors should be configured", collectors.size() > 0);
        for (String name: fullyConfiguredcollectorNames) {
            verifyCollectors(collectors, name);
        }
        Package samplePackage = config.getPackage("example1");
        assertNotNull("Example package ", samplePackage);

        System.out.println(samplePackage.getServices().size());

        for (String name : fullyConfiguredcollectorNames){
            verifyPackageService(samplePackage, name);
        }
    }

    private void verifyCollectors(List<Collector> collectors, String svcName) {
        Collector collector = collectors.stream().filter(c->c.getService().equals(svcName)).findFirst().orElse(null);
        assertNotNull("The collector " + svcName +" should be configured", collector);
    }

    private void verifyPackageService(Package pkg, String serviceName) {
        System.out.println(serviceName);
        assertNotNull("The service " + serviceName + "in package " + pkg.getName() + " should be configured", pkg.getService(serviceName));
    }
}
