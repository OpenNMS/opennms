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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.soa.Filter;


/**
 * FilterTest
 *
 * @author brozow
 */
public class FilterTest {
    
    @Test
    public void testNullProperties() {
        Filter findA = new PresenceFilter("a");
        assertThat("a is not in null map", findA.match(null), is(false));
    }

    @Test
    public void testPresence() {
       Map<String, String> dict = new HashMap<String, String>();
       dict.put("a", "abc");
       dict.put("b", "def");
       
       Filter findA = new PresenceFilter("a");
       Filter findZ = new PresenceFilter("z");
       
       assertThat("a is in the map", findA.match(dict), is(true));
       assertThat("z is not in the map", findZ.match(dict), is(false));
       
        
    }

    @Test
    public void tetSimpleEquals() {
       Map<String, String> dict = new HashMap<String, String>();
       dict.put("a", "abc");
       dict.put("b", "def");
       
       Filter isABC = new EqFilter("a", "abc");
       Filter isDEF = new EqFilter("a", "def");
       Filter isNotFound = new EqFilter("z", "abc");

       
       assertThat("a has the value abc", isABC.match(dict), is(true));
       assertThat("a is not def", isDEF.match(dict), is(false));
       assertThat("z is not in the tmap", isNotFound.match(dict), is(false));
       
        
    }

    @Test
    public void testPatternMatch() {
       Map<String, String> dict = new HashMap<String, String>();
       dict.put("a", "abc");
       dict.put("b", "defghi");
       
       Filter p1 = new PatternMatchingFilter("a", "a*c");
       Filter p2 = new PatternMatchingFilter("a", "*c");
       Filter p3 = new PatternMatchingFilter("a", "a*");
       Filter p4 = new PatternMatchingFilter("a", "d*f");
       Filter p5 = new PatternMatchingFilter("z", "a*c");
       Filter p6 = new PatternMatchingFilter("b", "d*f*i");
       
       assertThat("Expected " + p1 + " to match a=" + dict.get("a"), p1.match(dict), is(true));
       assertThat("Expected " + p2 + " to match a=" + dict.get("a"), p2.match(dict), is(true));
       assertThat("Expected " + p3 + " to match a=" + dict.get("a"), p3.match(dict), is(true));
       assertThat("Expected " + p4 + " to NOT match a=" + dict.get("a"), p4.match(dict), is(false));
       assertThat("Expected " + p5 + " to NOT match z=" + dict.get("z"), p5.match(dict), is(false));
       assertThat("Expected " + p6 + " to match b=" + dict.get("b"), p6.match(dict), is(true));
       
    }
    

    @Test
    public void testLessThan() {
       Map<String, String> dict = new HashMap<String, String>();
       dict.put("a", "2");
       dict.put("b", "4");
       dict.put("c", "x");
       
       Filter f1 = new LessThanFilter("a", "3");
       Filter f2 = new LessThanFilter("b", "3");
       Filter f3 = new LessThanFilter("z", "3");
       Filter f4 = new LessThanFilter("c", "y");
       Filter f5 = new LessThanFilter("c", "b");

       assertThat("Expected " + f1 + " to match a=" + dict.get("a"), f1.match(dict), is(true));
       assertThat("Expected " + f2 + " to match b=" + dict.get("b"), f2.match(dict), is(false));
       assertThat("Expected " + f3 + " to match z=" + dict.get("z"), f3.match(dict), is(false));
       assertThat("Expected " + f4 + " to match c=" + dict.get("c"), f4.match(dict), is(true));
       assertThat("Expected " + f5 + " to match c=" + dict.get("c"), f5.match(dict), is(false));
        
    }

    @Test
    public void testGreaterThan() {
        Map<String, String> dict = new HashMap<String, String>();
        dict.put("a", "2");
        dict.put("b", "4");
        dict.put("c", "x");
       
       Filter f1 = new GreaterThanFilter("a", "3");
       Filter f2 = new GreaterThanFilter("b", "3");
       Filter f3 = new GreaterThanFilter("z", "3");
       Filter f4 = new GreaterThanFilter("c", "y");
       Filter f5 = new GreaterThanFilter("c", "b");
       
       assertThat("Expected " + f1 + " to match a=" + dict.get("a"), f1.match(dict), is(false));
       assertThat("Expected " + f2 + " to match b=" + dict.get("b"), f2.match(dict), is(true));
       assertThat("Expected " + f3 + " to match z=" + dict.get("z"), f3.match(dict), is(false));
       assertThat("Expected " + f4 + " to match c=" + dict.get("c"), f4.match(dict), is(false));
       assertThat("Expected " + f5 + " to match c=" + dict.get("c"), f5.match(dict), is(true));
        
    }
    
    @Test
    public void testAndFilter() {
        Map<String, String> dict = new HashMap<String, String>();
        dict.put("a", "2");
        dict.put("b", "4");
        dict.put("c", "x");

        Filter f1 = new EqFilter("a", "2");
        Filter f2 = new EqFilter("b", "4");
        Filter f3 = new EqFilter("c", "x");
        Filter f4 = new EqFilter("z", "nope");
        
        Filter a1 = new AndFilter(f1, f2, f3);
        Filter a2 = new AndFilter(f1, f3, f4);
        
        assertThat("Expected " + a1 + " to match", a1.match(dict), is(true));
        assertThat("Expected " + a2 + " to match", a2.match(dict), is(false));
        
        
    }

    @Test
    public void testOrFilter() {
        Map<String, String> dict = new HashMap<String, String>();
        dict.put("a", "2");
        dict.put("b", "4");
        dict.put("c", "x");

        Filter f1 = new EqFilter("a", "1");
        Filter f2 = new EqFilter("a", "2");
        Filter f3 = new EqFilter("a", "3");
        Filter f4 = new EqFilter("a", "4");
        
        Filter o1 = new OrFilter(f1, f2, f3);
        Filter o2 = new OrFilter(f1, f3, f4);
        
        assertThat("Expected " + o1 + " to match", o1.match(dict), is(true));
        assertThat("Expected " + o2 + " to match", o2.match(dict), is(false));
        
        
    }

    @Test
    public void testNotFilter() {
        Map<String, String> dict = new HashMap<String, String>();
        dict.put("a", "2");
        dict.put("b", "4");
        dict.put("c", "x");

        Filter f1 = new EqFilter("a", "1");
        Filter f2 = new EqFilter("a", "2");
        Filter f3 = new EqFilter("a", "3");
        Filter f4 = new EqFilter("a", "4");
        
        Filter n1 = new NotFilter(f1);
        Filter n2 = new NotFilter(f2);
        Filter n3 = new NotFilter(f3);
        Filter n4 = new NotFilter(f4);

        
        assertThat("Expected " + n1 + " to match", n1.match(dict), not(f1.match(dict)));
        assertThat("Expected " + n2 + " to match", n2.match(dict), not(f2.match(dict)));
        assertThat("Expected " + n3 + " to match", n3.match(dict), not(f3.match(dict)));
        assertThat("Expected " + n4 + " to match", n4.match(dict), not(f4.match(dict)));

    }


}
