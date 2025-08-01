/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.monitors.support;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.monitors.support.DNSAddressRR;
import org.opennms.netmgt.poller.monitors.support.DNSAddressRequest;

public class DNSAddressRequestTest extends TestCase {

    private static final byte[] normalResponseBytes = new byte[] {
        (byte) 0x9e, (byte) 0xf2, (byte) 0x81, (byte) 0x80,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x09, (byte) 0x6c, (byte) 0x6f, (byte) 0x63,
        (byte) 0x61, (byte) 0x6c, (byte) 0x68, (byte) 0x6f,
        (byte) 0x73, (byte) 0x74, (byte) 0x00, (byte) 0x00,
        (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0xc0,
        (byte) 0x0c, (byte) 0x00, (byte) 0x01, (byte) 0x00,
        (byte) 0x01, (byte) 0x00, (byte) 0x0a, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x7f,
        (byte) 0x00, (byte) 0x00, (byte) 0x01
    };
    
    private static final byte[] servFailResponseBytes = new byte[] {
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
    
    private static final byte[] nxDomainResponseBytes = new byte[] {
        (byte) 0x2f, (byte) 0x3e, (byte) 0x81, (byte) 0x83,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
        (byte) 0x09, (byte) 0x6c, (byte) 0x6f, (byte) 0x63,
        (byte) 0x61, (byte) 0x6c, (byte) 0x68, (byte) 0x6f,
        (byte) 0x73, (byte) 0x74, (byte) 0x00, (byte) 0x00,
        (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
        (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01,
        (byte) 0x00, (byte) 0x01, (byte) 0x3f, (byte) 0x0a,
        (byte) 0x00, (byte) 0x40, (byte) 0x01, (byte) 0x41,
        (byte) 0x0c, (byte) 0x52, (byte) 0x4f, (byte) 0x4f,
        (byte) 0x54, (byte) 0x2d, (byte) 0x53, (byte) 0x45,
        (byte) 0x52, (byte) 0x56, (byte) 0x45, (byte) 0x52,
        (byte) 0x53, (byte) 0x03, (byte) 0x4e, (byte) 0x45,
        (byte) 0x54, (byte) 0x00, (byte) 0x05, (byte) 0x4e,
        (byte) 0x53, (byte) 0x54, (byte) 0x4c, (byte) 0x44,
        (byte) 0x0c, (byte) 0x56, (byte) 0x45, (byte) 0x52,
        (byte) 0x49, (byte) 0x53, (byte) 0x49, (byte) 0x47,
        (byte) 0x4e, (byte) 0x2d, (byte) 0x47, (byte) 0x52,
        (byte) 0x53, (byte) 0x03, (byte) 0x43, (byte) 0x4f,
        (byte) 0x4d, (byte) 0x00, (byte) 0x77, (byte) 0xce,
        (byte) 0x7e, (byte) 0xe0, (byte) 0x00, (byte) 0x00,
        (byte) 0x07, (byte) 0x08, (byte) 0x00, (byte) 0x00,
        (byte) 0x03, (byte) 0x84, (byte) 0x00, (byte) 0x09,
        (byte) 0x3a, (byte) 0x80, (byte) 0x00, (byte) 0x01,
        (byte) 0x51, (byte) 0x80
    };
    
    private DNSAddressRequest m_request;

    @Before
    @Override
    public void setUp() throws UnknownHostException {
        final String question = InetAddress.getLocalHost().getCanonicalHostName();
        m_request = new DNSAddressRequest(question);
    }

    @Test
    public void testNormalResponse() throws Exception {
        m_request.m_reqID = 0x9ef2;
        m_request.verifyResponse(normalResponseBytes, normalResponseBytes.length);
        
        m_request.receiveResponse(normalResponseBytes, normalResponseBytes.length);
        assertEquals(1, m_request.getAnswers().size());
        DNSAddressRR answer = (DNSAddressRR) m_request.getAnswers().get(0);
        assertEquals(4, answer.getAddress().length);
        assertEquals(127, answer.getAddress()[0]);
        assertEquals(0, answer.getAddress()[1]);
        assertEquals(0, answer.getAddress()[2]);
        assertEquals(1, answer.getAddress()[3]);
    }
    
    @Test
    public void testServerFailed() throws Exception {
        m_request.m_reqID = 24212;
        try {
            m_request.verifyResponse(servFailResponseBytes, servFailResponseBytes.length);            
        } catch (IOException ioe) {
            assertEquals("Server Failure (2)", ioe.getMessage());
            return;
        }
        throw new Exception("Should have caught an IOException for ServFail!");
    }
    
    @Test
    public void testNxDomainPass() throws Exception {
        List<Integer> fatalCodes = new ArrayList<>();
        fatalCodes.add(2);
        fatalCodes.add(3);
        m_request.m_reqID = 0x2f3e;
        m_request.setFatalResponseCodes(fatalCodes);
        try {
            m_request.verifyResponse(nxDomainResponseBytes, nxDomainResponseBytes.length);
        } catch (IOException ioe) {
            assertEquals("Non-Existent Domain (3)", ioe.getMessage());
            return;
        }
        throw new Exception("Should have caught an IOException for NXDomain!");
    }
}
