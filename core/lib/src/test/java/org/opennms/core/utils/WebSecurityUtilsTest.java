/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

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
		String html = "<table>";
		script = WebSecurityUtils.sanitizeString(script);
		html = WebSecurityUtils.sanitizeString(html);
		assertTrue("Script is sanitized",
				script.equals("&lt;&#x73;cript&gt;foo&lt;/&#x73;cript&gt;"));
		assertTrue("Html is sanitized", html.equals("&lt;table&gt;"));
	}

	@Test
	public void testHTMLallowedSanitizeString() {
		String script = "<script>foo</script>";
		String html = "<table>";
		script = WebSecurityUtils.sanitizeString(script, true);
		html = WebSecurityUtils.sanitizeString(html, true);
		assertTrue("Script is sanitized with HTML allowed",
				script.equals("<&#x73;cript>foo</&#x73;cript>"));
		assertTrue("HtmlTable is sanitized with HTML allowed, so unchanged",
				html.equals("<table>"));
	}

	@Test
	public void testBasicBeanSanitizer() {
		CommandBeanMockup bean = new CommandBeanMockup();
		bean = (CommandBeanMockup) WebSecurityUtils
				.sanitizeBeanStringProperties(bean, null);

		assertTrue("Script property is sanitized",
				WebSecurityUtils.sanitizeString("<script>foo</script>", false)
						.equals(bean.getScript()));
		assertTrue("Script property is not sanitized with Html allowed",
				!WebSecurityUtils.sanitizeString("<script>foo</script>", true)
						.equals(bean.getScript()));

		assertTrue("HtmlTable is sanitized and html removed", WebSecurityUtils
				.sanitizeString("<table>", false).equals(bean.getHtmlTable()));
		assertTrue(
				"Not, HtmlTable is sanitized with Html allowed",
				!WebSecurityUtils.sanitizeString("<table>", true).equals(
						bean.getHtmlTable()));
	}

	@Test
	public void testBeanSanitizerWithHtmlAllowList() {
		CommandBeanMockup bean = new CommandBeanMockup();
		HashSet<String> set = new HashSet<String>();
		set.add("htmltable");
		bean = (CommandBeanMockup) WebSecurityUtils
				.sanitizeBeanStringProperties(bean, set);

		assertTrue("Script property is sanitized no Html allowed",
				WebSecurityUtils.sanitizeString("<script>foo</script>", false)
						.equals(bean.getScript()));
		assertTrue("Not, Script property is sanitized with Html allowed",
				!WebSecurityUtils.sanitizeString("<script>foo</script>", true)
						.equals(bean.getScript()));

		assertTrue(
				"HtmlTable is sanitzied with Html allowed so, no changes",
				WebSecurityUtils.sanitizeString("<table>", true).equals(
						bean.getHtmlTable()));
		assertTrue(
				"Not, HtmlTable is sanitized and html removed",
				!WebSecurityUtils.sanitizeString("<table>", false).equals(
						bean.getHtmlTable()));
	}
}
