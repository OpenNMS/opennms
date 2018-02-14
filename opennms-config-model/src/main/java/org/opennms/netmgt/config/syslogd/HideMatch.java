/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.syslogd;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * List of substrings or regexes that, when matched, signal
 *  that the message has sensitive contents and should
 *  therefore be hidden
 */
@XmlRootElement(name = "hideMatch")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("syslog.xsd")
public class HideMatch implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The match expression
     */
    @XmlElement(name = "match", required = true)
    private Match m_match;

    public HideMatch() {
    }

    public Match getMatch() {
        return m_match;
    }

    public void setMatch(final Match match) {
        m_match = ConfigUtils.assertNotEmpty(match, "match");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_match);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof HideMatch) {
            final HideMatch that = (HideMatch)obj;
            return Objects.equals(this.m_match, that.m_match);
        }
        return false;
    }

}
