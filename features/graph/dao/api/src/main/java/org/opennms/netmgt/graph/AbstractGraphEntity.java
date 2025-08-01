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
package org.opennms.netmgt.graph;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

@Entity(name="graph_elements")
@Table(name="graph_elements")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type", discriminatorType = DiscriminatorType.STRING)
public class AbstractGraphEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "graphSequence")
    @SequenceGenerator(name = "graphSequence", sequenceName = "graphnxtid")
    @Column(name = "id", nullable = false)
    private Long dbId;

    @Column(name = "namespace", nullable = false)
    private String namespace;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "element_id", referencedColumnName = "id", nullable = false, updatable = true)
    @BatchSize(size=1000)
    private List<PropertyEntity> properties = new ArrayList<>();

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<PropertyEntity> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyEntity> properties) {
        this.properties = properties;
    }

    public PropertyEntity getProperty(String key) {
        return properties.stream().filter(p -> p.getName().equalsIgnoreCase(key)).findFirst().orElse(null);
    }

    public void mergeProperties(List<PropertyEntity> propertyEntities) {
        for (PropertyEntity propertyEntity : propertyEntities) {
            final PropertyEntity alreadyExisting = getProperty(propertyEntity.getName());
            if (alreadyExisting != null) {
                alreadyExisting.setType(propertyEntity.getType());
                alreadyExisting.setValue(propertyEntity.getValue());
            } else {
                getProperties().add(propertyEntity);
            }
        }
        // This may be caused if a property name has changed. So we remove all non existing names
        if (getProperties().size() != propertyEntities.size()) {
            final List<String> newPropertyNames = propertyEntities.stream().map(pe -> pe.getName()).collect(Collectors.toList());
            final List<String> allPropertyNames = getProperties().stream().map(pe -> pe.getName()).collect(Collectors.toList());
            allPropertyNames.removeAll(newPropertyNames);
            // Now remove all
            allPropertyNames.forEach(propertyName -> getProperties().remove(getProperty(propertyName)));
        }
    }

    public void setProperty(String key, Class<?> type, String stringValue) {
        final PropertyEntity existingProperty = getProperty(key);
        if (existingProperty != null) {
            if (stringValue == null) { // We must remove the property
                properties.remove(existingProperty);
            } else { // Update property
                existingProperty.setType(type);
                existingProperty.setValue(stringValue);
            }
        } else if (stringValue != null) { // only persist if value is not null
            final PropertyEntity propertyEntity = new PropertyEntity();
            propertyEntity.setType(type);
            propertyEntity.setName(key);
            propertyEntity.setValue(stringValue);
            properties.add(propertyEntity);
        }
    }

    protected String getPropertyValue(String key) {
        final PropertyEntity property = getProperty(key);
        if (property != null) {
            return property.getValue();
        }
        return null;
    }
}
