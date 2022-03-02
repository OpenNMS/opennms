/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.snmpmetadata;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

public abstract class SnmpMetadataBase {
    private SnmpMetadataBase parent;
    private final int id;
    private static int counter = 0;

    protected SnmpMetadataBase() {
        this.id = counter++;
    }

    @XmlTransient
    public SnmpMetadataBase getParent() {
        return parent;
    }

    public void setParent(final SnmpMetadataBase parent) {
        this.parent = parent;
    }

    protected String trimName(final String name) {
        final String arr[] = name.split("\\.");
        return arr[arr.length - 1];
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnmpMetadataBase that = (SnmpMetadataBase) o;
        return id == that.id && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, id);
    }
}
