/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
