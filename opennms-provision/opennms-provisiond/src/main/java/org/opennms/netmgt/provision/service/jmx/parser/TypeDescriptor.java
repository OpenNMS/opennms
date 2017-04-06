/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.jmx.parser;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularType;

import org.opennms.netmgt.provision.service.jmx.annotation.Composite;
import org.opennms.netmgt.provision.service.jmx.annotation.CompositeField;
import org.opennms.netmgt.provision.service.jmx.annotation.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeDescriptor {

    private static final Logger LOG = LoggerFactory.getLogger(TypeDescriptor.class);

    private final Class<?> type;
    private List<FieldDescriptor> fieldDescriptors;
    private CompositeType compositeType;
    private TabularType tabularType;

    public TypeDescriptor(Class<?> type) {
        this.type = Objects.requireNonNull(type);
    }

    public List<FieldDescriptor> getFieldDescriptors() {
        if (fieldDescriptors == null) {
            fieldDescriptors = buildFieldDescriptors();
        }
        return fieldDescriptors;
    }

    public String getDescription() {
        return ParserUtils.getDescription(type.getAnnotation(Description.class));
    }

    public String getName() {
        return ParserUtils.getName(type.getAnnotation(Composite.class), type.getSimpleName());
    }

    public CompositeType getCompositeType() {
        if (compositeType == null) {
            compositeType = buildCompositeType();
        }
        return compositeType;
    }

    private CompositeType buildCompositeType() {
        try {
            CompositeType compositeType = new CompositeType(
                    getName(),
                    getDescription(),
                    getFieldNames(),
                    getFieldDescriptions(),
                    getFieldTypes()
            );
            return compositeType;
        } catch (OpenDataException openDataException) {
            LOG.error("Error creating composite type", openDataException);
        }
        return null;
    }

    public TabularType getTabularType() {
        if (tabularType == null) {
            tabularType = buildTabularType();
        }
        return tabularType;
    }

    private TabularType buildTabularType() {
        final CompositeType rowType = getCompositeType();
        if (rowType != null) {
            try {
                TabularType tabularType = new TabularType(
                        String.format("%s.table", getName()),
                        String.format("Table for %s elements", getName()),
                        rowType,
                        getIdFields());
                return tabularType;
            } catch (OpenDataException openDataException) {
                LOG.error("Error creating tabular type", openDataException);
            }
        }
        return null;
    }

    public CompositeData getCompositeData(Object dataBean) {
        Objects.requireNonNull(dataBean);
        if (dataBean.getClass() != type) {
            throw new IllegalArgumentException("Provided bean is not of type " + type);
        }
        final CompositeType compositeType = getCompositeType();
        if (compositeType != null) {
            try {
                return new CompositeDataSupport(compositeType, getFieldNames(), getFieldValues(dataBean));
            } catch (OpenDataException e) {
                LOG.error("Error creating composite data support", e);
            }
        }
        return null;
    }

    private List<FieldDescriptor> buildFieldDescriptors() {
        // The field descriptors are build based on JAVA BEAN properties
        final Map<String, FieldDescriptor> fieldDescriptorMap = new HashMap<>();
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(type);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (propertyDescriptor.getReadMethod() != null
                        && ParserUtils.isSupportedType(propertyDescriptor.getReadMethod().getReturnType())) {
                    FieldDescriptor fieldDescriptor = new FieldDescriptor(type, propertyDescriptor);
                    fieldDescriptorMap.put(fieldDescriptor.getName(), fieldDescriptor);
                }
            }
        } catch (IntrospectionException ex) {
           LOG.error("Could not introspect bean {}", type, ex);
        }

        // Afterwards add fields and methods if they are not yet set
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(CompositeField.class)) {
                FieldDescriptor descriptor = new FieldDescriptor(field);
                if (!fieldDescriptorMap.containsKey(descriptor.getName())) {
                    if (!ParserUtils.isSupportedType(field.getType())) {
                        LOG.info("Type {} not supported. Skipping.", field.getType());
                        continue;
                    }
                    fieldDescriptorMap.put(descriptor.getName(), descriptor);
                }
            }
        }
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CompositeField.class)) {
                FieldDescriptor descriptor = new FieldDescriptor(method);
                if (!fieldDescriptorMap.containsKey(descriptor.getName())) {
                    if (!ParserUtils.isSupportedType(method.getReturnType())) {
                        LOG.info("Type {} not supported. Skipping.", method.getReturnType());
                        continue;
                    }
                    fieldDescriptorMap.put(descriptor.getName(), descriptor);
                }
            }
        }
        return new ArrayList<>(fieldDescriptorMap.values());
    }

    private String[] getFieldNames() {
        return getFieldDescriptors().stream()
                .map(fieldDescriptor -> fieldDescriptor.getName())
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }

    private String[] getFieldDescriptions() {
        return getFieldDescriptors().stream()
                .map(fieldDescriptor -> fieldDescriptor.getDescription())
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }

    private OpenType<?>[] getFieldTypes() {
        return getFieldDescriptors().stream()
                .map(fieldDescriptor -> fieldDescriptor.getType())
                .collect(Collectors.toList())
                .toArray(new OpenType<?>[0]);
    }

    private String[] getIdFields() {
        final List<String> idList = getFieldDescriptors().stream()
                .filter(fieldDescriptor -> fieldDescriptor.isId())
                .map(fieldDescriptor -> fieldDescriptor.getName())
                .collect(Collectors.toList());
        if (idList.isEmpty()) { // if no id is there, use all fields as id
            return getFieldNames();
        }
        return idList.toArray(new String[0]);
    }

    private Object[] getFieldValues(Object bean) {
        return getFieldDescriptors().stream()
                .map(fieldDescriptor -> fieldDescriptor.getValue(bean))
                .collect(Collectors.toList())
                .toArray();
    }


}
