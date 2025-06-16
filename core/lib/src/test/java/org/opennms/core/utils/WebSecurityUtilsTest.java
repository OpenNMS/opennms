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

import org.junit.Test;

/**
 * 
 * <p>
 * WebSecurityUtilsTest class.
 * </p>
 * 
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 * 
 */
public class WebSecurityUtilsTest {

	@Test
	public void testBasicSanitizeString() {
		String script = "<script>foo</script>";
		String imgXss = "<img src=/ onerror=\"alert('XSS');\"></img>";
		String html = "<table>";
		script = WebSecurityUtils.sanitizeString(script);
		imgXss = WebSecurityUtils.sanitizeString(imgXss);
		html = WebSecurityUtils.sanitizeString(html);
		assertEquals("Script is sanitized", "&lt;script&gt;foo&lt;/script&gt;", script);
		assertEquals("IMG XSS is sanitized", "&lt;img src=/ onerror=&#34;alert(&#39;XSS&#39;);&#34;&gt;&lt;/img&gt;", imgXss);
		assertEquals("Html is sanitized", "&lt;table&gt;", html);
	}

	@Test
	public void testHTMLallowedSanitizeString() {
		String script = "<script>foo</script><p>valid</p>";
		String imgXss = "<img src=/ onerror=\"alert('XSS');\"></img>";
		String inputXss = "tst<input type=image src=123 onerror=alert(1)> ";
		String html = "<table></table>";
		script = WebSecurityUtils.sanitizeString(script, true);
		imgXss = WebSecurityUtils.sanitizeString(imgXss, true);
		inputXss = WebSecurityUtils.sanitizeString(inputXss, true);
		html = WebSecurityUtils.sanitizeString(html, true);
		assertEquals("Script is sanitized with HTML allowed", "<p>valid</p>", script);
		assertEquals("IMG XSS is sanitized with HTML allowed", "<img src=\"/\" />", imgXss);
		assertEquals("INPUT XSS is sanitized with HTML allowed", "tst ", inputXss);
		assertEquals("HtmlTable is sanitized with HTML allowed, so unchanged", "<table></table>", html);
	}

}
