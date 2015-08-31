package org.opennms.netmgt.jasper.helper;


import org.junit.Assert;
import org.junit.Test;

public class MeasurementsHelperTest {

    @Test
    public void verifyGetNodeOrNodeSourceDescriptor() {
        Assert.assertEquals("node[null]", MeasurementsHelper.getNodeOrNodeSourceDescriptor(null, null, null));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "", ""));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", null, null));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "opennms.local", ""));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "", "201508240000"));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "opennms.local", null));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", null, "201508240000"));
        Assert.assertEquals("nodeSource[opennms.local:20150824000000]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "opennms.local", "20150824000000"));
    }
}
