package org.opennms.ovapi;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.opennms.nnm.swig.OVsnmpVal;

public class OVsnmpValTest extends TestCase {
    
    public void testCounter64() {

        OVsnmpVal val = new OVsnmpVal();
        
        val.setCounter64Value(BigInteger.valueOf(27));
        
        BigInteger twentySeven = val.getCounter64Value();
        assertEquals(27, twentySeven.intValue());

        long v = (long)(Integer.MAX_VALUE)+1L;

        val.setCounter64Value(BigInteger.valueOf(v));
        
        BigInteger actual = val.getCounter64Value();
        assertEquals("2147483648", actual.toString());
        assertEquals(v, actual.longValue());
        
        byte[] bytes = { (byte)0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };  
        
        BigInteger bv = new BigInteger(bytes);
        
        val.setCounter64Value(bv);
        
        BigInteger big = val.getCounter64Value();
        assertEquals(bv, big);
        
        
    }
    
    public void testInteger() {
        OVsnmpVal val = new OVsnmpVal();
        
        val.setIntValue(3);
        
        assertEquals(3, val.getIntValue());
    }
    
    public void testUnsigned32() {
        OVsnmpVal val = new OVsnmpVal();
        
        long v = ((long)Integer.MAX_VALUE)+100L;
        
        val.setUnsigned32Value(v);
        
        assertEquals(v, val.getUnsigned32Value());
    }
    
    public void testObjectId() {
        OVsnmpVal val = new OVsnmpVal();
        
        assertEquals(6, val.setObjectId(".1.2.3.4.5.6"));
        
        assertEquals(".1.2.3.4.5.6", val.getObjectId(6));
        
    }
    
    public void testOctetString() {
        
        String expected = "a display string";
        
        OVsnmpVal val = new OVsnmpVal();
        
        val.setOctetString(expected.getBytes());
        
        byte[] bytes = new byte[expected.getBytes().length];
        
        val.getOctetString(bytes);
        
        String actual = new String(bytes);
        
        assertEquals(expected, actual);
        
    }
    

}
