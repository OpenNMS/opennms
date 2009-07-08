/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.protocols.dns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DNSAddressRequestTest {
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
    
    private DNSAddressRequest m_request;
    
    @Before
    public void setUp() throws UnknownHostException {
        final String question = InetAddress.getLocalHost().getCanonicalHostName();
        m_request = new DNSAddressRequest(question);
    }

    @Test(expected=IOException.class)
    public void testServerFailed() throws IOException {
        m_request.m_reqID = 24212;
        // we pass in a server failed request... expect it to throw an exception
        m_request.verifyResponse(responseBytes, responseBytes.length);
    }
}
