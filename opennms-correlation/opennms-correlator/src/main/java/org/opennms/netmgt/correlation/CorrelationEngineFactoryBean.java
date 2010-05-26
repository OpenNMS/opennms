/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 29, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.correlation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public class CorrelationEngineFactoryBean implements FactoryBean<List<CorrelationEngine>>, InitializingBean, ApplicationContextAware {
    
    private List<CorrelationEngine> m_correlationEngines = new ArrayList<CorrelationEngine>(0);
    private ApplicationContext m_applicationContext;

    public List<CorrelationEngine> getObject() throws Exception {
        return m_correlationEngines;
    }

    public Class<? extends List> getObjectType() {
        return m_correlationEngines.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
    
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_applicationContext != null, "applicationContext must be set");
        
        Map<String, CorrelationEngine> beans = getBeans();
        
        // put them in a set to deduplicate the beans
        log().debug("Deduplicating engines");
        HashSet<CorrelationEngine> engineSet = new HashSet<CorrelationEngine>(beans.values()); 
        
        m_correlationEngines = new LinkedList<CorrelationEngine>(engineSet);
        
        log().debug("Found "+m_correlationEngines.size()+" engines");
    }

    private Map<String, CorrelationEngine> getBeans() {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(m_applicationContext, CorrelationEngine.class);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }

    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
