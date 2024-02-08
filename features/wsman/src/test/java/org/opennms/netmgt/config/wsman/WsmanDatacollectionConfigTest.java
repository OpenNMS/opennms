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
package org.opennms.netmgt.config.wsman;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.wsman.WSManConstants;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.wsman.Collection.IncludeAllSystemDefinitions;

public class WsmanDatacollectionConfigTest extends XmlTestNoCastor<WsmanDatacollectionConfig> {

    public WsmanDatacollectionConfigTest(WsmanDatacollectionConfig sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getWsmanDatacollectionConfig(),
                    new File("src/test/resources/wsman-datacollection-config.xml"),
                    null
                }
        });
    }

    private static WsmanDatacollectionConfig getWsmanDatacollectionConfig() {
        WsmanDatacollectionConfig wsmanDatacollectionConfig = new WsmanDatacollectionConfig();
        wsmanDatacollectionConfig.setRrdRepository("${install.share.dir}/rrd/snmp/");
        org.opennms.netmgt.config.wsman.Collection collection = new org.opennms.netmgt.config.wsman.Collection();
        collection.setName("default");

        Rrd rrd = new Rrd();
        rrd.setStep(30);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        rrd.addRra("RRA:AVERAGE:0.5:12:1488");
        collection.setRrd(rrd);

        collection.setIncludeAllSystemDefinitions(new IncludeAllSystemDefinitions());
        wsmanDatacollectionConfig.addCollection(collection);

        // Setup the drac-power-supply group
        Group group = new Group();
        group.setName("drac-power-supply");
        group.setResourceUri("http://schemas.dell.com/wbem/wscim/1/cim-schema/2/DCIM_ComputerSystem/DCIM_PowerSupplyView");
        group.setResourceType("node");

        Attrib attr = new Attrib();
        attr.setName("TotalOutputPower");
        attr.setAlias("dracOutputPower");
        attr.setType(AttributeType.GAUGE);
        group.addAttrib(attr);

        attr = new Attrib();
        attr.setName("InputVoltage");
        attr.setAlias("dracInputVoltage");
        attr.setType(AttributeType.GAUGE);
        group.addAttrib(attr);

        attr = new Attrib();
        attr.setName("OtherIdentifyingInfo");
        attr.setAlias("serviceTag");
        attr.setType(AttributeType.STRING);
        attr.setIndexOf("#IdentifyingDescriptions matches '.*ServiceTag'");
        group.addAttrib(attr);
        wsmanDatacollectionConfig.addGroup(group);

        // Setup the filtered-drac-power-supply group
        group = new Group();
        group.setName("filtered-drac-power-supply");
        group.setResourceUri(WSManConstants.CIM_ALL_AVAILABLE_CLASSES);
        group.setDialect(WSManConstants.XML_NS_WQL_DIALECT);
        group.setFilter("select Range1MaxInputPower from DCIM_PowerSupplyView where DetailedState != 'Absent' and PrimaryStatus != 0");
        group.setResourceType("node");

        attr = new Attrib();
        attr.setName("Range1MaxInputPower");
        attr.setAlias("dracRangeInputPower");
        attr.setType(AttributeType.GAUGE);
        group.addAttrib(attr);
        wsmanDatacollectionConfig.addGroup(group);

        // Setup the system definition
        SystemDefinition sysDef = new SystemDefinition();
        sysDef.setName("Dell iDRAC 6");
        sysDef.addRule("productVendor matches '^Dell.*' and productVersion matches '^6.*'");
        sysDef.addIncludeGroup("drac-power-supply");
        sysDef.addIncludeGroup("filtered-drac-power-supply");

        wsmanDatacollectionConfig.addSystemDefinition(sysDef);

        return wsmanDatacollectionConfig;
    }
}
