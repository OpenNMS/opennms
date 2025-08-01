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
package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This element is used for converting event 
 *  varbind value in static decoded string.
 */
@XmlRootElement(name="varbindsdecode")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_parmid", "m_decodes"})
public class Varbindsdecode implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The identifier of the parameters to be decoded
     */
    @XmlElement(name="parmid", required=true)
    private String m_parmid;

    /**
     * The value to string decoding map
     */
    @XmlElement(name="decode", required=true)
    private List<Decode> m_decodes = new ArrayList<>();

    public String getParmid() {
        return m_parmid;
    }

    public void setParmid(final String parmid) {
        m_parmid = ConfigUtils.assertNotEmpty(parmid, "parmid").intern();
    }

    public List<Decode> getDecodes() {
        return m_decodes;
    }

    public void setDecodes(final List<Decode> decodes) {
        ConfigUtils.assertMinimumSize(decodes, 1, "decode");
        if (m_decodes == decodes) return;
        m_decodes.clear();
        if (decodes != null) m_decodes.addAll(decodes);
    }

    public void addDecode(final Decode decode) {
        m_decodes.add(decode);
    }

    public boolean removeDecode(final Decode decode) {
        return m_decodes.remove(decode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_parmid, m_decodes);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Varbindsdecode) {
            final Varbindsdecode that = (Varbindsdecode) obj;
            return Objects.equals(this.m_parmid, that.m_parmid) &&
                    Objects.equals(this.m_decodes, that.m_decodes);
        }
        return false;
    }

}
