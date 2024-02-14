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
package org.opennms.netmgt.provision.detector.simple.support;

import org.apache.mina.core.session.IoSession;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.BaseDetectorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpDetectorHandler extends BaseDetectorHandler<LineOrientedRequest, LineOrientedResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(TcpDetectorHandler.class);
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        Object request = getConversation().getRequest();
        if(!getConversation().hasBanner() &&  request != null) {
            session.write(request);
       }else if(!getConversation().hasBanner() && request == null) {
           LOG.info("TCP session was opened, no banner was expected, and there are no more pending requests. Setting service detection to true.");
           getFuture().setServiceDetected(true);
           session.close(true);
       }
    }

}
