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
package org.opennms.netmgt.config.internal.collection;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.SnmpObjId;

public class DataCollectionGroupTest extends XmlTestNoCastor<DataCollectionGroupImpl> {

    public DataCollectionGroupTest(final DataCollectionGroupImpl sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getDell(),
                    DataCollectionGroupTest.class.getResource("datacollection/dell.xml")
                }
        });
    }

    private static DataCollectionGroupImpl getDell() {
        final DataCollectionGroupImpl group = new DataCollectionGroupImpl("Dell");
        final ResourceTypeImpl drsChassisIndex = new ResourceTypeImpl();
        drsChassisIndex.setName("drsChassisIndex");
        drsChassisIndex.setLabel("Dell DRAC Chassis");
        drsChassisIndex.setResourceNameExpression(new ExpressionImpl("${index}"));
        drsChassisIndex.setResourceLabelTemplate("${index}");
        group.addResourceType(drsChassisIndex);

        final ResourceTypeImpl drsPSUIndex = new ResourceTypeImpl("drsPSUIndex", "Dell DRAC PSU");
        drsPSUIndex.setResourceNameTemplate("${index}");
        drsPSUIndex.setResourceLabelTemplate("Chassis ${drsPSUChassisIndex} - ${drsPSULocation}");
        final ColumnImpl drsPSUChassisIndex = new ColumnImpl();
        drsPSUChassisIndex.setOid(SnmpObjId.get(".1.3.6.1.4.1.674.10892.2.4.2.1.1"));
        drsPSUChassisIndex.setAlias("drsPSUChassisIndex");
        drsPSUChassisIndex.setType("string");
        drsPSUIndex.addColumn(drsPSUChassisIndex);
        drsPSUIndex.addColumn(new ColumnImpl(".1.3.6.1.4.1.674.10892.2.4.2.1.3", "drsPSULocation", "string"));
        group.addResourceType(drsPSUIndex);

        final ResourceTypeImpl coolingDeviceIndex = new ResourceTypeImpl("coolingDeviceIndex", "Dell Cooling Device");
        coolingDeviceIndex.setResourceNameTemplate("${index}");
        coolingDeviceIndex.setResourceLabelTemplate("${coolingDeviceLocationName}");
        coolingDeviceIndex.addColumn(new ColumnImpl(".1.3.6.1.4.1.674.10892.1.700.12.1.8", "coolingDeviceLocationName", "string"));
        group.addResourceType(coolingDeviceIndex);

        final ResourceTypeImpl temperatureProbeIndex = new ResourceTypeImpl("temperatureProbeIndex", "Dell Temperature Probe");
        temperatureProbeIndex.setResourceNameTemplate("${index}");
        temperatureProbeIndex.setResourceLabelTemplate("${temperatureProbeLocationName}");
        temperatureProbeIndex.addColumn(new ColumnImpl(".1.3.6.1.4.1.674.10892.1.700.20.1.8", "temperatureProbeLocationName", "string"));
        group.addResourceType(temperatureProbeIndex);

        final ResourceTypeImpl powerUsageIndex = new ResourceTypeImpl("powerUsageIndex", "Dell Power Usage");
        powerUsageIndex.setResourceNameTemplate("${index}");
        powerUsageIndex.setResourceLabelTemplate("${powerUsageEntityName}");
        powerUsageIndex.addColumn(".1.3.6.1.4.1.674.10892.1.600.60.1.6", "powerUsageEntityName", "string");
        group.addResourceType(powerUsageIndex);

        final TableImpl openmanageCoolingdevices = new TableImpl();
        openmanageCoolingdevices.setName("openmanage-coolingdevices");
        openmanageCoolingdevices.setInstance("coolingDeviceIndex");
        openmanageCoolingdevices.addColumn(new ColumnImpl(".1.3.6.1.4.1.674.10892.1.700.12.1.6", "coolingDevReading", "integer"));
        openmanageCoolingdevices.addColumn(".1.3.6.1.4.1.674.10892.1.700.12.1.8", "coolingDeviceLocationName", "string");
        openmanageCoolingdevices.addColumn(".1.3.6.1.4.1.674.10892.1.700.12.1.13", "coolDevLowCritThres", "integer");
        group.addTable(openmanageCoolingdevices);

        final TableImpl openmanageTemperatureprobe = new TableImpl("openmanage-temperatureprobe", "temperatureProbeIndex");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.6", "tempProbeReading", "integer");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.8", "temperatureProbeLocationName", "string");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.10", "tempProbeUpCrit", "integer");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.11", "tempProbeUpNonCrit", "integer");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.12", "tempProbeLowNonCrit", "integer");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.13", "tempProbeLowCrit", "integer");
        group.addTable(openmanageTemperatureprobe);

        final TableImpl powerusage = new TableImpl("openmanage-powerusage", "powerUsageIndex");
        powerusage.addColumn(".1.3.6.1.4.1.674.10892.1.600.60.1.6", "powerUsageEntityName", "string");
        powerusage.addColumn(".1.3.6.1.4.1.674.10892.1.600.60.1.7", "powerUsageWattage", "Counter32");
        powerusage.addColumn(".1.3.6.1.4.1.674.10892.1.600.60.1.9", "powerUsagePeakWatts", "integer");
        group.addTable(powerusage);

        final TableImpl racChassis = new TableImpl("dell-rac-chassis", "drsChassisIndex");
        racChassis.addColumn(".1.3.6.1.4.1.674.10892.2.4.1.1.8", "drsWattsPeakUsage", "integer");
        racChassis.addColumn(".1.3.6.1.4.1.674.10892.2.4.1.1.10", "drsWattsMinUsage", "integer");
        racChassis.addColumn(".1.3.6.1.4.1.674.10892.2.4.1.1.13", "drsWattsReading", "integer");
        racChassis.addColumn(".1.3.6.1.4.1.674.10892.2.4.1.1.14", "drsAmpsReading", "integer");
        group.addTable(racChassis);

        final TableImpl racPsu = new TableImpl("dell-rac-psu", "drsPSUIndex");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.1", "drsPSUChassisIndex", "string");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.3", "drsPSULocation", "string");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.5", "drsPSUVoltsReading", "integer");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.6", "drsPSUAmpsReading", "integer");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.7", "drsPSUWattsReading", "integer");
        group.addTable(racPsu);

        final SystemDefImpl def = new SystemDefImpl("DELL RAC");
        def.setSysoid(".1.3.6.1.4.1.674.10892.2");
        def.setIncludes(new String[] {
                "mib2-interfaces",
                "mib2-tcp",
                "mib2-icmp",
                "dell-rac-chassis",
                "dell-rac-psu"
        });
        group.addSystemDef(def);
        
        return group;
    }


}
