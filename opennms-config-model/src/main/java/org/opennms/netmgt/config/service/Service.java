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
package org.opennms.netmgt.config.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Service to be launched by the manager.
 */
@XmlRootElement(name = "service")
@ValidateUsing("service-configuration.xsd")
public class Service implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "enabled")
    private Boolean m_enabled;

    @XmlElement(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "class-name", required = false)
    private String m_className;

    @XmlElement(name = "attribute")
    private List<Attribute> m_attributes = new ArrayList<>();

    @XmlElement(name = "invoke")
    private List<Invoke> m_invokes = new ArrayList<>();

    public Service() {
    }

    public Service(final String name, final String className,
        final List<Attribute> attributes, final List<Invoke> invokes) {
        setName(name);
        setClassName(className);
        setAttributes(attributes);
        setInvokes(invokes);
    }

    @XmlTransient
    public Boolean isEnabled() {
        return m_enabled == null? Boolean.TRUE : m_enabled;
    }

    public void setEnabled(final Boolean enabled) {
        m_enabled = enabled;
    }

    @XmlTransient
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    @XmlTransient
    public String getClassName() {
        return m_className;
    }

    public void setClassName(final String className) {
        m_className = ConfigUtils.normalizeString(className);
    }

    @XmlTransient
    public List<Attribute> getAttributes() {
        return m_attributes;
    }

    public void setAttributes(final List<Attribute> attributes) {
        if (attributes == m_attributes) return;
        m_attributes.clear();
        if (attributes != null) m_attributes.addAll(attributes);
    }

    public void addAttribute(final Attribute attribute) {
        m_attributes.add(attribute);
    }

    public boolean removeAttribute(final Attribute attribute) {
        return m_attributes.remove(attribute);
    }

    @XmlTransient
    public List<Invoke> getInvokes() {
        return m_invokes;
    }

    public void setInvokes(final List<Invoke> invokes) {
        if (invokes == m_invokes) return;
        m_invokes.clear();
        if (invokes != null) m_invokes.addAll(invokes);
    }

    public void addInvoke(final Invoke invoke) {
        m_invokes.add(invoke);
    }

    public boolean removeInvoke(final Invoke invoke) {
        return m_invokes.remove(invoke);
    }

    public int hashCode() {
        return Objects.hash(m_enabled, m_name, m_className, m_attributes, m_invokes);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Service) {
            final Service that = (Service) obj;
            return Objects.equals(this.m_enabled, that.m_enabled) &&
                    Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_className, that.m_className) &&
                    Objects.equals(this.m_attributes, that.m_attributes) &&
                    Objects.equals(this.m_invokes, that.m_invokes);
        }
        return false;
    }

    /**
     * Merges two Service objects, with user config taking precedence over defaults.
     *
     * @param userService the user-defined service configuration
     * @param defaultService the default service configuration from classpath
     * @return merged service with user config taking precedence
     */
    public static Service merge(final Service userService, final Service defaultService) {
        if (defaultService == null) {
            return userService;
        }

        if (userService == null) {
            return defaultService;
        }

        final Service merged = new Service();

        // Name must match (we find defaults by name)
        merged.setName(userService.getName());

        // User's enabled setting takes precedence
        // If user specified enabled explicitly, use it; otherwise default to true (enabled)
        // This allows users to enable a service by just including it in config
        merged.setEnabled(userService.m_enabled != null ? userService.m_enabled : Boolean.TRUE);

        // User's class-name takes precedence, otherwise use default
        merged.m_className = userService.m_className != null ? userService.m_className : defaultService.m_className;

        // User's attributes take precedence if not empty, otherwise use defaults
        if (userService.m_attributes != null && !userService.m_attributes.isEmpty()) {
            merged.setAttributes(new ArrayList<>(userService.m_attributes));
        } else if (defaultService.m_attributes != null) {
            merged.setAttributes(new ArrayList<>(defaultService.m_attributes));
        }

        // User's invokes take precedence if not empty, otherwise use defaults
        if (userService.m_invokes != null && !userService.m_invokes.isEmpty()) {
            merged.setInvokes(new ArrayList<>(userService.m_invokes));
        } else if (defaultService.m_invokes != null) {
            merged.setInvokes(new ArrayList<>(defaultService.m_invokes));
        }

        return merged;
    }

}
