package org.opennms.netmgt.jmx;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JmxUtilsTest {

    @Test
    public void testConvert() {
        Map<String, Object> input = new HashMap<>();
        input.put("1", "1 Value");
        input.put("2", "2 Value");
        input.put("3", 3);

        Map<String, String> output = JmxUtils.convertToStringMap(input);
        Assert.assertNotNull(output);
        Assert.assertEquals(2, output.size());

        Assert.assertEquals("1 Value", output.get("1"));
        Assert.assertEquals("2 Value", output.get("2"));
        Assert.assertNull(output.get("3"));
    }

    @Test
    public void testNotModifiable() {
        Map<String, Object> input = new HashMap<>();
        input.put("A", "VALUE");

        Map<String, String> output = JmxUtils.convertToStringMap(input);

        try {
            output.put("4", "4 Value");
            Assert.fail("The converted output map should not be modifiable");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void testNullInput() {
        Map<String, String> output = JmxUtils.convertToStringMap(null);
        Assert.assertNull(output);
    }
}
