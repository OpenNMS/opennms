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
    public void testMatchRangeHex() {
        // Test a bunch of case-sensitivity cases
        assertTrue(IPLike.matchRangeHex("C0", "BF-C1"));
        assertTrue(IPLike.matchRangeHex("c0", "BF-C1"));
        assertTrue(IPLike.matchRangeHex("C0", "bf-C1"));
        assertTrue(IPLike.matchRangeHex("C0", "BF-c1"));
        assertTrue(IPLike.matchRangeHex("C0", "bF-c1"));
        assertTrue(IPLike.matchRangeHex("C0", "Bf-c1"));
        assertTrue(IPLike.matchRangeHex("C0", "bf-c1"));
        assertTrue(IPLike.matchRangeHex("c0", "bf-c1"));

        assertTrue(IPLike.matchRangeHex("C0", "C0"));
        assertTrue(IPLike.matchRangeHex("c0", "C0"));
        assertTrue(IPLike.matchRangeHex("C0", "c0"));
        assertTrue(IPLike.matchRangeHex("c0", "c0"));

        assertTrue(IPLike.matchRangeHex("C0", "C0-C8"));
        assertTrue(IPLike.matchRangeHex("C0", "B4-C0"));

        assertTrue(IPLike.matchRangeHex("c0", "01-FF"));
        assertTrue(IPLike.matchRangeHex("c0", "*"));
        assertFalse(IPLike.matchRangeHex("c0", "01-09"));
        assertFalse(IPLike.matchRangeHex("c0", "1-9"));
        assertTrue(IPLike.matchRangeHex("5", "1-9"));
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

        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "*:*:*:*:*:*:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "*:*:*:*:*:*:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "*:*:*:*:*:*:*:*%4"));
        assertFalse(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "*:*:*:*:*:*:*:*%4"));

        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "fe80:*:*:*:*:*:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe80:*:*:*:*:*:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe80:*:*:*:*:*:*:*%4"));
        assertFalse(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "fe80:*:*:*:*:*:*:*%4"));

        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "*:*:*:0:*:*:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "*:*:*:0:*:*:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "*:*:*:0:*:*:*:*%4"));
        assertFalse(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "*:*:*:0:*:*:*:*%4"));

        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "*:*:*:*:*:bbbb:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "*:*:*:*:*:bbbb:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "*:*:*:*:*:bbbb:*:*%4"));
        assertFalse(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "*:*:*:*:*:bbbb:*:*%4"));

        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "*:*:*:*:*:bbb0-bbbf:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "*:*:*:*:*:bbb0-bbbf:*:*"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "*:*:*:*:*:bbb0-bbbf:*:*%4"));
        assertFalse(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "*:*:*:*:*:bbb0-bbbf:*:*%4"));

        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd%4"));
        assertFalse(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd%4"));

        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd%4"));
        assertFalse(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd%4"));

        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd"));
        assertTrue(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4"));
        assertFalse(IPLike.matches("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd", "fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4"));
    }

}
