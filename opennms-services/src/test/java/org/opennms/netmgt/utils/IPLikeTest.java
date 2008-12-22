package org.opennms.netmgt.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.core.utils.IPLike;

public class IPLikeTest {

    @Test
    public void testCountChar() {
        assertEquals(2, IPLike.countChar('-', "test-this-please"));
        assertEquals(3, IPLike.countChar('-', "test-this-please-"));
        assertEquals(4, IPLike.countChar('-', "-test-this-please-"));
    }
    
    @Test
    public void testMatchRange() {
        assertTrue(IPLike.matchRange("192", "191-193"));
        assertTrue(IPLike.matchRange("192", "192"));
        assertTrue(IPLike.matchRange("192", "192-200"));
        assertTrue(IPLike.matchRange("192", "1-255"));
        assertTrue(IPLike.matchRange("192", "*"));
        assertFalse(IPLike.matchRange("192", "1-9"));
    }
    
    @Test
    public void testMatchOctet() {
        assertTrue(IPLike.matchNumericListOrRange("192", "191,192,193"));
        assertFalse(IPLike.matchNumericListOrRange("192", "190,191,194"));
        assertTrue(IPLike.matchNumericListOrRange("192", "10,172,190-193"));
        assertFalse(IPLike.matchNumericListOrRange("192", "10,172,193-199"));
        assertTrue(IPLike.matchNumericListOrRange("205", "200-300,400-500"));
        assertTrue(IPLike.matchNumericListOrRange("405", "200-300,400-500"));
        assertFalse(IPLike.matchNumericListOrRange("505", "200-300,400-500"));
    }
    
    @Test
    public void testVerifyIpMatch() {
        assertTrue(IPLike.matches("192.168.0.1", "*.*.*.*"));
        assertTrue(IPLike.matches("192.168.0.1", "192.*.*.*"));
        assertTrue(IPLike.matches("192.168.0.1", "*.168.*.*"));
        assertTrue(IPLike.matches("192.168.0.1", "*.*.0.*"));
        assertTrue(IPLike.matches("192.168.0.1", "*.*.*.1"));
        assertTrue(IPLike.matches("192.168.0.1", "*.*.*.0-7"));
        assertTrue(IPLike.matches("192.168.0.1", "192.168.0.0-7"));
        assertTrue(IPLike.matches("192.168.0.1", "192.166,167,168.*.0,1,5-10"));
        assertFalse(IPLike.matches("192.168.0.1", "10.0.0.1"));
        assertFalse(IPLike.matches("192.168.0.1", "*.168.*.2"));
        assertFalse(IPLike.matches("192.168.0.1", "10.168.0.1"));
        assertTrue(IPLike.matches("10.1.1.1", "10.1.1.1"));
        
    }
    

}
