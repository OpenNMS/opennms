/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.collection.persistence.rrd;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;
import java.util.Locale;

import org.junit.Test;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersistOperationBuilder;

public class RrdPersistOperationBuilderTest {

    @Test
    public void canFormatNumbers() {
        assertEquals("2147483647", RrdPersistOperationBuilder.mapValue(Integer.MAX_VALUE));
        assertEquals("9223372036854775807", RrdPersistOperationBuilder.mapValue(Long.MAX_VALUE));
        assertEquals("-9223372036854775808", RrdPersistOperationBuilder.mapValue(Long.MIN_VALUE));
        assertEquals("2", RrdPersistOperationBuilder.mapValue(2.00d));
        assertEquals("2.0001", RrdPersistOperationBuilder.mapValue(2.0001d));

        assertEquals("U", RrdPersistOperationBuilder.mapValue(null));
        assertEquals("U", RrdPersistOperationBuilder.mapValue(Double.NaN));
        assertEquals("U", RrdPersistOperationBuilder.mapValue(Double.POSITIVE_INFINITY));
    }

    @Test
    public void canMapNumberToStringWithLocaleThatUsesCommasForDecimals() {
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.FRENCH);

        // Make sure we actually have a valid test
        NumberFormat nf = NumberFormat.getInstance();
        assertEquals("ensure that the newly set default locale (" + Locale.getDefault() + ") uses ',' as the decimal marker", "1,5", nf.format(1.5));  

        // The RRD persister should always use '.' instead of ','
        assertEquals("1.5", RrdPersistOperationBuilder.mapValue(1.5));

        Locale.setDefault(defaultLocale);
    }
}
