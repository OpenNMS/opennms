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
package org.opennms.netmgt.provision.detector.ssh.response;

/**
 * <p>SshResponse class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class SshResponse {
    
    private boolean m_isAvailable = false;
    
    /**
     * <p>receive</p>
     *
     * @param isAvail a boolean.
     */
    public void receive(boolean isAvail) {
        m_isAvailable = isAvail;
    }
    
    /**
     * <p>isAvailable</p>
     *
     * @return a boolean.
     */
    public boolean isAvailable() {
        return m_isAvailable;
    }
}
