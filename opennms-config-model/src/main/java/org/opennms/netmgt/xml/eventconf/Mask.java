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

import com.google.common.base.MoreObjects;

/**
 * The Mask for event configuration: The mask contains one
 *  or more 'maskelements' which uniquely identify an event.
 */
@XmlRootElement(name="mask")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_maskElements", "m_varbinds"})
public class Mask implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The mask element
     */
    @XmlElement(name="maskelement", required=true)
    private List<Maskelement> m_maskElements = new ArrayList<>();

    /**
     * The varbind element
     */
    @XmlElement(name="varbind")
    private List<Varbind> m_varbinds = new ArrayList<>();

    public List<Maskelement> getMaskelements() {
        return m_maskElements;
    }

    public void setMaskelements(final List<Maskelement> elements) {
        if (m_maskElements == elements) return;
        m_maskElements.clear();
        if (elements != null) m_maskElements.addAll(elements);
    }

    public void addMaskelement(final Maskelement element) {
        m_maskElements.add(element);
    }

    public boolean removeMaskelement(final Maskelement element) {
        return m_maskElements.remove(element);
    }

    public List<Varbind> getVarbinds() {
        return m_varbinds;
    }

    public void setVarbinds(final List<Varbind> varbinds) {
        if (m_varbinds == varbinds) return;
        m_varbinds.clear();
        if (varbinds != null) m_varbinds.addAll(varbinds);
    }

    public void addVarbind(final Varbind varbind) {
        m_varbinds.add(varbind);
    }

    public boolean removeVarbind(final Varbind varbind) {
        return m_varbinds.remove(varbind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_maskElements, m_varbinds);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Mask) {
            final Mask that = (Mask) obj;
            return Objects.equals(this.m_maskElements, that.m_maskElements) &&
                    Objects.equals(this.m_varbinds, that.m_varbinds);
        }
        return false;
    }

    public EventMatcher constructMatcher() {
        final EventMatcher[] matchers = new EventMatcher[m_maskElements.size()+m_varbinds.size()];
        int index = 0;
        for(final Maskelement maskElement : m_maskElements) {
            matchers[index] = maskElement.constructMatcher();
            index++;
        }

        for(final Varbind varbind : m_varbinds) {
            matchers[index] = varbind.constructMatcher();
            index++;
        }

        return EventMatchers.and(matchers);
    }

    public Maskelement getMaskElement(final String mename) {
        for(final Maskelement element : m_maskElements) {
            if (mename.equals(element.getMename())) {
                return element;
            }
        }
        return null;
    }

    public List<String> getMaskElementValues(final String mename) {
        final Maskelement element = getMaskElement(mename);
        return element == null ? null : element.getMevalues();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mask", m_maskElements)
                .add("varbinds", m_varbinds)
                .toString();
    }
}
