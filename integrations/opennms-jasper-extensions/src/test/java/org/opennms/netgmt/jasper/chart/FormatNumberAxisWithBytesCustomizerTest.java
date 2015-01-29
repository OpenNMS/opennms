package org.opennms.netgmt.jasper.chart;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.netmgt.jasper.chart.FormatNumberAxisWithBytesCustomizer.ByteFormat;

public class FormatNumberAxisWithBytesCustomizerTest {
    @Test
    public void canFormatBytes() {
        double megabyteInBytes = 1024 * 1024;
        double terabyteInBytes = megabyteInBytes * 1024 * 1024;
        double exabyteInBytes = terabyteInBytes * 1024 * 1024;

        ByteFormat formatter = new ByteFormat();
        assertEquals("1023B", formatter.format(1023));
        assertEquals("1KB", formatter.format(1024));
        assertEquals("1.001KB", formatter.format(1025));
        assertEquals("1MB", formatter.format(megabyteInBytes));
        assertEquals("1TB", formatter.format(terabyteInBytes));
        assertEquals("1PB", formatter.format(terabyteInBytes * 1024));
        assertEquals("1024PB", formatter.format(exabyteInBytes));
    }
}
