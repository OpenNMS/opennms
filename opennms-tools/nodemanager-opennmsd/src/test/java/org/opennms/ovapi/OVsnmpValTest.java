/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
