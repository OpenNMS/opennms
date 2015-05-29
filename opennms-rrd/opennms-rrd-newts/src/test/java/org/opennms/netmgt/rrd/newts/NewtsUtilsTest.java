package org.opennms.netmgt.rrd.newts;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-empty.xml",
})
@JUnitConfigurationEnvironment
public class NewtsUtilsTest {

    private String opennmsHome;

    @Before
    public void setUp() {
        opennmsHome = System.getProperty("opennms.home");
    }

    @Test
    public void canConvertFilenameToResource() {
        Resource resource = NewtsUtils.getResourceFromPath(opennmsHome + "/share/rrd/snmp/1/loadavg1.newts");
        assertEquals("snmp:1:loadavg1", resource.getId());

        // What if there's already a ':' in the filename?
        resource = NewtsUtils.getResourceFromPath(opennmsHome + "/share/rrd/snmp/1/load:avg1.newts");
        assertEquals("snmp:1:loadavg1", resource.getId());

        resource = NewtsUtils.getResourceFromPath(opennmsHome + "/share/rrd/snmp/1/eth0-04013f75f101/ifInOctets.newts");
        assertEquals("snmp:1:eth0-04013f75f101:ifInOctets", resource.getId());
    }

    @Test
    public void canConvertRrdUpdateStringToSampleSet() {
        RrdDataSource ds1 = new RrdDataSource("x", "GAUGE", 900, "0", "100");
        RrdDataSource ds2 = new RrdDataSource("y", "GAUGE", 900, "0", "100");
        RrdDef def = new RrdDef(opennmsHome + "/share/rrd/snmp/1", "loadavg1", Lists.newArrayList(ds1, ds2));
        List<Sample> samples = NewtsUtils.getSamplesFromRrdUpdateString(def, "1:U:9", null);
        assertEquals(2, samples.size());
    }
}
