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
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "binary")
    private Boolean m_binary;

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
                    && Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_execute, that.m_execute)
                    && Objects.equals(this.m_comment, that.m_comment)
                    && Objects.equals(this.m_contactType, that.m_contactType)
                    && Objects.equals(this.m_arguments, that.m_arguments);
        }
        return false;
    }

}
