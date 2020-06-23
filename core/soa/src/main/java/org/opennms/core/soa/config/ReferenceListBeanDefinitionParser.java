/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.soa.config;

import static org.opennms.core.soa.config.Constants.SERVICE_REGISTRY_BEAN_NAME;

import java.util.List;

import org.opennms.core.soa.support.ReferenceListFactoryBean;
import org.opennms.core.soa.support.RegistrationListenerBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * <p>ReferenceListBeanDefinitionParser class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReferenceListBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String INTERFACE_ATTR = "interface";
    private static final String FILTER_ATTR = "filter";
	private String m_serviceInterface = null;
	
    /** {@inheritDoc} */
    @Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext context) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ReferenceListFactoryBean.class);
		factory.addPropertyReference("serviceRegistry", SERVICE_REGISTRY_BEAN_NAME);
		
		String serviceInterface = element.getAttribute(INTERFACE_ATTR);
        if (StringUtils.hasText(serviceInterface)) {
        	m_serviceInterface = serviceInterface;
            factory.addPropertyValue("serviceInterface", serviceInterface);
        }
        
        String filter = element.getAttribute(FILTER_ATTR);
        if (StringUtils.hasText(filter)) {
            factory.addPropertyValue("filter", filter);
        }
		
        List<Element> childElements = DomUtils.getChildElementsByTagName(element, "listener");
        
        if (childElements != null && childElements.size() > 0) {
        	parseList(childElements, factory);
        }
        
		return factory.getBeanDefinition();
	}

	private void parseList(List<Element> childElements, BeanDefinitionBuilder factory) {
		BeanDefinitionBuilder listener = parseListener((Element)childElements.get(0));
		factory.addPropertyValue("listener", listener.getBeanDefinition());
	}

	private BeanDefinitionBuilder parseListener(Element element) {
		BeanDefinitionBuilder listener = BeanDefinitionBuilder.rootBeanDefinition(RegistrationListenerBean.class);
		listener.addPropertyReference("target", element.getAttribute("ref"));
		listener.addPropertyValue("serviceInterface", m_serviceInterface);
		listener.addPropertyValue("bindMethod", element.getAttribute("bind-method"));
		listener.addPropertyValue("unbindMethod", element.getAttribute("unbind-method"));
		
		return listener;
	}

}
