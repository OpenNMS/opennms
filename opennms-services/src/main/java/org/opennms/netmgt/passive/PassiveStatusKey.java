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

/**
 * <p>PassiveStatusKey class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PassiveStatusKey {
    
    private String m_nodeLabel;
    private String m_ipAddr;
    private String m_serviceName;

    /**
     * <p>Constructor for PassiveStatusKey.</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     */
    public PassiveStatusKey(String nodeLabel, String ipAddr, String serviceName) {
        m_nodeLabel = nodeLabel;
        m_ipAddr = ipAddr;
        m_serviceName = serviceName;
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return m_ipAddr;
    }

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof PassiveStatusKey) {
            PassiveStatusKey key = (PassiveStatusKey) o;
            return getNodeLabel().equals(key.getNodeLabel()) &&
                    getIpAddr().equals(key.getIpAddr()) &&
                    getServiceName().equals(key.getServiceName());
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return getNodeLabel().hashCode() ^ getIpAddr().hashCode() ^ getServiceName().hashCode();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getNodeLabel()+':'+getIpAddr()+':'+getServiceName();
    }


}
