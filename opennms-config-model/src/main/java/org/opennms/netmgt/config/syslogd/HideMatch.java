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
