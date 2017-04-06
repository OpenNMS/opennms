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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import javax.management.openmbean.OpenType;

import org.opennms.netmgt.provision.service.jmx.annotation.CompositeField;
import org.opennms.netmgt.provision.service.jmx.annotation.Description;
import org.opennms.netmgt.provision.service.jmx.annotation.TableId;

import com.google.common.collect.Lists;

class FieldDescriptor {
    private String name;

    private String description;

    private OpenType type;

    private boolean id;

    private Function<Object, Object> valueFunction;

    protected FieldDescriptor(Field field) {
        this.name = ParserUtils.getName(field.getAnnotation(CompositeField.class), field.getName());
        this.description = ParserUtils.getDescription(field.getAnnotation(Description.class));
        this.type = ParserUtils.getType(field.getType());
        this.id = field.isAnnotationPresent(TableId.class);
        this.valueFunction = bean -> {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                return field.get(bean);
            } catch (IllegalAccessException access) {
                return null;
            }
        };
    }

    protected FieldDescriptor(Method method) {
        this.name = ParserUtils.getName(method.getAnnotation(CompositeField.class), method.getName());
        this.description = ParserUtils.getDescription(method.getAnnotation(Description.class));
        this.type = ParserUtils.getType(method.getReturnType());
        this.id = method.isAnnotationPresent(TableId.class);
        this.valueFunction = bean -> {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            try {
                return method.invoke(bean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        };
    }

    public FieldDescriptor(Class<?> beanType, PropertyDescriptor propertyDescriptor) {
        Field declaredField = getField(beanType, propertyDescriptor.getName());
        Method declaredMethod = propertyDescriptor.getReadMethod();

        this.type = ParserUtils.getType(propertyDescriptor.getPropertyType());

        this.name = Lists.newArrayList(
                declaredField != null ? ParserUtils.getName(declaredField.getAnnotation(CompositeField.class)) : null,
                ParserUtils.getName(declaredMethod.getAnnotation(CompositeField.class)),
                propertyDescriptor.getName()
        ).stream().filter(name -> name != null).findFirst().get();

        this.description = Lists.newArrayList(
                declaredField != null ? ParserUtils.getDescription(declaredField.getAnnotation(Description.class), null) : null,
                ParserUtils.getDescription(declaredMethod.getAnnotation(Description.class))
        ).stream().filter(name -> name != null).findFirst().get();

        this.id = Lists.newArrayList(
                declaredField != null ? declaredField.getAnnotation(TableId.class) : null,
                declaredMethod.getAnnotation(TableId.class)
        ).stream().filter(id -> id != null).findFirst().isPresent();

        this.valueFunction = bean -> {
            try {
                return propertyDescriptor.getReadMethod().invoke(bean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        };

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public OpenType getType() {
        return type;
    }

    public boolean isId() {
        return id;
    }

    public Object getValue(Object bean) {
        return valueFunction.apply(bean);
    }

    private static Field getField(Class type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
