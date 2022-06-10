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

@XmlRootElement(name = "snmp-metadata-entry")
public class SnmpMetadataEntry extends SnmpMetadataBase {
    private String index;
    private List<SnmpMetadataValue> values = new ArrayList<>();

    public SnmpMetadataEntry() {
    }

    public SnmpMetadataEntry(final String index) {
        this.index = index;
    }

    @XmlAttribute(name = "index")
    public String getIndex() {
        return index;
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    @XmlElement(name = "snmp-metadata-value")
    public List<SnmpMetadataValue> getValues() {
        return values;
    }

    public void setValues(final List<SnmpMetadataValue> values) {
        this.values = values;
    }

    public SnmpMetadataValue addValue(final String name, final String value) {
        final Optional<SnmpMetadataValue> optionalEntry = values.stream()
                .filter(e -> trimName(name).equals(e.getName()))
                .findAny();

        final SnmpMetadataValue snmpMetadataValue;

        if (optionalEntry.isPresent()) {
            snmpMetadataValue = optionalEntry.get();
        } else {
            snmpMetadataValue = new SnmpMetadataValue(name);
            snmpMetadataValue.setParent(this);
            this.values.add(snmpMetadataValue);
        }

        snmpMetadataValue.setValue(value);

        return snmpMetadataValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SnmpMetadataEntry that = (SnmpMetadataEntry) o;
        return Objects.equals(index, that.index) && Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index, values);
    }
}
