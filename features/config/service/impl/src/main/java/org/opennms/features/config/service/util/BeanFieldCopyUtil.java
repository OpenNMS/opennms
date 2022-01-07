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

package org.opennms.features.config.service.util;

import org.opennms.features.config.exception.ConfigRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This util simply copy all field. (Shallow copy only.)
 * It solves pojo class fail to copy properties issue due to non-pair getter & setter (e.g. Optional vs primitive type)
 * e.g. BeanUtils.copyProperties()
 */
public class BeanFieldCopyUtil {
    // disable constructor
    private BeanFieldCopyUtil() {
    }

    public static <E> void copyFields(E source, E target) {
        if (source == null || target == null) {
            throw new ConfigRuntimeException("Source or target cannot be null.");
        }
        Iterable<Field> allFields = getAllFields(source.getClass());
        allFields.forEach(field -> {
            try {
                copyValue(source, target, field);
            } catch (IllegalAccessException e) {
                throw new ConfigRuntimeException("Failed to copy field value.", e);
            }
        });
    }

    private static <E> void copyValue(E source, E target, Field field) throws IllegalAccessException {
        // temp ignore accessible since we are copying field, assume that the source fields are all passing the setter logic already
        field.setAccessible(true);
        field.set(target, field.get(source));
    }

    public static Iterable<Field> getAllFields(Class<?> sourceClass) {
        return getAllFields(sourceClass, null);
    }

    /**
     * Return all fields in the class and all related parent class.
     * It will skip final & static fields.
     * @param sourceClass
     * @param currentParentClass
     * @return list of all fields
     */
    public static Iterable<Field> getAllFields(Class<?> sourceClass, Class<?> currentParentClass) {
        List<Field> currentClassFields = Arrays.stream(sourceClass.getDeclaredFields()).filter(
                f -> (!Modifier.isFinal(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())))
                .collect(Collectors.toList());

        Class<?> parentClass = sourceClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(currentParentClass))) {
            List<Field> parentClassFields =
                    (List<Field>) getAllFields(parentClass, currentParentClass);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }
}
