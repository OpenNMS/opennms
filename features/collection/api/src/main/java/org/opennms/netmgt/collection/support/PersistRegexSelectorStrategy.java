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
package org.opennms.netmgt.collection.support;

import java.util.List;

import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.collection.api.PersistenceSelectorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * PersistRegexSelectorStrategy
 *
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
/*
 * TODO Implement "match-strategy" (allow/deny)
 * TODO Implement "match-behavior" (any/all)
 */
public class PersistRegexSelectorStrategy implements PersistenceSelectorStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(PersistRegexSelectorStrategy.class);

    public static final String MATCH_EXPRESSION = "match-expression";
    public static final String MATCH_STRATEGY = "match-strategy";
    public static final String MATCH_BEHAVIOR = "match-behavior";

    private List<Parameter> m_parameterCollection;

    protected static final class EvaluatorContextVisitor extends AbstractCollectionSetVisitor {
        private StandardEvaluationContext context;

        public EvaluatorContextVisitor() {
            context = new StandardEvaluationContext();
        }

        @Override
        public void visitAttribute(CollectionAttribute attribute) {
            if (!attribute.getType().isNumeric()) {
                context.setVariable(attribute.getName(), attribute.getStringValue());
            }
        }

        public StandardEvaluationContext getEvaluationContext() {
            return context;
        }
    }

    @Override
    public boolean shouldPersist(CollectionResource resource) {
        LOG.debug("shouldPersist: checking resource {}", resource);
        if (m_parameterCollection == null) {
            LOG.warn("shouldPersist: no parameters defined; the resource will be persisted.");
            return true;
        }
        EvaluatorContextVisitor visitor = new EvaluatorContextVisitor();
        resource.visit(visitor);
        visitor.getEvaluationContext().setVariable("instance", resource.getInstance());
        ExpressionParser parser = new SpelExpressionParser();
        for (Parameter param : m_parameterCollection) {
            if (param.getKey().equals(MATCH_EXPRESSION)) {
                Expression exp = parser.parseExpression(param.getValue());
                boolean shouldPersist = false;
                try {
                    shouldPersist = (Boolean)exp.getValue(visitor.getEvaluationContext(), Boolean.class);
                } catch (Exception e) {
                    LOG.warn("shouldPersist: can't evaluate expression {} for resource {} because: {}", param.getValue(), resource, e.getMessage());
                }
                LOG.debug("shouldPersist: checking {} ? {}", param.getValue(), shouldPersist);
                if (shouldPersist)
                    return true;
            }
        }
        return false;
    }

    @Override
    public void setParameters(List<Parameter> parameterCollection) {
        m_parameterCollection = parameterCollection;
    }

}
