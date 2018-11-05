/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.daemon;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "daemon")
@XmlAccessorType(XmlAccessType.NONE)
public class DaemonInfo {

    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "internal")
    private boolean internal;
    @XmlAttribute(name = "enabled")
    private boolean enabled;
    @XmlAttribute(name = "reloadable")
    private boolean reloadable;

    // Required for JAXB
    public DaemonInfo() {

    }

    public DaemonInfo(String name, boolean internal, boolean enabled, boolean reloadable) {
        this.name = name;
        this.internal = internal;
        this.enabled = enabled;
        this.reloadable = reloadable;
    }

    public String getName() {
        return name;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

}