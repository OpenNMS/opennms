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
package org.opennms.netmgt.measurements.filters.impl;

import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.Expression;
import org.opennms.core.utils.jexl.OnmsJexlEngine;
import org.opennms.core.utils.jexl.OnmsJexlSandbox;
import org.opennms.core.utils.jexl.OnmsJexlUberspect;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;

import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

/**
 * Generic JEXL expression filter
 *
 * @author jwhite
 */
@FilterInfo(name="JEXL", description="Generic JEXL expression filter")
public class JEXL implements Filter {

    @FilterParam(key="expression", required=true, displayName="Expression", description="JEXL expression.")
    private String m_expression;

    private final OnmsJexlEngine jexl;

    protected JEXL() {
        jexl = new OnmsJexlEngine();
        jexl.white(Math.class.getName());
        jexl.white(StrictMath.class.getName());
        jexl.white(TreeBasedTable.class.getName());

        // Add additional functions to the engine
        Map<String, Object> functions = Maps.newHashMap();
        functions.put("math", Math.class);
        functions.put("strictmath", StrictMath.class);
        jexl.setFunctions(functions);
    }

    public JEXL(String expression) {
        this();
        m_expression = expression;
    }

    @Override
    public void filter(RowSortedTable<Long, String, Double> qrAsTable)
            throws Exception {

        // Prepare the JEXL context
        final Map<String, Object> jexlValues = Maps.newHashMap();
        jexlValues.put("table", qrAsTable);
        final JexlContext context = new MapContext(jexlValues);

        // Compile the expression
        Expression expression = jexl.createExpression(m_expression);

        // Evaluate the expression
        expression.evaluate(context);
    }
}
