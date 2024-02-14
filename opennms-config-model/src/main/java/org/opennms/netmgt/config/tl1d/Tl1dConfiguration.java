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
package org.opennms.netmgt.config.tl1d;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "tl1d-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("tl1d-configuration.xsd")
public class Tl1dConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "tl1-element")
    private List<Tl1Element> m_tl1Elements = new ArrayList<>();

    public List<Tl1Element> getTl1Elements() {
        return m_tl1Elements;
    }

    public void setTl1Elements(final List<Tl1Element> tl1Elements) {
        if (tl1Elements == m_tl1Elements) return;
        m_tl1Elements.clear();
        if (tl1Elements != null) m_tl1Elements.addAll(tl1Elements);
    }

    public void addTl1Element(final Tl1Element tl1Element) {
        m_tl1Elements.add(tl1Element);
    }

    public boolean removeTl1Element(final Tl1Element tl1Element) {
        return m_tl1Elements.remove(tl1Element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_tl1Elements);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Tl1dConfiguration) {
            final Tl1dConfiguration that = (Tl1dConfiguration)obj;
            return Objects.equals(this.m_tl1Elements, that.m_tl1Elements);
        }
        return false;
    }

}
