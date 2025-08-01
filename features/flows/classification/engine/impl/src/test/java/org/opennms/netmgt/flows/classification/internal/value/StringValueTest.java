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