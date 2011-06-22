/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.collectd;

import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.datacollection.Parameter;

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
        log().debug("shouldPersist: checking resource " + resource);
        if (m_parameterCollection == null) {
            log().warn("shouldPersist: no parameters defined; the resource will be persisted.");
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
                    log().warn("shouldPersist: can't evaluate expression " + param.getValue() + " for resource " + resource + " because: " + e.getMessage());
                }
                log().debug("shouldPersist: checking " + param.getValue() + " ? " + shouldPersist);
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

    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
