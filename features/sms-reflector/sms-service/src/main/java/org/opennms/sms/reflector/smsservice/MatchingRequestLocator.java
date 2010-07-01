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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.sms.reflector.smsservice;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.protocols.rt.RequestLocator;

/**
 * MatchingRequestLocator
 *
 * @author brozow
 * @version $Id: $
 */
public class MatchingRequestLocator implements RequestLocator<MobileMsgRequest, MobileMsgResponse> {
    
    private final Set<MobileMsgRequest> m_requests = new CopyOnWriteArraySet<MobileMsgRequest>();

    /**
     * <p>trackRequest</p>
     *
     * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     * @return a boolean.
     */
    public boolean trackRequest(MobileMsgRequest request) {
        m_requests.add(request);
        return true;
    }

    /**
     * <p>locateMatchingRequest</p>
     *
     * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
     * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     */
    public MobileMsgRequest locateMatchingRequest(MobileMsgResponse response) {
        for(MobileMsgRequest request : m_requests) {
            
            if (request.matches(response)) {
                return request;
            }
        }   
            
        return null;
    }
    
    /**
     * <p>requestTimedOut</p>
     *
     * @param timedOutRequest a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     */
    public MobileMsgRequest requestTimedOut(MobileMsgRequest timedOutRequest) {
        return m_requests.remove(timedOutRequest) ? timedOutRequest : null;
    }

    /**
     * <p>requestComplete</p>
     *
     * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     */
    public void requestComplete(MobileMsgRequest request) {
        m_requests.remove(request);
    }


}
