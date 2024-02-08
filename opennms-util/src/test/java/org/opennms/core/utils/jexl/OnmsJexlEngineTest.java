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
package org.opennms.core.utils.jexl;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.junit.Test;

public class OnmsJexlEngineTest {

    @Test
    public void testVariable() {
        final OnmsJexlEngine onmsJexlEngine = new OnmsJexlEngine();
        final JexlContext jexlContext = new MapContext();
        jexlContext.set("A", 2.0);
        jexlContext.set("A.x", 5.0);
        assertEquals(10.0, onmsJexlEngine.createExpression("A * A.x").evaluate(jexlContext));
    }

    @Test
    public void testWhitelisting() {
        final OnmsJexlEngine onmsJexlEngine = new OnmsJexlEngine();
        onmsJexlEngine.white(Foo.class.getName());
        onmsJexlEngine.white(Bar.class.getName());
        final JexlContext jexlContext = new MapContext();
        jexlContext.set("A", new Foo());
        jexlContext.set("B", new Bar());
        jexlContext.set("C", 2);
        assertEquals(2 * 42 * 10, onmsJexlEngine.createExpression("A.value * B.value * C").evaluate(jexlContext));
    }

    @Test(expected = JexlException.class)
    public void testMissingClass() {
        final OnmsJexlEngine onmsJexlEngine = new OnmsJexlEngine();
        onmsJexlEngine.white(Foo.class.getName());
        final JexlContext jexlContext = new MapContext();
        jexlContext.set("A", new Foo());
        jexlContext.set("B", new Bar());
        jexlContext.set("C", 2);
        assertEquals(2 * 42 * 10, onmsJexlEngine.createExpression("A.value * B.value * C").evaluate(jexlContext));
    }

    @Test(expected = JexlException.class)
    public void testMissingFunctions() {
        final OnmsJexlEngine onmsJexlEngine = new OnmsJexlEngine();
        final Map<String, Object> fn = new HashMap<>();
        onmsJexlEngine.setFunctions(fn);
        final JexlContext jexlContext = new MapContext();
        jexlContext.set("A", 2.0);
        jexlContext.set("B", 5.0);
        assertEquals(2.0, onmsJexlEngine.createExpression("math:min(A,B)").evaluate(jexlContext));
    }

    @Test
    public void testFunctions() {
        final OnmsJexlEngine onmsJexlEngine = new OnmsJexlEngine();
        onmsJexlEngine.white(Math.class.getName());
        final Map<String, Object> fn = new HashMap<>();
        fn.put("math", Math.class);
        onmsJexlEngine.setFunctions(fn);
        final JexlContext jexlContext = new MapContext();
        jexlContext.set("A", 2.0);
        jexlContext.set("B", 5.0);
        assertEquals(2.0, onmsJexlEngine.createExpression("math:min(A,B)").evaluate(jexlContext));
    }

    @Test(expected = JexlException.class)
    public void testClassAccess() {
        final OnmsJexlEngine onmsJexlEngine = new OnmsJexlEngine();
        onmsJexlEngine.white(Foo.class.getName());
        final JexlContext jexlContext = new MapContext();
        jexlContext.set("A", new Foo());
        assertEquals(10.0, onmsJexlEngine.createExpression("A.class.name").evaluate(jexlContext));
    }

    public static class Foo {
        public int getValue() {
            return 42;
        }
    }

    public static class Bar {
        public int getValue() {
            return 10;
        }
    }
}
