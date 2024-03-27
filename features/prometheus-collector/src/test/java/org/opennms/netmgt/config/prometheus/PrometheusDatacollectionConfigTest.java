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
