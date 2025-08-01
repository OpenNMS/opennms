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
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This element is used for converting event 
 *  varbind value in static decoded string.
 */
@XmlRootElement(name="decode")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Decode implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="varbindvalue", required=true)
    private String m_varbindvalue;

    @XmlAttribute(name="varbinddecodedstring",required=true)
    private String m_varbinddecodedstring;

    public String getVarbindvalue() {
        return m_varbindvalue;
    }

    public void setVarbindvalue(final String varbindvalue) {
        m_varbindvalue = ConfigUtils.assertNotNull(varbindvalue, "varbindvalue").intern();
    }

    public String getVarbinddecodedstring() {
        return m_varbinddecodedstring;
    }

    public void setVarbinddecodedstring(final String varbinddecodedstring) {
        m_varbinddecodedstring = ConfigUtils.assertNotNull(varbinddecodedstring, "varbinddecodedstring").intern();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_varbindvalue, m_varbinddecodedstring);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Decode) {
            final Decode that = (Decode) obj;
            return Objects.equals(this.m_varbindvalue, that.m_varbindvalue) &&
                    Objects.equals(this.m_varbinddecodedstring, that.m_varbinddecodedstring);
        }
        return false;
    }

    @Override
    public String toString() {
        return m_varbindvalue + '=' + m_varbinddecodedstring;
    }

}
