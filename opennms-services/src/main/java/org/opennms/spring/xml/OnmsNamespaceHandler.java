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
    
    public static class OnmsServiceBeanDefinitionParser extends AbstractBeanDefinitionParser {

        @Override
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionHolder beanDefHolder = parserContext.getDelegate().parseBeanDefinitionElement(element);
            return (AbstractBeanDefinition)beanDefHolder.getBeanDefinition();
        }

    }

    public static class AnnotatedSubscriptionBeanDefinitionDecorator implements BeanDefinitionDecorator {

        @Override
        public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
            throw new UnsupportedOperationException("AnnotatedSubscriptionBeanDefinitionDecorator.decorate is not yet implemented");
        }

    }


}
