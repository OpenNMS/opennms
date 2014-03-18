package org.opennms.netmgt.config.internal.collection;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.SnmpObjId;

public class TestDataCollectionGroup extends XmlTestNoCastor<DataCollectionGroup> {

    public TestDataCollectionGroup(final DataCollectionGroup sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getDell(),
                    TestDataCollectionGroup.class.getResource("datacollection/dell.xml")
                }
        });
    }

    private static DataCollectionGroup getDell() {
        final DataCollectionGroup group = new DataCollectionGroup("Dell");
        final ResourceType drsChassisIndex = new ResourceType();
        drsChassisIndex.setName("drsChassisIndex");
        drsChassisIndex.setLabel("Dell DRAC Chassis");
        drsChassisIndex.setResourceNameExpression(new Expression("${index}"));
        drsChassisIndex.setResourceLabelTemplate("${index}");
        group.addResourceType(drsChassisIndex);

        final ResourceType drsPSUIndex = new ResourceType("drsPSUIndex", "Dell DRAC PSU");
        drsPSUIndex.setResourceNameTemplate("${index}");
        drsPSUIndex.setResourceLabelTemplate("Chassis ${drsPSUChassisIndex} - ${drsPSULocation}");
        final Column drsPSUChassisIndex = new Column();
        drsPSUChassisIndex.setOid(SnmpObjId.get(".1.3.6.1.4.1.674.10892.2.4.2.1.1"));
        drsPSUChassisIndex.setAlias("drsPSUChassisIndex");
        drsPSUChassisIndex.setType("string");
        drsPSUIndex.addColumn(drsPSUChassisIndex);
        drsPSUIndex.addColumn(new Column(".1.3.6.1.4.1.674.10892.2.4.2.1.3", "drsPSULocation", "string"));
        group.addResourceType(drsPSUIndex);

        final ResourceType coolingDeviceIndex = new ResourceType("coolingDeviceIndex", "Dell Cooling Device");
        coolingDeviceIndex.setResourceNameTemplate("${index}");
        coolingDeviceIndex.setResourceLabelTemplate("${coolingDeviceLocationName}");
        coolingDeviceIndex.addColumn(new Column(".1.3.6.1.4.1.674.10892.1.700.12.1.8", "coolingDeviceLocationName", "string"));
        group.addResourceType(coolingDeviceIndex);

        final ResourceType temperatureProbeIndex = new ResourceType("temperatureProbeIndex", "Dell Temperature Probe");
        temperatureProbeIndex.setResourceNameTemplate("${index}");
        temperatureProbeIndex.setResourceLabelTemplate("${temperatureProbeLocationName}");
        temperatureProbeIndex.addColumn(new Column(".1.3.6.1.4.1.674.10892.1.700.20.1.8", "temperatureProbeLocationName", "string"));
        group.addResourceType(temperatureProbeIndex);

        final ResourceType powerUsageIndex = new ResourceType("powerUsageIndex", "Dell Power Usage");
        powerUsageIndex.setResourceNameTemplate("${index}");
        powerUsageIndex.setResourceLabelTemplate("${powerUsageEntityName}");
        powerUsageIndex.addColumn(".1.3.6.1.4.1.674.10892.1.600.60.1.6", "powerUsageEntityName", "string");
        group.addResourceType(powerUsageIndex);

        final Table openmanageCoolingdevices = new Table();
        openmanageCoolingdevices.setName("openmanage-coolingdevices");
        openmanageCoolingdevices.setInstance("coolingDeviceIndex");
        openmanageCoolingdevices.setIfType("all");
        openmanageCoolingdevices.addColumn(new Column(".1.3.6.1.4.1.674.10892.1.700.12.1.6", "coolingDevReading", "integer"));
        openmanageCoolingdevices.addColumn(".1.3.6.1.4.1.674.10892.1.700.12.1.8", "coolingDeviceLocationName", "string");
        openmanageCoolingdevices.addColumn(".1.3.6.1.4.1.674.10892.1.700.12.1.13", "coolDevLowCritThres", "integer");
        group.addTable(openmanageCoolingdevices);

        final Table openmanageTemperatureprobe = new Table("openmanage-temperatureprobe", "temperatureProbeIndex", "all");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.6", "tempProbeReading", "integer");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.8", "temperatureProbeLocationName", "string");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.10", "tempProbeUpCrit", "integer");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.11", "tempProbeUpNonCrit", "integer");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.12", "tempProbeLowNonCrit", "integer");
        openmanageTemperatureprobe.addColumn(".1.3.6.1.4.1.674.10892.1.700.20.1.13", "tempProbeLowCrit", "integer");
        group.addTable(openmanageTemperatureprobe);

        final Table powerusage = new Table("openmanage-powerusage", "powerUsageIndex", "all");
        powerusage.addColumn(".1.3.6.1.4.1.674.10892.1.600.60.1.6", "powerUsageEntityName", "string");
        powerusage.addColumn(".1.3.6.1.4.1.674.10892.1.600.60.1.7", "powerUsageWattage", "Counter32");
        powerusage.addColumn(".1.3.6.1.4.1.674.10892.1.600.60.1.9", "powerUsagePeakWatts", "integer");
        group.addTable(powerusage);

        final Table racChassis = new Table("dell-rac-chassis", "drsChassisIndex", "all");
        racChassis.addColumn(".1.3.6.1.4.1.674.10892.2.4.1.1.8", "drsWattsPeakUsage", "integer");
        racChassis.addColumn(".1.3.6.1.4.1.674.10892.2.4.1.1.10", "drsWattsMinUsage", "integer");
        racChassis.addColumn(".1.3.6.1.4.1.674.10892.2.4.1.1.13", "drsWattsReading", "integer");
        racChassis.addColumn(".1.3.6.1.4.1.674.10892.2.4.1.1.14", "drsAmpsReading", "integer");
        group.addTable(racChassis);

        final Table racPsu = new Table("dell-rac-psu", "drsPSUIndex", "all");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.1", "drsPSUChassisIndex", "string");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.3", "drsPSULocation", "string");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.5", "drsPSUVoltsReading", "integer");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.6", "drsPSUAmpsReading", "integer");
        racPsu.addColumn(".1.3.6.1.4.1.674.10892.2.4.2.1.7", "drsPSUWattsReading", "integer");
        group.addTable(racPsu);

        final SystemDef def = new SystemDef("DELL RAC");
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
