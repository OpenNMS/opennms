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
package org.opennms.netmgt.config.notificationCommands;

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

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notificationCommands.xsd")
public class Command implements Serializable {
    private static final long serialVersionUID = 3L;

    @XmlAttribute(name = "binary")
    private Boolean m_binary;

    @XmlAttribute(name = "service-registry")
    private Boolean m_serviceRegistry;

    @XmlElement(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "execute", required = true)
    private String m_execute;

    @XmlElement(name = "comment")
    private String m_comment;

    @XmlElement(name = "contact-type")
    private String m_contactType;

    @XmlElement(name = "argument")
    private List<Argument> m_arguments = new ArrayList<>();

    public Command() { }

    public Boolean getBinary() {
        return m_binary != null ? m_binary : Boolean.TRUE;
    }

    public void setBinary(final Boolean binary) {
        m_binary = binary;
    }

    public Boolean getServiceRegistry() {
        return m_serviceRegistry != null ? m_serviceRegistry : Boolean.FALSE;
    }

    public void setServiceRegistry(final Boolean serviceRegistry) {
        m_serviceRegistry = serviceRegistry;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getExecute() {
        return m_execute;
    }

    public void setExecute(final String execute) {
        m_execute = ConfigUtils.assertNotEmpty(execute, "execute");
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(m_comment);
    }

    public void setComment(final String comment) {
        m_comment = ConfigUtils.normalizeString(comment);
    }

    public Optional<String> getContactType() {
        return Optional.ofNullable(m_contactType);
    }

    public void setContactType(final String contactType) {
        m_contactType = ConfigUtils.normalizeString(contactType);
    }

    public List<Argument> getArguments() {
        return m_arguments;
    }

    public void setArguments(final List<Argument> arguments) {
        if (arguments == m_arguments) return;
        m_arguments.clear();
        if (arguments != null) m_arguments.addAll(arguments);
    }

    public void addArgument(final Argument argument) {
        m_arguments.add(argument);
    }

    public boolean removeArgument(final Argument argument) {
        return m_arguments.remove(argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_binary,
                            m_serviceRegistry,
                            m_name, 
                            m_execute, 
                            m_comment, 
                            m_contactType, 
                            m_arguments);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Command) {
            final Command that = (Command)obj;
            return Objects.equals(this.m_binary, that.m_binary)
                    && Objects.equals(this.m_serviceRegistry, that.m_serviceRegistry)
                    && Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_execute, that.m_execute)
                    && Objects.equals(this.m_comment, that.m_comment)
                    && Objects.equals(this.m_contactType, that.m_contactType)
                    && Objects.equals(this.m_arguments, that.m_arguments);
        }
        return false;
    }

}
