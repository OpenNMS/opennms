/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021 The OpenNMS Group, Inc.
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
