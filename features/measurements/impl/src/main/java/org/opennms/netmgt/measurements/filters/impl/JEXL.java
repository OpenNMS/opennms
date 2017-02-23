/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.filters.impl;

import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.Expression;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;

import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;

/**
 * Generic JEXL expression filter
 *
 * @author jwhite
 */
@FilterInfo(name="JEXL", description="Generic JEXL expression filter")
public class JEXL implements Filter {

    @FilterParam(key="expression", required=true, displayName="Expression", description="JEXL expression.")
    private String m_expression;

    private final JexlEngine jexl;

    protected JEXL() {
        jexl = new JexlEngine();
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
