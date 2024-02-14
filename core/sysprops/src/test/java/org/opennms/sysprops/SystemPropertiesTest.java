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
