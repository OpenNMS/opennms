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
