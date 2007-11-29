package org.opennms.spring.xml;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class OnmsNamespaceHandler extends NamespaceHandlerSupport {

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

        public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
            throw new UnsupportedOperationException("AnnotatedSubscriptionBeanDefinitionDecorator.decorate is not yet implemented");
        }

    }


}
