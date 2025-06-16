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
package org.opennms.netmgt.config.scriptd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "event-script")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = {
        "m_language",
        "m_ueis",
        "m_content"
})
@ValidateUsing("scriptd-configuration.xsd")
public class EventScript implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "language", required = true)
    private String m_language;

    @XmlElement(name = "uei")
    private List<Uei> m_ueis = new ArrayList<>();

    /**
     * internal content storage
     *
     * Work-around for capture the @XmlValue along side @XmlElements
     */
    @XmlPath(".")
    @XmlJavaTypeAdapter(MixedContentAdapter.class)
    private String m_content;

    public String getLanguage() {
        return m_language;
    }

    public void setLanguage(final String language) {
        m_language = ConfigUtils.assertNotEmpty(language, "language");
    }

    public List<Uei> getUeis() {
        return m_ueis;
    }

    public void setUeis(final List<Uei> ueis) {
        if (ueis == m_ueis) return;
        m_ueis.clear();
        if (ueis != null) m_ueis.addAll(ueis);
    }

    public void addUei(final Uei uei) {
        m_ueis.add(uei);
    }

    public boolean removeUei(final Uei uei) {
        return m_ueis.remove(uei);
    }

    public Optional<String> getContent() {
        return Optional.ofNullable(m_content);
    }

    public void setContent(final String content) {
        m_content = ConfigUtils.normalizeString(content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_language, 
                            m_content, 
                            m_ueis);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof EventScript) {
            final EventScript that = (EventScript)obj;
            return Objects.equals(this.m_language, that.m_language)
                    && Objects.equals(this.m_content, that.m_content)
                    && Objects.equals(this.m_ueis, that.m_ueis);
        }
        return false;
    }

}
