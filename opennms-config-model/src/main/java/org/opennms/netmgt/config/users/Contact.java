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
package org.opennms.netmgt.config.users;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "contact")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("users.xsd")
public class Contact implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "type", required = true)
    private String m_type;

    @XmlAttribute(name = "info")
    private String m_info;

    @XmlAttribute(name = "serviceProvider")
    private String m_serviceProvider;

    public Contact() {
    }

    public Contact(final String type) {
        m_type = type;
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        ConfigUtils.assertNotEmpty(type, "type");
        m_type = type;
    }

    public Optional<String> getInfo() {
        return Optional.ofNullable(m_info);
    }

    public void setInfo(final String info) {
        m_info = ConfigUtils.normalizeString(WebSecurityUtils.sanitizeString(info));
    }

    public Optional<String> getServiceProvider() {
        return Optional.ofNullable(m_serviceProvider);
    }

    public void setServiceProvider(final String serviceProvider) {
        m_serviceProvider = ConfigUtils.normalizeString(WebSecurityUtils.sanitizeString(serviceProvider));
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_type, 
                            m_info, 
                            m_serviceProvider);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Contact) {
            final Contact temp = (Contact)obj;
            return Objects.equals(temp.m_type, m_type)
                    && Objects.equals(temp.m_info, m_info)
                    && Objects.equals(temp.m_serviceProvider, m_serviceProvider);
        }
        return false;
    }

}
