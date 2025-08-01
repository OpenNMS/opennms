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
