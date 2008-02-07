package org.opennms.ovapi;

import junit.framework.TestCase;

import org.opennms.ovapi.OVsnmp.OVuint64;

public class OVuint64Test extends TestCase {
    
    public void testCreate() {
        OVuint64 val = new OVuint64(27);
        
        assertEquals("27", val.toString());
    }
    
    public void testAdd() {
        OVuint64 val1 = new OVuint64(27);
        OVuint64 val2 = new OVuint64(72);
        
        OVuint64 result = val1.add(val2);
        
        assertEquals("99", result.toString());
        
        print(val1);
        print(val2);
        print(result);
        
    }
    
    private void print(OVuint64 v) {
        System.err.println(v.toNativeValue());
    }

    public void testSubtract() {
        OVuint64 val1 = new OVuint64(72);
        OVuint64 val2 = new OVuint64(27);
        
        OVuint64 result = val1.subtract(val2);
        
        assertEquals("45", result.toString());
    }
    
    public void testShift() {
        OVuint64 val = new OVuint64(4);
        
        OVuint64 shiftLeft1 = val.shiftLeft(1);
        OVuint64 shiftRight1 = val.shiftRight(1);
        
        assertEquals("8", shiftLeft1.toString());
        assertEquals("2", shiftRight1.toString());
    }
    
    public void testFromLong() {
        long v = (long)(Integer.MAX_VALUE)+1L;
        OVuint64 val = new OVuint64(v);
        
        assertEquals("2147483648", val.toString());
        
        long allBits = 0xFFFFFFFFFFFFFFFFL;
        OVuint64 big = new OVuint64(allBits);
        
        assertEquals("0xFFFFFFFFFFFFFFFF", big.toHexString());
        
        print(val);
        print(big);

        assertEquals(v, val.longValue());
        assertEquals(allBits, big.longValue());
    }

}
