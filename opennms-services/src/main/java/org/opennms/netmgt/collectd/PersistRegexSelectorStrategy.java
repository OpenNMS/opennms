/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.util.List;

import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.datacollection.Parameter;
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

    protected class EvaluatorContextVisitor extends AbstractCollectionSetVisitor {
        private StandardEvaluationContext context;

        public EvaluatorContextVisitor() {
            context = new StandardEvaluationContext();
        }

        @Override
        public void visitAttribute(CollectionAttribute attribute) {
            if (StringAttributeType.supportsType(attribute.getType()))
                context.setVariable(attribute.getName(), attribute.getStringValue());
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
        ExpressionParser parser = new SpelExpressionParser();
        for (Parameter param : m_parameterCollection) {
            if (param.getKey().equals(MATCH_EXPRESSION)) {
                Expression exp = parser.parseExpression(param.getValue());
                boolean shouldPersist = false;
                try {
                    shouldPersist = exp.getValue(visitor.getEvaluationContext(), Boolean.class);
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
