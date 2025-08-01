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
package org.opennms.netmgt.config.destinationPaths;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "header")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("users.xsd")
public class Header implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "rev", required = true)
    private String m_rev;

    /**
     * Creation time in the 'dow mon dd hh:mm:ss zzz yyyy'
     *  format.
     */
    @XmlElement(name = "created", required = true)
    private String m_created;

    /**
     * Monitoring station? This is seemingly
     *  unused.
     */
    @XmlElement(name = "mstation", required = true)
    private String m_mstation;

    public Header() {
    }

    public Header(final String rev, final String created, final String mstation) {
        m_rev = rev;
        m_created = created;
        m_mstation = mstation;
    }

    public String getRev() {
        return m_rev;
    }

    public void setRev(final String rev) {
        m_rev = rev;
    }

    public String getCreated() {
        return m_created;
    }

    public void setCreated(final String created) {
        m_created = created;
    }

    public String getMStation() {
        return m_mstation;
    }

    public void setMStation(final String mstation) {
        m_mstation = mstation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_rev, 
                            m_created, 
                            m_mstation);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Header) {
            final Header temp = (Header)obj;
            return Objects.equals(temp.m_rev, m_rev)
                    && Objects.equals(temp.m_created, m_created)
                    && Objects.equals(temp.m_mstation, m_mstation);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Header[rev=" + m_rev + ", created=" + m_created
                + ", mstation=" + m_mstation + "]";
    }

}
