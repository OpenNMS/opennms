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
package org.opennms.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Random;

import org.junit.Test;
import org.owasp.encoder.Encode;

public class HtmlEncodingTest {

    @Test
    public void testInverseOfEncodeForHtml() {
        final String alphabet = "abcdefghijklmnopqrstuvwxyz&<>\"' #;0123456789";
        final Random random = new Random(20507);
        for (int n = 0; n < 1000; n++) {
            final StringBuilder s = new StringBuilder();
            final int length = random.nextInt(20);
            for (int i = 0; i < length; i++) {
                s.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            assertEquals(s.toString(), HtmlEncoding.decodeHtmlEncoded(Encode.forHtml(s.toString())));
        }
        for (final String s : new String[] { "", "O'Brien & Co", "&amp;", "&lt;b&gt;", "\"q\"", "&#39;", "a&", "&&amp;;" }) {
            assertEquals(s, HtmlEncoding.decodeHtmlEncoded(Encode.forHtml(s)));
        }
    }

    @Test
    public void testDecodesOnce() {
        assertEquals("&lt;", HtmlEncoding.decodeHtmlEncoded("&amp;lt;"));
    }

    @Test
    public void testLeavesOtherEntitiesAlone() {
        assertEquals("&nbsp;&#x27;&quot;", HtmlEncoding.decodeHtmlEncoded("&nbsp;&#x27;&quot;"));
    }

    @Test
    public void testNull() {
        assertNull(HtmlEncoding.decodeHtmlEncoded(null));
    }
}
