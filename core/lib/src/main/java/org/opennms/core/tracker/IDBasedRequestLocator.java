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
