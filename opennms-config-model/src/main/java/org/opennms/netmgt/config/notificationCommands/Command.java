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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notificationCommands.xsd")
public class Command implements Serializable {
    private static final long serialVersionUID = 3L;

    @XmlAttribute(name = "binary")
    @JsonProperty("binary")
    private Boolean binary;

    @XmlAttribute(name = "service-registry")
    @JsonProperty("service-registry")
    private Boolean serviceRegistry;

    @XmlElement(name = "name", required = true)
    @JsonProperty("name")
    private String name;

    @XmlElement(name = "execute", required = true)
    @JsonProperty("execute")
    private String execute;

    @XmlElement(name = "comment")
    @JsonProperty("comment")
    private String comment;

    @XmlElement(name = "contact-type")
    @JsonProperty("contact-type")
    private String contactType;

    @XmlElement(name = "argument")
    @JsonProperty("argument")
    private List<Argument> arguments = new ArrayList<>();

    public Command() { }

    public Boolean getBinary() {
        return binary != null ? binary : Boolean.TRUE;
    }

    public void setBinary(final Boolean binary) {
        this.binary = binary;
    }

    public Boolean getServiceRegistry() {
        return serviceRegistry != null ? serviceRegistry : Boolean.FALSE;
    }

    public void setServiceRegistry(final Boolean serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getExecute() {
        return execute;
    }

    public void setExecute(final String execute) {
        this.execute = ConfigUtils.assertNotEmpty(execute, "execute");
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    public void setComment(final String comment) {
        this.comment = ConfigUtils.normalizeString(comment);
    }

    public Optional<String> getContactType() {
        return Optional.ofNullable(contactType);
    }

    public void setContactType(final String contactType) {
        this.contactType = ConfigUtils.normalizeString(contactType);
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(final List<Argument> arguments) {
        if (arguments == this.arguments) return;
        this.arguments.clear();
        if (arguments != null) this.arguments.addAll(arguments);
    }

    public void addArgument(final Argument argument) {
        this.arguments.add(argument);
    }

    public boolean removeArgument(final Argument argument) {
        return this.arguments.remove(argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(binary,
                            serviceRegistry,
                            name,
                            execute,
                            comment,
                            contactType,
                            arguments);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Command) {
            final Command that = (Command)obj;
            return Objects.equals(this.binary, that.binary)
                    && Objects.equals(this.serviceRegistry, that.serviceRegistry)
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.execute, that.execute)
                    && Objects.equals(this.comment, that.comment)
                    && Objects.equals(this.contactType, that.contactType)
                    && Objects.equals(this.arguments, that.arguments);
        }
        return false;
    }

}
