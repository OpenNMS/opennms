/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.wsman.utils;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Utility functions for handling the results of WS-Man Get and Enumerate calls.
 * 
 * @author jwhite
 */
public class ResponseHandlingUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseHandlingUtils.class);

    public static ListMultimap<String, String> toMultiMap(Node node) {
        ListMultimap<String, String> elementValues = ArrayListMultimap.create();

        // Parse the values from the child nodes
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getLocalName() == null || child.getTextContent() == null) {
                continue;
            }

            elementValues.put(child.getLocalName(), child.getTextContent());
        }
        
        return elementValues;
    }

    public static int getMatchingIndex(String spelExpression, ListMultimap<String, String> values) throws NoSuchElementException {
        // Compile the expression
        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression(spelExpression);
        // Recursively check all levels, defaulting to the first element
        return getMatchingIndex(exp, values, 0);
    }

    private static int getMatchingIndex(Expression exp, ListMultimap<String, String> valuesByName, int depth) throws NoSuchElementException {
        // Build the context with values from all the attributes at the current depth
        final StandardEvaluationContext context = new StandardEvaluationContext();
        int maxDepth = 0;
        for (String name : valuesByName.keySet()) {
            List<String> values = valuesByName.get(name);
            // Keep track of the largest depth
            maxDepth = Math.max(maxDepth, values.size());

            // Skip the variable if there are no values are the current depth
            if (values.size() < depth + 1) {
                continue;
            }

            // Store the value for the current depth in the context
            context.setVariable(name, values.get(depth));
        }

        // Evaluate our expression
        try {
            if (exp.getValue(context, Boolean.class)) {
                return depth;
            }
        } catch (Exception e) {
            LOG.error("Failed to evaluate expression {}. Msg: {}", exp.getExpressionString(), e.getMessage());
            throw new NoSuchElementException();
        }

        if (maxDepth > depth) {
            // Recurse
            return getMatchingIndex(exp, valuesByName, depth+1);
        }

        throw new NoSuchElementException();
    }

    public static boolean matchesFilter(String spelFilter, ListMultimap<String, String> valuesByName) {
        // Compile the expression
        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression(spelFilter);

        // Build the context with the first values for all of the attributes
        final StandardEvaluationContext context = new StandardEvaluationContext();
        for (String name : valuesByName.keySet()) {
            final List<String> values = valuesByName.get(name);
            if (values.size() > 0) {
                context.setVariable(name, values.get(0));
            }
        }

        // Evaluate our expression
        try {
            return exp.getValue(context, Boolean.class);
        } catch (Exception e) {
            LOG.error("Failed to evaluate expression {}. Assuming match is negative. Msg: {}", exp.getExpressionString(), e.getMessage());
            throw Throwables.propagate(e);
        }
    }
}
