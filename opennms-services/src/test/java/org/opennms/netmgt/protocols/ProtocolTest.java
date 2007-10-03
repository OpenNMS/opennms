package org.opennms.netmgt.protocols;

import java.net.InetAddress;

import junit.framework.TestCase;

public class ProtocolTest extends TestCase {
    private static final String GOOD_HOST = "localhost";
    private static final String BAD_HOST = "asdfkjdflsjiaweofjiasodfjasdfoisadfoiasjdfoiasdjf.com";
    private static final int PORT = 22;
    private static final int TIMEOUT = 30000;
    
    public void testSSH() throws Exception {
        Protocol p = new SSH();
        assertTrue(p.exists(InetAddress.getByName(GOOD_HOST), PORT, TIMEOUT));
        assertFalse(p.exists(InetAddress.getByName(BAD_HOST), PORT, TIMEOUT));
    }
}
