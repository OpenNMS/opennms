package org.opennms.netmgt.config.internal.collection;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DataCollectionConfigTest extends XmlTestNoCastor<DataCollectionConfigImpl> {

    public DataCollectionConfigTest(final DataCollectionConfigImpl sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getDataCollectionObject(),
                    DataCollectionConfigTest.class.getResource("datacollection-config.xml")
                },
                {
                    getOnefileObject(),
                    DataCollectionConfigTest.class.getResource("datacollection-config-onefile.xml")
                }
        });
    }

    private static DataCollectionConfigImpl getOnefileObject() {
        final DataCollectionConfigImpl config = new DataCollectionConfigImpl();
        final SnmpCollectionImpl collection = new SnmpCollectionImpl("default");
        config.addSnmpCollection(collection);

        collection.addIncludedGroup("MIB2");
        collection.addIncludedGroup("Net-SNMP");
        
        final DataCollectionGroupImpl configGroup = new DataCollectionGroupImpl("Dell");
        collection.addDataCollectionGroup(configGroup);
        
        final ResourceTypeImpl resourceType = new ResourceTypeImpl("drsChassisIndex", "Dell DRAC Chassis");
        resourceType.setResourceNameTemplate("${index}");
        resourceType.setResourceLabelTemplate("${index}");
        configGroup.addResourceType(resourceType);
        
        final TableImpl table = new TableImpl("openmanage-coolingdevices", "coolingDeviceIndex");
        table.addColumn(".1.3.6.1.4.1.674.10892.1.700.12.1.6", "coolingDevReading", "integer");
        table.addColumn(".1.3.6.1.4.1.674.10892.1.700.12.1.8", "coolingDeviceLocationName", "string");
        table.addColumn(".1.3.6.1.4.1.674.10892.1.700.12.1.13", "coolDevLowCritThres", "integer");
        configGroup.addTable(table);

        final GroupImpl group = new GroupImpl("mib2-coffee-rfc2325");
        group.addMibObject(new MibObjectImpl(".1.3.6.1.2.1.10.132.2", "0", "coffeePotCapacity", "integer"));
        group.addMibObject(new MibObjectImpl(".1.3.6.1.2.1.10.132.4.1.2", "0", "coffeePotLevel", "integer"));
        group.addMibObject(new MibObjectImpl(".1.3.6.1.2.1.10.132.4.1.6", "0", "coffeePotTemp", "integer"));
        configGroup.addGroup(group);
        
        final SystemDefImpl def = new SystemDefImpl("DELL RAC");
        def.setSysoid(".1.3.6.1.4.1.674.10892.2");
        def.setIncludes(new String[] {
                "mib2-interfaces",
                "mib2-tcp",
                "mib2-icmp",
                "dell-rac-chassis",
                "dell-rac-psu"
        });
        configGroup.addSystemDef(def);

        return config;
    }

    private static DataCollectionConfigImpl getDataCollectionObject() {
        final DataCollectionConfigImpl config = new DataCollectionConfigImpl();

        final SnmpCollectionImpl collection = new SnmpCollectionImpl("default");
        config.addSnmpCollection(collection);

        collection.addIncludedGroup("MIB2");
        collection.addIncludedGroup("Net-SNMP");

        return config;
    }

}
