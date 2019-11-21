/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.search.providers.action;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.NONE)
public class Action {
    @XmlElement(name="label")
    private String label;

    @XmlElement(name="url")
    private String url;

    @XmlElement(name="icon")
    private String icon;

    @XmlElementWrapper(name="roles")
    @XmlElement(name="role")
    private List<String> privilegedRoles = Lists.newArrayList();

    @XmlElementWrapper(name="aliases")
    @XmlElement(name="alias")
    private List<String> aliases = Lists.newArrayList();

    @XmlElement(name="weight")
    private int weight;

    public Action() {

    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getPrivilegedRoles() {
        return privilegedRoles;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setPrivilegedRoles(List<String> privilegedRoles) {
        this.privilegedRoles = privilegedRoles;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
