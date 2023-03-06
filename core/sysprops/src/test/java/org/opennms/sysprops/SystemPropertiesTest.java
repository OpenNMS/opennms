/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.sysprops;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

import org.junit.Test;
import org.opennms.core.sysprops.SystemProperties;

import java.math.BigDecimal;


public class SystemPropertiesTest {

    private final static String KEY = SystemProperties.class.getName();

    @Test
    public void shouldResolveBigDecimalValues() {
        BigDecimal defaultValue = new BigDecimal(1.5);

        System.clearProperty(KEY);
        assertNull(SystemProperties.getBigDecimal(KEY));
        assertEquals(defaultValue, SystemProperties.getBigDecimal(KEY, defaultValue));

        System.setProperty(KEY, "2");
        assertEquals(new BigDecimal(2), SystemProperties.getBigDecimal(KEY));
        assertEquals(new BigDecimal(2), SystemProperties.getBigDecimal(KEY, defaultValue));
    }

    @Test
    public void shouldResolveLongValues(){
        Long defaultValue = 13L;

        System.clearProperty(KEY);
        assertNull(SystemProperties.getLong(KEY));
        assertEquals(defaultValue, SystemProperties.getLong(KEY, defaultValue));

        System.setProperty(KEY, "42");
        assertEquals(Long.valueOf(42L), SystemProperties.getLong(KEY));
        assertEquals(Long.valueOf(42L), SystemProperties.getLong(KEY, defaultValue));
    }

    @Test
    public void shouldResolveIntegerValues(){
        Integer defaultValue = 13;

        System.clearProperty(KEY);
        assertNull(SystemProperties.getLong(KEY));
        assertEquals(defaultValue, SystemProperties.getInteger(KEY, defaultValue));

        System.setProperty(KEY, "42");
        assertEquals(Integer.valueOf(42), SystemProperties.getInteger(KEY));
        assertEquals(Integer.valueOf(42), SystemProperties.getInteger(KEY, defaultValue));
    }
}
