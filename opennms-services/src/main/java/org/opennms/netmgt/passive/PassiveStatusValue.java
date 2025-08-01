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
package org.opennms.netmgt.passive;

import org.opennms.netmgt.poller.PollStatus;

/**
 * <p>PassiveStatusValue class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PassiveStatusValue {
    
    private PassiveStatusKey m_key;
    private PollStatus m_status;
    
    /**
     * <p>Constructor for PassiveStatusValue.</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param status a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PassiveStatusValue(String nodeLabel, String ipAddr, String serviceName, PollStatus status) {
        this(new PassiveStatusKey(nodeLabel, ipAddr, serviceName), status);
    }
    
    /**
     * <p>Constructor for PassiveStatusValue.</p>
     *
     * @param key a {@link org.opennms.netmgt.passive.PassiveStatusKey} object.
     * @param status a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PassiveStatusValue(PassiveStatusKey key, PollStatus status) {
        m_key = key;
        m_status = status;
    }
    
    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PollStatus getStatus() {
        return m_status;
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public void setStatus(PollStatus status) {
        m_status = status;
    }
    
    /**
     * <p>getKey</p>
     *
     * @return a {@link org.opennms.netmgt.passive.PassiveStatusKey} object.
     */
    public PassiveStatusKey getKey() {
        return m_key;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getKey().toString()+" -> "+m_status;
    }

    
}
