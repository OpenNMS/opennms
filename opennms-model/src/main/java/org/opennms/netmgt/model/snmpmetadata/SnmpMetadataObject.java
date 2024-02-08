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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import org.opennms.netmgt.model.OnmsMetaData;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@XmlRootElement(name = "snmp-metadata-object")
public class SnmpMetadataObject extends SnmpMetadataBase {
    @XmlTransient
    final static Pattern PATTERN_KEY = Pattern.compile("[^.]+\\[[^\\[]+\\]|[^.]+|[^.]+$");
    @XmlTransient
    final static Pattern PATTERN_INDEX = Pattern.compile("(.*)\\[(.*)\\]$");
    private String name;
    private List<SnmpMetadataObject> objects = new ArrayList<>();
    private List<SnmpMetadataValue> values = new ArrayList<>();
    private List<SnmpMetadataTable> tables = new ArrayList<>();

    public SnmpMetadataObject() {
    }

    public SnmpMetadataObject(final String name) {
        this.name = trimName(name);
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @XmlElement(name = "snmp-metadata-object")
    public List<SnmpMetadataObject> getObjects() {
        return objects;
    }

    public void setObjects(final List<SnmpMetadataObject> objects) {
        this.objects = objects;
    }

    @XmlElement(name = "snmp-metadata-value")
    public List<SnmpMetadataValue> getValues() {
        return values;
    }

    public void setValues(final List<SnmpMetadataValue> values) {
        this.values = values;
    }

    @XmlElement(name = "snmp-metadata-table")
    public List<SnmpMetadataTable> getTables() {
        return tables;
    }

    public void setTables(final List<SnmpMetadataTable> tables) {
        this.tables = tables;
    }

    public SnmpMetadataObject addObject(final String name) {
        final Optional<SnmpMetadataObject> optionalEntry = objects.stream()
                .filter(e -> trimName(name).equals(e.getName()))
                .findAny();

        final SnmpMetadataObject snmpMetadataObject;

        if (optionalEntry.isPresent()) {
            snmpMetadataObject = optionalEntry.get();
        } else {
            snmpMetadataObject = new SnmpMetadataObject(name);
            snmpMetadataObject.setParent(this);
            this.objects.add(snmpMetadataObject);
        }

        return snmpMetadataObject;
    }

    public SnmpMetadataTable addTable(final String name) {
        final Optional<SnmpMetadataTable> optionalEntry = tables.stream()
                .filter(e -> trimName(name).equals(e.getName()))
                .findAny();

        final SnmpMetadataTable snmpMetadataTable;

        if (optionalEntry.isPresent()) {
            snmpMetadataTable = optionalEntry.get();
        } else {
            snmpMetadataTable = new SnmpMetadataTable(name);
            snmpMetadataTable.setParent(this);
            this.tables.add(snmpMetadataTable);
        }

        return snmpMetadataTable;
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

    public static SnmpMetadataBase fromOnmsMetadata(final List<OnmsMetaData> onmsMetaData, final String context) {
        final Map<String, String> map = onmsMetaData.stream()
                .filter(m -> context.equals(m.getContext()))
                .collect(Collectors.toMap(OnmsMetaData::getKey, OnmsMetaData::getValue));
        return createStructuredMetaData(new SnmpMetadataObject(context), map, null);
    }

    private static SnmpMetadataBase createStructuredMetaData(final SnmpMetadataBase structure, final Map<String, String> data, final String prefix) {
        final Set<String> prefixes = Sets.newConcurrentHashSet();

        if (data.containsKey(prefix)) {
            if (structure instanceof SnmpMetadataObject) {
                ((SnmpMetadataObject) structure).addValue(prefix, data.get(prefix));
            }
            if (structure instanceof SnmpMetadataEntry) {
                ((SnmpMetadataEntry) structure).addValue(prefix, data.get(prefix));
            }

            return structure;
        }

        for (final Map.Entry<String, String> dataEntry : data.entrySet()) {
            String key = dataEntry.getKey();
            if (!Strings.isNullOrEmpty(prefix)) {
                if (!dataEntry.getKey().startsWith(prefix)) {
                    continue;
                }

                if (key.length() > prefix.length() + 1) {
                    key = key.substring(prefix.length() + 1);
                }
            }

            final Matcher matcher = PATTERN_KEY.matcher(key);

            if (matcher.find() && !Strings.isNullOrEmpty(matcher.group())) {
                prefixes.add(matcher.group());
            }
        }

        for (final String p : prefixes) {
            final Matcher matcher = PATTERN_INDEX.matcher(p);
            if (matcher.find()) {
                final String newPrefix = (prefix == null ? "" : prefix + ".") + matcher.group(1);

                prefixes.removeIf(e -> e.startsWith(matcher.group(1)) && PATTERN_INDEX.matcher(e).find());

                final SnmpMetadataTable table = ((SnmpMetadataObject) structure).addTable(matcher.group(1));

                for (final Map.Entry<String, String> dataEntry : data.entrySet()) {
                    String key = dataEntry.getKey();
                    if (!Strings.isNullOrEmpty(newPrefix)) {
                        if (!key.startsWith(newPrefix)) {
                            continue;
                        }
                        if (key.length() > newPrefix.length()) {
                            key = key.substring(newPrefix.length() + 1).split("\\].")[0];
                        }
                    }
                    final SnmpMetadataEntry entry = table.addEntry(key);

                    final String prefixAndKey = newPrefix + "[" + key + "]";

                    createStructuredMetaData(entry, data.entrySet().stream().filter(e->e.getKey().startsWith(prefixAndKey)).collect(Collectors.toMap(e->e.getKey(), e->e.getValue())), prefixAndKey);
                }
            } else {
                final String newPrefix = (prefix == null ? "" : prefix + ".") + p;

                if (structure instanceof SnmpMetadataObject) {
                    createStructuredMetaData(((SnmpMetadataObject) structure).addObject(p), data.entrySet().stream().filter(e->e.getKey().startsWith(newPrefix)).collect(Collectors.toMap(e->e.getKey(), e->e.getValue())), newPrefix);
                }
                if (structure instanceof SnmpMetadataEntry) {
                    createStructuredMetaData(structure, data.entrySet().stream().filter(e->e.getKey().startsWith(newPrefix)).collect(Collectors.toMap(e->e.getKey(), e->e.getValue())), newPrefix);
                }
            }
        }

        return structure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SnmpMetadataObject that = (SnmpMetadataObject) o;
        return Objects.equals(name, that.name) && Objects.equals(objects, that.objects) && Objects.equals(values, that.values) && Objects.equals(tables, that.tables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, objects, values, tables);
    }
}
