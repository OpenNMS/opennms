package org.opennms.web;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class JsStringConvertTest {
    
    @Test
    public void testJsStringConversion(){
        String convertStr = "a\\\"b\\tz\\n";
        
        String convertedStr = Util.convertToJsSafeString(convertStr);
        
        assertTrue("a\\\\\\\"b\\\\tz\\\\n".equals(convertedStr));
    }
}
