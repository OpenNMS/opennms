/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.prometheus;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class PrometheusDatacollectionConfigTest extends XmlTestNoCastor<PrometheusDatacollectionConfig> {

    public PrometheusDatacollectionConfigTest(PrometheusDatacollectionConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/prometheus-datacollection.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getPrometheusDatacollectionConfig(),
                    new File("src/test/resources/prometheus-datacollection-config.xml")
                }
        });
    }

    private static PrometheusDatacollectionConfig getPrometheusDatacollectionConfig() {
        PrometheusDatacollectionConfig config = new PrometheusDatacollectionConfig();
        config.setRrdRepository("/opt/opennms/share/rrd/snmp/");

        org.opennms.netmgt.config.prometheus.Collection collection = new org.opennms.netmgt.config.prometheus.Collection();
        collection.setName("default");

        Rrd rrd = new Rrd();
        rrd.setStep(30);
        rrd.getRra().add("RRA:AVERAGE:0.5:1:2016");
        rrd.getRra().add("RRA:AVERAGE:0.5:12:1488");
        collection.setRrd(rrd);

        collection.getGroupRef().add("node-exporter-filesystems");
        config.getCollection().add(collection);

        Group nodeExporterCpu = new Group();
        nodeExporterCpu.setName("node-exporter-filesystems");
        nodeExporterCpu.setFilterExp("name matches 'node_filesystem_.*' and labels[mountpoint] matches '.*home'");
        nodeExporterCpu.setGroupByExp("labels[mountpoint]");
        nodeExporterCpu.setResourceType("nodeExporterFilesytem");
        
        NumericAttribute attribute = new NumericAttribute();
        attribute.setAliasExp("labels[mode]");
        nodeExporterCpu.getNumericAttribute().add(attribute);

        config.getGroup().add(nodeExporterCpu);

        return config;
    }
}
