package org.opennms.core.spring.web;

import javax.servlet.ServletContext;

import org.opennms.core.spring.BeanFactoryReference;
import org.opennms.core.spring.ContextRegistry;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * Custom ContextLoaderListener that integrates with our Spring 5-compatible ContextRegistry
 * instead of the removed SingletonBeanFactoryLocator.
 */
public class ContextLoaderListener extends org.springframework.web.context.ContextLoaderListener {

    @Override
    protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
        // Get the parent context key from web.xml
        String parentContextKey = sc.getInitParameter("parentContextKey");
        
        ConfigurableWebApplicationContext context = (ConfigurableWebApplicationContext) super.createWebApplicationContext(sc);
        
        // If parentContextKey is specified, set the parent context from our ContextRegistry
        if (parentContextKey != null) {
            try {
                BeanFactoryReference parentRef = ContextRegistry.getInstance().getBeanFactory(parentContextKey);
                BeanFactory parentFactory = parentRef.getFactory();
                
                // The parent factory should be an ApplicationContext
                if (parentFactory instanceof ConfigurableApplicationContext) {
                    context.setParent((ConfigurableApplicationContext) parentFactory);
                }
            } catch (Exception e) {
                // Log the error but continue - the web app might still work without parent context
                sc.log("Failed to load parent context '" + parentContextKey + "': " + e.getMessage(), e);
            }
        }
        
        return context;
    }
}