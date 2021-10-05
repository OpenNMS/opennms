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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@XmlRootElement(name = "snmp-metadata-table")
public class SnmpMetadataTable extends SnmpMetadataBase {
    private String name;
    private List<SnmpMetadataEntry> entries = new ArrayList<>();

    public SnmpMetadataTable() {
    }

    public SnmpMetadataTable(final String name) {
        this.name = trimName(name);
    }

    public SnmpMetadataEntry addEntry(final String index, final String key, final String value) {
        final Optional<SnmpMetadataEntry> optionalEntry = entries.stream()
                .filter(e -> index.equals(e.getIndex()))
                .findAny();

        final SnmpMetadataEntry snmpMetadataEntry;

        if (optionalEntry.isPresent()) {
            snmpMetadataEntry = optionalEntry.get();
        } else {
            snmpMetadataEntry = new SnmpMetadataEntry(index);
            snmpMetadataEntry.setParent(this);
            this.entries.add(snmpMetadataEntry);
        }

        snmpMetadataEntry.addValue(key, value);
        return snmpMetadataEntry;
    }

    public SnmpMetadataEntry addEntry(final String index) {
        final Optional<SnmpMetadataEntry> optionalEntry = entries.stream()
                .filter(e -> index.equals(e.getIndex()))
                .findAny();

        final SnmpMetadataEntry snmpMetadataEntry;

        if (optionalEntry.isPresent()) {
            snmpMetadataEntry = optionalEntry.get();
        } else {
            snmpMetadataEntry = new SnmpMetadataEntry(index);
            snmpMetadataEntry.setParent(this);
            this.entries.add(snmpMetadataEntry);
        }

        return snmpMetadataEntry;
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @XmlElement(name = "snmp-metadata-entry")
    public List<SnmpMetadataEntry> getEntries() {
        return entries;
    }

    public void setEntries(final List<SnmpMetadataEntry> entries) {
        this.entries = entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SnmpMetadataTable that = (SnmpMetadataTable) o;
        return Objects.equals(name, that.name) && Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, entries);
    }
}
