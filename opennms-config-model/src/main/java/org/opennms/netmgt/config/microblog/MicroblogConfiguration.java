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
package org.opennms.netmgt.config.microblog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Microblog configuration groups
 */
@XmlRootElement(name = "microblog-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("microblog-configuration.xsd")
public class MicroblogConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "default-microblog-profile-name", required = true)
    private String m_defaultMicroblogProfileName;

    /**
     * This entity defines the parameters for a microblog service.
     *  
     */
    @XmlElement(name = "microblog-profile", required = true)
    private List<MicroblogProfile> m_profiles = new ArrayList<>();

    public String getDefaultMicroblogProfileName() {
        return m_defaultMicroblogProfileName;
    }

    public void setDefaultMicroblogProfileName(final String name) {
        m_defaultMicroblogProfileName = ConfigUtils.assertNotEmpty(name, "default-microblog-profile-name");
    }

    public List<MicroblogProfile> getMicroblogProfiles() {
        return m_profiles;
    }

    public void setMicroblogProfiles(final List<MicroblogProfile> profiles) {
        if (profiles == m_profiles) return;
        m_profiles.clear();
        if (profiles != null) m_profiles.addAll(profiles);
    }

    public void addMicroblogProfile(final MicroblogProfile profile) {
        m_profiles.add(profile);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof MicroblogConfiguration) {
            final MicroblogConfiguration that = (MicroblogConfiguration)obj;
            return Objects.equals(this.m_defaultMicroblogProfileName, that.m_defaultMicroblogProfileName)
                    && Objects.equals(this.m_profiles, that.m_profiles);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_defaultMicroblogProfileName, m_profiles);
    }

}
