/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.tracker;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RequestLocatorImpl
 *
 * @author brozow
 */
public class IDBasedRequestLocator<ReqIdT, ReqT extends Request<ReqIdT, ReqT, ReplyT>, ReplyT extends ResponseWithId<ReqIdT>> implements RequestLocator<ReqT, ReplyT> {

    private static final Logger s_log = LoggerFactory.getLogger(IDBasedRequestLocator.class);

    private Map<ReqIdT, ReqT> m_pendingRequests = new HashMap<ReqIdT, ReqT>();

    public ReqT requestTimedOut(ReqT timedOutRequest) {
        synchronized (m_pendingRequests) {
            ReqT pendingRequest = m_pendingRequests.get(timedOutRequest.getId());
            if (pendingRequest == timedOutRequest) {
                m_pendingRequests.remove(timedOutRequest.getId());
            }
            // we return pendingRequest anyway to the tracker processes this as an error
            return pendingRequest;
        }
    }

    public void requestComplete(ReqT request) {
        synchronized (m_pendingRequests) {
            m_pendingRequests.remove(request.getId());
        }
    }


    public ReqT locateMatchingRequest(ReplyT reply) {

        ReqIdT id = reply.getRequestId();
        synchronized (m_pendingRequests) {
            s_log.debug("Looking for request with Id: {} in map {}", id, m_pendingRequests);
            return m_pendingRequests.get(id);
        }

    }

    public boolean trackRequest(ReqT request) {
        synchronized(m_pendingRequests) {
            ReqT oldRequest = m_pendingRequests.get(request.getId());
            if (oldRequest != null) {
                request.processError(new IllegalStateException("Duplicate request; keeping old request: "+oldRequest+"; removing new request: "+request));
                return false;
            } else {
                m_pendingRequests.put(request.getId(), request);
            }
        }
        return true;
    }
}
