package org.opennms.netmgt.config.internal.collection;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class TestDataCollectionConfig extends XmlTestNoCastor<DataCollectionConfig> {

    public TestDataCollectionConfig(final DataCollectionConfig sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getDataCollectionObject(),
                    TestDataCollectionConfig.class.getResource("datacollection-config.xml")
                }
        });
    }

    private static DataCollectionConfig getDataCollectionObject() {
        final DataCollectionConfig config = new DataCollectionConfig();

        final SnmpCollection collection = new SnmpCollection("default", "select");
        config.addSnmpCollection(collection);

        final Rrd rrd = new Rrd(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        rrd.addRra("RRA:AVERAGE:0.5:12:1488");
        rrd.addRra("RRA:AVERAGE:0.5:288:366");
        rrd.addRra("RRA:MAX:0.5:288:366");
        rrd.addRra("RRA:MIN:0.5:288:366");
        collection.setRrd(rrd);

        collection.addIncludedGroup("MIB2");
        collection.addIncludedGroup("Net-SNMP");

        return config;
    }

}
