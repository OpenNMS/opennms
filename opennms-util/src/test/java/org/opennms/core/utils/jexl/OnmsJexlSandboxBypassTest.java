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

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.junit.Test;

import com.google.common.collect.TreeBasedTable;

import java.util.HashMap;
import java.util.Map;

/**
 * Regression tests for the JEXL sandbox bypass reported against the Measurements REST API.
 *
 * These tests mirror exactly how {@code org.opennms.netmgt.measurements.filters.impl.JEXL}
 * configures the engine: Math/StrictMath/TreeBasedTable are whitelisted, and a
 * TreeBasedTable is exposed to user expressions as the variable {@code table}.
 *
 * Before the OnmsJexlUberspect fix, invoking a java.lang.Class method (getName, forName,
 * ...) on the Class object of a whitelisted type was authorized against the represented
 * class rather than java.lang.Class, letting expressions load arbitrary classes. Every
 * test here now expects the call to be blocked.
 */
public class OnmsJexlSandboxBypassTest {

    private OnmsJexlEngine engine() {
        final OnmsJexlEngine engine = new OnmsJexlEngine();
        engine.white(Math.class.getName());
        engine.white(StrictMath.class.getName());
        engine.white(TreeBasedTable.class.getName());
        return engine;
    }

    private JexlContext context() {
        final JexlContext ctx = new MapContext();
        ctx.set("table", TreeBasedTable.create());
        return ctx;
    }

    /**
     * BASELINE / INTENDED GUARD. Reaching the Class object through the *property* path
     * is correctly blocked: getPropertyGet() checks obj.getClass().getName(), which for
     * a Class object is "java.lang.Class" -- not whitelisted -> JexlException.
     * (This is the same assertion the existing OnmsJexlEngineTest#testClassAccess makes.)
     */
    @Test(expected = JexlException.class)
    public void propertyPathToClassIsBlocked() {
        engine().createExpression("table.class.name").evaluate(context());
    }

    /**
     * THE FIX. The *method* path reaches the same Class object as the property path, and is
     * now blocked too: getName() is a java.lang.Class method, so it is authorized against
     * java.lang.Class (not whitelisted) rather than the represented TreeBasedTable.
     */
    @Test(expected = JexlException.class)
    public void methodPathToClassIsBlocked() {
        engine().createExpression("table.getClass().getName()").evaluate(context());
    }

    /**
     * THE FIX. Class.forName(String) is a java.lang.Class method, so it can no longer be
     * reached through a whitelisted type's Class object -- arbitrary class-loading is denied.
     */
    @Test(expected = JexlException.class)
    public void forNameOnClassIsBlocked() {
        engine()
                .createExpression("table.getClass().forName('java.lang.Runtime')")
                .evaluate(context());
    }

    /**
     * A longer reflection chain stays blocked. With the fix it fails at the first
     * java.lang.Class method (forName), so nothing downstream is ever reached.
     */
    @Test(expected = JexlException.class)
    public void reflectionChainIsBlocked() {
        engine()
                .createExpression("table.getClass().forName('java.lang.Runtime').getMethods()")
                .evaluate(context());
    }

    /**
     * The classic RCE recipe forName -> getMethod -> invoke -> exec stays blocked, again at
     * the forName() call.
     */
    @Test(expected = JexlException.class)
    public void reflectiveExecChainIsBlocked() {
        engine()
                .createExpression("table.getClass().forName('java.lang.Runtime').getMethod('getRuntime')")
                .evaluate(context());
    }

    /**
     * REGRESSION GUARD for the fix. Static methods invoked through the math:/strictmath:
     * namespace functions (registered as Class objects) must still work -- these are the
     * legitimate reason getMethod() special-cases a Class receiver.
     */
    @Test
    public void namespaceFunctionsStillWork() {
        final OnmsJexlEngine engine = engine();
        final Map<String, Object> functions = new HashMap<>();
        functions.put("math", Math.class);
        functions.put("strictmath", StrictMath.class);
        engine.setFunctions(functions);

        final JexlContext ctx = context();
        ctx.set("a", 2.0);
        ctx.set("b", 5.0);
        assertEquals(2.0, engine.createExpression("math:min(a,b)").evaluate(ctx));
        assertEquals(5.0, engine.createExpression("strictmath:max(a,b)").evaluate(ctx));
    }
}
