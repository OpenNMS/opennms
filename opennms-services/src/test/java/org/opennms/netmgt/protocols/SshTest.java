package org.opennms.netmgt.protocols;

import java.net.InetAddress;

import org.opennms.netmgt.protocols.ssh.Poll;

import junit.framework.TestCase;

public class SshTest extends TestCase {
    private static final String GOOD_HOST = "localhost";
    private static final String BAD_HOST = "asdfkjdflsjiaweofjiasodfjasdfoisadfoiasjdfoiasdjf.com";
    private static final int PORT = 22;
    private static final int TIMEOUT = 30000;
    
    public void testSsh() throws Exception {
        Poll p = new Poll();
        p.setPort(PORT);
        p.setTimeout(TIMEOUT);
        
        p.setAddress(InetAddress.getByName(GOOD_HOST));
        assertTrue(p.poll().isAvailable());

        p.setAddress(InetAddress.getByName(BAD_HOST));
        assertFalse(p.poll().isAvailable());
    }
}
