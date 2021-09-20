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

package org.opennms.netmgt.flows.classification.internal.value;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StringValueTest {

    @Test
    public void verifyStringValue() {
        // "Normal" value
        StringValue value = new StringValue("test");
        assertThat(value.getValue(), is("test"));
        assertThat(value.isRanged(), is(false));
        assertThat(value.hasWildcard(), is(false));
        assertThat(value.isNull(), is(false));
        assertThat(value.isEmpty(), is(false));
        assertThat(value.isNullOrEmpty(), is(false));

        // "" (empty) value
        value = new StringValue("");
        assertThat(value.getValue(), is(""));
        assertThat(value.isRanged(), is(false));
        assertThat(value.hasWildcard(), is(false));
        assertThat(value.isNull(), is(false));
        assertThat(value.isEmpty(), is(true));
        assertThat(value.isNullOrEmpty(), is(true));

        // "null" value
        value = new StringValue(null);
        assertThat(value.getValue(), nullValue());
        assertThat(value.isRanged(), is(false));
        assertThat(value.hasWildcard(), is(false));
        assertThat(value.isNull(), is(true));
        assertThat(value.isEmpty(), is(false));
        assertThat(value.isNullOrEmpty(), is(true));

        // * (wildcard) value
        value = new StringValue("test*");
        assertThat(value.getValue(), is("test*"));
        assertThat(value.isRanged(), is(false));
        assertThat(value.hasWildcard(), is(true));
        assertThat(value.isNull(), is(false));
        assertThat(value.isEmpty(), is(false));
        assertThat(value.isNullOrEmpty(), is(false));

        // - (ranged) value
        value = new StringValue("80-100");
        assertThat(value.getValue(), is("80-100"));
        assertThat(value.isRanged(), is(true));
        assertThat(value.hasWildcard(), is(false));
        assertThat(value.isNull(), is(false));
        assertThat(value.isEmpty(), is(false));
        assertThat(value.isNullOrEmpty(), is(false));
    }

}