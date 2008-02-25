package org.opennms.protocols.dns;

import java.io.IOException;

import junit.framework.TestCase;

public class DNSAddressRequestTest extends TestCase {
    private static final String question = "localhost"; // FIXME: "localhost."?

    private static final byte[] responseBytes = new byte[] {
            (byte) 0x5e,
            (byte) 0x94,
            (byte) 0x81,
            (byte) 0x82,
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x06,
            (byte) 0x67,
            (byte) 0x72,
            (byte) 0x65,
            (byte) 0x67,
            (byte) 0x6f,
            (byte) 0x72,
            (byte) 0x03,
            (byte) 0x63,
            (byte) 0x6f,
            (byte) 0x6d,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x01      
    };
    
    private DNSAddressRequest m_request = new DNSAddressRequest(question);

    // FIXME: This is hwere so we have at least one test so JUnit doesn't complain
    public void testBogus() {
    }

    // FIXME: This generates an IOException because the response is a server failed response; is this what we want?
    public void FIXMEtestServerFailed() throws IOException {
        m_request.m_reqID = 24212;
        m_request.verifyResponse(responseBytes, responseBytes.length);
    }
}
