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
package org.opennms.core.spring;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Registry for managing Spring contexts in OpenNMS.
 * This replaces the deprecated BeanFactoryLocator functionality for Spring 5+.
 * 
 * The registry loads context definitions from beanRefContext.xml files and manages
 * parent-child relationships between contexts.
 */
public class ContextRegistry {
    
    private static final Logger LOG = LoggerFactory.getLogger(ContextRegistry.class);
    private static final String BEAN_REF_CONTEXT_RESOURCE = "beanRefContext.xml";
    
    private final Map<String, ContextReference> contextRegistry = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    
    private static final ContextRegistry INSTANCE = new ContextRegistry();
    
    private ContextRegistry() {
        initializeContexts();
    }
    
    public static ContextRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize contexts by loading all beanRefContext.xml files from classpath
     */
    private void initializeContexts() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:" + BEAN_REF_CONTEXT_RESOURCE);
            
            if (resources.length == 0) {
                LOG.warn("No {} files found on classpath", BEAN_REF_CONTEXT_RESOURCE);
                return;
            }

            LOG.debug("Found {} {} files on classpath", resources.length, BEAN_REF_CONTEXT_RESOURCE);
            
            // Create a parent factory to merge all definitions
            DefaultListableBeanFactory mergedFactory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(mergedFactory);
            
            // Load all beanRefContext.xml files into the merged factory
            for (Resource resource : resources) {
                LOG.debug("Loading context definitions from: {}", resource.getURL());
                reader.loadBeanDefinitions(resource);
            }
            
            // Process all merged bean definitions
            processContextDefinitions(mergedFactory);
            
        } catch (Exception e) {
            LOG.error("Error initializing contexts", e);
        }
    }
    
    /**
     * Process context definitions from the merged factory
     */
    private void processContextDefinitions(DefaultListableBeanFactory factory) {
        // Process all bean definitions
        String[] beanNames = factory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Class<?> beanType = factory.getType(beanName);
                if (beanType != null && ClassPathXmlApplicationContext.class.isAssignableFrom(beanType)) {
                    // Register the context definition for lazy loading using Spring's factory
                    contextRegistry.put(beanName, new ContextReference(beanName, factory));
                    LOG.debug("Registered context definition: {}", beanName);
                }
            } catch (Exception e) {
                LOG.error("Error processing bean definition: {}", beanName, e);
            }
        }
    }
    
    /**
     * Get or create a context by its ID
     */
    public BeanFactoryReference getBeanFactory(String contextId) {
        synchronized (lock) {
            ContextReference ref = contextRegistry.get(contextId);
            if (ref == null) {
                throw new IllegalArgumentException("No context registered with ID: " + contextId);
            }
            return ref.getBeanFactoryReference();
        }
    }
    
    /**
     * Register a context programmatically
     */
    public void registerContext(String contextId, ApplicationContext context) {
        synchronized (lock) {
            contextRegistry.put(contextId, new ContextReference(contextId, context));
            LOG.debug("Programmatically registered context: {}", contextId);
        }
    }
    
    /**
     * Check if a context is registered
     */
    public boolean hasContext(String contextId) {
        return contextRegistry.containsKey(contextId);
    }
    
    /**
     * Reload contexts from classpath - useful for testing
     */
    public void reloadContexts() {
        synchronized (lock) {
            // Clear existing contexts
            contextRegistry.clear();
            // Reload from classpath
            initializeContexts();
        }
    }
    
    /**
     * Inner class to manage context references and lifecycle
     */
    private class ContextReference {
        private final String contextId;
        private final DefaultListableBeanFactory definitionFactory;
        private ApplicationContext context;
        private int referenceCount = 0;
        
        ContextReference(String contextId, DefaultListableBeanFactory definitionFactory) {
            this.contextId = contextId;
            this.definitionFactory = definitionFactory;
        }
        
        ContextReference(String contextId, ApplicationContext context) {
            this.contextId = contextId;
            this.definitionFactory = null;
            this.context = context;
        }
        
        synchronized BeanFactoryReference getBeanFactoryReference() {
            if (context == null && definitionFactory != null) {
                // Let Spring's factory create the context with all dependencies resolved
                context = (ApplicationContext) definitionFactory.getBean(contextId);
                LOG.debug("Lazy loaded context: {}", contextId);
            }
            
            referenceCount++;
            
            return new BeanFactoryReference() {
                private boolean released = false;
                
                @Override
                public BeanFactory getFactory() {
                    if (released) {
                        throw new IllegalStateException("BeanFactory reference has already been released");
                    }
                    return context;
                }
                
                @Override
                public void release() {
                    if (!released) {
                        synchronized (ContextReference.this) {
                            released = true;
                            referenceCount--;
                            
                            // Note: We don't automatically close contexts when reference count reaches 0
                            // because they might be needed again. In production, contexts typically
                            // live for the lifetime of the application.
                            LOG.debug("Released reference to context: {}, remaining references: {}", 
                                    contextId, referenceCount);
                        }
                    }
                }
            };
        }
    }
}