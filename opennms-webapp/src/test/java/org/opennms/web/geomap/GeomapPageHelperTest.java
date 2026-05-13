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
package org.opennms.web.geomap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GeomapPageHelperTest {

    @Test
    public void paramAsJsString_returnsBareNullLiteralWhenNull() {
        // Important: the bare token "null" (no quotes) -- the client
        // side isUndefinedOrNull check fires only against the JS null
        // value, not against the string "null".
        assertEquals("null", GeomapPageHelper.paramAsJsString(null));
    }

    @Test
    public void paramAsJsString_quotesNormalValue() {
        assertEquals("\"Alarms\"", GeomapPageHelper.paramAsJsString("Alarms"));
    }

    @Test
    public void paramAsJsString_quotesEmptyString() {
        // Empty is a real (if useless) value, distinct from null.
        assertEquals("\"\"", GeomapPageHelper.paramAsJsString(""));
    }

    @Test
    public void paramAsJsString_sanitizesQuoteChar() {
        // A bare " in the param value would close the JS string
        // literal early. WebSecurityUtils.sanitizeString HTML-encodes
        // it to &#34;. Inside a <script> block the HTML parser does
        // not decode entities, so the JS sees the literal characters
        // a, &, #, 3, 4, ;, b -- malformed input, but no JS injection.
        assertEquals("\"a&#34;b\"", GeomapPageHelper.paramAsJsString("a\"b"));
    }

    @Test
    public void paramAsJsString_sanitizesAngleBrackets() {
        assertEquals(
                "\"&lt;script&gt;\"",
                GeomapPageHelper.paramAsJsString("<script>"));
    }

    @Test
    public void paramAsJsBoolean_trueCaseInsensitive() {
        assertEquals("true", GeomapPageHelper.paramAsJsBoolean("true"));
        assertEquals("true", GeomapPageHelper.paramAsJsBoolean("True"));
        assertEquals("true", GeomapPageHelper.paramAsJsBoolean("TRUE"));
    }

    @Test
    public void paramAsJsBoolean_anythingElseIsFalse() {
        assertEquals("false", GeomapPageHelper.paramAsJsBoolean(null));
        assertEquals("false", GeomapPageHelper.paramAsJsBoolean(""));
        assertEquals("false", GeomapPageHelper.paramAsJsBoolean("false"));
        assertEquals("false", GeomapPageHelper.paramAsJsBoolean("yes"));
        assertEquals("false", GeomapPageHelper.paramAsJsBoolean("1"));
        assertEquals("false", GeomapPageHelper.paramAsJsBoolean(" true "));
    }
}
