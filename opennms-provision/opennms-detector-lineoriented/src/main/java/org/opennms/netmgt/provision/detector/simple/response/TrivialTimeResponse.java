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
package org.opennms.netmgt.provision.detector.simple.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>TrivialTimeResponse class.</p>
 *
 * @author Alejandro Galue <agalue@sync.com.ve>
 * @version $Id: $
 */
public class TrivialTimeResponse {
    
    private static final Logger LOG = LoggerFactory.getLogger(TrivialTimeResponse.class);
    boolean available;

    public TrivialTimeResponse() {
        available = false;
    }

    public TrivialTimeResponse(int remoteTime, int localTime, int allowedSkew) {
        available = false;
        LOG.debug("qualifyTime: checking remote time {} against local time {} with max skew of {}", remoteTime, localTime, allowedSkew);
        if ((localTime - remoteTime > allowedSkew) || (remoteTime - localTime > allowedSkew)) {
            if (localTime > remoteTime) {
                LOG.debug("Remote time is {} seconds slow", (localTime-remoteTime));
            } else {
                LOG.debug("Remote time is {} seconds fast", (remoteTime-localTime));
            }
        }
        if ((localTime > remoteTime) && (localTime - remoteTime > allowedSkew)) {
            LOG.debug("Remote time is {} seconds behind local, more than the allowable {}", (localTime - remoteTime), allowedSkew);
        } else if ((remoteTime > localTime) && (remoteTime - localTime > allowedSkew)) {
            LOG.debug("Remote time is {} seconds ahead of local, more than the allowable {}", (remoteTime - localTime), allowedSkew);
        } else {
            available = true;
        }
    }

    public boolean isAvailable() {
        return available;
    }

}
