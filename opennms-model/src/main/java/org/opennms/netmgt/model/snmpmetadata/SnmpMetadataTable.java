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
