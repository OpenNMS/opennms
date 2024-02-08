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
package org.opennms.features.collection.persistence.rrd;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;
import java.util.Locale;

import org.junit.Test;
import org.opennms.netmgt.collection.api.AttributeType;
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

        assertEquals("2", RrdPersistOperationBuilder.mapValue(2.0000d, AttributeType.COUNTER));
        assertEquals("2", RrdPersistOperationBuilder.mapValue(2.0000d, AttributeType.GAUGE));
        assertEquals("2", RrdPersistOperationBuilder.mapValue(2.0001d, AttributeType.COUNTER));
        assertEquals("2.0001", RrdPersistOperationBuilder.mapValue(2.0001d, AttributeType.GAUGE));
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
