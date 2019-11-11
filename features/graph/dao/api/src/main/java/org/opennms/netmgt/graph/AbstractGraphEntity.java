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
