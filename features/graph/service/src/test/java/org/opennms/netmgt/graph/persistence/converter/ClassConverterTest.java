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

package org.opennms.netmgt.graph.persistence.converter;

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ClassConverterTest {

    // Verifies that a class can converted
    @Test
    public void verifyClassConversion() {
        final Converter<Class> converter = new ClassConverter();
        assertThat(converter.canConvert(Class.class), Matchers.is(true));
    }

    // While introducing the ClassConverter it could convert all types by accident.
    // To avoid this: verify that all known other types are not convertable.
    @Test
    public void verifyCannotConvertAllTypes() {
        final Converter<Class> converter = new ClassConverter();
        final List<Class> typeList = Lists.newArrayList(Boolean.class, Integer.class, String.class, Float.class, Double.class, Short.class, Byte.class, Enum.class);

        // Verify cannot convert "normal" types
        for (Class type : typeList) {
            assertThat(converter.canConvert(type), Matchers.is(false));
        }
        // Verify cannot convert "normal" collections
        for (Class type : typeList) {
            final List genericList = Collections.checkedList(new ArrayList<>(), type);
            assertThat(converter.canConvert(genericList.getClass()), Matchers.is(false));
        }

        // Verify cannot convert with a concrete example
        final List<String> listValue = ImmutableList.of("E", "F");
        assertThat(converter.canConvert(listValue.getClass()), Matchers.is(false));

        // Also verify that a List of classes cannot be converted
        final List<Class> classList = ImmutableList.of(ClassConverter.class, ClassConverterTest.class);
        assertThat(converter.canConvert(classList.getClass()), Matchers.is(false));
    }

}