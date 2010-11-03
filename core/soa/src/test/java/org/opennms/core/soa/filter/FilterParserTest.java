/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.core.soa.filter;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.soa.Filter;


/**
 * FilterParserTest
 *
 * @author brozow
 */
public class FilterParserTest {
    
    private void parseFilterThenValidateString(String filterString) {
        parseFilterThenValidateString(filterString, filterString);
    }

    private void parseFilterThenValidateString(String filterString, String expectedString) {
        Filter filter = new FilterParser().parse(filterString);
        assertThat("Unable to parse filter", filter, notNullValue());
        assertThat("Unexpected string value for filter", filter.toString(), equalTo(expectedString));
    }

    @Test
    public void parseGreaterThanFilter() {
        parseFilterThenValidateString("(a>=2)");
    }

    @Test
    public void parseLessThanFilter() {
        parseFilterThenValidateString("(a<=2)");
    }

    @Test
    public void parseNotFilter() {
        parseFilterThenValidateString("(!(a<=1))");
    }
    @Test
    public void parseAndFilter() {
        parseFilterThenValidateString("(&(a>=1)(a<=2))");
    }
    @Test
    public void parseOrFilter() {
        parseFilterThenValidateString("(|(a>=1)(a<=2))");
    }
    @Test
    public void parseNestedFilter() {
        parseFilterThenValidateString("(| (&(a>=1)(a<=2)) (&(b>=1)(b<=2)) )", "(|(&(a>=1)(a<=2))(&(b>=1)(b<=2)))");
    }
    @Test
    public void parsePresenceFilter() {
        parseFilterThenValidateString("(a=*)");
    }
    @Test
    public void parseSimpleFilter() {
        parseFilterThenValidateString("(a=1)");
    }
    @Test
    public void parseSimpleFilterWithEscapedParen() {
        parseFilterThenValidateString("(a=\\))");
    }
    @Test
    public void parseSimpleFilterWithEscapedStar() {
        parseFilterThenValidateString("(a=\\*)");
    }
    @Test
    public void parseSimpleFilterWithEscapedBackslash() {
        parseFilterThenValidateString("(a=\\\\)");
    }
    @Test
    public void parsePatternMatchingFilter() {
        parseFilterThenValidateString("(a=a*c)");
    }
    @Test
    public void parsePatternMatchingFilterWithEscapedStar() {
        parseFilterThenValidateString("(a=a\\)*\\**c)");
    }
    
    @Test
    public void testAndFilter() {
        Map<String, String> dict = new HashMap<String, String>();
        dict.put("a", "2");
        dict.put("b", "4");
        dict.put("c", "x");
        
        FilterParser parser = new FilterParser();
        
        Filter a1 = parser.parse("(&(a=2)(b=4)(c=x))");
        Filter a2 = parser.parse("(&(a=2)(b=4)(z=nope))");

        assertThat("Expected " + a1 + " to match", a1.match(dict), is(true));
        assertThat("Expected " + a2 + " to match", a2.match(dict), is(false));
        
        
    }


}
