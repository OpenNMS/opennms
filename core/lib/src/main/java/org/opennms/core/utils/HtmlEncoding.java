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

/**
 * Reverses the HTML encoding that older versions applied to the text of users.xml (full names and contacts).
 */
public final class HtmlEncoding {

    private HtmlEncoding() {
    }

    /**
     * Replaces {@code &amp; &lt; &gt; &#34; &#39;} by the character they stand for, in one left-to-right pass, so
     * {@code &amp;lt;} becomes {@code &lt;}. These are the only entities {@code Encode.forHtml} writes for the
     * characters it changes in ASCII text. Any other text, including other entities, is left as it is.
     *
     * @return the decoded text; null for null
     */
    public static String decodeHtmlEncoded(final String text) {
        if (text == null || text.indexOf('&') < 0) {
            return text;
        }
        final StringBuilder result = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            final char c = text.charAt(i);
            if (c == '&') {
                if (text.startsWith("&amp;", i)) {
                    result.append('&');
                    i += 5;
                    continue;
                } else if (text.startsWith("&lt;", i)) {
                    result.append('<');
                    i += 4;
                    continue;
                } else if (text.startsWith("&gt;", i)) {
                    result.append('>');
                    i += 4;
                    continue;
                } else if (text.startsWith("&#34;", i)) {
                    result.append('"');
                    i += 5;
                    continue;
                } else if (text.startsWith("&#39;", i)) {
                    result.append('\'');
                    i += 5;
                    continue;
                }
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }
}
