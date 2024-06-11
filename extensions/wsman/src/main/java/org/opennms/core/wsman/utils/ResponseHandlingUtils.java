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
