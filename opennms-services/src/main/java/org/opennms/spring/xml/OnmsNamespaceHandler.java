/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.spring.xml;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>OnmsNamespaceHandler class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class OnmsNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * <p>init</p>
     */
    @Override
    public void init() {
        registerBeanDefinitionParser("service", new OnmsServiceBeanDefinitionParser());
        registerBeanDefinitionDecorator("annotated-subscription", new AnnotatedSubscriptionBeanDefinitionDecorator());
    }
    
    public class OnmsServiceBeanDefinitionParser extends AbstractBeanDefinitionParser {

        @Override
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionHolder beanDefHolder = parserContext.getDelegate().parseBeanDefinitionElement(element);
            AbstractBeanDefinition def = (AbstractBeanDefinition)beanDefHolder.getBeanDefinition();
            return def;
        }

    }

    public class AnnotatedSubscriptionBeanDefinitionDecorator implements BeanDefinitionDecorator {

        @Override
        public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
            throw new UnsupportedOperationException("AnnotatedSubscriptionBeanDefinitionDecorator.decorate is not yet implemented");
        }

    }


}
