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
