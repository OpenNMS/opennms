package org.opennms.netmgt.protocols;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.protocols.ssh.Poll;

import junit.framework.TestCase;

public class SshTest extends TestCase {
    private static final String GOOD_HOST = "127.0.0.1";
    private static final String BAD_HOST = "1.1.1.1";
    private static final int PORT = 22;
    private static final int TIMEOUT = 30000;

    Poll p;
    InetAddress good, bad;
    
    public void setUp() throws Exception {
        p = new Poll();
        p.setPort(PORT);
        p.setTimeout(TIMEOUT);

        try {
            good = InetAddress.getByName(GOOD_HOST);
            bad  = InetAddress.getByName(BAD_HOST);
        } catch (UnknownHostException e) {
            throw e;
        }
    }
    
    public void testSshGoodHost() throws Exception {
        p.setAddress(good);
        assertTrue(p.poll().isAvailable());
    }
    
    public void testSshBadHost() throws Exception {
        p.setAddress(bad);
        assertFalse(p.poll().isAvailable());
    }
}
