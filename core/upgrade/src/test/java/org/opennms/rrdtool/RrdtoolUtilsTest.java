package org.opennms.rrdtool;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.rrd.model.RrdParseUtils;
import org.opennms.netmgt.rrd.model.v3.RRDv3;

public class RrdtoolUtilsTest {
    
    @Test
    public void testDumpAndRestore() throws Exception {
        System.setProperty("rrd.binary", "/opt/local/bin/rrdtool");
        RRDv3 rrd = RrdParseUtils.dumpRrd(new File("/Users/agalue/Development/opennms/git/experiments/tempA.rrd"));
        Assert.assertNotNull(rrd);
        RrdParseUtils.restoreRrd(rrd, new File("/Users/agalue/Development/opennms/git/experiments/tempA-restored.rrd"));
    }

}
