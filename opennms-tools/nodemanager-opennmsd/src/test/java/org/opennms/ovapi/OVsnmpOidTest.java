package org.opennms.ovapi;

import junit.framework.TestCase;

import org.opennms.ovapi.OVsnmp.ObjectID;
import org.opennms.ovapi.OVsnmp.ObjectIDByReference;

import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public class OVsnmpOidTest extends TestCase {
    
    OVsnmp libovsnmp;
    
    public void setUp() {
        libovsnmp = OVsnmp.INSTANCE;
    }
    
    public void testCreateHard() {
        
        ObjectIDByReference pOid = new ObjectIDByReference();
        IntByReference pLen = new IntByReference();
        
        String expected = ".1.2.3.4.5.6";
        libovsnmp.OVsnmpOidFromStr(pOid, pLen, expected);
        
        ObjectID oid = pOid.getValue();
        int len = pLen.getValue();
        
        Memory mem = new Memory(1024);
        
        String actual = libovsnmp.OVsnmpOidToStr(oid, len, mem, 1024);
        
        assertEquals(expected, actual);
        
        libovsnmp.OVsnmpFree(oid.getPointer());
        
    }
    
    public void testCreateSimple() {
        
        String expected = ".1.2.3.4.5.6";
        OVObjectId oid = new OVObjectId(expected);
        
        assertEquals(expected, oid.toString());
        
        oid.free();
    }
    
    public void testAppend() {
        
        String first = ".1.2.3.4.5.6";
        String second = ".7.8.9.10.11.12";
        
        String result = ".1.2.3.4.5.6.7.8.9.10.11.12";
        
        OVObjectId oid1 = new OVObjectId(first);
        OVObjectId oid2 = new OVObjectId(second);
        
        oid1.append(oid2);
        
        assertEquals(result, oid1.toString());
        assertEquals(second, oid2.toString());
        
        oid1.free();
        oid2.free();
        
    }

    public void testConcat() {
        
        String first = ".1.2.3.4.5.6";
        String second = ".7.8.9.10.11.12";
        
        String result = ".1.2.3.4.5.6.7.8.9.10.11.12";
        
        OVObjectId oid1 = new OVObjectId(first);
        OVObjectId oid2 = new OVObjectId(second);
        
        OVObjectId oid3 = oid1.concat(oid2);
        
        assertEquals(first, oid1.toString());
        assertEquals(second, oid2.toString());
        assertEquals(result, oid3.toString());
        
        oid1.free();
        oid2.free();
        oid3.free();
        
    }
    
    
    public void testCopy() {
        String oidStr = ".1.2.3.4.5";
        
        OVObjectId oid1 = new OVObjectId(oidStr);
        
        OVObjectId oid2 = oid1.copy();
        
        oid1.free();
        
        assertEquals(oidStr, oid2.toString());
        
        oid2.free();
    }

}
