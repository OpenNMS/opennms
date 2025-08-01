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
package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;

public class SystemDefChoice implements Serializable {
    private static final long serialVersionUID = 4037895445383242171L;

    /**
     * system object identifier (sysoid) which uniquely identifies the system.
     */
    private String m_sysoid;

    /**
     * Sysoid mask which can be used to match multiple systems if their sysoid
     * begins with the mask
     */
    private String m_sysoidMask;

    public SystemDefChoice() {
        super();
    }

    /**
     * system object identifier (sysoid) which uniquely identifies the system.
     */
    public String getSysoid() {
        return m_sysoid;
    }

    /**
     * Sysoid mask which can be used to match multiple systems if their sysoid
     * begins with the mask
     */
    public String getSysoidMask() {
        return m_sysoidMask;
    }


    public void setSysoid(final String sysoid) {
        m_sysoid = sysoid == null? null : sysoid.intern();
    }

    public void setSysoidMask(final String sysoidMask) {
        m_sysoidMask = sysoidMask == null? null : sysoidMask.intern();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_sysoid == null) ? 0 : m_sysoid.hashCode());
        result = prime * result + ((m_sysoidMask == null) ? 0 : m_sysoidMask.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SystemDefChoice)) {
            return false;
        }
        final SystemDefChoice other = (SystemDefChoice) obj;
        if (m_sysoid == null) {
            if (other.m_sysoid != null) {
                return false;
            }
        } else if (!m_sysoid.equals(other.m_sysoid)) {
            return false;
        }
        if (m_sysoidMask == null) {
            if (other.m_sysoidMask != null) {
                return false;
            }
        } else if (!m_sysoidMask.equals(other.m_sysoidMask)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SystemDefChoice [sysoid=" + m_sysoid + ", sysoidMask=" + m_sysoidMask + "]";
    }
}
