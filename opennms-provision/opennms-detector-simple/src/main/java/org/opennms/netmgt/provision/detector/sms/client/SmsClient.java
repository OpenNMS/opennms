/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 5, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.sms.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.provision.detector.sms.response.SmsResponse;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.support.Client;

/**
 * Detector client implemenation for the <code>SmsDetector</code>
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class SmsClient implements Client<LineOrientedRequest, SmsResponse> {
    
    private String m_address;
    private boolean m_isSupported = false;
    
    public void close() {
        
    }

    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        m_address = address.getHostAddress();
    }

    public SmsResponse receiveBanner() throws IOException, Exception {
        return receiveResponse();
    }

    public SmsResponse sendRequest(LineOrientedRequest request) throws IOException, Exception {
        return null;
    }
    
    private SmsResponse receiveResponse() {
        SmsResponse smsResponse = new SmsResponse();
        return smsResponse;
    }

    public void setSupported(boolean isSupported) {
        m_isSupported = isSupported;
    }

    public boolean isSupported() {
        return m_isSupported;
    }

}
