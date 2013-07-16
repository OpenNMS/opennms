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

package org.opennms.netmgt.correlation;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * <p>CorrelationEngineFactoryBean class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class CorrelationEngineFactoryBean implements FactoryBean<List<CorrelationEngine>>, InitializingBean, ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(CorrelationEngineFactoryBean.class);
    
	private List<CorrelationEngine> m_correlationEngines = Collections.emptyList();
    private ApplicationContext m_applicationContext;

    /**
     * <p>getObject</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
        @Override
    public List<CorrelationEngine> getObject() throws Exception {
        return m_correlationEngines;
    }

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
        @Override
    public Class<?> getObjectType() {
        return m_correlationEngines.getClass();
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
        @Override
    public boolean isSingleton() {
        return true;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_applicationContext != null, "applicationContext must be set");
        
        final Map<String, CorrelationEngine> beans = getBeans();
        
        // put them in a set to deduplicate the beans
        LOG.debug("Deduplicating engines");
        final HashSet<CorrelationEngine> engineSet = new HashSet<CorrelationEngine>(beans.values()); 
        m_correlationEngines = new LinkedList<CorrelationEngine>(engineSet);
        
        LOG.debug("Found {} engines.", m_correlationEngines.size());
    }

    private Map<String, CorrelationEngine> getBeans() {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(m_applicationContext, CorrelationEngine.class);
    }

    /** {@inheritDoc} */
        @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
}
