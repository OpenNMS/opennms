/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

/**
 * Microblog configuration groups
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "microblog-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
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
        if (name == null) {
            throw new IllegalArgumentException("'default-microblog-profile-name' is a required attribute!");
        }
        m_defaultMicroblogProfileName = name;
    }

    public List<MicroblogProfile> getMicroblogProfiles() {
        return m_profiles;
    }

    public void setMicroblogProfiles(final List<MicroblogProfile> profiles) {
        m_profiles.clear();
        m_profiles.addAll(profiles);
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
            return Objects.equals(that.m_defaultMicroblogProfileName, this.m_defaultMicroblogProfileName)
                && Objects.equals(that.m_profiles, this.m_profiles);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            m_defaultMicroblogProfileName, 
            m_profiles);
    }

}
