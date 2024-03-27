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
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "stop-script")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("scriptd-configuration.xsd")
public class StopScript implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * internal content storage
     */
    @XmlValue
    private String m_content;

    @XmlAttribute(name = "language", required = true)
    private String m_language;

    public Optional<String> getContent() {
        return Optional.ofNullable(m_content);
    }

    public void setContent(final String content) {
        m_content = ConfigUtils.normalizeString(content);
    }

    public String getLanguage() {
        return m_language;
    }

    public void setLanguage(final String language) {
        m_language = ConfigUtils.assertNotEmpty(language, "language");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_content, m_language);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof StopScript) {
            final StopScript that = (StopScript)obj;
            return Objects.equals(this.m_content, that.m_content)
                && Objects.equals(this.m_language, that.m_language);
        }
        return false;
    }

}
